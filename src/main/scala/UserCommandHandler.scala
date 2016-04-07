import java.io.File

import App.session
import FileHandler._
import UserCommand._
import scalikejdbc._

import scala.collection.mutable.ListBuffer
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object UserCommandHandler {


  def environmentToString(env: Environment): String = {
    env match {
      case Environment.Production => "Production"
      case Environment.Development => "Development"
      case Environment.Test => "Test"
    }
  }

  def readShowCommand(service: List[Service]): Unit = {
    StdIn.readLine("Type \"1\" for screen showing \nType \"2\" for save CSV file\nType \"3\" for save JSON file\nType \"4\" don't show result\n") match {
      case "1" => CommandLineInterface.printServices(service)
        handleUserCommand
      case "2" => saveCsvFile(service)
      case "3" => saveJsonFile(service)
      case "4" => handleUserCommand
      case _ =>
        println("Wrong number!")
        readShowCommand(service)
    }
  }

  def handleAddServiceCommand(command: UserCommand.AddService): ServiceResult.AddServiceResult = {
    val s = new Service(command.host, command.port, command.name, command.holderEmail, command.environment)
    Try(sql" insert into service values (${s.host}, ${s.port}, ${s.name}, ${s.holderEmail} , ${environmentToString(s.environment)})".update().apply()) match {
      case Success(some) =>
        ServiceResult.AddServiceResult(true)
      case Failure(_) =>
        ServiceResult.AddServiceResult(false)
    }
  }


  def servicesToBD(services: List[Service]): Boolean = {
    for (s <- services) {
      val serv = new Service(s.host, s.port, s.name, s.holderEmail, s.environment)
      Try(sql" insert into service values (${s.host}, ${s.port}, ${s.name}, ${s.holderEmail} , ${environmentToString(s.environment)})".update().apply()) match {
        case Failure(_) => return false
      }
    }
    return true
  }

  def handleImportServiceCommand(com: UserCommand.ImportService): ServiceResult.ImportServiceResult = com match {
    case com: UserCommand.ImportCsv =>
      val listOfServices = FileHandler.convertCsvToService(com.content)
      if (listOfServices.isEmpty) ServiceResult.ImportServiceResult(false,"Not possible to parse file - incorrect content or file is empty")
      else if (servicesToBD(listOfServices)) ServiceResult.ImportServiceResult(true,"")
      else ServiceResult.ImportServiceResult(false,"Some services already exist")
    case com: UserCommand.ImportJson =>
      val listOfServices = FileHandler.convertJsonToServices(com.content)
      if (listOfServices.isEmpty) ServiceResult.ImportServiceResult(false,"Not possible to parse file - incorrect content or file is empty")
      else if (servicesToBD(listOfServices)) ServiceResult.ImportServiceResult(true,"")
      else ServiceResult.ImportServiceResult(false,"Some services already exist")
    }

  def readAddServiceCommand: UserCommand.AddService =
    AddService(Reader.readHost, Reader.readPort, Reader.readName, Reader.readHolderEmail, Reader.readEnvironment)

  def readFindServiceCommand: UserCommand.FindService = {
    val hostAndPort = Reader.readHostAndPort
    FindService(hostAndPort._1, hostAndPort._2)
  }

  def handleFindServiceCommand(command: FindService): ServiceResult.FindServiceResult = {
    val result = sql"select * from service where host = ${command.host} AND port = ${command.port}".map(rs => Service(rs)).single.apply()
    ServiceResult.FindServiceResult(result)
  }


  def readUpdateServiceCommand: UserCommand.UpdateService = {
    val hostAndPort = Reader.readHostAndPort
    UpdateService(hostAndPort._1, hostAndPort._2, Reader.readHost, Reader.readPort, Reader.readName, Reader.readHolderEmail, Reader.readEnvironment)
  }

  def handleUpdateServiceCommand(com: UpdateService): ServiceResult.UpdateServiceResult = {
    sql"select * from service where host = ${com.hostToUpdate} AND port = ${com.portToUpdate}".map(rs => Service(rs)).single.apply() match {
      case Some(s) =>
        Try(sql"update service set host=${com.host}, port=${com.port}, name=${com.name}, holderEmail=${com.holderEmail}, environment=${environmentToString(com.environment)} where host=${com.hostToUpdate} AND port=${com.portToUpdate}".update.apply()) match {
          case Success(some) => ServiceResult.SuccessUpdateServiceResult
          case Failure(_) => ServiceResult.FailedUpdateServiceResult("Service was found but not updated!")
        }
      case None => ServiceResult.FailedUpdateServiceResult("Service wasn't found")
    }
  }

  def readDeleteServiceCommand: DeleteService = {
    val hostAndPort = Reader.readHostAndPort
    DeleteService(hostAndPort._1, hostAndPort._2)
  }

  def handleDeleteServiceCommand(com: DeleteService): ServiceResult.DeleteServiceResult = {
    sql"select * from service where host = ${com.host} AND port = ${com.port}".map(rs => Service(rs)).single.apply() match {
      case Some(s) =>
        Try(sql"delete from service where host=${s.host} and port=${s.port}".update().apply()) match {
          case Success(some) => ServiceResult.DeleteServiceResult(true)
          case Failure(_) => ServiceResult.DeleteServiceResult(false)
        }
      case None =>
        ServiceResult.DeleteServiceResult(false)
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
      case "6" => readImportServiceCommand
      case "exit" => UserCommand.Exit
      case _ => readUserCommand
    }
  }

  def handleShowAllServices: ServiceResult.ShowAllServicesResult = {
    val services = sql"select * from service".map(rs => Service(rs)).list.apply()
    ServiceResult.ShowAllServicesResult(Some(services))
  }






  def readImportServiceCommand: UserCommand.ImportService = {
    println("Choose type of the file:")
    StdIn.readLine("Type \"1\" for CSV file \nType \"2\" for JSON file\nType \"exit\" for quit\n") match {
      case "1" =>
        val tmp = StdIn.readLine("Type full file name with path, i.e. /home/solovyev/Documents/jsonfiles/test.json\n")
        val file = new File(tmp)
        if (!file.exists()) {
          println("File doesn't exist!")
          readImportServiceCommand
        }
        else if (!tmp.endsWith(".json")) {
          println("It's not json file")
          readImportServiceCommand
        }
        else {
          println("File was found!")
          val fileLines = scala.io.Source.fromFile(file).getLines().mkString
          ImportCsv(fileLines)
        }
      case "2" =>
        val tmp = StdIn.readLine("Type full file name with path, i.e. /home/solovyev/Documents/csvfiles/test.csv\n")
        val file = new File(tmp)
        if (!file.exists()) {
          println("File doesn't exist!")
          readImportServiceCommand
        }
        else if (!tmp.endsWith(".csv")) {
          println("It's not csv file")
          readImportServiceCommand
        }
        else {
          println("File was found!")
          val fileLines = scala.io.Source.fromFile(file).getLines().mkString
          ImportJson(fileLines)
        }
      case _ =>
        println("Wrong number")
        readImportServiceCommand
    }
  }


  def handleUserCommand: Unit = {
    readUserCommand match {
      case com: AddService => handleAddServiceCommand(com)
      case com: FindService => handleFindServiceCommand(com)
      case com: UpdateService => handleUpdateServiceCommand(com)
      case com: DeleteService => handleDeleteServiceCommand(com)
      case UserCommand.ShowAll => handleShowAllServices
      case com: ImportService => handleImportServiceCommand(com)
      case UserCommand.Exit => System.exit(0)
    }
  }
}
