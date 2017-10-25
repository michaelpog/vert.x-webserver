package michaelpog.com;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VerticlesDeployer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    public VerticlesDeployer(Vertx vertx, WebServerVerticle webServerVerticle){
        deployVerticles(vertx, webServerVerticle);
    }

    public void deployVerticles(Vertx vertx, WebServerVerticle webServerVerticle){
        int cores = Runtime.getRuntime().availableProcessors();
        logger.info("starting with {} threads", cores);
        for(int i =0 ; i<cores; i++){
            vertx.deployVerticle(webServerVerticle);
        }
        setExceptionHandlers(vertx);
    }

    private void setExceptionHandlers(Vertx vertx) {
        ((VertxInternal) vertx).getAcceptorEventLoopGroup().next().execute(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    System.out.println("Thread "+t.getName()+ "exception " +e);
                    e.printStackTrace();
                }
            });
        });
    }


}
