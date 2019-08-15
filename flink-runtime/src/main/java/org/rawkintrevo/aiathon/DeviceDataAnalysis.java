package org.rawkintrevo.aiathon;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink;
import org.apache.flink.util.Collector;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.rawkintrevo.aiathon.asyncio.AsyncEndpointServerRestRequest;
import org.rawkintrevo.aiathon.asyncio.AsyncPingModels;
import org.rawkintrevo.aiathon.sources.MQTTSource;
import org.rawkintrevo.aiathon.windowfns.ProcessWindowAnalyticsFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// org.rawkintrevo.aiathon.DeviceDataAnalysis
public class DeviceDataAnalysis {


    public static void main(String[] args) throws Exception {
        ParameterTool parameter = ParameterTool.fromArgs(args);

        Logger LOG = LoggerFactory.getLogger(DeviceDataAnalysis.class);
        String Org_ID = parameter.getRequired("orgid"); // somethign like snpaca
        String App_Id = parameter.getRequired("appid"); // some name for your app
        String API_Key = parameter.getRequired("apikey"); // "a-snpaca-fvbtgfakg6";
        String APP_Authentication_Token = parameter.getRequired("authtoken"); //"vm+Krw-wff0rgurMoD";
        String ES_HOST_NAME = parameter.getRequired("esurl"); // "elasticsearch.default"; // cause k8s
        final String ENDPOINT_URL = parameter.getRequired("endpointurl"); // "https://a5056918.us-south.apiconnect.appdomain.cloud/aiathon/endpointserver";

        // --orgid dwbsnh  --appid org_rawkintrevo_aiathon --apikey a-dwbsnh-s06bdobyc6 --authtoken i2e0OyhT*HHxx&7OOa --esurl es.ai-a-thon.us-south.containers.appdomain.cloud --endpointurl https://a5056918.us-south.apiconnect.appdomain.cloud/aiathon/endpointserver
        // Todo: this should also be a paramter
        final String schemaJson = "{'eightd_has_available_keys': Bool,\n" +
                " 'shim-dt': 'String',\n" +
                " 'eightd_active_station_services': 'String',\n" +
                " 'is_installed': 'Numeric',\n" +
                " 'is_renting': 'Numeric',\n" +
                " 'is_returning': 'Numeric',\n" +
                " 'last_reported': 'String',\n" +
                " 'num_bikes_available': 'Numeric',\n" +
                " 'num_bikes_disabled': 'Numeric',\n" +
                " 'num_docks_available': 'Numeric',\n" +
                " 'num_docks_disabled': 'Numeric',\n" +
                " 'num_ebikes_available': 'Numeric',\n" +
                " 'shim-dt': 'String',\n" +
                " 'station_id': 'String'}";

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties mqttProperties = new Properties();

        // client id = a:<Organization_ID>:<App_Id>

        // mqtt will disconnect brokers w same id bc it thinks they are old and stale, so make it rando
        // if there is ever a bug where restarting the app will randomly make it work- its bc this isn't rando enough
        String clientBase = String.format("a:%s:%s", Org_ID, App_Id).substring(0, 10);
        String clientID = clientBase + System.currentTimeMillis();
        mqttProperties.setProperty(MQTTSource.CLIENT_ID, clientID );
        LOG.info("clientID: " + clientID);

        String mqttURL = String.format("tcp://%s.messaging.internetofthings.ibmcloud.com:1883", Org_ID);
        mqttProperties.setProperty(MQTTSource.URL, mqttURL );

        String mqttTopic = "iot-2/type/+/id/+/evt/+/fmt/+";
        mqttProperties.setProperty(MQTTSource.TOPIC, mqttTopic);



        mqttProperties.setProperty(MQTTSource.USERNAME, API_Key);
        mqttProperties.setProperty(MQTTSource.PASSWORD, APP_Authentication_Token);

        MQTTSource mqttSource = new MQTTSource(mqttProperties);
        DataStreamSource<String> iotDataSource = env.addSource(mqttSource);

        DataStream<Tuple2<String, String>> tupleStream = iotDataSource
                .map((MapFunction<String, Tuple2<String,String>>) s -> {
//        DataStream<FlinkRecord> stream = iotDataSource.map((MapFunction<String, FlinkRecord>) s -> {
            String[] arrOfS = s.split("\\|");
            String topic = arrOfS[0];
            String msgRaw = arrOfS[1];

            // Process the topic
            Pattern DEVICE_EVENT_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/evt/(.+)/fmt/(.+)");
            Matcher matcher = DEVICE_EVENT_PATTERN.matcher(topic);
//            if (!matcher.matches()) { return new FlinkRecord(null, "matcher didn't match"); }
            if (!matcher.matches()) { return new Tuple2(null, "matcher didn't match"); }
            String type = matcher.group(1);
            String id = matcher.group(2);
            String event = matcher.group(3);
            String format = matcher.group(4);
            // https://static.javadoc.io/com.google.code.gson/gson/2.8.5/com/google/gson/JsonObject.html
            // https://static.javadoc.io/com.google.code.gson/gson/2.8.5/com/google/gson/JsonElement.html

//            return new FlinkRecord(type+"_"+id+"_"+event, msgRaw);
            return new Tuple2<String, String>(type+"_"+id+"_"+event, msgRaw);

//        }).returns(new TypeHint<FlinkRecord>(){});
        })
                .returns(new TypeHint<Tuple2<String, String>>(){})
                .name("tuple packaging");

        DataStream<Tuple2<String, String>> processedStream = tupleStream
                .map(t -> Tuple3.of(t.f0, t.f1, schemaJson))
                .returns(Types.TUPLE(Types.STRING, Types.STRING, Types.STRING))
                .keyBy(t -> t.f0)
                .window(SlidingProcessingTimeWindows.of(Time.seconds(45), Time.seconds(15)))
                .process(new ProcessWindowAnalyticsFunction())
                .name("120/30 window slider");



        // line 140 (2 down from erhe) if you some day want to pass device types or sensors or whatevs, change that but
        // don't be calling get End points 500 x at once or it does nothing.
        DataStream<Tuple2<String,String>> timeDelayStream = tupleStream
                .map(t -> Tuple2.of("baz", 1))
                .returns(Types.TUPLE(Types.STRING, Types.INT))
                .keyBy(t -> t.f0)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(30)))
                .sum(1)
                .map(t -> Tuple2.of(t.f0, ENDPOINT_URL))
                .returns(Types.TUPLE(Types.STRING, Types.STRING))
