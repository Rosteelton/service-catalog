import scalikejdbc._

case class Service(host: String, port: Int, name: String, holderEmail: Option[String], environment: Option[String])

object Service extends SQLSyntaxSupport[Service] {
  override val tableName = "service"

  def apply(rs: WrappedResultSet): Service =
    new Service(rs.string("host"), rs.int("port"), rs.string("name"), rs.stringOpt("holderEmail"), rs.stringOpt("environment"))
}

