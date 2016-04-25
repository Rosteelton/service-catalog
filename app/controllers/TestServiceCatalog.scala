package controllers

import javax.inject.Inject

import play.api.mvc.{Accepting, Action, Controller}
import model._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

class TestServiceCatalog @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {


  def serviceHandler = Action { implicit request =>
    val result = UserCommandHandler.handleShowAllServices
    Ok(views.html.services(result.services.get, Service.createServiceForm, Service.deleteFindServiceForm, "msg", null))
  }


  def createService = Action { implicit request =>
    Service.createServiceForm.bindFromRequest.fold(
      formWithErrors => {
        //Redirect(routes.TestServiceCatalog.serviceHandler("Wrong data! Try again!"))
        makeResponseWithMsg("Wrong data! Try again!")
      },
      serviceData => {
        val result = UserCommandHandler.handleAddServiceCommand(serviceData)
        //Redirect(routes.TestServiceCatalog.serviceHandler(Printer.printAddServiceResult(result)))
        makeResponseWithMsgAndService(Printer.printAddServiceResult(result), serviceData)
      }
    )
  }


  def makeResponseWithMsg(msg: String) = {
    val result = UserCommandHandler.handleShowAllServices
    Ok(views.html.services(result.services.get, Service.createServiceForm, Service.deleteFindServiceForm, msg, null))
  }

  def makeResponseWithMsgAndService(msg: String, handledService: Service) = {
    val result = UserCommandHandler.handleShowAllServices
    Ok(views.html.services(result.services.get, Service.createServiceForm, Service.deleteFindServiceForm, msg, handledService))
  }

  def deleteService = Action { implicit request =>
    Service.deleteFindServiceForm.bindFromRequest.fold(
      err => {
       // Redirect(routes.TestServiceCatalog.serviceHandler("Empty field!"))
        makeResponseWithMsg("Empty field!")
      },
      data => {
        UserCommandHandler.readDeleteServiceCommand(data) match {
          case Some(command) =>
            val result = UserCommandHandler.handleDeleteServiceCommand(command)
            val content = Printer.printDeleteServiceResult(result)
            //Redirect(routes.TestServiceCatalog.serviceHandler(content))
            makeResponseWithMsg(content)
          case None =>
            //Redirect(routes.TestServiceCatalog.serviceHandler("Incorrect host and port"))
            makeResponseWithMsg("Incorrect host and port")
        }
      }
    )
  }

  def findService = Action { implicit request =>
  Service.deleteFindServiceForm.bindFromRequest.fold(
    err => {
      //Redirect(routes.TestServiceCatalog.serviceHandler("Empty field!"))
      makeResponseWithMsg("Empty field!")
    },
    data => {
      UserCommandHandler.readFindServiceCommand(data) match {
        case Some(command) =>
          val result = UserCommandHandler.handleFindServiceCommand(command)
          result.foundService match {
            case Some(service)  =>
              //Redirect(routes.TestServiceCatalog.serviceHandler("Success!"))
              makeResponseWithMsgAndService("Success!",service)
            case None =>
              //Redirect(routes.TestServiceCatalog.serviceHandler("Service doesn't exist!"))
              makeResponseWithMsg("Service doesn't exist!")
          }
        case None =>
          //Redirect(routes.TestServiceCatalog.serviceHandler("Incorrect host and port"))
          makeResponseWithMsg("Incorrect host and port")
      }
    }
  )
  }

 // def updateService = Action { implicit request =>
  //}
}

