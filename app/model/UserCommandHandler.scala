package model

import UserCommand._
import scalikejdbc._
import scala.util.{Failure, Success, Try}

object UserCommandHandler {

  Class.forName("com.mysql.jdbc.Driver")
  ConnectionPool.singleton("jdbc:mysql://localhost:3306/services_catalog", "root", "12345")
  implicit val session = AutoSession

  scalikejdbc.GlobalSettings.loggingSQLAndTime = new LoggingSQLAndTimeSettings(enabled = false)

  def environmentToString(env: Environment): String = {
    env match {
      case Environment.Production => "Production"
      case Environment.Development => "Development"
      case Environment.Test => "Test"
    }
  }

  def handleAddServiceCommand(s: Service):ServiceResult.AddServiceResult = {
    Try(sql" insert into service values (${s.host}, ${s.port}, ${s.name}, ${s.holderEmail} , ${environmentToString(s.environment)})".update().apply()) match {
      case Success(some) =>
        ServiceResult.AddServiceResult(true)
      case Failure(_) =>
        ServiceResult.AddServiceResult(false)
    }
  }

  def servicesToBD(services: List[Service]): Boolean = {
    var allIsGood = true
    for (s <- services) {
      val serv = new Service(s.host, s.port, s.name, s.holderEmail, s.environment)
      Try(sql" insert into service values (${s.host}, ${s.port}, ${s.name}, ${s.holderEmail} , ${environmentToString(s.environment)})".update().apply()) match {
        case Success(some) =>
        case Failure(_) => allIsGood=false
      }
    }
    allIsGood
  }

  def handleImportServiceCommand(com: UserCommand.ImportService): ServiceResult.ImportServiceResult = com match {
    case com: UserCommand.ImportCsv =>
      val listOfServices = FormatHandler.csvToServices(com.content)

      listOfServices match {
        case Left(error) => ServiceResult.ImportServiceResult(false, error)
        case Right(services) =>
          val isSaved = servicesToBD(services)
          if (isSaved)
            ServiceResult.ImportServiceResult(true, "Successfully saved!")
          else
            ServiceResult.ImportServiceResult(false, "OK but some services already exist!")
      }
    case com: UserCommand.ImportJson =>
      val isSaved = servicesToBD(com.content)
      if (com.content.isEmpty) ServiceResult.ImportServiceResult(false, "Not possible to parse file")
      else if (isSaved) ServiceResult.ImportServiceResult(true, "Successfully saved!")
      else ServiceResult.ImportServiceResult(false, "OK but some services already exist!")
  }

  def handleFindServiceCommand(command: FindService): ServiceResult.FindServiceResult = {
    val result = sql"select * from service where host = ${command.host} AND port = ${command.port}".map(rs => Service(rs)).single.apply()
    ServiceResult.FindServiceResult(result)
  }

  def handleUpdateServiceCommand(com: UpdateService): ServiceResult.UpdateServiceResult = {
    sql"select * from service where host = ${com.hostToUpdate} AND port = ${com.portToUpdate}".map(rs => Service(rs)).single.apply() match {
      case Some(s) =>
        Try(sql"update service set host=${com.host}, port=${com.port}, name=${com.name}, holderEmail=${com.holderEmail}, environment=${environmentToString(com.environment)} where host=${com.hostToUpdate} AND port=${com.portToUpdate}".update.apply()) match {
          case Success(some) => ServiceResult.SuccessUpdateServiceResult
          case Failure(_) => ServiceResult.FailedUpdateServiceResult("Service was found but not updated!")
        }
      case None => ServiceResult.FailedUpdateServiceResult("Service wasn't found")
    }
  }

  def handleDeleteServiceCommand(com: DeleteService): ServiceResult.DeleteServiceResult = {
    sql"select * from service where host = ${com.host} AND port = ${com.port}".map(rs => Service(rs)).single.apply() match {
      case Some(s) =>
        Try(sql"delete from service where host=${s.host} and port=${s.port}".update().apply()) match {
          case Success(some) => ServiceResult.DeleteServiceResult(true)
          case Failure(_) => ServiceResult.DeleteServiceResult(false)
        }
      case None =>
        ServiceResult.DeleteServiceResult(false)
    }
  }

  def handleShowAllServices: ServiceResult.ShowAllServicesResult = {
    val services = sql"select * from service".map(rs => Service(rs)).list.apply()
    ServiceResult.ShowAllServicesResult(Some(services))
  }

  def readFindServiceCommand(hostAndPort: String): Option[FindService] = {
    hostAndPort.split(":").toList match {
      case host :: port :: Nil => {
        Try(port.toInt) match {
          case Success(intPort) if (port.length <= 5) => Some(FindService(host,intPort))
          case Failure(_) => None
        }
      }
      case _ => None
    }
  }

  def readUpdateServiceCommand(updateService: Service, hostToUpdate: String, portToUpdate: Int): UserCommand.UpdateService =
    UpdateService(hostToUpdate, portToUpdate, updateService.host, updateService.port, updateService.name, updateService.holderEmail, updateService.environment)

  def readDeleteServiceCommand(hostAndPort: String): Option[DeleteService] = {
    hostAndPort.split(":").toList match {
      case host :: port :: Nil => {
        Try(port.toInt) match {
          case Success(intPort) => Some(DeleteService(host,intPort))
          case Failure(_) => None
        }
      }
      case _ => None
    }
  }
}