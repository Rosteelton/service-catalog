import java.io.File
import model.{Service, ServiceResult, UserCommand}
import scala.io.StdIn

object CommandLineInterface {

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