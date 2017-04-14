package com.uoscs09.theuos2.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.uoscs09.theuos2.util.AppUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
            try {
                //noinspection unchecked
                T t = (T) data;
                setPrevAsyncData(t);
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
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
            */
        }

    }

    /**
     * 비동기 작업이 실행되기 전 호출된다.
     */
    protected void onPreExecute() {
    }

    protected void onPostExecute() {
    }

    protected final Observable<T> appTask(@NonNull Observable<T> task) {
        WeakReference<AbsAsyncFragment<T>> ref = new WeakReference<>(this);
        final String TAG = getTag();

        this.onPreExecute();
        sAsyncDataStoreMap.remove(getClass().getName());

        task = task.publish().autoConnect();

        Subscription subscription = task.subscribe(
                result -> {
                    AbsAsyncFragment<T> f = ref.get();
                    if (f != null) {
                        if (!f.isVisible()) {
                            f.putAsyncData(getClass().getName(), result);
                        }
                    }
                },
                t -> {
                    AbsAsyncFragment f = ref.get();
                    if (f != null) {
                        if (f.isVisible()) {
                            f.onPostExecute();
                        } else {
                            if (t instanceof IOException) {
                                return;
                            }

                            FirebaseCrash.logcat(Log.ERROR, TAG, "AsyncFragment background error");
                            FirebaseCrash.report(t);
                            //Log.w(tag, "error", t);
                        }
                    }
                },
                () -> {
                    AbsAsyncFragment f = ref.get();
                    if (f != null && f.isVisible()) {
                        f.onPostExecute();
                        //if (r != null) r.run();
                    }
                }
        );


        return task.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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

    protected abstract void setPrevAsyncData(T data);


}
