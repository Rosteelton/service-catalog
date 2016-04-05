import App.session
import FileHandler._
import UserCommand._
import scalikejdbc._

import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object UserCommandHandler {
  def printServices(service: List[Service]) = {
    println()
    println("HOST                                    PORT      NAME                                    EMAIL                                   ENVIRONMENT")
    println("---------------------------------------------------------------------------------------------------------------------------------------------")
    for (s <- service) {
      println(s.toString)
    }
  }

  def environmentToString(env: Environment): String = {
    env match {
      case Environment.Production => "Production"
      case Environment.Development => "Development"
      case Environment.Test => "Test"
    }
  }

  def readShowCommand(service: List[Service]): Unit = {
    StdIn.readLine("Type \"1\" for screen showing \nType \"2\" for save CSV file\nType \"3\" for save JSON file\nType \"4\" don't show result\n") match {
      case "1" =>
        printServices(service)
        handleUserCommand
      case "2" => saveCsvFile(service)
      case "3" => saveJsonFile(service)
      case "4" => handleUserCommand
      case _ =>
        println("Wrong number!")
        readShowCommand(service)
    }
  }

  def handleAddServiceCommand(command: UserCommand.AddService): Unit = {
    val s = new Service(command.host, command.port, command.name, command.holderEmail, command.environment)
    Try(sql" insert into service values (${s.host}, ${s.port}, ${s.name}, ${s.holderEmail} , ${environmentToString(s.environment)})".update().apply()) match {
      case Success(some) =>
        println("Success!")
        readShowCommand(List(s))
      case Failure(_) =>
        println("This service already existed!")
        handleUserCommand
    }
  }

  def handleAddServiceCommand(services: List[Service]): Unit = {
    for (s <- services) {
      val serv = new Service(s.host, s.port, s.name, s.holderEmail, s.environment)
      Try(sql" insert into service values (${s.host}, ${s.port}, ${s.name}, ${s.holderEmail} , ${environmentToString(s.environment)})".update().apply()) match {
        case Success(some) =>
        case Failure(_) => printf("%s %d already existed!\n", s.host, s.port)
      }
    }
    println("Finished!")
    handleUserCommand
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
        readShowCommand(List(service))
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
        readShowCommand(List(s))
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
    println("5 - catalog of services   |")
    println("6 - import cat from file  |")
    println("exit - exit application   |")
    println("---------------------------\n")
    StdIn.readLine() match {
      case "1" => readAddServiceCommand
      case "2" => readFindServiceCommand
      case "3" => readUpdateServiceCommand
      case "4" => readDeleteServiceCommand
      case "5" => ShowAll
      case "6" => ImportService
      case "exit" => UserCommand.Exit
      case _ => readUserCommand
    }
  }

  def showAllServices = readShowCommand(sql"select * from service".map(rs => Service(rs)).list.apply())


  def readImportServiceCommand = {
    println("Choose type of the file:")
    StdIn.readLine("Type \"1\" for CSV file \nType \"2\" for JSON file\nType \"exit\" for quit\n") match {
      case "1" => handleAddServiceCommand(importCsvFile)
      case "2" => handleAddServiceCommand(importJsonFile)
      case _ =>
        println("Wrong number")
        handleUserCommand
    }
  }


  def handleUserCommand: Unit = {
    readUserCommand match {
      case com: AddService => handleAddServiceCommand(com)
      case com: FindService => handleFindServiceCommand(com)
      case com: UpdateService => handleUpdateServiceCommand(com)
      case com: DeleteService => handleDeleteServiceCommand(com)
      case UserCommand.ShowAll => showAllServices
      case UserCommand.ImportService => readImportServiceCommand
      case UserCommand.Exit => System.exit(0)
    }
  }
}