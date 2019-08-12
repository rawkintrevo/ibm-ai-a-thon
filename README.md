
This projects needs:

[ ] Github URL which contains
[ ] AI Model,
[ ] Input data and output results # no input data / output data- that's downloaded and created in R/T from json feed. but you do need to create a local divy shim to run , even though its cheap trash
[ ] UI (if any) and
[ ] a readme to run the code

### Overview / Problem Statement

_Prompts: 4_
who will benefit from project (ds are a dime per 3 dozen anymore)

Background / problem- The world is moving faster, data scientists are
becoming cheaper, but WatsonIoT Analtyics is locked in to an archaic
model.

Client data scientists know their data / problems better than we ever
will (except maybe on lengthy consulting engagements). We need to enable
client data scientists to deploy and manage their own models using what
ever tools _they_ choose.


#### The Data Source

_Prompts: 6,7_
Todo: stub- divvy bikes.

![Divvy Station at Milwaukee and California](divvy_station.JPG)

Todo: add photo of bike dock

[link](https://gbfs.divvybikes.com/gbfs/en/station_status.json)

```json
{   "station_id":"68",
    "num_bikes_available":9,
    "num_ebikes_available":0,
    "num_bikes_disabled":0,
    "num_docks_available":14,
    "num_docks_disabled":0,
    "is_installed":1,
    "is_renting":1,
    "is_returning":1,
    "last_reported":1565625872,
    "eightd_has_available_keys":false
}
```

Json Sources.

Issues with Strings (what you did to solve)


#### Solution Architecture

_DONE_

See [1](https://docs.google.com/drawings/d/1ATdY49nh_6BLsk9Qhnsiia80-65SAXJ0qxmmT_YGs-s/edit?usp=sharing)
for visual diagram.

The main components of this solution are
- WatsonIoT Platform (MQTT - IBMCloud)
- Apache Flink (on Kubernetes)
- ElasticSearch (on Kubernetes or IBMCloud)
- Apache OpenWhisk (Model serving, but other options available, e.g. WatsonML, TensorFlow Serving, etc.)
- Cloud Foundry (only for the Divvy -> Watson IoT-P shim, not a core component)

Additionally, the WebUI uses the following components.
- CloudFoundry (IBMCloud)
- React
- Carbon

Programming Languages Used:
- Java
- Scala
- Python
- JavaScript (React)

Let us walk through the path an individual event will take as it flows
through this system.  Initially, the event is generated on the device,
in this case, a Divvy Bike Station. The shim[2](https://github.com/rawkintrevo/ibm-ai-a-thon/tree/master/divvy-shim-cf)
is constantly polling the Divvy Endpoint, and publishing the status of
each station to the WatsonIoT Platform Instance (MQTT broker).

Apache Flink uses an MQTT as a source[more information](https://ci.apache.org/projects/flink/flink-docs-stable/dev/connectors/#predefined-sources-and-sinks)
however, the reader will see there is no MQTT source connector built in
to Flink. The [MQTT connector we use](https://github.com/rawkintrevo/ibm-ai-a-thon/blob/master/flink-runtime/src/main/java/org/rawkintrevo/aiathon/sources/MQTTSource.java)
is based on [luckyyuyong](https://github.com/luckyyuyong/flink-mqtt-connector)'s
work, however was heavily modified to work with Flink 1.8 and recent
Watson IoT-P.

Flink picks up the records from the MQTT broker (Watson IoT-P), and using a [slinding window](https://ci.apache.org/projects/flink/flink-docs-stable/dev/stream/operators/windows.html#sliding-windows)
calculates a number of metrics on the events, by device as well as by
device type). At the time of writing, these metrics are:

- Sum
- Count
- Min
- Max
- Sum of Squared Deviations
- Standard Deviation

The duration of the window is **TODO** and it emits metrics every **TODO**
seconds, both of these values are currently hardcoded, but will be
part of the job setup configuration in the future.

The most recent appropriate analytics are merged with each record as it
passes through.  For example, a device `myord:dev_type1:0001` will be
merged with its own most recent summary statistics, as well as the
most recent summary statistics for all of `dev_type1`.  Other heiracrchies
are not currentl supported but could be added very easily, as well as
configured durring the job set up.

In another thread, there is a stream which once every **TODO** seconds,
polls the endpoint server URL, so collect a list of active endpoints.
The results from the analytics metrics stream are merged with this list,
and for each analytics record, each endpoint is called with a json containing

1. The original device event
1. The analytics metrics for similar device types
1. The analytics metrics for this device

The responses, as well as the metrics, and original device events are
all then written to the ElasticSearch sink.

#### Introducing this into Current Products

**Existing Gap in IBM Products**

**A breif note on windowing, event time, and minibatching.**

**Ability to integrat into current offerings**

_TODO_ Techincal feasibiltiy- cloud native, etc

However, ut is unlikely this product could be implemented at IBM in its current
form, more for political reasons than technical limitations. Namely,
IBM Streams and IBM DB2 are king, and all frameworks must rely on heavily
on them, not Open Source equivelents.  I'm not sure of the capabilities
of IBM Stream, specifically around windowing, and so I can't be sure that
one could simply replace Apache Flink with IBM Streams. It should also
be noted, that Apache Spark Streaming at this time _should not_ be used
in place of Apache Flink as it is a sorely under developed product, that
is glitchy, and in fact is only mini-batching, which is precisely the
problem we seek to allieviate with Flink.


#### The Example Models

_Prompts: 1, 9, 10_

how does this solution infuse AI

model selection process

results- here actually talk about how you would train a model in R/T, describe how it is different than batch. and why it is important to look at R/T vs theoretical R/T


#### Highlights

**Cloud Native** The Cloud Native Design of this Applicaiton allows it
to be deployed _as it_ to IBM Cloud (it actually arleady is deployed to
IBMCloud).  With a little tweaking, this could be refactored as KNative
as well.

**Open Source**

**Enables AI at the Client** Clients know their own data and own problems
much better than we can hope to.  By enabling them to write their own
uniques solutions to their own unique problems we are enabling them to
unlock more value and success than we could ever hope to deliver.  However,
for clients who are not sofisticated enough yet to harness this power,
we can also support them with IBM's Global Business Services.  The advantage
to GBS is that they can also devote their time to understanding the problem
and writing solutions, not to fighting with the systems.


#### Analysis

_Prompts: 2, 8,_

analysis- what works, what doesnt, what to investigate futher~~

novelty / creativity / uniqueness / prior art / new concept in the idea

uniqueness of approach, algorithm, methods.

todo: Video Demo

#### Reference

1 - https://docs.google.com/drawings/d/1ATdY49nh_6BLsk9Qhnsiia80-65SAXJ0qxmmT_YGs-s/edit?usp=sharing
2 - https://github.com/rawkintrevo/ibm-ai-a-thon/tree/master/divvy-shim-cf


