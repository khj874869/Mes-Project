package com.mesproject.eventrouter.stream;

@FunctionalInterface
public interface IdempotencyExtractor<V> {
    String extract(V value);
}
