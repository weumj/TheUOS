package com.uoscs09.theuos2.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.util.AppUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mj.android.utils.task.DelayedTask;
import mj.android.utils.task.ErrorListener;
import mj.android.utils.task.ResultListener;
import mj.android.utils.task.Task;
import mj.android.utils.task.TaskQueue;

public abstract class AbsAsyncFragment<T> extends BaseTabFragment {

    private final static Map<String, Object> sAsyncDataStoreMap = new ConcurrentHashMap<>();

    /**
     * {@code super.onCreate()}를 호출하면, 이전의 비 동기 작업 처리 결과에 따라<br>
     * {@code @AsyncData} annotation 이 설정된 객체의 값이 설정 될 수도 있다.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Object data = getAsyncData(getClass().getName());
        if (data != null) {
            Field[] fs = getClass().getDeclaredFields();
            for (Field f : fs) {
                if (f.getAnnotation(AsyncData.class) != null) {
                    f.setAccessible(true);
                    try {
                        f.set(this, data);
                    } catch (IllegalAccessException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                    f.setAccessible(false);
                    break; // 현재 하나의 변수만 취급함
                }
            }
        }

    }

    /**
     * 비동기 작업이 실행되기 전 호출된다.
     */
    protected void onPreExecute() {
    }

    protected void onPostExecute() {
    }

    // todo 클래스 외부로 리팩토링
    protected static final class TaskWrapper<T> {
        private final WeakReference<AbsAsyncFragment<T>> ref;
        private Task<T> task;
        private ResultListener<T> r;
        private ErrorListener e;
        private Runnable last;

        private TaskWrapper(AbsAsyncFragment<T> fragment, @NonNull Task<T> task) {
            ref = new WeakReference<>(fragment);
            this.task = task;
        }

        public static <T> TaskWrapper<T> create(AbsAsyncFragment<T> fragment, @NonNull Task<T> task) {
            return new TaskWrapper<>(fragment, task);
        }


        public TaskWrapper<T> result(@NonNull final ResultListener<T> r) {
            this.r = result -> {
                AbsAsyncFragment<T> f = ref.get();
                if (f != null) {
                    if (f.isVisible()) {
                        r.onResult(result);
                    } else {
                        f.putAsyncData(getClass().getName(), result);
                    }
                }
                // else
                // putAsyncData
            };
            return this;
        }

        public TaskWrapper<T> error(@Nullable final ErrorListener e) {
            final String tag = ref.get().getTag();
            this.e = t -> {
                AbsAsyncFragment f = ref.get();
                if (f != null && f.isVisible()) {
                    if (e != null) {
                        e.onError(t);
                    } else {
                        FirebaseCrash.logcat(Log.ERROR, tag, "AsyncFragment background error");
                        FirebaseCrash.report(t);
                        //Log.w(tag, "error", t);
                    }
                }
            };
            return this;
        }

        public TaskWrapper<T> atLast(@Nullable final Runnable r) {
            this.last = () -> {
                AbsAsyncFragment f = ref.get();
                if (f != null && f.isVisible()) {
                    f.onPostExecute();
                    if (r != null) r.run();
                }
            };
            return this;
        }

        public DelayedTask<T> build() {
            setupRef();
            DelayedTask<T> delayedTask = task.delayed().result(r).error(e).atLast(last);
            cleanUpRef();

            AbsAsyncFragment f = ref.get();
            if (f == null) {
                Log.w("AbsAsyncFragment", "error(host fragment == null)");
                return delayedTask;
            }

            f.onPreExecute();
            sAsyncDataStoreMap.remove(getClass().getName());

            return delayedTask;
        }

        public DelayedTask<T> buildWithQueue(String tag) {
            AbsAsyncFragment f = ref.get();

            if (f == null) {
                DelayedTask<T> delayedTask = task.delayed();
                setupRef();
                delayedTask.result(r).error(e).atLast(last);
                cleanUpRef();

                Log.w(tag, "error(host fragment == null)");
                return delayedTask;
            }

            TaskQueue taskQueue = f.taskQueue();
            if (taskQueue == null) {
                DelayedTask<T> delayedTask = task.delayed();
                setupRef();
                delayedTask.result(r).error(e).atLast(last);
                cleanUpRef();

                AppUtil.showToast(f.getActivity(), R.string.error_cannot_execute, true);
                return delayedTask;
            }

            f.onPreExecute();
            sAsyncDataStoreMap.remove(getClass().getName());

            DelayedTask<T> delayedTask = taskQueue.enqueue(tag, task);
            setupRef();
            delayedTask.result(r).error(e).atLast(last);
            cleanUpRef();
            return delayedTask;
        }

        private void setupRef(){
            if(r == null) result((result) -> {});
            if(e == null) error(null);
            if(last == null) atLast(null);
        }

        private void cleanUpRef() {
            r = null;
            e = null;
            last = null;
        }

    }


    protected final TaskWrapper<T> appTask(@NonNull Task<T> task) {
        return TaskWrapper.create(this, task);
    }

    protected void simpleErrorRespond(Throwable e) {
        e.printStackTrace();
        if (e instanceof IOException) {
            AppUtil.showInternetConnectionErrorToast(getActivity(), isMenuVisible());
        } else {
            AppUtil.showErrorToast(getActivity(), e, isMenuVisible());
        }
    }

    /**
     * 비동기 작업이 끝난 후, Fragment 가 이미 파괴되었을 때 호출되어 <br>
     * 전역적인 Map 에 데이터를 보관한다.
     *
     * @param key 보관할 데이터의 key, 어플리케이션에서 전역적인 데이터이므로 겹치지 않게 주의하여야 한다.
     * @param obj 보관할 데이터
     * @return 저장 성공 여부, 해당 key 가 존재했다면 저장되지 않고 false 를 반환한다.
     */
    protected boolean putAsyncData(String key, T obj) {
        if (!sAsyncDataStoreMap.containsKey(key)) {
            sAsyncDataStoreMap.put(key, obj);
            return true;
        } else
            return false;
    }

    /**
     * 비동기 작업으로 인해 저장된 data 를 가져온다. 가져온 data 는 Map 에서 삭제된다.
     *
     * @param key 저장된 data 를 가져올 key
     * @return 저장된 data, 저장된 data 가 없다면 null 을 반환한다.
     */
    protected static Object getAsyncData(String key) {
        return sAsyncDataStoreMap.remove(key);
    }

}
