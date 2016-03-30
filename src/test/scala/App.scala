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

}
