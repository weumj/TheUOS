package com.uoscs09.theuos2.util;

import java.util.Collection;
import java.util.List;

import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;

public class TaskUtil {
    public static boolean cancel(Task task) {
        return Tasks.cancelTask(task);
    }


    public static <T, Q extends Collection<T>, R extends Task<Q>> Task<List<T>> parallelTaskTypedCollection(Collection<R> requests){
        return Tasks.Parallel.parallelTaskTypedCollection(requests, OptimizeStrategy.isSafeToOptimize() && requests.size() > 7 ? 2 : 1);
    }
}
