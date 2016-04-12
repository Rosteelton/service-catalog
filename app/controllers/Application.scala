package controllers

import model.ServiceResult.UpdateServiceResult
import play.api.mvc.{Action, Controller}
import model._
import play.api.libs.json.{JsResult, Json}

import scala.util.{Failure, Success, Try}

object Application extends Controller {

  def hello = Action { request =>
    Ok("Hello! Welcome to service catalog v1.0")
  }

  def showAll = Action { request =>
    val result = UserCommandHandler.handleShowAllServices
    val content = CommandLineInterface.printServices(result)
    Ok(content)
  }

  def deleteService(hostAndPort: String) = Action { request =>
    CommandLineInterface.readDeleteServiceCommand(hostAndPort) match {
      case Some(command) =>
        val result = UserCommandHandler.handleDeleteServiceCommand(command)
        val content = CommandLineInterface.printDeleteServiceResult(result)
        Ok("Service " + hostAndPort + content)
      case None => BadRequest("Incorrect host and port")
    }
  }

  //  def importService = Action { request =>
  //    convertJsonToService(request.body.toString) match {
  //      case s :: Nil =>
  //        val result = UserCommandHandler.handleAddServiceCommand(s)
  //        val content = CommandLineInterface.printAddServiceResult(result)
  //        Ok(content)
  //      case List() => BadRequest("Wrong Json format")
  //      case s :: rest => BadRequest("Need only one service!")
  //    }
  //  }

  def addServiceWithSpray = Action { request =>
    request.body.asText match {
      case Some(js) =>
        FileHandler.convertJsonToService(js) match {
          case Right(service) =>
            val result = UserCommandHandler.handleAddServiceCommand(service)
            val content = CommandLineInterface.printAddServiceResult(result)
            Created(content)
          case Left(exception) => BadRequest("Not possible to parse Json")
        }
      case None => BadRequest("Wrong Json Format")
    }
  }

  def addService = Action { request =>
    request.body.asJson match {
      case Some(jsValue) =>
        val placeResult = jsValue.validate[Service]
        val service = placeResult.get
        val result = UserCommandHandler.handleAddServiceCommand(service)
        if (result.success) Created(CommandLineInterface.printAddServiceResult(result))
        else Conflict(CommandLineInterface.printAddServiceResult(result))
      case None => BadRequest("Wrong json format!")
    }
  }

  def updateService(hostAndPort: String) = Action { request =>
    CommandLineInterface.readFindServiceCommand(hostAndPort) match {
      case Some(findService) =>
        request.body.asJson match {
          case Some(jsValue) =>
            val placeResult = jsValue.validate[Service]
            Try(placeResult.get) match {
              case Success(service) =>
                val command = CommandLineInterface.readUpdateServiceCommand(service, findService.host, findService.port)
                UserCommandHandler.handleUpdateServiceCommand(command) match {
                  case ServiceResult.SuccessUpdateServiceResult => Ok(CommandLineInterface.printUpdateServiceResult(ServiceResult.SuccessUpdateServiceResult))
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
      CommandLineInterface.readFindServiceCommand(hostAndPort) match {
        case Some(findService) =>
          val result = UserCommandHandler.handleFindServiceCommand(findService)
          result.foundService match {
            case Some(service) => Ok("Service was found: \n" + CommandLineInterface.printServices(List(service)))
            case None => NotFound("Service wasn't found")
          }
        case None => BadRequest("Incorrect host and port")
      }
  }
}
