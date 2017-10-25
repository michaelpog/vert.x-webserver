package michaelpog.com;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;

@Configuration
public class BeanConfig {

    @Bean
    public Vertx vertxBean() {
        return Vertx.vertx();
    }

    @Bean
    public Responder responder(Vertx vertx) {
        return new Responder(vertx);
    }

    @Bean
    @ConditionalOnProperty(value = { "httpserver.range" })
    public WebServerVerticle webServerVerticleWithRange(
            @Value("#{'${httpserver.range}'.split(',')}") final List<Integer> portsRange,
            @Value("${httpserver.sleep.meanMs:120.0}") double sleepMeanMs,
            @Value("${http.response.immediate.percent}") float httpResponseImmediatePercent,
            final HttpServerOptions httpServerOptions,
            final Responder responder) {
        return new WebServerVerticle(portsRange.get(0),
                portsRange.get(1),
                httpServerOptions,
                responder,
                sleepMeanMs,
                httpResponseImmediatePercent);
    }

    @Bean
    @ConditionalOnProperty(value = { "httpserver.ports" })
    public WebServerVerticle webServerVerticleWithPortsList(
            @Value("#{'${httpserver.ports}'.split(',')}") final List<Integer> ports,
            @Value("${httpserver.sleep.meanMs:120.0}") double sleepMeanMs,
            @Value("${http.response.immediate.percent}") float httpResponseImmediatePercent,
            final HttpServerOptions httpServerOptions,
            final Responder responder) {
        return new WebServerVerticle(ports, httpServerOptions, responder, sleepMeanMs, httpResponseImmediatePercent);
    }

    @Bean
    public HttpServerOptions httpServerOptions() {
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        return httpServerOptions;
    }
}
