name := "service-catalog"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"       % "2.3.5",
  "mysql" % "mysql-connector-java" % "5.1.6",
  "ch.qos.logback"  %  "logback-classic"   % "1.1.3"
)