package com.uoscs09.theuos2.util;

import mj.android.utils.task.ErrorListener;
import mj.android.utils.task.Func;
import mj.android.utils.task.ResultListener;
import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;

public class TaskUtil {
    public static boolean cancel(Task task) {
        return task != null && task.cancel();
    }


    public static abstract class AbstractTask<T> implements Task<T> {

        private Task<T> task;

        @Override
        public void getAsync(ResultListener<T> r, ErrorListener e) {
            task = Tasks.newTask(this::get);
            task.getAsync(r, e);
        }

        @Override
        public <V> Task<V> wrap(final Func<T, V> func) {
            return Tasks.newTask(() -> func.func(get()));
        }

        @Override
        public boolean cancel() {
            return TaskUtil.cancel(task);
        }

    }
}
