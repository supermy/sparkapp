package my.first

/**
  * Created by moyong on 16/6/12.
  */
import org.apache.spark.{SparkConf, SparkContext}

object MyWordCount {
  def main(args: Array[String]) {
    //创建SparkConf()并设置App名称
    val conf = new SparkConf().setAppName("MyCount")
    //创建SparkContext，该对象是提交spark App的入口
    val sc = new SparkContext(conf)
    //使用sc创建RDD并执行相应的transformation和action
    sc.textFile(args(0)).flatMap(_.split(" ")).map((_, 1)).reduceByKey(_+_, 1).sortBy(_._2, false).saveAsTextFile(args(1))
    //停止sc，结束该任务
    sc.stop()
  }
}
