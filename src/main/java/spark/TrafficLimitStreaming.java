package spark;

import database.DatabaseUtil;
import org.apache.hadoop.util.Time;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.sql.SQLException;
import java.util.Properties;

public class TrafficLimitStreaming {

    public static void main(String[] args) throws InterruptedException {
        JavaStreamingContext jsc = new JavaStreamingContext("local[2]", "TrafficLimits", Durations.seconds(60));
        JavaReceiverInputDStream<String> messages = jsc.socketTextStream("localhost", 9999);

        Producer<String, String> producer = new KafkaProducer<>(getKafkaProperties());
        producer.initTransactions();

        JavaDStream<Package> packages = messages.map(Package::new);
        packages = filterTraffic(packages, args);
        packages.map(Package::getSize)
                .reduceByWindow(Long::sum, Durations.seconds(300), Durations.seconds(60))
                .foreachRDD(rdd ->
                        rdd.collect().forEach(size -> {

                            try {
                                if (size > DatabaseUtil.getMaxLimit()) {
                                    producer.beginTransaction();
                                    producer.send(new ProducerRecord<>("alerts", "Maximal limit exceeded, traffic amount = " + size + " bytes. "));
                                    producer.commitTransaction();
                                }
                                if (size < DatabaseUtil.getMinLimit()) {
                                    producer.beginTransaction();
                                    producer.send(new ProducerRecord<>("alerts", "Minimal limit exceeded, traffic amount = " + size + " bytes."));
                                    producer.commitTransaction();
                                }
                            } catch (SQLException | ClassNotFoundException throwables) {
                                throwables.printStackTrace();
                            }

                        })
                );
        jsc.start();
        jsc.awaitTermination();
        producer.close();
    }

    protected static JavaDStream<Package> filterTraffic(JavaDStream<Package> traffic, String[] args) {

        if (args == null || args.length == 0) {
            return traffic;
        }

        if (args[0] != null) {
            String[] param = args[0].split("=");
            param[1] = param[1].replaceAll("/", "");
            if (param[0].equals("toIp")) {
                traffic = traffic.filter(p -> p.getToIp().equals(param[1]));
            } else if (param[0].equals("fromIp")) {
                traffic = traffic.filter(p -> p.getFromIp().equals(param[1]));
            } else {
                System.out.println("Wrong argument, traffic won't be filtered");
            }
        }
        return traffic;
    }

    protected static Properties getKafkaProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("transactional.id", "my-transactional-id");
        return props;
    }
}
