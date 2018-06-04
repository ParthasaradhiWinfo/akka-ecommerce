/*
 * Copyright (c) 2015. Rogers Communications Inc. All Rights reserved.
 */

import sbt._

object Dependencies {

  val akkaVersion = "2.5.11"
  val akkaHttpVersion = "10.1.1"

  val akka_test : Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-testkit" % "2.5.11" % "test",
    "org.powermock" % "powermock-core" % "1.7.3" % "test",
    "org.powermock" % "powermock-module-junit4-rule" % "1.7.3" % "test",
    "org.powermock" % "powermock-api-mockito2" % "1.7.3" % "test",
    "org.powermock" % "powermock-module-junit4" % "1.7.3" % "test"
  )


  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val googleGuice = "com.google.inject" % "guice" % "3.0"
  val googleGuava = "com.google.guava" % "guava" % "18.0" force()
  val akkaHttpJackson = "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpVersion force()
  val dptCore = "com.rogers" %% "dpt-cassandra-core" % "1.0.0"
  val swaggerHttp = "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.14.0"
  val ningAsyncHttp ="com.ning" % "async-http-client" % "1.9.40"
  val junits = "junit" % "junit" % "4.12" % "test"
}
