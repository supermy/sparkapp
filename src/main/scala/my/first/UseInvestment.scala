package my.first

/**
  * Created by moyong on 16/6/12.
  */
object UseInvestment {
  def main(args: Array[String]) {
    val investment = new Investment("xyz Corporation", InvestmentType.STOCK) //java类
    println(investment.getClass())

    val theYield = investment.`yield` // yield是scala关键字，所以要`括起来`
    println("theYield is " + theYield);
  }
}
