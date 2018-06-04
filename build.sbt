import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}
import Dependencies._
version in ThisBuild := "0.0.1"

val akkaVersion = "2.5.11"
val akkaHttpVersion = "10.1.1"


val `sample-akka-http` = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    organization := "Rogers",
    scalaVersion := "2.12.5",
    scalacOptions in Compile ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
    javacOptions in Compile ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    javacOptions in doc in Compile := Seq("-Xdoclint:none"),
    libraryDependencies ++= Seq(
        akkaHttp,
        akkaSlf4j,
        googleGuice,
        googleGuava,
        akkaHttpJackson,
        swaggerHttp,
        junits
    ).++(akka_test),

    fork in run := true,
    mainClass in(Compile, run) := Some("bootstrap.ServiceBootstrap"),
    // disable parallel tests
    parallelExecution in Test := false,
    licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0"))),
    // These values will be filled in by the k8s StatefulSet and Deployment
    dockerEntrypoint ++= Seq(
      """-Dconfig.file="$CONFIG_FILE_PATH""""
      /* """-DactorSystemName="$AKKA_ACTOR_SYSTEM_NAME"""",
      """-Dakka.remote.netty.tcp.hostname="$AKKA_REMOTING_LOGICAL_HOST"""",
      """-Dakka.remote.netty.tcp.port="$AKKA_REMOTING_BIND_PORT"""",
      """-Dakka.remote.netty.tcp.bind-hostname="$AKKA_REMOTING_BIND_HOST"""",
      """-Dakka.remote.netty.tcp.bind-port="$AKKA_REMOTING_BIND_PORT"""",
      """-Dakka.cluster.seed-nodes.0="akka.tcp://${AKKA_ACTOR_SYSTEM_NAME}@${AKKA_SEED_NODE_HOST_0}:${AKKA_SEED_NODE_PORT}"""",
      """-Dakka.cluster.seed-nodes.1="akka.tcp://${AKKA_ACTOR_SYSTEM_NAME}@${AKKA_SEED_NODE_HOST_1}:${AKKA_SEED_NODE_PORT}"""",
      """-Dakka.cluster.seed-nodes.2="akka.tcp://${AKKA_ACTOR_SYSTEM_NAME}@${AKKA_SEED_NODE_HOST_2}:${AKKA_SEED_NODE_PORT}"""",
      """-Dsample.http.hostname="$SAMPLE_HTTP_HOST"""",
      """-Dsample.http.port="$SAMPLE_HTTP_PORT"""",
      "-Dakka.io.dns.resolver=async-dns",
      "-Dakka.io.dns.async-dns.resolve-srv=true",
      "-Dakka.io.dns.async-dns.resolv-conf=on" */
    ),
    // Make sure that the shell variables are available for the command
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args@_*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case v => Seq(v)
      },
    // Which docker repository to publish to
    dockerRepository := Some("samplegroup"),
    // Update the latest tag when publishing
    dockerUpdateLatest := true
  )

resolvers += Resolver.sbtPluginRepo("releases")