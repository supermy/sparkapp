package kafka;


import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * 生产数据
 *
 * Created by moyong on 16/6/12.
 */
public class MyProducer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("metadata.broker.list","192.168.99.101:9092,192.168.99.101:9093,192.168.99.101:9094");
        props.setProperty("serializer.class","kafka.serializer.StringEncoder");
        props.put("request.required.acks","1");
        ProducerConfig config = new ProducerConfig(props);
        //创建生产这对象
        Producer<String, String> producer = new Producer<String, String>(config);
        //生成消息
        KeyedMessage<String, String> data1 = new KeyedMessage<String, String>("test","155058480;49;2015-11-27T00:00:03.285");
        KeyedMessage<String, String> data2 = new KeyedMessage<String, String>("test","155058480;33;2015-11-27T00:00:05.000");
        KeyedMessage<String, String> data3 = new KeyedMessage<String, String>("test","155058480; ;2015-11-27T00:00:05.000");
        try {
            int i =1;
            while(i < 100){
                //发送消息
                producer.send(data1);
                producer.send(data2);
                producer.send(data3);
                i++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        producer.close();
    }
}