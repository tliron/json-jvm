
JSON JVM
========

A lightweight, extensible JSON encoder/decoder intended to make it easy to
support JSON on non-Java languages running on the JVM.

It's extensible in two ways:

* Easy to add support for custom types, such as native dict, list, string,
  number, and other implementations. This is efficient because it means you
  always work directly with your language's native types, without the extra step
  of having to translate through java.util.HashMap, java.util.ArrayList, etc.

* Easy to add support for extended JSON formats, such as
  [MongoDB's](http://docs.mongodb.org/manual/reference/mongodb-extended-json/). For an
  implementation based on this library, see [MongoDB JVM](https://github.com/tliron/mongodb-jvm).

JSON JVM is very lightweight and straightforward by design. If you need a more
robust solution with many more features, see [Jackson](https://github.com/FasterXML/jackson).

Currently the project supports standard Java types, and JavaScript via both the
[Nashorn](http://openjdk.java.net/projects/nashorn/) and [Rhino](https://github.com/mozilla/rhino)
engines.

[![Download](http://threecrickets.com/media/download.png "Download")](http://repository.threecrickets.com/maven/com/threecrickets/jvm/json-jvm/)

Maven:

    <repository>
        <id>threecrickets</id>
        <name>Three Crickets Repository</name>
        <url>http://repository.threecrickets.com/maven/</url>
    </repository>
    <dependency>
        <groupId>com.threecrickets.jvm<</groupId>
        <artifactId>json-jvm</artifactId>
    </dependency>


Building JSON JVM
-----------------

All you need to build JSON JVM is [Ant](http://ant.apache.org/).

Then, simply change to the "/build/" directory and run "ant".

Your JDK should be at least version 8 in order to support the Nashorn
implementation, although there is a workaround for earlier JDK versions (see
comment in "/build/custom.properties".)

During the build process, build and distribution dependencies will be
downloaded from an online repository at http://repository.threecrickets.com/, so
you will need Internet access.

The result of the build will go into the "/build/distribution/" directory.
Temporary files used during the build process will go into "/build/cache/",
which you are free to delete.


Configuring the Build
---------------------

The "/build/custom.properties" file contains configurable settings, along with
some commentary on what they are used for. You are free to edit that file,
however to avoid git conflicts, it would be better to create your own
"/build/private.properties" instead, in which you can override any of the
settings. That file will be ignored by git.

To avoid the "bootstrap class path not set" warning during compilation
(harmless), configure the "compile.boot" setting in "private.properties" to the
location of an "rt.jar" file belonging to JVM version 7.


Deploying to Maven
------------------

You do *not* need Maven to build JSON JVM, however you can deploy your build to
a Maven repository using the "deploy-maven" Ant target. To enable this, you must
install [Maven](http://maven.apache.org/) and configure its path in
"private.properties".


Still Having Trouble?
---------------------

Join the [Prudence Community](http://groups.google.com/group/prudence-community), and tell us where
you're stuck! We're very happy to help newcomers get up and running.