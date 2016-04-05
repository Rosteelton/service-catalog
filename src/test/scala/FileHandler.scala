import java.io._
import java.text.SimpleDateFormat
import java.util.Calendar

import UserCommandHandler._
import spray.json._
import MyJsonProtocol._

import scala.collection.mutable.ListBuffer
import scala.io.{Source, StdIn}
import scala.util.{Failure, Success, Try}

object FileHandler {

  def saveJsonFile(services: List[Service]) = {
    val data = Calendar.getInstance().getTime
    val dateFormat = new SimpleDateFormat("dd.MM.yyyy_H:mm:ss")
    val file = new File("/home/solovyev/Documents/jsonfiles/" + dateFormat.format(data) + ".json")
    val bw = new BufferedWriter(new FileWriter(file))
    val jsonDoc = services.toJson.prettyPrint
    bw.write(jsonDoc)
    bw.close()
    println("\n" + dateFormat.format(data) + " successfully created!")
    handleUserCommand
  }

  def saveCsvFile(services: List[Service]) = {
    val data = Calendar.getInstance().getTime
    val dateFormat = new SimpleDateFormat("dd.MM.yyyy_H:mm:ss")
    val file = new File("/home/solovyev/Documents/csvfiles/" + dateFormat.format(data) + ".csv")
    val bw = new BufferedWriter(new FileWriter(file))
    for (service <- services) {
      bw.write(service.host + ";" + service.port + ";" + service.name + ";" + service.holderEmail + ";" + service.environment.toString + "\n")
    }
    bw.close()
    println("\n" + dateFormat.format(data) + " successfully created!")
    handleUserCommand
  }

  def importJsonFile: List[Service] = {
    val tmp = StdIn.readLine("Type full file name with path, i.e. /home/solovyev/Documents/jsonfiles/test.json\n")
    val file = new File(tmp)
    if (!file.exists()) {
      println("File doesn't exist!")
      importJsonFile
    }
    else if (!tmp.endsWith(".json")) {
      println("It's not json file")
      importJsonFile
    }
    else {
      println("File was found!")
      val fileLines = io.Source.fromFile(file).getLines().mkString
      Try(fileLines.parseJson) match {
        case Success(js) =>
          js.convertTo[List[Service]]
        case Failure(_) =>
          println("Can't parse JSON")
          List.empty[Service]
      }
    }
  }

  def importCsvFile: List[Service] = {
    val tmp = StdIn.readLine("Type full file name with path, i.e. /home/solovyev/Documents/csvfiles/test.csv\n")
    val file = new File(tmp)
    var listOfServices = new ListBuffer[Service]
    if (!file.exists()) {
      println("File doesn't exist!")
      importCsvFile
    }
    else if (!tmp.endsWith(".csv")) {
      println("It's not csv file")
      importCsvFile
    }
    else {
      println("File was found!")
      for (line <- Source.fromFile(file).getLines()) {
        val list = line.split(";|,").toList
        if (list.length == 5) {
          Try(new Service(list(0).trim, list(1).trim.toInt, list(2).trim, list(3).trim, ServiceJsonFormat.fromString(list(4).trim))) match {
            case Success(some) => listOfServices += some
            case Failure(_) =>
              println("Something went wrong!")
          }
        } else {
          println("File is not correct!")
        }
      }
      listOfServices.toList
    }
  }
}
