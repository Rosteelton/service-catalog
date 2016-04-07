import ServiceResult._

object CommandLineInterface {

  def resultToConsole(result: ServiceResult): Unit = result match {
    case res: AddServiceResult => printAddServiceResult(res)
    case res: FindServiceResult => printFindServiceResult(res)
    case res: UpdateServiceResult => printUpdateServiceResult(res)
    case res: DeleteServiceResult => printDeleteServiceResult(res)
    case res: ShowAllServicesResult => printServices(res)
    case res: ImportServiceResult => printImportServiceResult(res)
  }

  def printAddServiceResult(res: AddServiceResult) = {
    if (res.success) println("Service successfully added!")
    else println("Service hasn't been added!")
  }

  def printFindServiceResult(res: FindServiceResult): Unit = res.foundService match {
    case Some(service) =>
      println("Server was found!")
      printServices(List(service))
    case None =>
      println("Server wasn't found!")
  }

  def printUpdateServiceResult(res: UpdateServiceResult): Unit = res match {
    case SuccessUpdateServiceResult => println("Service successfully updated!")
    case res: FailedUpdateServiceResult => println(res.err)
  }

  def printDeleteServiceResult(res: DeleteServiceResult): Unit = {
    if (res.deleteSuccess) println("Service successfully deleted!")
    else println("Service hasn't been deleted!")
  }

  def printImportServiceResult(res: ImportServiceResult): Unit = {
    if (res.importSuccess) println("Services successfully imported!")
    else println(res.err)
  }

  def printServices(service: List[Service]) = {
    println()
    println("HOST                                    PORT      NAME                                    EMAIL                                   ENVIRONMENT")
    println("---------------------------------------------------------------------------------------------------------------------------------------------")
    for (s <- service) {
      println(s.toString)
    }
  }

  def printServices(res: ShowAllServicesResult): Unit = res.services match {
    case Some(services) => printServices(services)
    case None => println("Nothing to show!")
  }
}
