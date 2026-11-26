### zfoo Spring Boot Support

- zfoo boot makes it easy to create a Spring Boot application with zfoo using auto-configuration

#### Protocol discovery on the JVM

A `NetConfig` bean with no `protocolLocations` uses Spring Boot's auto-configuration packages (normally the package of your `@SpringBootApplication` class and its subpackages). Put message classes there and annotate them with `@Protocol(id = ...)`. They do not need to be Spring beans. The built-in common packets, router attachments and broker registration packet are included. All discovered protocols use the default module.

To **replace** the Boot roots, set `protocolLocations` explicitly:

| Example | Effect |
| --- | --- |
| `["com.example.messages", "com.example.chat"]` | Scan these packages and their subpackages. |
| `["protocol.xml", "chat-protocol.xml"]` | Merge and load these XML resources. Missing XML is an error. |
| `["protocol.xml", "com.example.chat"]` | Load the XML resource and scan the package into the default module; explicit XML definitions take precedence. |

An existing classpath resource takes precedence over a bare package name. An explicit package must contain at least one `@Protocol` class. Explicit locations also work in plain Spring; only default root discovery requires Boot. Keep XML for named modules, XML-assigned IDs or native images. To **extend** rather than replace Boot's roots, use `@AutoConfigurationPackage(basePackageClasses = ...)`; changing only `@ComponentScan` does not change them.
