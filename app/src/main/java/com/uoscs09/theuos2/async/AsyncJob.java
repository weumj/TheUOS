package com.uoscs09.theuos2.async;

import com.javacan.asyncexcute.AsyncCallback;

import java.util.concurrent.Callable;

@Deprecated
public interface AsyncJob<V> extends Callable<V>, AsyncCallback<V> {

    @Deprecated
    abstract class Base<V> extends AsyncCallback.Base<V> implements AsyncJob<V>{
    }

}
