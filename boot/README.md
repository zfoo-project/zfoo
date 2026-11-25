### zfoo Spring Boot Support

- zfoo boot makes it easy to create a Spring Boot application with zfoo using auto-configuration

#### Protocol discovery on the JVM

A `NetConfig` bean with no `protocolLocation` uses Spring Boot's auto-configuration packages (normally the package of your `@SpringBootApplication` class and its subpackages). Put message classes there and annotate them with `@Protocol(id = ...)`. They do not need to be Spring beans. The built-in common packets, router attachments and broker registration packet are included. All discovered protocols use the default module.

Set `protocolLocation` to a classpath XML file for explicit control, e.g. for named modules, XML-assigned IDs or native images. An explicit location is always used as-is, even if the resource is missing. For shared messages elsewhere on the JVM, extend Boot's roots with `@AutoConfigurationPackage(basePackageClasses = ...)` or use XML. Changing only `@ComponentScan` does not change these roots. Plain Spring applications retain their existing XML configuration.
