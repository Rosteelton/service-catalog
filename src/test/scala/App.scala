import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import scalikejdbc._

import scala.io.StdIn

object App extends App {

  Class.forName("com.mysql.jdbc.Driver")
  ConnectionPool.singleton("jdbc:mysql://localhost:3306/services_catalog", "root", "12345")

  implicit val session = AutoSession
  /*
   sql"""
   CREATE TABLE IF NOT EXISTS service (
   host VARCHAR(40) NOT NULL,
   port INT(5) NOT NULL,
   name VARCHAR(40) NOT NULL,
   holderEmail VARCHAR(40),
   environment ENUM("Production","Test","Development"),
   PRIMARY KEY (host,port)
   )
      """.execute.apply()

  //sql" insert into service values ('api-m1-01.qiwi.com', 8000, 'QIWI API', 'd.mikhaylov@qiwi.ru', 'Production')".update().apply()
  */

  def tryToFindServiceByHostAndPort(s: String): Option[Service] = {
    try {
      val hostAndPort: Array[String] = s.split(":")
      val host = hostAndPort(0)
      val port = hostAndPort(1).toInt
      val service = sql"select * from service where host = ${host} AND port = ${port}".map(rs => Service(rs)).single.apply()
      service match {
        case Some(service) => {
          println("The service was found:")
          println(service.toString)
          Option(service)
        }
        case None => {
          println("The service wasn't found:")
          None
        }
      }
    } catch {
      case e: ArrayIndexOutOfBoundsException => {
        println("Error! Please write someth like this: api-m1-01.qiwi.com:8000")
        None
      }
      case e1: NumberFormatException => {
        println("Error! Wrong port")
        None
      }
    }
  }

  def updateService(s: Service): Unit = {
    val s2 = StdIn.readLine("host: ")
    val s3 = StdIn.readLine("port: ")
    val s4 = StdIn.readLine("name: ")
    val s5 = StdIn.readLine("holderEmail: ")
    val s6 = StdIn.readLine("environment: ")
    sql"update service set host=${s2}, port=${s3.toInt}, name=${s4}, holderEmail=${s5}, environment=${s6} where host=${s.host} AND port=${s.port}".update.apply()
    println("Success!")
  }


  def delete(s: Service): Unit = {
    sql"delete from service where host=${s.host} and port=${s.port}".update().apply()
    println("Success!")
  }

  def addNewService(host: String, port: Int, name: String, holderEmail: Option[String], environment: Option[String]) = {
    try {
      val s1 = new Service(host, port, name, holderEmail, environment)
      sql"insert into service values (${s1.host},${s1.port},${s1.name},${s1.holderEmail},${s1.environment})".update().apply()
      println("Service added")
    } catch {
      case e: MySQLIntegrityConstraintViolationException => println("This service is already existed")
    }
  }

  //findServiceByHostAndPort("api-m1-01.qiwi.com:8000")
  //findServiceByHostAndPort("asdfa:1233")
  //addNewService("api-m1-02.qiwi.com",1000,"API QIWI2",Option("an.solovev@qiwi.ru"),Option("Test"))

  println("Hello!")

  try {
    while (true) {
      println("1 - add new service")
      println("2 - find some service")
      println("3 - update some service")
      println("4 - delete some service")
      println("exit - exit application")

      StdIn.readLine() match {
        case "1" => {
          val s1 = StdIn.readLine("host: ")
          val s2 = StdIn.readLine("port: ")
          val s3 = StdIn.readLine("name: ")
          val s4 = StdIn.readLine("holderEmail: ")
          val s5 = StdIn.readLine("environment: ")
          addNewService(s1, s2.toInt, s3, Option(s4), Option(s5))
        }
        case "2" => {
          println("Write host:port to find, for example: qiwi.com:8080")
          val s1 = StdIn.readLine("host:port ")
          tryToFindServiceByHostAndPort(s1)

        }
        case "3" => {
          println("Write host:port combination to update, for example: qiwi.com:8080")
          val s1 = StdIn.readLine("host:port ")
          val tmp = tryToFindServiceByHostAndPort(s1)
          tmp match {
            case Some(tmp) => updateService(tmp)
            case None => println("Failed!")
          }
        }
        case "4" => {
          println("Write host:port combination to delete, for example: qiwi.com:8080")
          val s1 = StdIn.readLine("host:port ")
          val tmp = tryToFindServiceByHostAndPort(s1)
          tmp match {
            case Some(tmp) => delete(tmp)
            case None => println("Failed!")
          }
        }
        case "exit" => throw new Exception("By")
        case _ => println("That string is incorrect")
      }
    }
  } catch {
    case e: Exception => println("By!")
  }
}
