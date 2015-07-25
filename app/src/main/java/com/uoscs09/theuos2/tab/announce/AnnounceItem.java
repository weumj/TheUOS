package com.uoscs09.theuos2.tab.announce;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.StringUtil;

public class AnnounceItem implements Parcelable {
    public static final int TYPE_NOTICE = -1;
    public static final int TYPE_NORMAL = 1;

    public int type = TYPE_NORMAL;
    public int number = 0;
    public String title = StringUtil.NULL;
    public String date = StringUtil.NULL;
    public String pageURL = StringUtil.NULL;
    public String attachedFileUrl = StringUtil.NULL;

    public AnnounceItem(){
    }

    private AnnounceItem(Parcel source) {
        type = source.readInt();
        number = source.readInt();
        title = source.readString();
        date = source.readString();
        pageURL = source.readString();
        attachedFileUrl = source.readString();
    }

    /**
     * number / title / date / pageURL / attachedFileUrl
     */
    public String[] toStringArray(Context context) {
        return new String[]{getNumber(context), title, date, pageURL, attachedFileUrl};
    }

    public String getNumber(Context context) {
        if (type == TYPE_NOTICE)
            return context.getString(R.string.tab_announce_type_notice);
        else
            return Integer.toString(number);
    }

    public boolean isTypeNotice(){
        return type == TYPE_NOTICE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(number);
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(pageURL);
        dest.writeString(attachedFileUrl);
    }

    public static final Parcelable.Creator<AnnounceItem> CREATOR = new Parcelable.Creator<AnnounceItem>() {

        @Override
        public AnnounceItem[] newArray(int size) {
            return new AnnounceItem[size];
        }

        @Override
        public AnnounceItem createFromParcel(Parcel source) {
            return new AnnounceItem(source);
        }
    };
}
