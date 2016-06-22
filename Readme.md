## 20160615
   生产数据,发送到kafka,kafka 获取数据,计算存入到redis.
    
## 20160613
    todo  数据到redis
    
    kafka 示例生产数据与消费数据完成
     #kafka 环境准备
     cd /Users/moyong/project/env-myopensource/3-tools/mytools/common/mykafka
     fig up -d 
     fig stop && fig rm -v --force
     fig up -d  && fig ps 
     dm ssh dev 
     sudo su -
     df -h
     清楚绑定数据,遗留数据可能造成下一次kafka 不能正常运行......
     ls /mnt/sda1/var/lib/docker/volumes/
     
     
     #消息生产与消费示例1
          创建topic
          kafka-topics.sh --create --zookeeper 192.168.99.101:2181 --replication-factor 1 --partitions 1 --topic test
          kafka-topics.sh --create --zookeeper 192.168.99.101:2181 --replication-factor 1 --partitions 1 --topic mykafka
          列表topic
          kafka-topics.sh --list --zookeeper 192.168.99.101:2181
          生产数据
          kafka-console-producer.sh --broker-list 192.168.99.101:9092,192.168.99.101:9093,192.168.99.101:9094 --topic test
          消费数据
          kafka-console-consumer.sh --zookeeper 192.168.99.101:2181 --topic test --from-beginning
          
          #消息生产与消费示例2
          步骤1：停止运行刚才的kafka-console-producer和kafka-console-consumer
          步骤2：运行KafkaWordCountProducer
          run-example org.apache.spark.examples.streaming.KafkaWordCountProducer 192.168.99.101:9092,192.168.99.101:9093,192.168.99.101:9094 test 3 5
          解释一下参数的意思，192.168.99.101:9092,192.168.99.101:9093,192.168.99.101:9094表示producer的地址和端口, test表示topic，3表示每秒发多少条消息，5表示每条消息中有几个单词
          步骤3：运行KafkaWordCount
           run-example org.apache.spark.examples.streaming.KafkaWordCount 192.168.99.101:2181 test-consumer-group test 1
          解释一下参数， 192.168.99.101:2181表示zookeeper的监听地址，test-consumer-group表示consumer-group的名称，必须和$KAFKA_HOME/config/consumer.properties中的group.id的配置内容一致，test表示topic，1表示线程数。
          
          #消息生产与晓峰说示例java
          MyProducer
          JavaKafkaWordCount
          
          #消息生产与消费示例scala
          KafkaWordCountProducer
                192.168.99.101:9092,192.168.99.101:9093,192.168.99.101:9094 test 2  3
          KafkaWordCount
                -Dspark.master=local
                192.168.99.101:2181 test-consumer-group test 3
          
          #消息生产与示例消费-for 集群
          spark-submit --name "my kafka producer"  \
          --class KafkaWordCountProducer \
          --master spark://hadoop001:8070 \
          --num-executors 3 \
          --driver-memory 4g \
          --executor-memory 2g \
          --executor-cores 1 \
          sparkapp-1.0-SNAPSHOT.jar \
          192.168.99.101:9092,192.168.99.101:9093,192.168.99.101:9094 test 3 5
          
          #消息消费
           spark-submit --name "my kafka consumer"  \
           --class KafkaWordCount \
           --master spark://hadoop001:8070 \
           --num-executors 3 \
           --driver-memory 4g \
           --executor-memory 2g \
           --executor-cores 1 \
           sparkapp-1.0-SNAPSHOT.jar \
           192.168.99.101:2181 test-consumer-group test 1  
           
                   
## 20160612
   # java and scala 的相互调用
   # sbt 编译打包支持完成
        sbt package
   # maven 编译打包支持完成
        
        Spark是用Scala语言开发的，目前对Scala语言支持较好的是IDEA的插件，这里我们编写一个Spark入门级程序，然后用Maven编译成jar包，
        然后提交到集群。
        mvn package
        
        
        1.选择编译成功的jar包，并将该jar上传到Spark集群中的某个节点上
        2.首先启动hdfs和Spark集群
        启动hdfs
        /usr/local/hadoop-2.6.1/sbin/start-dfs.sh
        启动spark
        /usr/local/spark-1.5.2-bin-hadoop2.6/sbin/start-all.sh
        
        3.本地环境:使用spark-submit命令提交Spark应用（注意参数的顺序）
        /Users/moyong/Downloads/thunder-download/spark-1.6.1-bin-hadoop2.6/bin/spark-submit \
        --class my.first.MyWordCount \
        --master spark://localhost:7077 \
        --executor-memory 2G \
        --total-executor-cores 4 \
        /Users/moyong/project/env-myopensource/2-cloud/sparkapp/target/sparkapp-1.0-SNAPSHOT.jar \
        file:///Users/moyong/Downloads/thunder-download/spark-1.6.1-bin-hadoop2.6/README.md   \
        file:///Users/moyong/Downloads/thunder-download/spark-1.6.1-bin-hadoop2.6/target/wordcount/
        
        4.集群环境
        4.1 数据准备
        hdfs dfs -put README.md hdfs://192.168.11.201:9000/temp
        hdfs dfs -ls hdfs://192.168.11.201:9000/temp
        hdfs dfs -rmdir temp/out
        4.2登陆到服务器
              spark-submit --name "my count"  \
              --class my.first.MyWordCount \
              --master spark://hadoop001:8070 \
              --num-executors 3 \
              --driver-memory 4g \
              --executor-memory 2g \
              --executor-cores 1 \
              sparkapp-1.0-SNAPSHOT.jar \
              hdfs://192.168.11.201:9000/temp/README.md \
              hdfs://192.168.11.201:9000/temp/out
              


        4.3 查看程序执行结果
         hdfs dfs  -ls   hdfs://192.168.11.201:9000/temp/yyout 
         hdfs dfs  -cat    hdfs://192.168.11.201:9000/temp/yyout/part-00000

        (hello,6)
        (tom,3)
        (kitty,2)
        (jerry,1)