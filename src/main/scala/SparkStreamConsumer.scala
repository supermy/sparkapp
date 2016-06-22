/**
  * Created by moyong on 16/6/14.
  */

import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import utils.RedisClient

/**
  * 应用的场景为统计一段时间内各个小区的网络信号覆盖率，计算公式如下所示：

  * 分子：信号强度大于35的采样点个数

  * 分母：信号强度为非空的所有采样点个数

  * 网络覆盖率=分子/分母

  * 原始数据为xml格式，记录各小区在各时刻的采样点，采样时间精确到ms，我们需要做的是计算单个小区以小时为间隔的信号覆盖率。通过简单的java代码解析xml文件，并将解析后的数据通过kafka生产者进程发送的kafka消息集群中，利用spark streaming进行实时处理并将处理结果存入redis。下面是数据处理过程

  * 原始数据格式：  小区ID              信号强度             时间

  * 155058480           49                   2015-11-27T00:00:03.285

  * 155058480           33                   2015-11-27T00:00:05.000

  * 155058480           空              2015-11-27T00:00:23.285

  * 原始数据处理：  小区ID        分子        分母     时间

  * 155058480     1           1        2015-11-27T00

  * 155058480     0           1        2015-11-27T00

  * 155058480     0           0        2015-11-27T00

  * 统计合并处理：  小区ID        分子        分母     时间

  * 155058480     1           2        2015-11-27T00

  * 小区155058480的网络覆盖率=分子/分母=1/2=0.5

  * 说明：以小区155058480为例的三个采样点，其中满足上述分子条件的非空记录的分子记为为1，不满足分子条件的非空记录与空记录的分子记为0，非空记录的分母记为1。同时对时间进行分割，保留到小时，并以时间个小区id为复合主键利用reduceByKey方法进行累加统计。
  */
object SparkStreamConsumer {

  private val checkpointDir = "data-checkpoint"
  //private val msgConsumerGroup = "message-consumer-group"
  private val msgConsumerGroup = "test-consumer-group"


  def main(args: Array[String]) {
    if (args.length < 2) {
      println("Usage:zkserver1:2181,zkserver2:2181,zkserver3:2181 consumeMsgDataTimeInterval(secs)")
      //kafka-topics.sh --create --zookeeper 192.168.99.101:2181 --replication-factor 1 --partitions 1 --topic test
      //-Dspark.master=local[3]
      //192.168.99.101:2181 test-consumer-group test 1


      System.exit(1)
    }

    val Array(zkServers, processingInterval) = args
    val conf = new SparkConf().setAppName("小区")
    val ssc = new StreamingContext(conf, Seconds(processingInterval.toInt))
    //using updateStateByKey asks for enabling checkpoint
    ssc.checkpoint(checkpointDir)

    //Map of (topic_name -> numPartitions) to consume. Each partition is consumed in its own thread
    //spark-stream-topic
    val kafkaStream = KafkaUtils.createStream(ssc, zkServers, msgConsumerGroup, Map("test" -> 3))
    //原始数据为 （topic,data）
    val msgDataRDD = kafkaStream.map(_._2)
    //原始数据处理
    val lines = msgDataRDD.map { msgLine => {

      val dataArr: Array[String] = msgLine.split(";")
      System.out.println("........"+dataArr(0))
      System.out.println("........"+dataArr(1))
      System.out.println("........"+dataArr(2))
      val id = dataArr(0)
      val timeArr: Array[String] = dataArr(2).split(":")
      val time = timeArr(0)
      val val1: Double = {
        if (dataArr(1).trim().isEmpty()) 0 else if (dataArr(1).toFloat > 35) 1 else 0
      }
      val val2: Double = {
        if (dataArr(1).trim().isEmpty()) 0 else if (dataArr(1).toFloat > 0) 1 else 0
      }
      //数据格式
      ((id, time), (val1, val2))
    }
    }
    //通过reduceByKey方法对相同键值的数据进行累加
    val test = lines.reduceByKey((a, b) => (a._1 + b._1, a._2 + b._2))
    //错误记录：Task not serializable
    //遍历接收到的数据，存入redis数据库
    test.foreachRDD(rdd => {
      rdd.foreachPartition(partition => {
        //val jedis = new Jedis("127.0.0.1", 6379)
        val jedis = RedisClient.pool.getResource //更换为redis 资源链接池

        partition.foreach(pairs => {
          //Redis Hincrbyfloat 命令用于为哈希表中的字段值加上指定浮点数增量值。
          //如果指定的字段不存在，那么在执行命令前，字段的值被初始化为 0 。
          jedis.hincrByFloat("test", pairs._1._1 + "_" + pairs._1._2 + ":1", pairs._2._1)
          jedis.hincrByFloat("test", pairs._1._1 + "_" + pairs._1._2 + ":2", pairs._2._2)

        })
          //释放到资源池
          jedis.close()
      })
    })
    /*//通过保存在spark内存中的数据与当前数据累加并保存在内存中
    val updateValue = (newValue:Seq[(Double,Double)], prevValueState:Option[(Double,Double)]) => {
     
    val val1:Double = newValue.map(x=>x._1).foldLeft(0.0)((sum,i)=>sum+i)
    val val2:Double = newValue.map(x=>x._2).foldLeft(0.0)((sum,i)=>sum+i)
    // 已累加的值
    val previousCount:(Double,Double) = prevValueState.getOrElse((0.0,0.0))
     
    Some((val1,val2))
    }
    val initialRDD = ssc.sparkContext.parallelize(List((("id","time"), (0.00,0.00))))
     
    val stateDstream = lines.updateStateByKey[(Double,Double)](updateValue)
    //set the checkpoint interval to avoid too frequently data checkpoint which may
    //may significantly reduce operation throughput
    stateDstream.checkpoint(Duration(8*processingInterval.toInt*1000))
    //after calculation, we need to sort the result and only show the top 10 hot pages
    stateDstream.foreachRDD { rdd => {
    val sortedData = rdd.map{ case (k,v) => (v,k) }.sortByKey(false)
    val topKData = sortedData.take(10).map{ case (v,k) => (k,v) }
    //val topKData = sortedData.map{ case (v,k) => (k,v) }
    //org.apache.spark.SparkException: Task not serializable
    topKData.foreach(x => {
    println(x)
    jedis.hincrByFloat("test",x._1._1+"_"+x._1._2+":1",x._2._1)
    })
    }
    }*/
    ssc.start()
    ssc.awaitTermination()
  }
}