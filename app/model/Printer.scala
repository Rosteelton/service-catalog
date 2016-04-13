package model

import ServiceResult._

object Printer {

  def printAddServiceResult(res: AddServiceResult): String = {
    if (res.success) "Service successfully added!"
    else "Service already existed!"
  }

  def printUpdateServiceResult(res: UpdateServiceResult): String = res match {
    case SuccessUpdateServiceResult => "Service successfully updated!"
    case res: FailedUpdateServiceResult => res.err
  }

  def printDeleteServiceResult(res: DeleteServiceResult): String = {
    if (res.deleteSuccess) " successfully deleted!"
    else " hasn't been found!"
  }

  def printImportServiceResult(res: ImportServiceResult): Unit = {
    println(res.err)
  }

  def printServices(service: List[Service]): String = {
    val resultString = new StringBuilder()
    resultString.append("\nHOST                                    PORT      NAME                                    EMAIL                                   ENVIRONMENT")
    resultString.append("\n---------------------------------------------------------------------------------------------------------------------------------------------")
    for (s <- service) {
      resultString.append("\n" + s.toString)
    }
    resultString.toString()
  }

  def printServices(res: ShowAllServicesResult): String = res.services match {
    case Some(services) => printServices(services)
    case None => "Nothing to show!"
  }

  def printServicesAsCsv(res: ShowAllServicesResult): String = res.services match {
    case Some(services) => FormatHandler.servicesToCsv(services)
    case None => "Nothing to show!"
  }
}