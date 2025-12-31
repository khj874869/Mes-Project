package com.mesproject.eventrouter.stream;

import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Instant;

/**
 * idempotencyKey 기준으로 중복 이벤트를 걸러냄.
 * - store에는 마지막 seen timestamp를 저장
 */
public class DedupTransformer<K, V> implements ValueTransformerWithKey<K, V, V> {

    public static final String STORE_NAME = "idempotency-store";

    private final IdempotencyExtractor<V> extractor;
    private KeyValueStore<String, Long> store;

    public DedupTransformer(IdempotencyExtractor<V> extractor) {
        this.extractor = extractor;
    }

    @Override
    public void init(ProcessorContext context) {
        //noinspection unchecked
        this.store = (KeyValueStore<String, Long>) context.getStateStore(STORE_NAME);
    }

    @Override
    public V transform(K readOnlyKey, V value) {
        String key = extractor.extract(value);
        if (key == null || key.isBlank()) {
            return value;
        }
        Long seen = store.get(key);
        if (seen != null) {
            return null; // drop duplicate
        }
        store.put(key, Instant.now().toEpochMilli());
        return value;
    }

    @Override
    public void close() {}
}
