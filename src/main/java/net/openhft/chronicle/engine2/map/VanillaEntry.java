package net.openhft.chronicle.engine2.map;

import net.openhft.chronicle.engine2.api.map.KeyValueStore;

import java.util.Optional;

/**
 * Created by peter on 22/05/15.
 */
public class VanillaEntry<K, V> implements KeyValueStore.Entry<K, V> {
    private final K key;
    private final V value;

    public VanillaEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K key() {
        return key;
    }

    @Override
    public V value() {
        return value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }
}
