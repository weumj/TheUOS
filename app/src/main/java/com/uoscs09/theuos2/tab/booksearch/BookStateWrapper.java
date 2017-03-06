package com.uoscs09.theuos2.tab.booksearch;

import android.support.annotation.Keep;

import com.uoscs09.theuos2.parse.IParser;

import java.util.List;

import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Keep
@Root(name = "location", charset = "utf-8")
public class BookStateWrapper implements IParser.IPostParsing {
    @ListContainer(name = "noholding")
    private List<BookStateInfo> bookStateInfo;

    public List<BookStateInfo> bookStateList() {
        return bookStateInfo;
    }

    @Override
    public void afterParsing() {
        if (bookStateInfo != null)
            for (BookStateInfo info : bookStateInfo) {
                info.afterParsing();
            }
    }
}
