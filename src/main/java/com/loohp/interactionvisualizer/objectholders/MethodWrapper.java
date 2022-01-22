package com.loohp.interactionvisualizer.objectholders;

import java.lang.reflect.Method;

public class MethodWrapper<T> {

    private final Method method;
    private final Object invoke;
    private final Object[] args;

    public MethodWrapper(Method method, Object invoke, Object... args) {
        this.method = method;
        this.invoke = invoke;
        this.args = args;
    }

    @SuppressWarnings("unchecked")
    public T execute() throws Exception {
        return (T) method.invoke(invoke, args);
    }

}
