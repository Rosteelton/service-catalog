import java.io._
import java.text.SimpleDateFormat
import java.util.Calendar

import MyJsonProtocol._
import UserCommandHandler._
import spray.json._

import scala.collection.mutable.ListBuffer
import scala.io.{Source}
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

  def convertJsonToServices(content: String): List[Service] = {
    Try(content.parseJson) match {
      case Success(js) =>
        js.convertTo[List[Service]]
      case Failure(_) =>
        List.empty[Service]
    }
  }

  def convertCsvToService(content: String): List[Service] = {
    var listOfServices = new ListBuffer[Service]
    for (line <- content.split("\n")) {
      val list = line.split(";|,").toList
      if (list.length == 5) {
        Try(new Service(list(0).trim, list(1).trim.toInt, list(2).trim, list(3).trim, ServiceJsonFormat.fromString(list(4).trim))) match {
          case Success(some) => listOfServices += some
          case Failure(_) => List.empty[Service]
        }
      } else {
        List.empty[Service]
      }
    }
    listOfServices.toList
  }
}
