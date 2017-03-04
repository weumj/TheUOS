package com.uoscs09.theuos2.parse;

import java.util.Collection;

/**
 * 필요한 정보를 parsing 하는 interface
 */
public interface IParser<Param, Return>  {

    /**
     * 주어진 정보를 parsing 한다.
     */
    Return parse(Param param) throws Throwable;

    interface IPostParsing {
        void afterParsing();
    }

    abstract class Base<Param, Return> implements IParser<Param, Return> {

        protected static boolean checkNull(Collection c) {
            return c == null || c.isEmpty();
        }

    }

}
