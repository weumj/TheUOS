package com.uoscs09.theuos2.async;

public interface Processor<In, Out> {
    Out process(In in) throws Exception;
}

