import UserCommand._
import scalikejdbc._

import scala.io.StdIn
import scala.util.{Failure, Success, Try}

sealed trait UserCommand

object UserCommand {

  case class AddService(host: String, port: Int, name: String, holderEmail: String, environment: Environment) extends UserCommand

  case class FindService(host: String, port: Int) extends UserCommand

  case class UpdateService(hostToUpdate: String, portToUpdate: Int, host: String, port: Int, name: String, holderEmail: String, environment: Environment) extends UserCommand

  case class DeleteService(host: String, port: Int) extends UserCommand

  case object ShowAll extends UserCommand

  case object Exit extends UserCommand

}

object App extends App {

  Class.forName("com.mysql.jdbc.Driver")
  ConnectionPool.singleton("jdbc:mysql://localhost:3306/services_catalog", "root", "12345")
  implicit val session = AutoSession

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



  def printService(service: Service) = println(service.toString)

  def environmentToString(env: Environment): String = {
    env match {
      case Environment.Production => "Production"
      case Environment.Development => "Development"
      case Environment.Test => "Test"
    }
  }

  def handleAddServiceCommand(command: UserCommand.AddService): Unit = {
    val s = new Service(command.host, command.port, command.name, command.holderEmail, command.environment)
    Try(sql" insert into service values (${s.host}, ${s.port}, ${s.name}, ${s.holderEmail} , ${environmentToString(s.environment)})".update().apply()) match {
      case Success(some) =>
        println("Success!")
        handleUserCommand
      case Failure(_) =>
        println("This service already existed!")
        handleUserCommand
    }
  }

  def readAddServiceCommand: UserCommand.AddService =
    AddService(Reader.readHost, Reader.readPort, Reader.readName, Reader.readHolderEmail, Reader.readEnvironment)

  def readFindServiceCommand: UserCommand.FindService = {
    val hostAndPort = Reader.readHostAndPort
    FindService(hostAndPort._1, hostAndPort._2)
  }

  def handleFindServiceCommand(command: FindService): Unit = {
    sql"select * from service where host = ${command.host} AND port = ${command.port}".map(rs => Service(rs)).single.apply() match {
      case Some(service) =>
        println("Service was found!")
        printService(service)
        handleUserCommand
      case None =>
        println("Service wasn't found!")
        handleUserCommand
    }
  }

  def readUpdateServiceCommand: UserCommand.UpdateService = {
    val hostAndPort = Reader.readHostAndPort
    UpdateService(hostAndPort._1, hostAndPort._2, Reader.readHost, Reader.readPort, Reader.readName, Reader.readHolderEmail, Reader.readEnvironment)
  }

  def handleUpdateServiceCommand(com: UpdateService): Unit = {
    sql"select * from service where host = ${com.hostToUpdate} AND port = ${com.portToUpdate}".map(rs => Service(rs)).single.apply() match {
      case Some(s) =>
        println("Service was found!")
        sql"update service set host=${com.host}, port=${com.port}, name=${com.name}, holderEmail=${com.holderEmail}, environment=${environmentToString(com.environment)} where host=${com.hostToUpdate} AND port=${com.portToUpdate}".update.apply()
        println("Service was updated")
        handleUserCommand
      case None =>
        println("Service wasn't found!")
        handleUserCommand
    }
  }

  def readDeleteServiceCommand: DeleteService = {
    val hostAndPort = Reader.readHostAndPort
    DeleteService(hostAndPort._1, hostAndPort._2)
  }

  def handleDeleteServiceCommand(com: DeleteService): Unit = {
    sql"select * from service where host = ${com.host} AND port = ${com.port}".map(rs => Service(rs)).single.apply() match {
      case Some(s) =>
        println("Service was found!")
        sql"delete from service where host=${s.host} and port=${s.port}".update().apply()
        println("Service was deleted")
        handleUserCommand
      case None =>
        println("Service wasn't found!")
        handleUserCommand
    }
  }

  def readUserCommand: UserCommand = {
    println("\n___________________________")
    println("1 - add new service       |")
    println("2 - find some service     |")
    println("3 - update some service   |")
    println("4 - delete some service   |")
    println("5 - show all services     |")
    println("exit - exit application   |")
    println("---------------------------\n")
    StdIn.readLine() match {
      case "1" => readAddServiceCommand
      case "2" => readFindServiceCommand
      case "3" => readUpdateServiceCommand
      case "4" => readDeleteServiceCommand
      case "5" => ShowAll
      case "exit" => UserCommand.Exit
      case _ => readUserCommand
    }
  }

  def showAllServices = {
    sql"select * from service".map(rs => Service(rs)).list.apply().foreach(println _)
    handleUserCommand
  }

  def handleUserCommand: Unit = {
    readUserCommand match {
      case com: AddService => handleAddServiceCommand(com)
      case com: FindService => handleFindServiceCommand(com)
      case com: UpdateService => handleUpdateServiceCommand(com)
      case com: DeleteService => handleDeleteServiceCommand(com)
      case UserCommand.ShowAll => showAllServices
       case UserCommand.Exit => System.exit(0)
    }
  }

  // PROGRAM START POINT
  println("Hello!")
  handleUserCommand
}
