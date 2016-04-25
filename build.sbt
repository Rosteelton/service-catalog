name := "service-catalog"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "2.3.5",
  "mysql" % "mysql-connector-java" % "5.1.6",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "io.spray" %% "spray-json" % "1.3.2",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.3.5",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.5.0"
)
routesGenerator := StaticRoutesGenerator


enablePlugins(PlayScala)