//                .reduce((ReduceFunction<Tuple2<String, String>>) (v1, v2) -> new Tuple2<>(v1.f0, ENDPOINT_URL))
//                .map(t -> {LOG.info("tumble window fire now"); return t;} )
//                .returns(Types.TUPLE(Types.STRING, Types.STRING))
                .name("time delay stream");


        DataStream<Tuple2<String,String>> restStream = AsyncDataStream.unorderedWait(timeDelayStream,
                new AsyncEndpointServerRestRequest(),
                30,
                TimeUnit.SECONDS,
                100)
                .name("rest req stream"); // Tuple2("mqtt_event_id", {jsonOf: [list, of, endpoint, to, hit]})


        DataStream<Tuple3<String, String, String>> tmpStream = tupleStream
                .connect(processedStream)
                .process(new CoProcessFunction<Tuple2<String, String>, Tuple2<String, String>, Tuple3<String, String, String>>() {
                    Map<String, String> lastAnalyticsMap = new HashMap<>();
                    @Override
                    public void processElement1(Tuple2<String, String> in1,
                                                Context context,
                                                Collector<Tuple3<String, String, String>> collector) throws Exception {

                        if (lastAnalyticsMap.containsKey(in1.f0)) {
                            collector.collect(new Tuple3(in1.f0, in1.f1, lastAnalyticsMap.get(in1.f0)));
                        }
                    }

                    @Override
                    public void processElement2(Tuple2<String, String> in2,
                                                Context context,
                                                Collector<Tuple3<String, String, String>> collector) throws Exception {
                        lastAnalyticsMap.put(in2.f0, in2.f1);
                    }
                });

        DataStream<Tuple4<String, String, String, String>> readyForRESTStream = restStream.connect(tmpStream)
                .process(new CoProcessFunction<Tuple2<String, String>, Tuple3<String, String, String>, Tuple4<String, String,String, String>>() {

                    String listOfURLs = "{\"echo\":\"https://a5056918.us-south.apiconnect.appdomain.cloud/aiathon/echo\"}";
                    @Override
                    public void processElement1(Tuple2<String, String> in1,
                                                Context context,
                                                Collector<Tuple4<String, String, String, String>> collector) throws Exception {

                        if (!(in1 == null)){
                            LOG.info("ok, i guess were going for it?");
                            JsonObject messageJson = new Gson().fromJson(in1.f1, JsonElement.class).getAsJsonObject();
                            JsonArray endpointArray = messageJson.get("endpoints").getAsJsonArray();
                            Iterator<JsonElement> myIterator = endpointArray.iterator();
                            ArrayList<String> myList = new ArrayList<String>();
                            while( myIterator.hasNext()) {
                                JsonElement nextObject = myIterator.next();
                                LOG.info("next element: " + nextObject.toString());
                                myList.add(nextObject.toString());
                            }
                            listOfURLs = myList.toString();
                        } else {
                            LOG.info("line 166, in1 is still empty...");
                        }

                    }

                    @Override
                    public void processElement2(Tuple3<String, String, String> in2,
                                                Context context,
                                                Collector<Tuple4<String, String, String, String>> collector) throws Exception {

                        collector.collect(Tuple4.of(in2.f0, in2.f1, in2.f2, listOfURLs));
                    }

                }).flatMap(new FlatMapFunction<Tuple4<String, String, String, String>,
                    Tuple4<String, String, String, String>>() {
                        @Override
                        public void flatMap(Tuple4<String, String, String, String> in,
                                            Collector<Tuple4<String, String, String, String>> collector) throws Exception {
                            for (String url: in.f3.split(",")) {
                                collector.collect(new Tuple4<String, String, String, String>(in.f0, in.f1, in.f2, url));
                            }
                        }
                    });

        DataStream<Tuple2<String, String>> outputStream = AsyncDataStream.unorderedWait(readyForRESTStream,
                new AsyncPingModels(),
                30,
                TimeUnit.SECONDS,
                50);

        List<HttpHost> httpHosts = new ArrayList<>();
        httpHosts.add(new HttpHost(ES_HOST_NAME, 9200, "http"));

        ElasticsearchSink.Builder<String> esSinkBuilder = new ElasticsearchSink.Builder<>(
                httpHosts,
                new ElasticsearchSinkFunction<String>() {
                    public IndexRequest createIndexRequest(String element) {
                        HashMap<String, Object> json = new Gson().fromJson(element.toString(), HashMap.class);

                        return Requests.indexRequest()
                                .index("analytics")
                                .type("window-analytics")
                                .source(json);
                    }

                    @Override
                    public void process(String element, RuntimeContext ctx, RequestIndexer indexer) {
                        indexer.add(createIndexRequest(element));
                    }
                }
        );

        ElasticsearchSink.Builder<String> esAllDocsSinkBuilder = new ElasticsearchSink.Builder<>(
                httpHosts,
                new ElasticsearchSinkFunction<String>() {
                    public IndexRequest createIndexRequest(String element) {
                        HashMap<String, Object> json = new Gson().fromJson(element.toString(), HashMap.class);

                        return Requests.indexRequest()
                                .index("all-events")
                                .type("events")
                                .source(json);
                    }

                    @Override
                    public void process(String element, RuntimeContext ctx, RequestIndexer indexer) {
                        indexer.add(createIndexRequest(element));
                    }
                }
        );

        ElasticsearchSink.Builder<String> esModelResultsSinkBuilder = new ElasticsearchSink.Builder<>(
                httpHosts,
                new ElasticsearchSinkFunction<String>() {
                    public IndexRequest createIndexRequest(String element) {
                        HashMap<String, Object> json = new Gson().fromJson(element.toString(), HashMap.class);

                        return Requests.indexRequest()
                                .index("model-results")
                                .type("model-result")
                                .source(json);
                    }

                    @Override
                    public void process(String element, RuntimeContext ctx, RequestIndexer indexer) {
                        indexer.add(createIndexRequest(element));
                    }
                }
        );
        // configuration for the bulk requests; this instructs the sink to emit after every element, otherwise they would be buffered
        esSinkBuilder.setBulkFlushMaxActions(1);


        tupleStream.map(t -> t.f1).addSink(esAllDocsSinkBuilder.build()).name("All events sink");
        outputStream.map(t -> t.f1).addSink(esModelResultsSinkBuilder.build()).name("ES model-results doc sink"); // todo add key here
        processedStream.map(t -> t.f1).addSink(esSinkBuilder.build()).name("ES agg sink");

        env.execute("Flink IoT Runtime");
    }


}
