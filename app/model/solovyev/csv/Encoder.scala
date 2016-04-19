package model.solovyev.csv

trait Encoder[A]{

  def writes(o: A): String
  val defaultDelimiter = ","
}


object DefaultEncoder {

  implicit object IntEncoder extends Encoder[Int] {

    def writes(o: Int) = o.toString
  }

//  implicit object DoubleEncoder extends Encoder[Double] {
//    def writes(o: Double) = o.toString
//  }
//
//  implicit object StringEncoder extends Encoder[String] {
//    def writes(o: String) = o
//  }
//
//  implicit object LongEncoder extends Encoder[Long] {
//    def writes(o: Long) = o.toString
//  }
//
//  implicit def OptionEncoder[A](implicit fmt: Encoder[A]): Encoder[Option[A]] = new Encoder[Option[A]] {
//    def writes(o: Option[A]) = o match {
//      case Some(value) => fmt.writes(value)
//      case None => "NULL"
   // }
  //}

 // case class CsvTable(rows: List[CsvRow])
//  case class CsvRow(columns: List[CsvColumn])
 // case class CsvColumn(value: String)
}



object CSV {
  def toCSV[T](o: T)(implicit tcsv: Encoder[T]): String = tcsv.writes(o)
  def parse[T](str: String)(implicit tcsv: Decoder[T]) = tcsv.reads(str)

}

