package com.uoscs09.theuos2.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.List;

public class CollectionUtil {

    @Nullable
    public static <T> T elementAt(@Nullable List<T> list, int position) {
        return list != null ? list.size() > position ? list.get(position) : null : null;
    }

    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean addAll(@NonNull Collection<T> parent, Collection<T> add) {
        return add != null && parent.addAll(add);
    }
}
