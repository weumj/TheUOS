package com.uoscs09.theuos2.util;

import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;

public class TaskUtil {
    public static boolean cancel(Task task) {
        return Tasks.cancelTask(task);
    }


    public static abstract class AbstractTask<T> extends Tasks.AbstractTask<T> {

    }

}
