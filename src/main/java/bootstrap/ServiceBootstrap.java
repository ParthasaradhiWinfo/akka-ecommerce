package bootstrap;

import akka.actor.ActorSystem;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import constants.RogersConstants;
import constants.SessionConstants;
import util.*;

import java.util.logging.Logger;


public class ServiceBootstrap {

    private static final Injector injector = Guice.createInjector(new AppModule());
    private static Config appConfig = ConfigFactory.load();
    private static final Logger LOGGER = Logger.getLogger(ServiceBootstrap.class.getName());

    public static void main(String[] args) {

        appConfig = injector.getInstance(Key.get(Config.class, Names.named(SessionConstants.APP_CONFIG)));
        if (args.length > 0) {
            // Check that we have an even number of ports, one remoting and one http for each actor system
            if (args.length % RogersConstants.EVEN_NUM == RogersConstants.DIGIT_ONE) {
                LOGGER.warning("[ERROR] Need an even number of ports! One remoting and one HTTP port for each actor system.");
                System.exit(1);
            }
            createAndStartActorSystem();
        } else {
            createAndStartActorSystem();
        }
    }

    private static void createAndStartActorSystem() {
        final ActorSystem system = injector.getInstance(Key.get(ActorSystem.class, Names.named(RogersConstants.ACTOR_SYSTEM)));
        //TODO
        //final AsyncHttpClient asyncHttpClient = injector.getInstance(Key.get(AsyncHttpClient.class, Names.named(AsyncClient.HTTPCLIENT)));
        //createRoutesActor(system, asyncHttpClient);
        createRoutesActor(system);
    }

    private static void createRoutesActor(ActorSystem system) {
        system.actorOf(RoutesActor.props(appConfig));
    }
}
