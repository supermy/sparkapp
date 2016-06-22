package my.first

/**
  * Created by moyong on 16/6/12.
  */
class Person(val name:String,val age:Int)
//伴生对象
object Person{
  def getIdentityNo()= {"test"}
}
