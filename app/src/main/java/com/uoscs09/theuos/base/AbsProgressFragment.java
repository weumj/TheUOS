package com.uoscs09.theuos.base;

import android.view.View;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.annotaion.ReleaseWhenDestroy;

public abstract class AbsProgressFragment<T> extends AbsAsyncFragment<T> {
    @ReleaseWhenDestroy
    private View mProgressLayout;
    @ReleaseWhenDestroy
    private ProgressWheel mProgressWheel;

    protected void registerProgressView(View progressView) {
        this.mProgressLayout = progressView;
        mProgressWheel = (ProgressWheel) progressView.findViewById(R.id.progress_wheel);
    }

    /**
     * '로딩 중' 을 나타내는 View를 반환한다.
     */
    protected final View getProgressView() {
        return mProgressLayout;
    }

    @Override
    protected void onTransactPostExecute() {
        if (mProgressWheel != null)
            mProgressWheel.stopSpinning();
        if (mProgressLayout != null)
            mProgressLayout.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void execute() {
        if (mProgressWheel != null)
            mProgressLayout.setVisibility(View.VISIBLE);
        if (mProgressWheel != null)
            mProgressWheel.spin();

        super.execute();
    }

}
