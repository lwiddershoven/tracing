# Tracing with Spring Cloud Sleuth, OpenTelemetry and Jaeger

## Attribution

This project is copied from https://github.com/thombergs/code-examples/tree/master/spring-cloud/tracing.
The code-examples project is large, this is just the spring-cloud/tracing example.

The original article is: [Tracing with Spring Boot, OpenTelemetry, and Jaeger](https://reflectoring.io/spring-boot-tracing)

This has been adapted to
- use base images from the new location (adoptopenjdk -> temurin)
- update java version 11 -> 21
- not use logz.io but an in-memory Jaeger setup as collector

# Running this project

```shell
./mvnw clean install
docker compose up 
# or docker compose up --build --force-recreate -d # to make sure the new code or application.yaml is used by the container
```

Important URLS:
- http://localhost:16686  Jaeger UI to view registered traces
- http://localhost:8086/customers/2  REST call to API project to generate 1 trace for 'customers'
- http://localhost:8086/oldcustomers/6 REST call to API project to generate 1 trace for 'oldcustomers'


## How does it work

This project uses the micrometer API to collect metrics and traces. The traces are then send (exported)to Jaeger
using the opentelemetry protocol. This is configured in the pom:
```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```

These dependencies are then configured in application.yml (or application.properties or application-<profile>.yml or...)
```yaml
management:
  tracing:
    sampling:
      probability: 1 # Register every trace instead of the default 0.1 (10%)
      
    baggage:
      remote-fields: use-case-baggage # incoming headers to read as baggage
      tag-fields: use-case-baggage # baggage to add to trace info submitted to jaeger

    tracing:
      endpoint: http://collector:4317 # Jaeger endpoint
      transport: grpc # 
```

The code extends the standard traces with both baggage and custom attributes. Baggage is extra
information that is passed through to the next system in line, for example using a header. That system 
can then use it. Attributes are extra information on the span that gets transmitted to the tracing system (jaeger).

The configuration property `management.tracing.baggage.tag-fields` tells Spring which Baggage should
be copied to attributes and thus also end up in Jaeger. With that done one can search in jaeger on `tag=value`
and create a 'Deep Dependency Graph' to see which systems are associated with that tag.

In this demo, we determine the use-case at the entry point and by doing a Deep Dependency Graph we generate a live
view on which systems are involved in handling that use-case.

The awkward names 'use-case-baggage' and 'use-case-tag' are for demo purposes only, so that it is really clear where 
the data came from.

## Generic Tips

Some extra tips on working with this code.

- If you want a fast restart after changes, do `./mvnw clean install && or docker compose up --build --force-recreate -d`.
- if you use oh-my-zsh be sure to add the docker and docker-compose autocomplete plugins to your .zshrc
- you can view the detected baggage by doing `docker logs tracing-customer-service-1` (automcomplete helps)
- ctrl-r part-of-command searches through the history
- if you think you have connectivity issues in Docker, be sure to `docker exec -it <pod>  sh` and do a wget from within there. Type exit when done.


# Docs

https://docs.spring.io/spring-boot/reference/actuator/tracing.html