import java.io._
import java.text.SimpleDateFormat
import java.util.Calendar

import model.Service
import spray.json._

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.{Failure, Success, Try}

object FileHandler {

  def saveJsonFile(services: List[Service]): (String,Boolean)  = {
    val data = Calendar.getInstance().getTime
    val dateFormat = new SimpleDateFormat("dd.MM.yyyy_H:mm:ss")
    val file = new File("/home/solovyev/Documents/jsonfiles/" + dateFormat.format(data) + ".json")
    val bw = new BufferedWriter(new FileWriter(file))
    val jsonDoc = services.toJson.prettyPrint
    Try (bw.write(jsonDoc)) match {
      case Success(some) =>
        bw.close()
        (dateFormat.format(data), true)
      case Failure(_) =>
        bw.close()
        ("fail", false)
    }
  }

  def saveCsvFile(services: List[Service]): (String,Boolean) = {
    val data = Calendar.getInstance().getTime
    val dateFormat = new SimpleDateFormat("dd.MM.yyyy_H:mm:ss")
    val file = new File("/home/solovyev/Documents/csvfiles/" + dateFormat.format(data) + ".csv")
    val bw = new BufferedWriter(new FileWriter(file))
    Try (for (service <- services) {
      bw.write(service.host + ";" + service.port + ";" + service.name + ";" + service.holderEmail + ";" + service.environment.toString + "\n")
    }) match {
      case Success(some) =>
        bw.close()
        (dateFormat.format(data), true)
      case Failure(_) =>
        bw.close()
        ("fail", false)
    }
  }

  def convertJsonToServices(content: String): List[Service] = {
    Try(content.parseJson) match {
      case Success(js) =>
        js.convertTo[List[Service]]
      case Failure(_) =>
        List.empty[Service]
    }
  }

  def convertCsvToService(content: String): Either[String,List[Service]] = {
    var listOfServices = new ListBuffer[Service]
    for (line <- content.split("\n")) {
      val list = line.split(";|,").toList
      if (list.length == 5) {
        Try(new Service(list(0).trim, list(1).trim.toInt, list(2).trim, list(3).trim, ServiceJsonFormat.fromString(list(4).trim))) match {
          case Success(some) => listOfServices += some
          case Failure(e) => Left(e.getMessage)
        }
      } else {
        Left("Invalid csv format: expected 5 fields but found: " + list.length)
      }
    }
    Right(listOfServices.toList)
  }
}