package actors.service.cassandra;

import akka.actor.*;
import com.typesafe.config.Config;
import messages.GetCartRequest;

public class CassandraDataReaderActor extends AbstractActor {

    private final Config appConfig;

    public CassandraDataReaderActor(Config appConfig) {
        this.appConfig = appConfig;
    }

    public static Props props(Config config) {
        return Props.create(CassandraDataReaderActor.class,
                () -> new CassandraDataReaderActor(config));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetCartRequest.class, message ->{
                    getSender().tell("Reading Data From Cassandra TODO", ActorRef.noSender());
                })
                .build();
    }

}
