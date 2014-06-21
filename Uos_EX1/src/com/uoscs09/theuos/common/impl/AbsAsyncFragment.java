package com.uoscs09.theuos.common.impl;

import java.io.IOException;
import java.util.concurrent.Callable;

import pkg.asyncexcute.AsyncCallback;
import pkg.asyncexcute.AsyncExecutor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.uoscs09.theuos.common.util.AppUtil;

/**
 * {@code Fragment}에 {@code AsyncExcutor} 인터페이스를 구현한 클래스<br>
 * 이 클래스를 상속 받는 클래스는 {@code Callable} 인터페이스를 반드시 구현해야한다.<br>
 * 구현한 {@code Callable} 은 백그라운드 작업이 실행되는 콜백이다.
 */
public abstract class AbsAsyncFragment<T> extends Fragment implements
		AsyncCallback<T>, Callable<T> {
	private AsyncExecutor<T> executor;

	@Override
	public void exceptionOccured(Exception e) {
		if (e instanceof IOException) {
			AppUtil.showInternetConnectionErrorToast(getActivity(),
					isMenuVisible());
		} else {
			AppUtil.showErrorToast(getActivity(), e, isMenuVisible());
		}
	}

	@Override
	public void cancelled() {
		AppUtil.showCanceledToast(getActivity(), isMenuVisible());
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
		executor = new AsyncExecutor<T>();
		executor.setCallable(this).setCallback(this)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * 현재 실행되고 있는 백그라운드 작업을 취소한다.
	 * 
	 * @return {@code true} - 작업을 성공적으로 취소하였을 때<br>
	 *         {@code false} - 작업이 설정되지 않았거나 ({@code null}),<br>
	 *         작업을 취소할 수 없을 때<br>
	 *         (대개 이런경우는 작업이 이미 정상적으로 종료된 경우이다.)
	 */
	final protected boolean cancelExecutor() {
		if (executor != null) {
			return executor.cancel(true);
		} else {
			return false;
		}
	}

	/** 작업을 처리하는 AsyncExcutor 객체를 얻는다. */
	final protected AsyncExecutor<T> getExecutor() {
		return executor;
	}
}
