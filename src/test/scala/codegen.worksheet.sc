def idents(i: Int, name: String = "_") = 1 to i map(i0 => name+i0)
def types(i: Int, name: String="T") = idents(i,name) mkString ","
def vals(i: Int, name: String="t") = idents(i,name) mkString ","
def valsWithTypes(i: Int) = {
  idents(i) zip idents(i,"T") map{ case (v,t) => s"$v:$t"} mkString ","
}

def tuple(i: Int) = {
  s"""
  implicit def formatTuple$i[${idents(i,"T") map (_+": Format") mkString ","}]:Format[(${types(i)})] = new Format[(${types(i)})]{
    def reads(json: JsValue) = json.validate[JsArray].flatMap{
      case JsArray(scala.collection.Seq(${vals(i)})) =>
        val (${vals(i,"r")}) =
          (${1 to i map (j => s"t$j.validate[T$j]") mkString ","})
        mergeErrors(
          Seq(${vals(i,"r")}),
          (${idents(i,"r") map (_+".get") mkString ","})
        )
      case _ => JsError(JsonValidationError(s"Expected array of size $i, found: "+json))
    }
    def writes(t: (${types(i)})) =
      JsArray(Seq(${idents(i) map (t => s"Json.toJson(t.$t)") mkString(", ")}))
  }
"""
}

println(
  s"""package ai.x.play.json.tuples
// Formatters for Scala tuples
// Autogenerated using codegen.sh
import _root_.play.api.libs.json._
object `package`{
  private def mergeErrors[T](
    results: Seq[JsResult[_]],
    success: => T
  ): JsResult[T] = {
    val errors = results.collect{
      case JsError(values) => values
    }.flatten
    if(errors.isEmpty){
      JsSuccess(success)
    } else JsError(errors)
  }
  ${2 to 22 map tuple mkString ""}
}
"""
)
