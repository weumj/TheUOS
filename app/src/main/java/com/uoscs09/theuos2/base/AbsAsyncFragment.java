package com.uoscs09.theuos2.base;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.javacan.asyncexcute.AsyncCallback;
import com.javacan.asyncexcute.AsyncExecutor;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.async.AsyncFragmentJob;
import com.uoscs09.theuos2.util.AppUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code Fragment}에 {@code AsyncExcutor} 인터페이스를 구현한 클래스<br>
 * 이 클래스를 상속 받는 클래스는 {@code Callable} 인터페이스를 반드시 구현해야한다.<br>
 * 구현한 {@code Callable} 은 백그라운드 작업이 실행되는 콜백이다.
 */
public abstract class AbsAsyncFragment<T> extends BaseTabFragment {

    private final static Map<String, Object> sAsyncDataStoreMap = new ConcurrentHashMap<>();

    /**
     * {@code super.onCreate()}를 호출하면, 이전의 비 동기 작업 처리 결과에 따라<br>
     * {@code @AsyncData} annotation이 설정된 객체의 값이 설정 될 수도 있다.
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

    /**
     * 주어진 비동기 작업을 실행한다.
     */
    @NonNull
    public final AsyncTask<Void, Void, T> execute(@NonNull AsyncFragmentJob<T> job) {
        InnerJob<T> innerJob = new InnerJob<>(this, job);

        sAsyncDataStoreMap.remove(getClass().getName());

        onPreExecute();

        return new AsyncExecutor<T>().setCallable(job).setCallback(innerJob).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void onPostExecute(){
    }


    private static class InnerJob<V> implements AsyncCallback<V> {
        private AsyncFragmentJob<V> mAsyncFragmentJob;
        private AbsAsyncFragment<V> mFragment;

        private InnerJob(AbsAsyncFragment<V> fragment, AsyncFragmentJob<V> asyncJob) {
            this.mFragment = fragment;
            this.mAsyncFragmentJob = asyncJob;
        }

        @Override
        public void onResult(V v) {
            if (mFragment.isVisible())
                mAsyncFragmentJob.onResult(v);
            else {
                mFragment.putAsyncData(mFragment.getClass().getName(), v);
                mAsyncFragmentJob.onResultBackground(v);
            }

            releaseResource();
        }

        @Override
        public void exceptionOccured(Exception e) {
            e.printStackTrace();
            Context context = mFragment.getActivity();
            if (mFragment.isVisible()) {
                if(!mAsyncFragmentJob.exceptionOccurred(e)) {
                    if (e instanceof IOException) {
                        AppUtil.showInternetConnectionErrorToast(context, mFragment.isMenuVisible());
                    } else {
                        AppUtil.showErrorToast(context, e, mFragment.isMenuVisible());
                    }
                }

            } else {
                mAsyncFragmentJob.errorOnBackground(e);
            }

            releaseResource();
        }

        @Override
        public void cancelled() {
            if (mFragment.isVisible()) {
                mAsyncFragmentJob.cancelled();
            }

            releaseResource();
        }

        @Override
        public void onPostExcute() {
            if (mFragment.isVisible()) {
                mFragment.onPostExecute();
            }
            mAsyncFragmentJob.onPostExcute();
        }

        private void releaseResource() {
            mAsyncFragmentJob = null;
            mFragment = null;
        }
    }

    /**
     * 비동기 작업이 끝난 후, Fragment가 이미 파괴되었을 때 호출되어 <br>
     * 전역적인 Map에 데이터를 보관한다.
     *
     * @param key 보관할 데이터의 key, 어플리케이션에서 전역적인 데이터이므로 겹치지 않게 주의하여야 한다.
     * @param obj 보관할 데이터
     * @return 저장 성공 여부, 해당 key가 존재했다면 저장되지 않고 false를 반환한다.
     */
    protected boolean putAsyncData(String key, T obj) {
        if (!sAsyncDataStoreMap.containsKey(key)) {
            sAsyncDataStoreMap.put(key, obj);
            return true;
        } else
            return false;
    }

    /**
     * 비동기 작업으로 인해 저장된 data를 가져온다. 가져온 data는 Map에서 삭제된다.
     *
     * @param key 저장된 data를 가져올 key
     * @return 저장된 data, 저장된 data가 없다면 null을 반환한다.
     */
    protected static Object getAsyncData(String key) {
        return sAsyncDataStoreMap.remove(key);
    }


   /*
    protected void errorOnBackground(Context context, T result) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification noti;
        CharSequence resultMesage;
        if (result instanceof Exception) {
            resultMesage = context.getText(R.string.progress_fail);
        } else {
            resultMesage = context.getText(R.string.finish_update);
        }

        int titleRes = AppUtil.getPageResByClass(getClass());
        CharSequence title;
        if (titleRes != -1) {
            title = context.getText(titleRes);
        } else {
            title = context.getText(R.string.progress_finish);
        }
        String msg = resultMesage + " : " + title;

        noti = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentTitle(msg)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(msg)
                .build();

        final int notiId = AppUtil.titleResIdToOrder(titleRes);
        nm.notify(notiId, noti);

        HANDLER.postDelayed(new Runnable() {

            @Override
            public void run() {
                nm.cancel(notiId);
            }
        }, 2000);
    }
    */
}
