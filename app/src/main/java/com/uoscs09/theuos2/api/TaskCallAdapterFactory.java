package com.uoscs09.theuos2.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import mj.android.utils.task.Task;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

class TaskCallAdapterFactory extends CallAdapter.Factory {

    private TaskCallAdapterFactory() {
    }

    @Override
    public CallAdapter<Task<?>> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Task.class) {
            return null;
        }
        final Type responseType = getCallResponseType(returnType);
        return new CallAdapter<Task<?>>() {

            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public <R> Task<R> adapt(Call<R> call) {
                return new CallTaskImpl<>(call);
            }
        };
    }

    private static Type getCallResponseType(Type returnType) {
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Call return type must be parameterized as Call<Foo> or Call<? extends Foo>");
        }
        return getParameterUpperBound(0, (ParameterizedType) returnType);
    }

    private static TaskCallAdapterFactory instance;

    public static TaskCallAdapterFactory getInstance() {
        if(instance == null){
            instance = new TaskCallAdapterFactory();
        }

        return instance;
    }

}
