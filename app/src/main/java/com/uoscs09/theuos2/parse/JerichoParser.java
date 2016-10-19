package com.uoscs09.theuos2.parse;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

/**
 * JerichoParser 를 사용하는 파서
 */
public abstract class JerichoParser<T> extends IParser.Base<String, T> {

    public T parse(String param) throws Throwable {
        return parseHtmlBody(new Source(param));
    }

    protected abstract T parseHtmlBody(Source source) throws Throwable;

    protected final static class Values {
        public static String string(Element e) {
            return e.getTextExtractor().toString();
        }

        public static int intVal(Element e) {
            return intVal(string(e));
        }

        public static int intVal(String s) {
            try {
                return Integer.parseInt(s);
            } catch (Exception ee) {
                return -1;
            }
        }

        public static float floatVal(Element e) {
            return floatVal(string(e));
        }

        public static float floatVal(String s) {
            try {
                return Float.parseFloat(s);
            } catch (Exception ee) {
                return -1;
            }
        }
    }
}
