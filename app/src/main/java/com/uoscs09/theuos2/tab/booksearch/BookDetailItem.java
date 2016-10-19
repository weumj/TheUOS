package com.uoscs09.theuos2.tab.booksearch;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.uoscs09.theuos2.tab.booksearch.BookItem.BOOK_STATE_AVAILABLE;

public class BookDetailItem {

    public List<Pair<String, Object>> detailInfoList = new ArrayList<>();
    public RelationInfo relationInfo;

    public static class UrlObject{
        public final String info;
        public final String url;

        public UrlObject(String info, String url) {
            this.info = info;
            this.url = url;
        }
    }


    public static class RelationInfo{
        public String title;
        public List<SubRelationInfo> subRelationInfoList = new ArrayList<>();
    }

    public interface SubRelationInfo {

    }

    public static class LocationInfo implements SubRelationInfo{
        public String title;
        public List<String> infoList = new ArrayList<>();
        public int state;
        public String link;

        public boolean isBookAvailable() {
            return (state & BOOK_STATE_AVAILABLE) == BOOK_STATE_AVAILABLE;
        }

        public int bookStateStringRes(){
            return BookItem.bookStateStringRes(state);
        }
    }


}
