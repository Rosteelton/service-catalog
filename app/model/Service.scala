package model

import java.sql.ResultSet

import model.Environment.{Development, Production, Test}
import play.api.libs.json.{JsValue, _}
import scalikejdbc._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}
import play.api.libs.functional.syntax._
import play.api.libs.json
import solovyev.csv.{Decoder, Encoder}

import scala.util.{Try, Success, Failure}

sealed trait Environment

object Environment {

  case object Production extends Environment

  case object Test extends Environment

  case object Development extends Environment

}

case class Service(host: String, port: Int, name: String, holderEmail: String, environment: Environment) {

  override def toString: String = {
    val tmp = this.environment.toString
    String.format("%-40s%-10s%-40s%-40s%11s", host, port: Integer, name, holderEmail, tmp)
  }
}

object Service {

  implicit val envBinder: TypeBinder[Environment] = new TypeBinder[Environment] {

    def fromString(string: String): Environment = string match {
      case "Production" => Environment.Production
      case "Test" => Environment.Test
      case "Development" => Environment.Development
    }


    override def apply(rs: ResultSet, columnIndex: Int): Environment =
      fromString(rs.getString(columnIndex))

    override def apply(rs: ResultSet, columnLabel: String): Environment =
      fromString(rs.getString(columnLabel))
  }

  def apply(rs: WrappedResultSet): Service =
    new Service(rs.string("host"), rs.int("port"), rs.string("name"), rs.string("holderEmail"), rs.get[Environment]("environment"))


  def fromStringToEnvironment(string: String): Either[String, Environment] = string match {
    case "Production" => Right(Environment.Production)
    case "Test" => Right(Environment.Test)
    case "Development" => Right(Environment.Test)
    case _ => Left("Wrong string of environment!")
  }

  def fromEnvironmentToString(environment: Environment): String = environment match {
    case Environment.Test => "Test"
    case Environment.Development => "Development"
    case Environment.Production => "Production"
  }

  implicit val environmentReads: Reads[Environment] = Reads[Environment] {
    case play.api.libs.json.JsString(value) =>
      fromStringToEnvironment(value) match {
        case Right(some) => JsSuccess(some)
        case Left(string) => JsError("Wrong string")
      }
    case _ => JsError("Expect string")
  }


  implicit val environmentWrites: Writes[Environment] = Writes[Environment] {
    environment => json.JsString(fromEnvironmentToString(environment))
  }


  implicit val serviceReads: Reads[Service] = (
    (JsPath \ "host").read[String] and
      (JsPath \ "port").read[Int] and
      (JsPath \ "name").read[String] and
      (JsPath \ "holderEmail").read[String] and
      (JsPath \ "environment").read[Environment]
    ) ((host, port, name, holderEmail, environment) => Service(host, port, name, holderEmail, environment))


  implicit val serviceEncoder: Encoder[Service] = new Encoder[Service] {
    override val defaultDelimiter = "#"

    override def writes(o: Service): String = {
      o.host + defaultDelimiter + o.port.toString + defaultDelimiter + o.name + defaultDelimiter + o.holderEmail + defaultDelimiter + o.environment.toString
    }
  }

  implicit val serviceDecoder: Decoder[Service] = new Decoder[Service] {
    override val defaultDelimiter = "#"
    override def reads(str: String): Either[String, Service] = {
      val list = str.split(defaultDelimiter).toList
      if (list.length == 5) {
        fromStringToEnvironment(list(4)) match {
          case Left(err) => Left(err)
          case Right(some) =>
            Try(new Service(list.head.trim, list(1).trim.toInt, list(2).trim, list(3).trim, some)) match {
              case Success(serv) => Right(serv)
              case Failure(e) => Left(e.getMessage)
            }
        }
      } else {
        return Left("Invalid csv format: expected 5 fields but found: " + list.length)
      }
    }
  }

    implicit val serviceWrites: Writes[Service] = (
      (JsPath \ "host").write[String] and
        (JsPath \ "port").write[Int] and
        (JsPath \ "name").write[String] and
        (JsPath \ "holderEmail").write[String] and
        (JsPath \ "environment").write[Environment]
      ) (unlift(Service.unapply))




  object MyJsonProtocol extends DefaultJsonProtocol {

    implicit object ServiceJsonFormat extends RootJsonFormat[Service] {

      def write(s: Service) = JsObject(
        "host" -> JsString(s.host),
        "port" -> JsNumber(s.port),
        "name" -> JsString(s.name),
        "holderEmail" -> JsString(s.holderEmail),
        "environment" -> JsString(s.environment.toString)
      )

      def fromString(string: String) = string match {
        case "Production" => Environment.Production
        case "Test" => Environment.Test
        case "Development" => Environment.Development
      }

      def read(value: spray.json.JsValue) = {
        value.asJsObject.getFields("host", "port", "name", "holderEmail", "environment") match {
          case Seq(JsString(host), JsNumber(port), JsString(name), JsString(holderEmail), JsString(environment)) =>
            new Service(host, port.toInt, name, holderEmail, fromString(environment))
          case _ => throw new DeserializationException("Service expected")
        }
      }
    }

  }

}

