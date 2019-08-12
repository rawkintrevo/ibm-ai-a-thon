package org.rawkintrevo.aiathon.sources;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class TimeDelaySource extends ProcessFunction<Long, Tuple2<String,String>> {

    // delay after which an alert flag is thrown
    private final long timeOut;
    // state to remember the last timer set
    private transient ValueState<Long> lastTimer;

    public TimeDelaySource(long timeOut) {
        this.timeOut = timeOut;
    }

    @Override
    public void open(Configuration conf) {
        // setup timer state
        ValueStateDescriptor<Long> lastTimerDesc =
                new ValueStateDescriptor<Long>("lastTimer", Long.class);
        lastTimer = getRuntimeContext().getState(lastTimerDesc);
    }

    @Override
    public void processElement(Long value, Context ctx, Collector<Tuple2<String, String>> out) throws Exception {
        // get current time and compute timeout time
        long currentTime = ctx.timerService().currentProcessingTime();
        long timeoutTime = currentTime + timeOut;
        // register timer for timeout time
        ctx.timerService().registerProcessingTimeTimer(timeoutTime);
        // remember timeout time
        lastTimer.update(timeoutTime);
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple2<String, String>> out) throws Exception {
        // check if this was the last timer we registered
        if (timestamp == lastTimer.value()) {
            // it was, so no data was received afterwards.
            // fire an alert.
            long currentTime = ctx.timerService().currentProcessingTime();
            long timeoutTime = currentTime + timeOut;
            // register timer for timeout time
            ctx.timerService().registerProcessingTimeTimer(timeoutTime);
            lastTimer.update(timeoutTime);
            out.collect(Tuple2.of("foo", "baz"));
        }
    }
}
