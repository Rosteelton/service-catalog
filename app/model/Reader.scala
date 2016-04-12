package model

import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object Reader {

  def readHost: String = {
    val tmp = StdIn.readLine("host: ")
    if (tmp.matches("[\\.a-zA-Z0-9-]{3,40}"))
      tmp
    else {
      println("Incorrect host!")
      readHost
    }
  }

  // def read[A](prompt: String, convert: String => Either[String, A]): A
  //read[Int]("port")

  def readPort: Int = {
    Some(StdIn.readLine("port: ")).flatMap {
      case p if p.length <= 5 => Try(p.toInt).toOption
      case _ => None
    }.getOrElse {
      println("Incorrect port!")
      readPort
    }
  }

  def readName: String = {
    val name = StdIn.readLine("name: ")
    if (name.length < 40)
      name
    else {
      println("Incorrect name!")
      readName
    }
  }

  def readHolderEmail: String = {
    val mail = StdIn.readLine("HolderEmail: ")
    if (mail.matches(".+@.+"))
      mail
    else {
      println("Incorrect holder email!")
      readHolderEmail
    }
  }

  def readEnvironment: Environment = {
    val env = StdIn.readLine("Type \"t\" for test environment\nType \"d\" for development environment\nType \"p\" for production enironment: ")
    env match {
      case "t" => Environment.Test
      case "d" => Environment.Development
      case "p" => Environment.Production
      case _ =>
        println("Incorrect letter!")
        readEnvironment
    }
  }

  def readHostAndPort: (String, Int) = {
    StdIn.readLine("host:port ").split(":").toList match {
      case host :: port :: Nil => {
        Try(port.toInt) match {
          case Success(intPort) if (port.length <= 5) => (host, intPort)
          case Failure(_) =>
            println("Write correct value")
            readHostAndPort
        }
      }
      case _ =>
        println("Write correct value")
        readHostAndPort
    }
  }
}