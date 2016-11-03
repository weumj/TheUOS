package com.uoscs09.theuos2.util;

import android.util.SparseArray;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.http.NetworkRequests;
import com.uoscs09.theuos2.tab.announce.AnnounceItem;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.tab.booksearch.BookStateInfo;
import com.uoscs09.theuos2.tab.buildings.BuildingRoom;
import com.uoscs09.theuos2.tab.buildings.ClassroomTimeTable;
import com.uoscs09.theuos2.tab.emptyroom.EmptyRoom;
import com.uoscs09.theuos2.tab.libraryseat.SeatInfo;
import com.uoscs09.theuos2.tab.libraryseat.SeatItem;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.WeekRestItem;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;
import com.uoscs09.theuos2.tab.subject.CoursePlanItem;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
import com.uoscs09.theuos2.tab.timetable.SubjectInfoItem;
import com.uoscs09.theuos2.tab.timetable.Timetable2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;

public class AppRequests {

    public static class Announces {
        public static Task<List<AnnounceItem>> normalRequest(int category, int page) {
            return NetworkRequests.Announces.normalRequest(category, page);
            /*
                    .wrap(announceItems -> {
                        if (PrefHelper.Announces.isAnnounceExceptNoticeType()) {
                            final int size = announceItems.size();
                            for (int i = size - 1; i >= 0; i--) {
                                AnnounceItem item = announceItems.get(i);
                                if (item.isTypeNotice())
                                    announceItems.remove(i);
                            }
                        }

                        return announceItems;
                    });
                    */
        }

        public static Task<List<AnnounceItem>> searchRequest(int category, int pageIndex, String query) {
            return NetworkRequests.Announces.searchRequest(category, pageIndex, query);
            /*
                    .wrap(announceItems -> {
                        final int size = announceItems.size();
                        for (int i = size - 1; i >= 0; i--) {
                            AnnounceItem item = announceItems.get(i);
                            if (item.isTypeNotice())
                                announceItems.remove(i);
                        }

                        return announceItems;
                    });
                    */
        }
    }


    public static class Books {
        public static Task<List<BookStateInfo>> requestBookStateInfo(String url) {
            return NetworkRequests.Books.requestBookStateInfo(url);
        }

        public static Task<List<BookItem>> request(String query, int page, int os, int oi) {
            return NetworkRequests.Books.request(query, page, os, oi)
                    .wrap(originalList -> {
                                if (PrefHelper.Books.isFilterUnavailableBook() && originalList.size() > 0) {
                                    ArrayList<BookItem> newList = new ArrayList<>();
                                    String emptyMsg = AppUtil.context().getString(R.string.tab_book_not_found);
                                    final int N = originalList.size();
                                    for (int i = 0; i < N; i++) {
                                        BookItem item = originalList.get(i);
                                        if (item.isBookAvailable()) {
                                            newList.add(item);
                                        }
                                    }

                                    if (newList.size() == 0) {
                                        BookItem item = new BookItem();
                                        item.bookInfo = emptyMsg;
                                        newList.add(item);
                                    }

                                    return newList;
                                } else {
                                    return new ArrayList<>(originalList);
                                }
                            }
                    );
        }
    }


    public static class Restaurants {

