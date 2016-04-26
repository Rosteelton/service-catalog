package controllers

import play.api.mvc.{Accepting, Action, Controller}
import model._
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}


object ServiceCatalog extends Controller {

  def hello = Action { request =>
        Ok(views.html.index())
    }


  def showAll = Action { request =>
    val result = UserCommandHandler.handleShowAllServices
    val AcceptCsv = Accepting("application/csv")
    request match {
      case req if req.acceptedTypes.toList.length==1 && req.acceptedTypes.toList.head.toString()=="*/*" => Ok(Printer.printServices(result))
      case Accepts.Json() => Ok(Json.toJson(result.services))
      //case Accepts.Json() => Ok(views.html.services(result.services.get))
      case AcceptCsv() => Ok(Printer.printServicesAsCsv(result))
      case _ => Ok(Printer.printServices(result))
    }
  }


  def deleteService(hostAndPort: String) = Action { request =>
    UserCommandHandler.readDeleteServiceCommand(hostAndPort) match {
      case Some(command) =>
        val result = UserCommandHandler.handleDeleteServiceCommand(command)
        val content = Printer.printDeleteServiceResult(result)
        Ok("Service " + hostAndPort + content)
      case None => BadRequest("Incorrect host and port")
    }
  }

  def updateService(hostAndPort: String) = Action { request =>
    UserCommandHandler.readFindServiceCommand(hostAndPort) match {
      case Some(findService) =>
        request.body.asJson match {
          case Some(jsValue) =>
            val placeResult = jsValue.validate[Service]
            Try(placeResult.get) match {
              case Success(service) =>
                val command = UserCommandHandler.readUpdateServiceCommand(service, findService.host, findService.port)
                UserCommandHandler.handleUpdateServiceCommand(command) match {
                  case ServiceResult.SuccessUpdateServiceResult => Ok(Printer.printUpdateServiceResult(ServiceResult.SuccessUpdateServiceResult))
                  case res: ServiceResult.FailedUpdateServiceResult => BadRequest(res.err)
                }
              case Failure(_) => BadRequest("Not possible to get service from json")
            }
          case None => BadRequest("Wrong json format!")
        }
      case None => BadRequest("Wrong format of host and port")
    }
  }

  def findService(hostAndPort: String) = Action {
    request =>
      UserCommandHandler.readFindServiceCommand(hostAndPort) match {
        case Some(findService) =>
          val result = UserCommandHandler.handleFindServiceCommand(findService)
          result.foundService match {
            case Some(service) => Ok("Service was found: \n" + Printer.printServices(List(service)))
            case None => NotFound("Service wasn't found")
          }
        case None => BadRequest("Incorrect host and port")
      }
  }

  def findServiceToJson(hostAndPort: String) = Action {
    request =>
      UserCommandHandler.readFindServiceCommand(hostAndPort) match {
        case Some(findService) =>
          val result = UserCommandHandler.handleFindServiceCommand(findService)
          result.foundService match {
            case Some(service) =>
              Ok(Json.toJson(service))
            case None => NotFound("Service wasn't found")
          }
        case None => BadRequest("Incorrect host and port")
      }
  }

  def findServiceToCsv(hostAndPort: String) = Action { request =>
    UserCommandHandler.readFindServiceCommand(hostAndPort) match {
      case Some(findService) =>
        val result = UserCommandHandler.handleFindServiceCommand(findService)
        result.foundService match {
          case Some(service) =>
            Ok(FormatHandler.servicesToCsv(List(service)))
          case None => NotFound("Service wasn't found")
        }
      case None => BadRequest("Incorrect host and port")
    }
  }

  def addService = Action { request =>
    request.body.asJson match {
      case Some(jsValue) =>
        val placeResult = jsValue.validate[Service]
        val service = placeResult.get
        val result = UserCommandHandler.handleAddServiceCommand(service)
        if (result.success) Created(Printer.printAddServiceResult(result))
        else Conflict(Printer.printAddServiceResult(result))
      case None => BadRequest("Wrong json format!")
    }
  }

  def importJsonFile = Action { request =>
    request.body.asJson match {
      case Some(jsValue) =>
        val placeResult = jsValue.validate[List[Service]]
        Try(placeResult.get) match {
          case Success(services) =>
            val result = UserCommandHandler.handleImportServiceCommand(UserCommand.ImportJson(services))
            if (result.importSuccess) Created(result.err)
            else MultiStatus(result.err)
          case Failure(_) => BadRequest("Can't get list of services! Mistake in file!")
        }
      case None => BadRequest("Wrong json format!")
    }
  }

  def importCsvFile = Action { request =>
    request.body.asText match {
      case Some(string) =>
        val result = UserCommandHandler.handleImportServiceCommand(UserCommand.ImportCsv(string))
        if (result.importSuccess) Created(result.err)
        else MultiStatus(result.err)
      case None => BadRequest("Empty file!")
    }
  }

  def untrail(path: String) = Action {
    MovedPermanently("/" + path)
  }


}




