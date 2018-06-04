package bootstrap;

import actors.service.cassandra.CassandraDataReaderActor;
import actors.supervisor.GetCartActor;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.*;
import akka.pattern.PatternsCS;
import com.fasterxml.jackson.databind.ObjectMapper;
import akka.actor.*;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.NotUsed;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.Config;
import constants.RogersConstants;
import messages.GetCartRequest;
import sun.awt.ConstrainableGraphics;

import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

import javax.ws.rs.Path;


public class RoutesActor extends AbstractActor {

    private final Config appConfig;
    private final ActorRef getCartSupervisorActor;
    public final ObjectMapper objectMapper;
    private final CompletionStage<ServerBinding> binding;

    public RoutesActor(Config appConfig) {
        super();
        this.appConfig = appConfig;
        ActorSystem system = context().system();
        this.objectMapper = new ObjectMapper();
        this.getCartSupervisorActor = system.actorOf(GetCartActor.props(appConfig), "GetCartActor");
        // Set up and start the HTTP server
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        // In order to access all directives we need an instance where the routes are defined
        final Routes routes = new Routes();
        final String hostname = appConfig.getString(RogersConstants.CARTMS_HTTP_HOSTNAME);
        final int port = appConfig.getInt(RogersConstants.CARTMS_HTTP_PORT);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow =
                routes.createRoute(getSelf(), hostname, port)
                        .flow(system, materializer);
        binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost(hostname, port), materializer);

    }

    public static Props props(Config config) {
        return Props.create(RoutesActor.class, () -> new RoutesActor(config));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Stop.class, stop -> {
                    //Trigger HTTP server unbinding from the port
                    binding.thenCompose(ServerBinding::unbind)
                            //Shutdown when done
                            .thenAccept(unbound -> getContext().stop(getSelf()));
                }).build();
    }

    private enum Stop {
        INSTANCE
    }

    // In order to access all directives the code needs to be inside an instance of a class that extends AllDirectives
    class Routes extends AllDirectives {
        public Route createRoute(ActorRef mainActor, String hostname, int port) {
            final String host = hostname + ":" + port;
            return route(
                    fetchCartDetails(),
                    getStopPath(mainActor, host)
            );
        }

        public Route fetchCartDetails() {
            ///v1/commerce/cart/{cartID}
            final PathMatcher1 segment = PathMatchers.segment(appConfig.getString(RogersConstants.CARTMS_API_COMMERCE))
                    .slash(appConfig.getString(RogersConstants.CARTMS_API_CART))
                    .slash(PathMatchers.segment());
            return route(
                get(() -> pathPrefix(appConfig.getString(RogersConstants.CARTMS_API_VERSION),
                            () -> path(segment, cartId ->{
                                    final CompletionStage<Object> consolidatedDetailF =
                                    PatternsCS.ask(getCartSupervisorActor, new GetCartRequest(cartId.toString()),
                                    appConfig.getLong("actors.timeout"));
                                    return onSuccess(() -> consolidatedDetailF,
                                    cartDetails -> this.complete(StatusCodes.OK, cartDetails, Jackson.marshaller()));
                            })
                            )
                        )

            );
        }

        @Path("/stop")
        public Route getStopPath(ActorRef actorRef, String host) {
            return route(path(RogersConstants.STOP_ROUTE, () ->
                            get(() -> {
                                actorRef.tell(Stop.INSTANCE, ActorRef.noSender());
                                return complete("Server " + host + " is shutting Down\n");
                            })
                    )
            );
        }
    }


}
