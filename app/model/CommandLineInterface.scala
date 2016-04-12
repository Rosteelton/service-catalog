package model

import java.io.File

import FileHandler._
import ServiceResult._
import UserCommand.{ImportJson, _}

import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object CommandLineInterface {

  def resultToConsole(result: ServiceResult): Unit = result match {
    case res: AddServiceResult => printAddServiceResult(res)
    case res: FindServiceResult => foundServiceTo(res)
    case res: UpdateServiceResult => printUpdateServiceResult(res)
    case res: DeleteServiceResult => printDeleteServiceResult(res)
    case res: ShowAllServicesResult => printServices(res)
    case res: ImportServiceResult => printImportServiceResult(res)
  }

  def printAddServiceResult(res: AddServiceResult):String = {
    if (res.success) "Service successfully added!"
    else "Service already existed!"
  }

  def printUpdateServiceResult(res: UpdateServiceResult): String = res match {
    case SuccessUpdateServiceResult => "Service successfully updated!"
    case res: FailedUpdateServiceResult => res.err
  }

  def printDeleteServiceResult(res: DeleteServiceResult): String = {
    if (res.deleteSuccess) " successfully deleted!"
    else " hasn't been found!"
  }

  def printImportServiceResult(res: ImportServiceResult): Unit = {
    println(res.err)
  }

  def printServices(service: List[Service]): String = {
    var resultString = new StringBuilder()
    resultString.append("\nHOST                                    PORT      NAME                                    EMAIL                                   ENVIRONMENT")
    resultString.append("\n---------------------------------------------------------------------------------------------------------------------------------------------")
    for (s <- service) {
      resultString.append("\n" + s.toString)
    }
    resultString.toString()
//    println()
//    println("HOST                                    PORT      NAME                                    EMAIL                                   ENVIRONMENT")
//    println("---------------------------------------------------------------------------------------------------------------------------------------------")
//    for (s <- service) {
//      println(s.toString)
//    }
  }

  def printServices(res: ShowAllServicesResult): String = res.services match {
    case Some(services) => printServices(services)
    case None => "Nothing to show!"
  }


  def foundServiceTo(res: ServiceResult.FindServiceResult) = res.foundService match {
    case Some(service) =>
      StdIn.readLine("Type \"1\" for screen showing \nType \"2\" for save CSV file\nType \"3\" for save JSON file\nType \"4\" don't show result\n") match {
        case "1" => printServices(List(service))
        case "2" => saveCsvFile(List(service)) match {
          case (fileName, true) => println("\n" + fileName + " successfully created!")
          case (_, false) => println("\nFile creation failed!")
        }
        case "3" => saveJsonFile(List(service)) match {
          case (fileName, true) => println("\n" + fileName + " successfully created!")
          case (_, false) => println("\nFile creation failed!")
        }
        case "4" =>
        case _ =>
          println("Wrong number!")
      }
    case None => println("Nothing to show!")
  }



  def readAddServiceCommand: UserCommand.AddService =
    AddService(Reader.readHost, Reader.readPort, Reader.readName, Reader.readHolderEmail, Reader.readEnvironment)

  def readFindServiceCommand(hostAndPort: String): Option[FindService] = {
    hostAndPort.split(":").toList match {
      case host :: port :: Nil => {
        Try(port.toInt) match {
          case Success(intPort) if (port.length <= 5) => Some(FindService(host,intPort))
          case Failure(_) => None
        }
      }
      case _ => None
    }
  }

  def readUpdateServiceCommand(updateService: Service, hostToUpdate: String, portToUpdate: Int): UserCommand.UpdateService =
    UpdateService(hostToUpdate, portToUpdate, updateService.host, updateService.port, updateService.name, updateService.holderEmail, updateService.environment)


  def readDeleteServiceCommand(hostAndPort: String): Option[DeleteService] = {
    hostAndPort.split(":").toList match {
      case host :: port :: Nil => {
        Try(port.toInt) match {
          case Success(intPort) if (port.length <= 5) => Some(DeleteService(host,intPort))
          case Failure(_) => None
        }
      }
      case _ => None
    }
  }


  def readImportServiceCommand: UserCommand.ImportService = {
    val tmp = StdIn.readLine("Type full file name with path, i.e. /home/solovyev/Documents/jsonfiles/test.json\n")
    val file = new File(tmp)
    if (!file.exists()) {
      println("File doesn't exist!")
      readImportServiceCommand
    }
    val fileLines = scala.io.Source.fromFile(file).getLines().mkString
    tmp match {
      case s if s.endsWith(".json") => ImportJson(fileLines)
      case s if s.endsWith(".csv") => ImportCsv(fileLines)
      case _ =>
        println("Wrong file format")
        readImportServiceCommand
    }
  }
}