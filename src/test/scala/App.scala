import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import scalikejdbc._

object App extends App {

  Class.forName("com.mysql.jdbc.Driver")
  ConnectionPool.singleton("jdbc:mysql://localhost:3306/services_catalog", "root", "12345")

  implicit val session = AutoSession

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

  def findServiceByHostAndPort(s: String) = {
    try {
      val hostAndPort: Array[String] = s.split(":")
      val host = hostAndPort(0)
      val port = hostAndPort(1).toInt
      val service = sql"select * from service where host = ${host} AND port = ${port}".map(rs => Service(rs)).single.apply() match {
        case Some(service) => println("The service was found:")
          println(service.toString)
        case None => println("The service wasn't found:")
      }
    } catch {
      case e: ArrayIndexOutOfBoundsException => println("Error! Please write someth like this: api-m1-01.qiwi.com:8000")
      case e1: NumberFormatException => println("Error! Wrong port")
    }
  }

  def addNewService (host: String, port: Int, name: String, holderEmail: Option[String], environment: Option[String]) = {
    try {
      val s1 = new Service(host, port, name, holderEmail, environment)
      sql"insert into service values (${s1.host},${s1.port},${s1.name},${s1.holderEmail},${s1.environment})".update().apply()
      println("Service added")
    } catch {
      case e:MySQLIntegrityConstraintViolationException => println("This service is already existed")
    }
  }





  findServiceByHostAndPort("api-m1-01.qiwi.com:8000")
  findServiceByHostAndPort("asdfa:1233")
  addNewService("api-m1-02.qiwi.com",1000,"API QIWI2",Option("an.solovev@qiwi.ru"),Option("Test"))


}
