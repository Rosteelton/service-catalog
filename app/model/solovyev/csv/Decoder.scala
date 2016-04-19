package model.solovyev.csv


trait Decoder[A] {
  val defaultDelimiter = ","
  def reads(str: String): Either[String, A]
}












