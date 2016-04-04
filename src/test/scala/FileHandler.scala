import java.io.{BufferedWriter, File, FileWriter}
import java.text.SimpleDateFormat
import java.util.Calendar
import UserCommandHandler._
import spray.json._
import MyJsonProtocol._

object FileHandler {

  def saveJSONFile(services: List[Service]) = {
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

  def saveCSVFile(services: List[Service]) = {
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

  //def readJSONFile()
}
