import scalikejdbc._
import UserCommandHandler._

sealed trait UserCommand
object UserCommand {
  case class AddService(host: String, port: Int, name: String, holderEmail: String, environment: Environment) extends UserCommand
  case class FindService(host: String, port: Int) extends UserCommand
  case class UpdateService(hostToUpdate: String, portToUpdate: Int, host: String, port: Int, name: String, holderEmail: String, environment: Environment) extends UserCommand
  case class DeleteService(host: String, port: Int) extends UserCommand
  case object ShowAll extends UserCommand
  case object ImportService extends UserCommand
  case object Exit extends UserCommand
}

object App extends App {

  Class.forName("com.mysql.jdbc.Driver")
  ConnectionPool.singleton("jdbc:mysql://localhost:3306/services_catalog", "root", "12345")
  implicit val session = AutoSession

  GlobalSettings.loggingSQLAndTime = new LoggingSQLAndTimeSettings(enabled = false)

  //   sql"""
  //   CREATE TABLE IF NOT EXISTS service (
  //   host VARCHAR(40) NOT NULL,
  //   port INT(5) NOT NULL,
  //   name VARCHAR(40) NOT NULL,
  //   holderEmail VARCHAR(40),
  //   environment ENUM("Production","Test","Development"),
  //   PRIMARY KEY (host,port)
  //   )
  //      """.execute.apply()
  //
  // sql" insert into service values ('api-m1-01.qiwi.com', 8000, 'QIWI API', 'd.mikhaylov@qiwi.ru', 'Production')".update().apply() //insert sql sample

  // PROGRAM START POINT
  println("Hello!")
  handleUserCommand
}
