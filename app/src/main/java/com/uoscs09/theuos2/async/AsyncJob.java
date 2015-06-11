package com.uoscs09.theuos2.async;

import com.javacan.asyncexcute.AsyncCallback;

import java.util.concurrent.Callable;

public interface AsyncJob<V> extends Callable<V>, AsyncCallback<V> {

    public static abstract class Base<V> extends AsyncCallback.Base<V> implements AsyncJob<V>{
    }

}
