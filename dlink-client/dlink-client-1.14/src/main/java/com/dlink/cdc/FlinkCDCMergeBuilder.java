package com.dlink.cdc;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.dlink.assertion.Asserts;
import com.dlink.model.FlinkCDCConfig;

/**
 * FlinkCDCMergeBuilder
 *
 * @author wenmo
 * @since 2022/1/29 22:37
 */
public class FlinkCDCMergeBuilder {

    public static void buildMySqlCDC(StreamExecutionEnvironment env, FlinkCDCConfig config) {
        if (Asserts.isNotNull(config.getParallelism())) {
            env.setParallelism(config.getParallelism());
        }
        if (Asserts.isNotNull(config.getCheckpoint())) {
            env.enableCheckpointing(config.getCheckpoint());
        }
        DataStreamSource<String> streamSource = CDCBuilderFactory.buildCDCBuilder(config).build(env);
        streamSource.sinkTo(getKafkaProducer(config.getBrokers(), config.getTopic()));
    }

    private static KafkaSink<String> getKafkaProducer(String brokers, String topic) {
        return KafkaSink.<String>builder()
            .setBootstrapServers(brokers)
            .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                .setTopic(topic)
                .setValueSerializationSchema(new SimpleStringSchema())
                .build()
            )
            .build();
    }
}
