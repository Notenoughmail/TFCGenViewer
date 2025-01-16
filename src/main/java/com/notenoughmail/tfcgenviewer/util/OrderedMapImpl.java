package com.notenoughmail.tfcgenviewer.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class OrderedMapImpl<K, V> implements OrderedMap<K, V> {

    private final LinkedList<K> order = new LinkedList<>();
    private final HashMap<K, V> map = new HashMap<>();

    @Override
    public int indexOf(K key) {
        return order.indexOf(key);
    }

    @Override
    @Nullable
    public K getKey(int index) {
        if (isEmpty()) {
            return null;
        } else if (index < 0 || index >= order.size()) {
            return order.getLast();
        } else {
            return order.get(index);
        }
    }

    @Override
    public boolean remove(int index) {
        final K key;
        if (index < 0 || index >= order.size()) {
            key = order.getLast();
            order.removeLast();
        } else {
            key = order.get(index);
            order.remove(index);
        }
        return map.remove(key) != null;
    }

    @Override
    @Nullable
    public V put(int index, K key, V value) {
        if (index < 0 || index > order.size()) {
            order.addLast(key);
        } else {
            order.add(index, key);
        }
        return map.put(key, value);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        final boolean o = order.isEmpty();
        if (o != map.isEmpty()) {
            throw new IllegalStateException("Order and map are out of sync!");
        }
        return o;
    }

    @Override
    public boolean containsKey(Object key) {
        return indexOf((K) key) != -1;
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V remove(Object key) {
        order.remove(key);
        return map.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        order.clear();
        map.clear();
    }

    @Override
    @Nullable
    public V replace(K key, V value) {
        // If not present, do not replace, keeps the position in the order
        return indexOf(key) == -1 ? null : map.replace(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        V currentValue = map.get(key);
        if (indexOf(key) == -1 || !Objects.equals(currentValue, oldValue)) {
            return false;
        }
        map.put(key, newValue);
        return true;
    }

    @Override
    public V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        V oldValue = map.get(key);
        V newValue = oldValue == null ? value : remappingFunction.apply(oldValue, value);
        if (newValue == null) {
            remove(key);
        } else {
            map.put(key, newValue);
        }
        return newValue;
    }

    @Override
    @NotNull
    public Collection<V> values() {
        return order.stream().map(map::get).toList();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        for (K k : order) {
            action.accept(k, get(k));
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        map.replaceAll(function);
    }

    @Override
    @NotNull
    public Set<K> keySet() {
        return new LinkedHashSet<>(order);
    }

    @Override
    @NotNull
    public Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> internal = map.entrySet();
        final Set<Entry<K, V>> out = new LinkedHashSet<>(internal.size());
        for (K k : order) {
            internal.forEach(entry -> {
                if (!out.contains(entry) && Objects.equals(k, entry.getKey())) {
                    out.add(entry);
                }
            });
        }
        return out;
    }
}
