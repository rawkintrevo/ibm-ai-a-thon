package org.rawkintrevo.aiathon;

// Trevor's Note: ALL credit to https://github.com/luckyyuyong/flink-mqtt-connector for this- but MIT license to loot and run.

import org.rawkintrevo.aiathon.sources.MQTTSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


// org.rawkintrevo.aiathon.DeviceDataAnalysis
public class DeviceDataAnalysis {


    public static void main(String[] args) throws Exception {
        Logger LOG = LoggerFactory.getLogger(DeviceDataAnalysis.class);
        String Org_ID = "snpaca";
        String App_Id = "org_rawkintrevo_aiathon";
        String API_Key = "a-snpaca-fvbtgfakg6";
        String APP_Authentication_Token = "vm+Krw-wff0rgurMoD";
        String Device_Type = " pizza-machine"; //"+";
        String Device_ID = "01"; //"+";
        String EVENT_ID = "applyPeperonis"; //"+";

//        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        Properties properties = new Properties();
//        properties.load(new FileInputStream("src/main/resources/application.properties"));
        LOG.info("Test logging...");
        Properties mqttProperties = new Properties();

        // client id = a:<Organization_ID>:<App_Id>

        String clientID = String.format("a:%s:%s", Org_ID, App_Id);
        mqttProperties.setProperty(MQTTSource.CLIENT_ID, clientID );
//                        properties.getProperty("Org_ID"),
//                        properties.getProperty("App_Id")));
        LOG.info(String.format("clientID set as: %s", clientID));

        // mqtt server url = tcp://<Org_ID>.messaging.internetofthings.ibmcloud.com:1883
        String mqttURL = String.format("tcp://%s.messaging.internetofthings.ibmcloud.com:1883", Org_ID);
        mqttProperties.setProperty(MQTTSource.URL, mqttURL );
//                        properties.getProperty("Org_ID")));
        LOG.info(String.format("mqttURL: %s", mqttURL));

        // topic = iot-2/type/<Device_Type>/id/<Device_ID>/evt/<Event_Id>/fmt/json
//        String mqttTopic = String.format("iot-2/type/%s/id/%s/evt/%s/fmt/json", Device_Type, Device_ID, EVENT_ID);
        String mqttTopic = "iot-2/type/+/id/+/evt/+/fmt/+";
        mqttProperties.setProperty(MQTTSource.TOPIC, mqttTopic);
//                        properties.getProperty("Device_Type"),
//                        properties.getProperty("Device_ID"),
//                        properties.getProperty("EVENT_ID")));
        LOG.info(String.format("mqtt topic: %s", mqttTopic));

        mqttProperties.setProperty(MQTTSource.USERNAME,
//                properties.getProperty("API_Key"));
                API_Key);
        mqttProperties.setProperty(MQTTSource.PASSWORD,
//                properties.getProperty("APP_Authentication_Token"));
                APP_Authentication_Token);



        MQTTSource mqttSource = new MQTTSource(mqttProperties);
        DataStreamSource<String> iotDataSource = env.addSource(mqttSource);
        DataStream<String> stream = iotDataSource.map((MapFunction<String, String>) s -> {
            // Split the Topic and Message
            String[] arrOfS = s.split("\\|");
            String topic = arrOfS[0];
            String msgRaw = arrOfS[1];

            // Process the topic
            Pattern DEVICE_EVENT_PATTERN = Pattern.compile("iot-2/type/(.+)/id/(.+)/evt/(.+)/fmt/(.+)");
            Matcher matcher = DEVICE_EVENT_PATTERN.matcher(topic);
            if (!matcher.matches()) { return "matcher didn't match"; }
            String type = matcher.group(1);
            String id = matcher.group(2);
            String event = matcher.group(3);
            String format = matcher.group(4);

            // Make Json of Topic

            // Process Message into Json
            JsonObject messageJson = new Gson().fromJson(msgRaw, JsonObject.class);
            // https://static.javadoc.io/com.google.code.gson/gson/2.8.5/com/google/gson/JsonObject.html
            String armOnePeps = messageJson.get("peperoniArm1").toString();
//            https://static.javadoc.io/com.google.code.gson/gson/2.8.5/com/google/gson/JsonElement.html


            return type + ":" + armOnePeps;
            // get urls for model from some endpoint (openwhisk)
            // call WatsonML for anamolies. (or openwhisk)
            // elasticsearch sink
            // https://github.com/dataArtisans/flink-streaming-demo
            // kibanna
/*
            String outputStr = "device " + id + "has " + armOnePeps + " peperonis deployed";
            LOG.info(outputStr);
            return outputStr;
        */

        });
        stream.print();

        env.execute("Flink IoT Runtime");
    }

}
