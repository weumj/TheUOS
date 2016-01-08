package com.uoscs09.theuos2.base;

import android.view.View;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.uoscs09.theuos2.R;

public abstract class AbsProgressFragment<T> extends AbsAsyncFragment<T> {
    private View mProgressLayout;
    private ProgressWheel mProgressWheel;


    protected void registerProgressView(View progressView) {
        this.mProgressLayout = progressView;

        if (progressView != null)
            mProgressWheel = (ProgressWheel) progressView.findViewById(R.id.progress_wheel);
    }

    /**
     * '로딩 중' 을 나타내는 View를 반환한다.
     */
    protected final View getProgressView() {
        return mProgressLayout;
    }

    @Override
    protected final void onPostExecute() {
        if (mProgressWheel != null)
            mProgressWheel.stopSpinning();
        if (mProgressLayout != null)
            mProgressLayout.setVisibility(View.INVISIBLE);
    }


    @Override
    protected final void onPreExecute() {
        if (mProgressWheel != null)
            mProgressLayout.setVisibility(View.VISIBLE);
        if (mProgressWheel != null)
            mProgressWheel.spin();
    }

}
