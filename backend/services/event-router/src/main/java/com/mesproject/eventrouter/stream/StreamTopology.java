package com.mesproject.eventrouter.stream;

import com.mesproject.contract.RfidNormalizedEvent;
import com.mesproject.contract.RfidRawEvent;
import com.mesproject.contract.Topics;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;

@Configuration
public class StreamTopology {

    @Bean
    public org.apache.kafka.streams.Topology topology(StreamsBuilder builder) {
        var rawSerde = new JsonSerde<>(RfidRawEvent.class);
        var normSerde = new JsonSerde<>(RfidNormalizedEvent.class);

        builder.addStateStore(
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(DedupTransformer.STORE_NAME),
                        Serdes.String(),
                        Serdes.Long()
                )
        );

        builder.stream(Topics.RFID_RAW, Consumed.with(Serdes.String(), rawSerde))
                .mapValues(raw -> new RfidNormalizedEvent(
                        raw.eventId(),
                        raw.occurredAt(),
                        raw.idempotencyKey(),
                        raw.tagId(),
                        raw.stationCode(),
                        raw.direction()
                ))
                .transformValues(() -> new DedupTransformer<>((RfidNormalizedEvent e) -> e.idempotencyKey()),
                        DedupTransformer.STORE_NAME)
                .filter((k, v) -> v != null)
                .to(Topics.RFID_NORMALIZED, Produced.with(Serdes.String(), normSerde));

        return builder.build();
    }
}
