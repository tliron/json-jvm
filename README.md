
JSON JVM
========

A lightweight, extensible JSON encoder/decoder intended to make it easy to
support JSON on non-Java languages running on the JVM.

It's extensible in two ways:

* Easy to add support for custom types, for example native dict and list
  implementations. This is efficient because it means you always work directly
  with your language's native types, without the extra step of having to
  translate through java.util.HashMap, java.util.ArrayList.

* Easy to add support for extended JSON formats, such as [MongoDB's]
  (http://docs.mongodb.org/manual/reference/mongodb-extended-json/). For an
  implementation based on this library, see [MongoDB JVM]
  (https://github.com/tliron/mongodb-jvm).

JSON JVM is very lightweight and straightforward by design. If you need a more
robust solution with many more features, see [Jackson]
(https://github.com/FasterXML/jackson).

Currently the project supports standard Java types, and JavaScript via both the [Nashorn]
(http://openjdk.java.net/projects/nashorn/) and 
[Rhino]
(https://github.com/mozilla/rhino) engines.