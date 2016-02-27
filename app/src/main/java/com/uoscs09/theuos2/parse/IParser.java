package com.uoscs09.theuos2.parse;

import mj.android.utils.task.Func;

/**
 * 필요한 정보를 parsing 하는 interface
 */
public interface IParser<Param, Return> extends Func<Param, Return> {

    /**
     * 주어진 정보를 parsing 한다.
     */
    Return parse(Param param) throws Throwable;

    interface IPostParsing {
        void afterParsing();
    }

    abstract class Base<Param, Return> implements IParser<Param, Return> {

        @Override
        public final Return func(Param param) throws Throwable {
            return parse(param);
        }

    }

}
