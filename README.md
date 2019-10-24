# GreenVulcano VCL Adapter for Redis (Alpha)

This is the implementation of a GreenVulcano VCL Adapter for the Redis database platform. It's meant to run as an Apache Karaf bundle.

## Getting started

### Prerequisites

First, you need to have installed Java Development Kit (JDK) 1.7 or above.

Then, you need to have installed Apache Maven (3.5.4 or higher) and Apache Karaf 4.2.6. Please refer to the following links for further reference:
- Apache Maven 3.5.4:
    - [Download](http://mirror.nohup.it/apache/maven/maven-3/3.5.4/binaries/apache-maven-3.5.4-bin.tar.gz)
    - [Installation steps](https://maven.apache.org/install.html)
- Apache Karaf 4.2.6:
    - [Download](http://www.apache.org/dyn/closer.lua/karaf/4.2.6/apache-karaf-4.2.6.tar.gz)
    - Installation steps: simply extract the Apache Karaf directory in any path you want. Verify that the Apache Karaf installation is operational by running the executable ```./bin/karaf``` from the Apache Karaf root directory (a Karaf shell should be displayed).

Next, you need to install the GreenVulcano engine on the Apache Karaf container. Please refer to [this link](https://github.com/kylan11/gv-engine/blob/master/quickstart-guide.md) for further reference.

In order to install the bundle in Apache Karaf to use it for a GreenVulcano application project, you need to install its dependencies. Open the Apache Karaf terminal by running the Karaf executable and type the following command:

```shell
karaf@root()> bundle:install mvn:org.apache.commons/commons-pool2/2.6.0 && bundle:install -l 81 mvn:redis.clients/jedis/3.1.0
```
In case of success, this command will print the ID of the installed bundles:

```shell
Bundle ID: n
```

Before installing the VCL adapter bundle, you have to start the both bundles by specifying their IDs. In the Apache Karaf terminal, type (replacing n with the bundle IDs):

```shell
karaf@root()> start n
```

In case you can't find the bundle ID, just type:

```shell
karaf@root()> list | grep Apache Commons Pool
karaf@root()> list | grep Jedis
```

Having done that, use the ```list``` command to make sure both bundles are in ```Active``` status.

Then, you need to install the VCL adapter bundle itself in Apache Karaf.

### Installing the VCL adapter bundle in Apache Karaf

Clone or download this repository on your computer, and then run ```mvn install``` in its root folder:

```shell
git clone https://github.com/kylan11/gv-adapter-redis
cd gv-adapter-redis
mvn install
```

In case of success, the ```mvn install``` command will install the VCL adapter bundle in the local Maven repository folder.  
After this operation, you have to add the Maven repository project as an Apache Karaf bundle, telling Karaf to load it after the GreenVulcano core bundles, since the VCL adapter requires the GreenVulcano bundles in order to start correctly.  
This constraint can be enforced by properly configuring the *level* of the Karaf bundle: the lower the level number, the earlier the bundle will be loaded by Karaf.

For the VCL adapter bundle, we will use a bundle level higher than the GreenVulcano core bundles (i.e. ```80```). The following command will install the VCL adapter bundle and set its level to ```96``` by convention, using the ```-l``` attribute:

```shell
karaf@root()> bundle:install -l 96 mvn:it.greenvulcano.gvesb.adapter/gvvcl-redis/4.0.0-SNAPSHOT

Bundle ID: x
```

Make sure that the bundle ```GreenVulcano ESB Adapter for Redis``` appears in the ```list``` of installed bundles in ```Installed``` status and with bundle level (```Lvl```) equal to ```96``` (or at least strictly higher than ```80```).  
Then, use its ID to put the bundle in ```Active``` status by executing the following command:

```shell
karaf@root()> start x

list | grep GreenVulcano ESB Adapter for Redis
```

## Using the VCL adapter in your GreenVulcano project

In order to use the features of the Redis adapter in your GreenVulcano project, you need to define a proper System-Channel-Operation set of nodes. You can do that by manually editing the GVCore.xml file, or by using DeveloperStudio. In that case, you will have to download the dtds/ folder on this repository and replace it with the one in your current project. 

### Declaring the System-Channel-Operation for the Redis database

Let's assume you want to interact with your database in "localhost". By default, that's gonna be at ```127.0.0.1:6379```.
Insert the ```<redis-call>``` XML node in the ```<Systems></Systems>``` section of file ```GVCore.xml```. Here's an example:

```xml
<System id-system="REDIS" system-activation="on">
    <Channel enabled="true" endpoint="localhost" id-channel="redis-channel" type="RedisDBAdapter">
    	<redis-call name="ciao" type="call">
      	<set name="prova" value="ciao2"/>
         <get name="prova"/>
      </redis-call>
	</Channel>
</System>
```

Some constraints apply to these XML nodes.

- The ```<Channel>``` XML node must comply with the following syntax:
    - ```endpoint``` must contain a URI string correctly referencing the hostname and the port of an operational Redis server. In case you're running it locally, you may also simply insert ```"localhost"```;
- The ```<redis-call>``` XML node must comply with the following syntax:
    - ```type``` must be declared and set equal to ```"call"```;
    - ```name``` must be declared: it defines the name of the Operation node;

- The ```redis-call``` node, as of version 1.0, supports three types of operation:
    - ```get``` its "key" attribute must be set to the key you're looking to retrieve from the platform;
    - ```set``` contains "name", "key" and "value". These respectively represent the operation name, the name of the key you're looking to set, and the value it must be set to. ***If "value" is left empty, GV will automatically parse the GVBuffer data***;
    - ```delete``` "name" and "key", representing the name of the key you wish to delete;
    - ```keys``` "expression" takes a pattern which lets you specify which key names to retrieve;
    
When we're done defining our System node, we can now use it in a Service-Operation, such as:

```xml
<Services>
            <Description>This section contains a list of all services provided by GreenVulcano ESB</Description>
            <Service group-name="DEFAULT_GRP" id-service="testService"
                     service-activation="on" statistics="off">
                <Operation class="it.greenvulcano.gvesb.core.flow.GVFlowWF"
                           loggerLevel="ALL" name="testOperation"
                           operation-activation="on" type="operation">
                    <Flow first-node="RedisCall">
                        <GVOperationNode class="it.greenvulcano.gvesb.core.flow.GVOperationNode"
                                         id="RedisCall" id-channel="redis-channel"
                                         id-system="REDIS" input="input"
                                         next-node-id="terminated" op-type="call"
                                         operation-name="ciao" point-x="231" point-y="150"
                                         type="flow-node"/>
                        <GVEndNode class="it.greenvulcano.gvesb.core.flow.GVEndNode"
                                   id="terminated" op-type="end" output="input"
                                   point-x="435" point-y="150" type="flow-node"/>
                    </Flow>
                </Operation>
            </Service>
</Services>
```

You can test these Services selecting them in the Execute section of the GreenVulcano dashboard.

#### WARNING: This GreenVulcano extension is currently in Alpha stage and its features are still limited and mostly untested. Feel free to report any bugs!
# gv-adapter-redis
