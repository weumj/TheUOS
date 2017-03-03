package com.uoscs09.theuos2.appwidget.libraryseat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsListRemoteViewsFactory;
import com.uoscs09.theuos2.tab.libraryseat.SeatInfo;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.CollectionUtil;

import java.util.Arrays;
import java.util.List;

public class LibrarySeatListService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this, intent);
    }

    private static class ListRemoteViewsFactory extends AbsListRemoteViewsFactory<SeatInfo> {
        private static final int[] STUDY_ROOM_INDEX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23, 24, 25, 26, 27, 28};
        private final int mColorRed, mColorGreen;

        ListRemoteViewsFactory(Context context, Intent intent) {
            super(context, intent);
            List<SeatInfo> extraList = intent.getBundleExtra(LibrarySeatWidget.LIBRARY_SEAT_WIDGET_DATA).getParcelableArrayList(LibrarySeatWidget.LIBRARY_SEAT_WIDGET_DATA);
            if (!CollectionUtil.isEmpty(extraList)) {
                clear();
                addAll(extraList);
            }
            mColorRed = ContextCompat.getColor(context, R.color.material_red_400);
            mColorGreen = ContextCompat.getColor(context, R.color.material_green_700);

        }

        @Override
        public RemoteViews getViewAt(int position) {
            SeatInfo item = getItem(position);
            RemoteViews rv = new RemoteViews(getContext().getPackageName(), R.layout.list_layout_widget_library_seat);

            String room = item.roomName;
            if (room.contains("전문"))
                room = room.replace("전문", "");
            rv.setTextViewText(android.R.id.text1, room);

            int vacancySeatCount;
            if (item.vacancySeat.trim().equals("")) {
                vacancySeatCount = 0;
            } else {
                try {
                    vacancySeatCount = Integer.parseInt(item.vacancySeat.trim());
                } catch (Exception e) {
                    vacancySeatCount = 0;
                }
            }

            int occupySeatCount;
            if (item.occupySeat.trim().equals("")) {
                occupySeatCount = 0;
            } else {
                try {
                    occupySeatCount = Integer.parseInt(item.occupySeat.trim());
                } catch (Exception e) {
                    occupySeatCount = 0;
                }
            }

            int size = vacancySeatCount + occupySeatCount;
            rv.setTextViewText(android.R.id.text2, item.vacancySeat);
            rv.setTextViewText(android.R.id.summary, "/" + size);

            boolean inStudyRoom = Arrays.binarySearch(STUDY_ROOM_INDEX, position) > -1;

            int color;
            try {
                color = item.utilizationRate < (inStudyRoom ? 50 : 70) ? mColorGreen : mColorRed;
            } catch (Exception e) {
                color = mColorGreen;
            }

            rv.setTextColor(android.R.id.text2, color);
            rv.setTextColor(android.R.id.summary, color);

            if (color == mColorRed && inStudyRoom) {
                rv.setTextColor(android.R.id.text1, Color.LTGRAY);
            } else {
                rv.setTextColor(android.R.id.text1, Color.DKGRAY);
            }


            //Bundle extras = new Bundle();
            //extras.putSerializable(LibrarySeatWidget.LIBRARY_SEAT_WIDGET_DATA,
            //		(Serializable) item);
            //Intent fillInIntent = new Intent();
            //fillInIntent
            //		.setAction(LibrarySeatWidget.LIBRARY_SEAT_WIDGET_ACTIVITY);
            //fillInIntent.putExtras(extras);
            //rv.setOnClickFillInIntent(android.R.id.widget_frame, fillInIntent);

            return rv;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onDataSetChanged() {
            super.onDataSetChanged();
            clear();

            AppRequests.LibrarySeats
                    .readFile()
                    .subscribe(this::addAll, Throwable::printStackTrace);

        }

    }
}
