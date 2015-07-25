package com.uoscs09.theuos2.parse;

import com.uoscs09.theuos2.async.Processor;

/**
 * 필요한 정보를 parsing 하는 interface
 */
public interface IParser<Param, Return> extends Processor<Param, Return> {

    /**
     * 주어진 정보를 parsing 한다.
     */
    Return parse(Param param) throws Exception;

    interface AfterParsable {
        void afterParsing();
    }

    abstract class Base<Param, Return> implements IParser<Param, Return> {
        @Override
        public final Return process(Param param) throws Exception {
            return parse(param);
        }
    }

}
