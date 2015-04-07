Native support for conversion to/from native language objects and JSON.

Currently supported engines:

  * Nashorn/JavaScript
  * Rhino/JavaScript

### Faster ###

There are JSON libraries available for some languages, for example the [JSON API for JavaScript](http://www.json.org/js.html). However, our code is written in pure Java and works directly with the native language engine objects for much better performance.

### Extensible ###

The API is also designed to be extensible, allowing you to plug-in high-performance JSON extensions

For an example, see the [MongoDB JVM](http://code.google.com/p/mongodb-jvm/) project, where support is added for [MongoDB's extended JSON specification](http://docs.mongodb.org/manual/reference/mongodb-extended-json/).

### Get It ###

The latest JVM binary and Java API documentation jars are available [here](http://repository.threecrickets.com/maven/com/threecrickets/jvm/json-jvm/).

To install the just the JVM binary via Maven:
```
<repository>
    <id>three-crickets</id>  
    <name>Three Crickets Repository</name>  
    <url>http://repository.threecrickets.com/maven/</url>  
</repository>

<dependency>
    <groupId>com.threecrickets.jvm</groupId>
    <artifactId>json-jvm</artifactId>
    <version>[1.1,1.2)</version>
</dependency>
```
Hosted by [Three Crickets](http://threecrickets.com/).