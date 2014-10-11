package com.uoscs09.theuos.common.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import com.javacan.asyncexcute.AsyncCallback;
import com.javacan.asyncexcute.AsyncExecutor;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.util.AppUtil;

/**
 * {@code Fragment}에 {@code AsyncExcutor} 인터페이스를 구현한 클래스<br>
 * 이 클래스를 상속 받는 클래스는 {@code Callable} 인터페이스를 반드시 구현해야한다.<br>
 * 구현한 {@code Callable} 은 백그라운드 작업이 실행되는 콜백이다.
 */
public abstract class AbsAsyncFragment<T> extends BaseFragment implements
		AsyncCallback<T>, Callable<T> {
	private AsyncExecutor<T> executor;
	private boolean mRunning = false;
	private final static Map<String, Object> sAsyncDataStoreMap = new ConcurrentHashMap<String, Object>();
	private Context mContext;

	/**
	 * {@code super.onCreate()}를 호출하면, 이전의 비 동기 작업 처리 결과에 따라<br>
	 * {@code @AsyncData} annotation이 설정된 객체의 값이 설정 될 수도 있다.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mContext = getActivity();
		Object data = getAsyncData(getClass().getName());
		if (data != null) {
			Field[] fs = getClass().getDeclaredFields();
			for (Field f : fs) {
				if (f.getAnnotation(AsyncData.class) != null) {
					f.setAccessible(true);
					try {
						f.set(this, data);
					} catch (IllegalAccessException e) {
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					f.setAccessible(false);
					break; // 현재 하나의 변수만 취급함
				}
			}
		}
	}

	/** 현재 백그라운드 작업이 실행 중 인지 여부를 반환한다. */
	public final boolean isRunning() {
		return mRunning;
	}

	/**
	 * Main Thread에서 비동기 작업을 설정하고, 실행하는 메소드<br>
	 * 비동기 작업이 실행되기 전에 필요한 작업은 이 메소드를 호출하기 전에 <br>
	 * 처리하거나, 이 메소드를 상속받아 적절히 구현한다.<br>
	 */
	protected void excute() {
		if (executor != null && !executor.isCancelled()) {
			executor.cancel(true);
		}
		mRunning = true;
		setExcuter(true);
		executor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		sAsyncDataStoreMap.remove(getClass().getName());
	}

	/**
	 * 백그라운드 작업을 설정한다.
	 * 
	 * @param force
	 *            다른 작업이 실행중인것과 관계없이 강제로 설정하는지 여부
	 * @return 설정 여부
	 */
	public final boolean setExcuter(boolean force) {
		if (!force && executor.getStatus().equals(AsyncTask.Status.RUNNING))
			return false;
		else {
			executor = new AsyncExecutor<T>().setCallable(this).setCallback(
					this);
			return true;
		}
	}

	/**
	 * 현재 실행되고 있는 백그라운드 작업을 취소한다.
	 * 
	 * @return {@code true} - 작업을 성공적으로 취소하였을 때<br>
	 *         {@code false} - 작업이 설정되지 않았거나 ({@code null}),<br>
	 *         작업을 취소할 수 없을 때<br>
	 *         (대개 이런경우는 작업이 이미 정상적으로 종료된 경우이다.)
	 */
	protected final boolean cancelExecutor() {
		if (executor != null) {
			boolean b = executor.cancel(true);
			if (b || executor.getStatus().equals(AsyncTask.Status.FINISHED))
				mRunning = false;
			return b;
		} else {
			mRunning = false;
			return false;
		}
	}

	/** 작업을 처리하는 AsyncExcutor 객체를 얻는다. */
	protected final AsyncExecutor<T> getExecutor() {
		return executor;
	}

	@Override
	public final void onPostExcute() {
		mRunning = false;
		if (isVisible()) {
			onTransactPostExcute();
		}
	}

	/** 현재 Fragment가 존재하는 상태에서 비동기 작업이 끝나고 UI Thread로 진입 할 때 호출된다. */
	protected abstract void onTransactPostExcute();

	@Override
	public final void onResult(T result) {
		if (isVisible())
			onTransactResult(result);
		else {
			putAsyncData(getClass().getName(), result);
			notifyFinishWhenBackground(mContext, result);
			mContext = null;
		}
	}

	@Override
	public void exceptionOccured(Exception e) {
		if (isVisible()) {
			if (e instanceof IOException) {
				AppUtil.showInternetConnectionErrorToast(getActivity(),
						isMenuVisible());
			} else {
				AppUtil.showErrorToast(getActivity(), e, isMenuVisible());
			}
		} else {
			notifyFinishWhenBackground(mContext, e);
			mContext = null;
		}
	}

	@Override
	public void cancelled() {
		AppUtil.showCanceledToast(getActivity(), isMenuVisible());
	}

	/**
	 * 비동기 작업이 끝났지만, Fragment가 파괴되었을 때, 호출된다. <br>
	 * <br>
	 * 기본적으로 구현된 작업은 notification을 띄우는 것이다.
	 * 
	 * @param context
	 *            Fragment의 Activity
	 * @param result
	 *            작업이 성공했을 시 - 'T' 객체<br>
	 *            작업이 실패하였을 때 - Exception 객체
	 */
	protected void notifyFinishWhenBackground(Context context, Object result) {
		final NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
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

		noti = new NotificationCompat.Builder(context).setAutoCancel(true)
				.setContentTitle(title).setContentText(resultMesage)
				.setSmallIcon(R.drawable.ic_launcher)
				.setTicker(context.getText(R.string.progress_finish)).build();
		final int notiId = AppUtil.titleResIdToOrder(titleRes);
		nm.notify(notiId, noti);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				nm.cancel(notiId);
			}
		}, 2000);
		mContext = null;
	}

	@Override
	public void onDetach() {
		if (!mRunning) {
			mContext = null;
		}
		super.onDetach();
	}

	/** 현재 Fragment가 존재하고, 비동기 작업이 성공적으로 끝났을 때 호출된다. */
	public abstract void onTransactResult(T result);

	/**
	 * 비동기 작업이 끝난 후, Fragment가 이미 파괴되었을 때 호출되어 <br>
	 * 전역적인 Map에 데이터를 보관한다.
	 * 
	 * @param key
	 *            보관할 데이터의 key, 어플리케이션에서 전역적인 데이터이므로 겹치지 않게 주의하여야 한다.
	 * @param obj
	 *            보관할 데이터
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
	 * @param key
	 *            저장된 data를 가져올 key
	 * @return 저장된 data, 저장된 data가 없다면 null을 반환한다.
	 * */
	protected final static Object getAsyncData(String key) {
		return sAsyncDataStoreMap.remove(key);
	}
}
