package org.rawkintrevo.aiathon.windowfns;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class ProcessWindowAnalyticsFunction
        extends ProcessWindowFunction<Tuple3<String,String,String>,  // input type
//        extends ProcessWindowFunction<  String,  // input type
                                        Tuple2<String, String>,       // output type
                                        String, // key type
//                                        Tuple,       // key type
                                        TimeWindow> { // window type

    @Override
    public void process(String key, Context context, Iterable<Tuple3<String,String,String>> input, Collector<Tuple2<String,String>> out) {
//      public void process(String key, Context context, Iterable<String> input, Collector<String> out) {
        long count = 0;
        HashMap<String, Double> outputMaxMap = new HashMap<String, Double>();
        HashMap<String, Double> outputMinMap = new HashMap<String, Double>();
        HashMap<String, Double> outputSumMap = new HashMap<String, Double>();
        HashMap<String, Double> outputAvgMap = new HashMap<String, Double>();
        Logger LOG = LoggerFactory.getLogger(ProcessWindowFunction.class);


        for (Tuple3<String,String,String> in: input) {
            count++;

            String jsonStr = in.f1;
            JsonObject jsonSchema = new Gson().fromJson(in.f2, JsonElement.class).getAsJsonObject();;


//            Cannot find line for links processing            // Process Message into Json
//            HashMap<String, Object> json = new Gson().fromJson(jsonStr, HashMap.class);
            JsonObject messageJson = new Gson().fromJson(jsonStr, JsonElement.class).getAsJsonObject();
            Set<String> keys = messageJson.keySet();

            for (String jsonkey : keys){
                // Calculate Max
                if (jsonSchema.keySet().contains(jsonkey)) {
                    String keyType = jsonSchema.get(jsonkey).getAsString();
                    if (keyType.equals("Numeric")) {
                        outputMaxMap.put(jsonkey,
                                Math.max(outputMaxMap.getOrDefault(jsonkey, 0.0), messageJson.get(jsonkey).getAsDouble()));
                        outputMinMap.put(jsonkey,
                                Math.min(outputMaxMap.getOrDefault(jsonkey, 0.0), messageJson.get(jsonkey).getAsDouble()));
                        outputSumMap.put(jsonkey,
                                outputMaxMap.getOrDefault(jsonkey, 0.0) + messageJson.get(jsonkey).getAsDouble());
                    }
                } else {
                    LOG.warn("Json Record has key '" + jsonkey + "' but the schema has no info on that..?");
                }

            }
        }

        Set<String> numericKeys = outputMaxMap.keySet();
        for (String jsonkey : numericKeys){
            outputAvgMap.put(jsonkey, (double) outputSumMap.get(jsonkey) / count);
        }


        // sum of square diffs
        HashMap<String, Double> outputSumSqDiffMap = new HashMap<String, Double>();
        for (Tuple3<String,String,String> in: input) {
            String jsonStr = in.f1;
            JsonObject messageJson = new Gson().fromJson(jsonStr, JsonElement.class).getAsJsonObject();
            for (String jsonkey : numericKeys){
                Double squareDiff = Math.pow(messageJson.get(jsonkey).getAsDouble() - outputAvgMap.get(jsonkey), 2);
                outputSumSqDiffMap.put(jsonkey,
                        outputSumSqDiffMap.getOrDefault(jsonkey, 0.0) + squareDiff);
            }
        }
        // Stdev
        HashMap<String, Double> outputStdDevMap = new HashMap<String, Double>();
        for (String jsonkey : numericKeys){
            outputStdDevMap.put(jsonkey, Math.sqrt( outputSumSqDiffMap.get(jsonkey) / count));
        }

            // You're being weird- stop it. Convert the original json with the hashmap<string,doubles> to a json, then add the keys, then
        // write the whole thing to a string. easy, breezy, beautiful.
        HashMap<String, HashMap<String, Double>> outputMap = new HashMap<String, HashMap<String, Double>>();
        outputMap.put("max", outputMaxMap);
        outputMap.put("min", outputMinMap);
        outputMap.put("sum", outputSumMap);
        outputMap.put("avg", outputAvgMap);
        outputMap.put("ssd", outputSumSqDiffMap);
        outputMap.put("std", outputStdDevMap);
        Gson gsonObj = new Gson();
        JsonObject outputJson = new JsonObject();
        outputJson.add("properties", gsonObj.toJsonTree(outputMap));
        outputJson.addProperty("count", count);
        outputJson.addProperty("devicetype", key.split("_")[0]);
        outputJson.addProperty("id", key.split("_")[1]);
        outputJson.addProperty("event", key.split("_")[2]);
        String nowDt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
        outputJson.addProperty("windowclose date", nowDt);
        // todo stdev

//        String jsonStr = gsonObj.toJson(outputMaxMap);
        Tuple2<String, String> output = new Tuple2<>(key, outputJson.toString());
        out.collect( output );

    }
}