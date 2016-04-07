import java.sql.ResultSet

import scalikejdbc._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

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

  //  def get[A: TypeBinder](columnName: ColumnName): A
  // def get[A](columnName: ColumnName)(implicit binder: TypeBinder[A]): A

  implicit val envBinder: TypeBinder[Environment] = new TypeBinder[Environment] {

    def fromString(string: String) = string match {
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

}

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

    def read(value: JsValue) = {

      value.asJsObject.getFields("host", "port", "name", "holderEmail", "environment") match {
        case Seq(JsString(host), JsNumber(port), JsString(name), JsString(holderEmail), JsString(environment)) =>
          new Service(host, port.toInt, name, holderEmail, fromString(environment))
        case _ => throw new DeserializationException("Service expected")
      }
    }
  }
}