package com.uoscs09.theuos2.async;

@Deprecated
public interface AsyncFragmentJob<V> extends AsyncJob<V> {
    /**
     * 비동기 작업이 끝났지만, Fragment 가 파괴되었을 때, 호출된다.
     */
    void errorOnBackground(Throwable error);

    /**
     * 비동기 작업이 끝났지만, Fragment 가 파괴되었을 때, 호출된다.
     */
    void onResultBackground(V result);

    /**
     * @return Exception 의 처리 여부
     * */
    boolean exceptionOccurred(Exception e);

    @Deprecated
    abstract class Base<V> extends AsyncJob.Base<V> implements AsyncFragmentJob<V> {

        @Override
        public void errorOnBackground(Throwable error) {
        }

        @Override
        public void onResultBackground(V result) {
        }

        @Override
        @Deprecated
        public final void exceptionOccured(Exception e) {
        }

        @Override
        public boolean exceptionOccurred(Exception e) {
            return false;
        }
    }
}