        public static Task<SparseArray<RestItem>> request(boolean shouldForceUpdate) {
            return Tasks.newTask(() -> {
                if (!shouldForceUpdate && PrefHelper.Restaurants.isDownloadTimeWithin(3)) {
                    try {
                        SparseArray<RestItem> result = readFromFile();

                        if (result.size() > 0)
                            return result;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return NetworkRequests.Restaurants.request()
                        .wrap(restItemSparseArray -> {
                            SerializableArrayMap<Integer, RestItem> writingObject = SerializableArrayMap.fromSparseArray(restItemSparseArray);
                            IOUtil.writeObjectToInternalFile(IOUtil.FILE_REST, writingObject);
                            PrefHelper.Restaurants.putDownloadTime(OApiUtil.getDateTime());
                            return restItemSparseArray;
                        }).get();
            });
        }

        public static SparseArray<RestItem> readFromFile() {
            return SerializableArrayMap.toSparseArray(IOUtil.readInternalFileSilent(IOUtil.FILE_REST));
        }

        public static final String WEEK_FILE_NAME = "FILE_REST_WEEK_ITEM";

        public static Task<WeekRestItem> readWeekInfo(String code, boolean shouldUpdateUsingInternet) {

            return Tasks.newTask(() -> {
                // 이번주의 식단이 기록된 파일이 있으면, 인터넷에서 가져오지 않고 그 파일을 읽음
                if (!shouldUpdateUsingInternet && PrefHelper.Restaurants.isTodayWithinWeekItemFetchTime(code, OApiUtil.getDate())) {

                    WeekRestItem result = (WeekRestItem) IOUtil.internalFileOpenTask(WEEK_FILE_NAME + code).get();

                    if (result != null)
                        return result;
                }

                return NetworkRequests.Restaurants.requestWeekInfo(code)
                        .wrap(IOUtil.<WeekRestItem>newInternalFileWriteFunc(WEEK_FILE_NAME + code))
                        .wrap(item -> {
                            PrefHelper.Restaurants.putWeekItemFetchTime(code, item);
                            return item;
                        })
                        .get();
            });

        }

    }


    public static class TimeTables {

        public static Task<Timetable2> dummyRequest(CharSequence id, CharSequence passwd, OApiUtil.Semester semester, CharSequence year) {
            return Tasks.newTask(() -> {
                throw new Exception(String.format("Dummy Request - [\nid : %s\nsemester : %s\nyear : %s\n]", id, semester.name(), year));
            });
        }


        public static Task<Timetable2> request(CharSequence id, CharSequence passwd, OApiUtil.Semester semester, CharSequence year) {
            return NetworkRequests.TimeTables.request(id, passwd, semester, year)
                    .wrap(IOUtil.<Timetable2>newInternalFileWriteFunc(IOUtil.FILE_TIMETABLE));
        }

        public static Task<Timetable2> readFile() {
            return Tasks.newTask(() -> IOUtil.readInternalFileSilent(IOUtil.FILE_TIMETABLE));
        }
    }


    public static class LibrarySeats {
        public static Task<SeatInfo> request() {
            return NetworkRequests.LibrarySeats.request()
                    .wrap(seatInfo -> {
                        if (PrefHelper.LibrarySeats.isFilterOccupyingRoom()) {
                            ArrayList<SeatItem> list = seatInfo.seatItemList;
                            // 스터디룸 인덱스
                            final int[] filterArr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23, 24, 25, 26, 27, 28};
                            final int size = filterArr.length;
                            for (int i = size - 1; i > -1; i--) {
                                SeatItem item = list.get(filterArr[i]);
                                if (item.utilizationRate >= 50)
                                    list.remove(item);
                            }
                        }

                        return seatInfo;
                    });
        }
    }


    public static class EmptyRooms {
        public static Task<List<EmptyRoom>> request(String building, int time, int term) {
            return NetworkRequests.EmptyRooms.request(building, time, term);
        }
    }


    public static class Subjects {
        public static Task<List<SubjectItem2>> requestCulture(String year, int term, String subjectDiv, String subjectName) {
            return NetworkRequests.Subjects.requestCulture(year, term, subjectDiv, subjectName);
        }

        public static Task<List<SubjectItem2>> requestMajor(String year, int term, Map<String, String> majorParams, String subjectName) {
            return NetworkRequests.Subjects.requestMajor(year, term, majorParams, subjectName);
        }

        public static Task<List<CoursePlanItem>> requestCoursePlan(SubjectItem2 item) {
            return NetworkRequests.Subjects.requestCoursePlan(item);
        }

        public static Task<List<SubjectInfoItem>> requestSubjectInfo(String subjectName, int year, String termCode) {
            return NetworkRequests.Subjects.requestSubjectInfo(subjectName, year, termCode);
        }
    }


    public static class UnivSchedules {
        public static final String FILE_NAME = "file_univ_schedule_r1";

