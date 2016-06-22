package my.first

/**
  * Created by moyong on 16/6/12.
  */
class Car(val year: Int) {
  private[this] var miles: Int = 0

  def drive(distance: Int) {
    miles += distance
  }

  override def toString(): String = "year:" + year + " miles:" + miles
}
