package com.uoscs09.theuos2.parse;

import java.io.InputStream;

public class XmlParser2<T> extends mj.android.utils.xml.XmlParser<T> implements IParser<InputStream, T> {
    public XmlParser2(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public T parse(InputStream is) throws Exception {
        T t = super.parse(is);

        if (t instanceof IPostParsing)
            ((IPostParsing) t).afterParsing();

        return t;
    }

    @Override
    public final T func(InputStream param) throws Throwable {
        return parse(param);
    }
}
