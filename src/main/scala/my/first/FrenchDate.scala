package my.first

/**
  * Created by moyong on 16/6/12.
  */
import java.text.DateFormat._
import java.util.{Date, Locale}

object FrenchDate {
  def main(args: Array[String]) {
    val now = new Date
    val df = getDateInstance(LONG, Locale.FRANCE)
    println(df format now)


    import java.lang.reflect._
    import java.util.Date//导入java类

    println("Today is " + new Date())

    val methods = getClass.getMethods()//使用java类
    methods.foreach {
      methods: Method => println(methods.getName)
    }

  }
}