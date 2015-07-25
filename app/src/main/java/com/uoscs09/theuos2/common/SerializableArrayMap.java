package com.uoscs09.theuos2.common;


import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializableArrayMap<K, V> extends android.support.v4.util.ArrayMap<K, V> implements Serializable {
    private static final long serialVersionUID = -4985689937871015665L;

    public SerializableArrayMap() {
        super();
    }

    /**
     * Create a new SerializableArrayMap with a given initial capacity.
     */
    public SerializableArrayMap(int capacity) {
        super(capacity);
    }

    /**
     * Create a new SerializableArrayMap with the mappings from the given SerializableArrayMap.
     */
    public SerializableArrayMap(SimpleArrayMap map) {
        super(map);
    }

    public static <V> SerializableArrayMap<Integer, V> fromSparseArray(@NonNull SparseArray<V> sparseArray) {

        final int N = sparseArray.size();

        SerializableArrayMap<Integer, V> map = new SerializableArrayMap<>(N);

        for (int i = 0; i < N; i++) {
            map.put(sparseArray.keyAt(i), sparseArray.valueAt(i));
        }

        return map;
    }

    @NonNull
    public static <V> SparseArray<V> toSparseArray(ArrayMap<Integer, V> map) {

        final int N = map != null ? map.size() : 0;
        SparseArray<V> sparseArray = new SparseArray<>(N);

        for (int i = 0; i < N; i++) {
            sparseArray.put(map.keyAt(i), map.valueAt(i));
        }

        return sparseArray;
    }

    private synchronized void writeObject(ObjectOutputStream stream) throws IOException {

        stream.writeInt(size());
        for (Entry<K, V> e : entrySet()) {
            stream.writeObject(e.getKey());
            stream.writeObject(e.getValue());
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        int size = stream.readInt();
        if (size < 0) {
            throw new InvalidObjectException("Size: " + size);
        }

        ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked") K key = (K) stream.readObject();
            @SuppressWarnings("unchecked") V val = (V) stream.readObject();
            put(key, val);
        }
    }
}
