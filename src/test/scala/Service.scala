import java.sql.ResultSet
import scalikejdbc._

sealed trait Environment
object Environment {
  case object Production extends Environment
  case object Test extends Environment
  case object Development extends Environment
}


case class Service(host: String, port: Int, name: String, holderEmail: String, environment: Environment) {
 override def toString: String = {
   "host: " + this.host + " |port: " + this.port + " |name: " + this.name + " |holderEmail: "+ this.holderEmail.toString + " |environment: " + this.environment
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