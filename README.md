

## Table of Contents

- 'Live' Demo (and video)
- Overview and Problem Statement
- The Divvy Bike Data Source
- Solution Architecture
- Merging this Into Current Products
- Modeling Real Time and AIoT
- Highlights
- Conclusion
- Reference


## 'Live' Demo

This product is 100% cloud native, and running on Kubernetes. See the "live-demo" at
http://www.ai-a-thon.us-south.containers.appdomain.cloud (note, bc IBM's K8s clusters are wicked expensive, this is only "on"
at certain times. Hopefully there is a demo portion or the judges can let me know and I'll fire it up for y'all to kick the tires)

Video:
<iframe width="1280" height="720" src="https://www.youtube.com/embed/EKzv1YGukEk" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Overview / Problem Statement

_Prompts: 4_
who will benefit from project (ds are a dime per 3 dozen anymore)

Buzz words: "The journey to being a real-time enterprise."
"Timely extraction of value from data"
"Building the real-time enterprise requires new ways to process streams of actionable data"
"Data engineers face new challenges"
"Batch processing is not suitable for new challenges"
"New Standards emerging for DS/Data engineers"
"Choice is vital"
Background / problem- The world is moving faster, data scientists are
becoming cheaper, but WatsonIoT Analtyics is locked in to an archaic
model.

Client data scientists know their data / problems better than we ever
will (except maybe on lengthy consulting engagements). We need to enable
client data scientists to deploy and manage their own models using what
ever tools _they_ choose.


## The Data Source

It's important to note, that the data source chosen has little to do with the value proposition of this solution. It is
designed to be a platform upgrade that can take on multiple datasources. That said, some description of the data you are
looking at will probably be helpful, so we will include it:

The data set comes from the [City of Chicago's Divvy Bike Program](https://www.divvybikes.com/how-it-works)
, a bike share program the on the city's north and west sides. There are 580 stations, and over 5000 bikes. The city provides
near real time data on the status of each station, and makes this data available for public consumption.

![Divvy Station at Milwaukee and California](divvy_station.JPG)

All of the data available for consumption can be found [here](https://www.divvybikes.com/system-data) however, we are focusing
on `station_status` because of the real time endpoints, it has the most variablility. [link](https://gbfs.divvybikes.com/gbfs/en/station_status.json)

An example record we get back from this endpoint follows.

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

One of the major issues we ran into when moving from synthetic data to "real world" data was type safety.  If our record,
we see `station_id` a string, that looks like a numeric. Several integegers, a long, and a boolean.  While we wanted to
create a system that could automatically infer schema, this feature was considered low priority and as such you will now
see that the user must pass the schema as a json when she creates the stream.

## Solution Architecture


<img src="https://docs.google.com/drawings/d/e/2PACX-1vTHXbdxy7vG23LWQNHc7WJ-0XMsreRinnKpy1Y1hKQIbgI4d_wZI5J_FflhERAvIiw2C-RSqJ-N6XLV/pub?w=960&amp;h=1920">
<figcaption><center>Figure: Architecture Diagram of Current and Planned Components.</center></figcaption>

<p></p>


The main components of this solution are
- WatsonIoT Platform (MQTT - IBMCloud)
- Apache Flink (on Kubernetes)
- ElasticSearch (on Kubernetes or IBMCloud)
- Apache OpenWhisk (Model serving, but other options available, e.g. WatsonML, TensorFlow Serving, etc.)
- Cloud Foundry (only for the Divvy -> Watson IoT-P shim, not a core component)

Additionally, the WebUI uses the following components.
- ~~CloudFoundry (IBMCloud)~~ Kubernetes now.
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

## Introducing this into Current Products

**Existing Gap in IBM Products**

The existing gap in IBM products is that our current IoT Analytics Service is
1. Not Real Time
2. Does not have a framework that allows users to bring their own model 

**A breif note on windowing, event time, and minibatching.**

See [this article](https://flink.apache.org/news/2015/12/04/Introducing-windows.html)

**Ability to integrate into current offerings**

To make this product ready for production their are a few bugs that need
to be worked out, however- as it is K8s native, and already running in cloud
against a quasi production workload and is composed of all near-linearly scalable
components, the ability to _Ability to Integrate_ criteria should recieve full marks.

However, IBM Streams and IBM DB2 are king, and all frameworks must rely on heavily
on them, not Open Source equivelents.  I'm not sure of the capabilities
of IBM Stream, specifically around windowing, and so I can't be sure that
one could simply replace Apache Flink with IBM Streams. 

It should also be noted, that Apache Spark Streaming at this time _should not_ be used
in place of Apache Flink as it is a sorely under developed product, that
is glitchy, and in fact is only mini-batching, which is precisely the
problem we seek to allieviate with Flink.


## Modeling Real Time and AIoT

This framework supports Lambda-Arch based Machine learning models (the sort of models
we were supposed to create with this hackathon) as well as Kappa-arch models.

For difference, here's a shameless plug to a video I did ~~last year~~ two years ago...(wow).  
https://www.youtube.com/watch?v=O3gd6elZOlA

This solution infuses AI by facilitating other data scientists to quickly and easily field
their models into production.

Models should be selected at run time with A/B tests, multi arm bandits, and diverting traffic.

There are a number of issues with real time models that aren't present in batch models. 
Please see video above to see what I knew about it 2 years ago. THere's more now, but I 
simply didn't realize how much more I needed to write on this doc which is due in four minutes.



## Highlights

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


## Analysis

#### What Works

- Deploy to K8s
- Model Stream Ingests Data
- Stream dumps to Elastic Search
- ES displays models
- Hokey React Dashboard

#### What Doesn't
- Error handling in stream, almost non-existant right now
- No alarms for when data isn't pumping
- No dynamic detection of json schema (must give when kicking off job)
- No front end for job deployment
- Reactive charts, not ES for visualizing results.

#### Further Analyises
- Fix things that don't work. 

## Reference

1 - https://docs.google.com/drawings/d/1ATdY49nh_6BLsk9Qhnsiia80-65SAXJ0qxmmT_YGs-s/edit?usp=sharing
2 - https://github.com/rawkintrevo/ibm-ai-a-thon/tree/master/divvy-shim-cf


