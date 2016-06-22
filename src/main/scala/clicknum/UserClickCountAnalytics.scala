package clicknum

import kafka.serializer.StringDecoder
import net.sf.json.JSONObject
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream.toPairDStreamFunctions
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import utils.RedisClient


/**
  * Created by moyong on 16/6/15.
  */
object UserClickCountAnalytics {

  def main(args: Array[String]): Unit = {
    var masterUrl = "local[1]"
    if (args.length > 0) {
      masterUrl = args(0)
    }

    // Create a StreamingContext with the given master URL
    val conf = new SparkConf().setMaster(masterUrl).setAppName("UserClickCountStat")
    val ssc = new StreamingContext(conf, Seconds(5))

    // Kafka configurations
    val topics = Set("user_events")
    val brokers = "192.168.99.101:9092,192.168.99.101:9093,192.168.99.101:9094"
    val kafkaParams = Map[String, String](
      "metadata.broker.list" -> brokers, "serializer.class" -> "kafka.serializer.StringEncoder")

    val dbIndex = 1
    val clickHashKey = "app::users::click"

    // Create a direct stream
    val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)



    val events = kafkaStream.flatMap(line => {
      val data = JSONObject.fromObject(line._2)
      Some(data)
    })

    // Compute user click times
    val userClicks = events.map(x => (x.getString("uid"), x.getInt("click_count"))).reduceByKey(_ + _)
    userClicks.foreachRDD(rdd => {
      rdd.foreachPartition(partitionOfRecords => {

        val jedis = RedisClient.pool.getResource //更换为redis 资源链接池

        partitionOfRecords.foreach(pair => {
          System.out.println("======:"+pair._1);
          val uid = pair._1
          val clickCount = pair._2

          // todo 优化
          jedis.select(dbIndex)

          System.out.println("======:"+clickHashKey);
          System.out.println("======:"+uid);
          System.out.println("======:"+clickCount);

          jedis.hincrBy(clickHashKey, uid, clickCount)
          //RedisClient.pool.returnResource(jedis)


        })

        jedis.close()

      })
    })

    ssc.start()
    ssc.awaitTermination()

  }
}
