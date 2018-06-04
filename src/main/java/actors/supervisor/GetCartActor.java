package actors.supervisor;

import actors.service.cassandra.CassandraDataReaderActor;
import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.routing.FromConfig;
import com.typesafe.config.Config;
import messages.GetCartRequest;
import scala.concurrent.duration.Duration;

import java.io.IOException;

public class GetCartActor extends AbstractActor {
    private final ActorRef cassandraDataReaderActor;
    private final Config config;

    public GetCartActor(Config config) {
        this.config = config;
        this.cassandraDataReaderActor = getContext().actorOf(FromConfig.getInstance().
                        props(CassandraDataReaderActor.props(config)), "cassandraDataReaderActor");
    }

    public static Props props(Config config) {
        return Props.create(GetCartActor.class, () -> new GetCartActor(config));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetCartRequest.class, message ->{
                    cassandraDataReaderActor.tell(message, getSender());
                }).build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(50, Duration.create("1 minute"),
                DeciderBuilder.match(NullPointerException.class, e -> SupervisorStrategy.resume())
                        .match(IOException.class, e -> SupervisorStrategy.resume())
                        .match(Exception.class, e -> SupervisorStrategy.restart())
                        .match(Throwable.class, e -> SupervisorStrategy.restart())
                        .build()
        );
    }

}
