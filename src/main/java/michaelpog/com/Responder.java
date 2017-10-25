package michaelpog.com;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Responder
{
    private Vertx vertx;
    private String defaultBid;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Responder(Vertx vertx){
        this.vertx =  vertx;
        initDefaults();
    }

    private void initDefaults(){
        vertx.fileSystem().readFile("defaultBid.json", result -> {
            if (result.succeeded()) {

                defaultBid = result.result().toString();

            } else {
                logger.error("Oh no ...", result.cause());

            }
        });
    }

    public String getResponse(){
        return defaultBid;
    }

    public void updateDefaultResponse(final String response){
        defaultBid = response;
    }
}
