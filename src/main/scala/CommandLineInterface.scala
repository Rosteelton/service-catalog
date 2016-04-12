import java.io.File

import model.{Reader, Service, ServiceResult, UserCommand}

import scala.io.StdIn

object CommandLineInterface {

  def resultToConsole(result: ServiceResult): Unit = result match {
    case res: AddServiceResult => printAddServiceResult(res)
    case res: FindServiceResult => foundServiceTo(res)
    case res: UpdateServiceResult => printUpdateServiceResult(res)
    case res: DeleteServiceResult => printDeleteServiceResult(res)
    case res: ShowAllServicesResult => printServices(res)
    case res: ImportServiceResult => printImportServiceResult(res)
  }

  def printAddServiceResult(res: AddServiceResult) = {
    if (res.success) println("Service successfully added!")
    else println("Service hasn't been added!")
  }

  def printUpdateServiceResult(res: UpdateServiceResult): Unit = res match {
    case SuccessUpdateServiceResult => println("Service successfully updated!")
    case res: FailedUpdateServiceResult => println(res.err)
  }

  def printDeleteServiceResult(res: DeleteServiceResult): Unit = {
    if (res.deleteSuccess) println("Service successfully deleted!")
    else println("Service hasn't been deleted!")
  }

  def printImportServiceResult(res: ImportServiceResult): Unit = {
    println(res.err)
  }

  def printServices(service: List[Service]) = {
    println()
    println("HOST                                    PORT      NAME                                    EMAIL                                   ENVIRONMENT")
    println("---------------------------------------------------------------------------------------------------------------------------------------------")
    for (s <- service) {
      println(s.toString)
    }
  }

  def printServices(res: ShowAllServicesResult): Unit = res.services match {
    case Some(services) => printServices(services)
    case None => println("Nothing to show!")
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
        case "4" => readUserCommand
        case _ =>
          println("Wrong number!")
      }
    case None => println("Nothing to show!")
  }

  def readUserCommand: UserCommand = {
    println("\n___________________________")
    println("1 - add new service       |")
    println("2 - find some service     |")
    println("3 - update some service   |")
    println("4 - delete some service   |")
    println("5 - show catalog          |")
    println("6 - import cat from file  |")
    println("---------------------------\n")
    StdIn.readLine() match {
      case "1" => readAddServiceCommand
      case "2" => readFindServiceCommand
      case "3" => readUpdateServiceCommand
      case "4" => readDeleteServiceCommand
      case "5" => ShowAll
      case "6" => readImportServiceCommand
      case _ => readUserCommand
    }
  }

  def readAddServiceCommand: UserCommand.AddService =
    AddService(Reader.readHost, Reader.readPort, Reader.readName, Reader.readHolderEmail, Reader.readEnvironment)

  def readFindServiceCommand: UserCommand.FindService = {
    val hostAndPort = Reader.readHostAndPort
    FindService(hostAndPort._1, hostAndPort._2)
  }

  def readUpdateServiceCommand: UserCommand.UpdateService = {
    val hostAndPort = Reader.readHostAndPort
    UpdateService(hostAndPort._1, hostAndPort._2, Reader.readHost, Reader.readPort, Reader.readName, Reader.readHolderEmail, Reader.readEnvironment)
  }

  def readDeleteServiceCommand: DeleteService = {
    val hostAndPort = Reader.readHostAndPort
    DeleteService(hostAndPort._1, hostAndPort._2)
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