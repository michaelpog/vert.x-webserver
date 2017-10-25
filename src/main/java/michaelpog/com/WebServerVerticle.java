package michaelpog.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class WebServerVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String STATUS_PATH = "/status";
    private final HttpServerOptions httpServerOptions;
    private final Responder responder;
    private List<Integer> ports;
    private final double sleepMeanMs;
    private Random random = new Random();
    private final float httpResponseImmediatePercent;

    public WebServerVerticle(final List<Integer> ports,
            final HttpServerOptions httpServerOptions,
            final Responder responder,
            double sleepMeanMs,
            float httpResponseImmediatePercent) {
        this.httpServerOptions = httpServerOptions;
        this.responder = responder;
        this.ports = ports;
        this.sleepMeanMs = sleepMeanMs;
        this.httpResponseImmediatePercent = httpResponseImmediatePercent;
    }

    public WebServerVerticle(final int portLowerBound,
            final int portUpperBound,
            final HttpServerOptions httpServerOptions,
            final Responder responder,
            final double sleepMeanMs,
            float httpResponseImmediatePercent) {
        this(new ArrayList<>(), httpServerOptions, responder, sleepMeanMs, httpResponseImmediatePercent);
        for (int port = portLowerBound; port <= portUpperBound; port++) {
            this.ports.add(port);
        }
    }

    @Override
    public void start() throws Exception {
        logger.debug("started thread {}", Thread.currentThread().getName());
        for (int port : ports) {
            HttpServer httpServer = getVertx().createHttpServer(httpServerOptions);
            Router router = setupRoutes();
            httpServer.requestHandler(router::accept);
            logger.info("Listening on port {}", port);
            httpServer.listen(port);
        }
    }

    private Router setupRoutes() {
        Router router = Router.router(getVertx());
        router.get(STATUS_PATH).handler(this::statusHandler);
        router.route().handler(BodyHandler.create());
        router.post().handler(this::handlePostRequest);
        router.put().handler(this::handleUpdateRequest);
        return router;
    }

    private void statusHandler(RoutingContext routingContext) {
        routingContext.response().end("ok");
    }

    private void handlePostRequest(RoutingContext routingContext) {
        if(isRespondAsync()) {
            respondAsync(routingContext);
        }
        else {
            respondImmediately(routingContext);
        }
    }

    private boolean isRespondAsync() {
        return httpResponseImmediatePercent == 0.0 || httpResponseImmediatePercent < random.nextFloat()  ;
    }

    private void respondImmediately(RoutingContext routingContext) {
        routingContext.response().end(responder.getResponse());
    }

    private void respondAsync(RoutingContext routingContext) {
        //    http://www.javamex.com/tutorials/random_numbers/gaussian_distribution_2.shtml
        // sleep interval is approximately normally distributed:
        // 40ms - 200ms
        // == 160ms

        // mean is 120 (ms)
        // P(120-3*s <= x <= 120+3*s) == 0.9973
        // 120-3*s == 40, 120+3*s == 200
        // 3*s = 120-40 = 80, s = 26.666
        double delay = random.nextGaussian() * 26.6666 + this.sleepMeanMs;

        vertx.setTimer((long) (delay), id -> {
            routingContext.response().end(responder.getResponse());
        });
    }

    private void handleUpdateRequest(RoutingContext routingContext) {

        if (routingContext.getBody().length() <= 0) {
            routingContext.response().setStatusCode(404).end();
            return;
        }
        final String bidRequest = routingContext.getBodyAsString();
        responder.updateDefaultResponse(bidRequest);
        routingContext.response().end();
    }
}