        public static Task<List<UnivScheduleItem>> request(boolean force) {
            return Tasks.newTask(() -> {
                if (PrefHelper.UnivSchedules.isMonthEqualToFetchMonth() && !force) {
                    try {
                        //noinspection unchecked
                        ArrayList<UnivScheduleItem> result = (ArrayList<UnivScheduleItem>) IOUtil.internalFileOpenTask(FILE_NAME).get();

                        if (result != null)
                            return result;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                return NetworkRequests.UnivSchedules.request()
                        .wrap(IOUtil.<List<UnivScheduleItem>>newInternalFileWriteFunc(AppRequests.UnivSchedules.FILE_NAME))
                        .wrap(univScheduleItems -> {
                            PrefHelper.UnivSchedules.putFetchMonth(univScheduleItems.get(0).getDate(true).get(Calendar.MONTH));
                            return univScheduleItems;
                        })
                        .get();
            }).wrap(univScheduleItems -> {
                Collections.sort(univScheduleItems, (lhs, rhs) -> {
                    int lDay = lhs.dateStart.day, rDay = rhs.dateStart.day;
                    if (lDay == rDay)
                        return 0;
                    else if (lDay > rDay)
                        return 1;
                    else
                        return -1;
                });

                return univScheduleItems;
            });
        }
    }

    public static class Buildings {
        private static final String FILE_BUILDINGS = "FILE_BUILDINGS";

        public static Task<BuildingRoom> buildingRooms(boolean forceDownload) {
            if (forceDownload)
                return downloadBuildingRooms();
            else
                return Tasks.newTask(() -> {
                    long downloadedTimeL = PrefHelper.Buildings.downloadTime();

                    if (downloadedTimeL > 0) {
                        Calendar current = Calendar.getInstance(), downloadedTime = Calendar.getInstance();
                        downloadedTime.setTimeInMillis(downloadedTimeL);

                        // 다운로드 한 날짜가 현재 시각보다 5개월 이전 이면
                        if (Math.abs(current.get(Calendar.MONTH) - downloadedTime.get(Calendar.MONTH)) >= 5) {
                            return downloadBuildingRooms().get();
                        }
                    }

                    try {
                        BuildingRoom buildingRoom = IOUtil.readFromInternalFile(FILE_BUILDINGS);
                        if (buildingRoom != null) {
                            buildingRoom.afterParsing();
                            return buildingRoom;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return downloadBuildingRooms().get();
                });
        }

        private static Task<BuildingRoom> downloadBuildingRooms() {
            return NetworkRequests.Buildings.buildingRooms()
                    .wrap(room -> {
                        PrefHelper.Buildings.putDownloadTime(System.currentTimeMillis());
                        return room;
                    })
                    .wrap(IOUtil.newInternalFileWriteFunc(FILE_BUILDINGS));
        }

        public static Task<ClassroomTimeTable> classRoomTimeTables(String year, String term, EmptyRoom emptyRoom, boolean forceDownload) {
            return buildingRooms(forceDownload).wrap(room -> {
                int findingRoomNum = Integer.valueOf(emptyRoom.roomNo.split("-")[0]);
                //todo search
                /* roomName 에 대해 정렬 되어있지 않으므로 사용 불가
                int findingRoomNum = Integer.valueOf(emptyRoom.roomNo.split("-")[0]);


                BuildingRoom.RoomInfo info = new BuildingRoom.RoomInfo("", emptyRoom.roomNo, "" + (findingRoomNum < 10 ? "0" + findingRoomNum : findingRoomNum));

                int index = Collections.binarySearch(room.getRoomInfoList(), info, (lhs, rhs) -> {
                    int buildingCodeCompared = lhs.buildingCode().compareTo(rhs.buildingCode());

                    if (buildingCodeCompared == 0) {
                        return lhs.roomName().compareTo(rhs.roomName());
                    } else
                        return buildingCodeCompared;
                });

                if (index != -1)
                    return classRoomTimeTables(year, term, room.getRoomInfoList().get(index)).get();
                */

                final String[] buildings = {
                        "01", "02", "03", "04", "05",
                        "06", "08", "09", "10", "11",
                        "13", "14", "15", "16", "17",
                        "18", "19", "20", "23", "24",
                        "25", "33"
                };

                for (String building : buildings) {
                    BuildingRoom.Pair pair = room.roomInfoList(building);
                    if (pair == null)
                        continue;

                    try {
                        if (Integer.valueOf(pair.buildingInfo().code()) != findingRoomNum)
                            continue;

                        for (BuildingRoom.RoomInfo info : pair.roomInfoList()) {
                            try {
                                if (info.roomName().contains(emptyRoom.roomNo))
                                    return classRoomTimeTables(year, term, info).get();
                            } catch (Exception e) {
                                //ignore
                            }
                        }

                    } catch (Exception e) {
                        //ignore
                    }

                }

                throw new IllegalArgumentException(AppUtil.context().getString(R.string.tab_empty_room_search_timetable_no_match));
            });
        }

        public static Task<ClassroomTimeTable> classRoomTimeTables(String year, String term, BuildingRoom.RoomInfo roomInfo) {
            return NetworkRequests.Buildings.classRoomTimeTables(year, term, roomInfo);
        }
    }

    public static class WiseScores {
        public static Task<com.uoscs09.theuos2.tab.score.WiseScores> wiseScores(String id, String pw) {
            return NetworkRequests.WiseScores.wiseScores(id, pw);
        }
    }
}