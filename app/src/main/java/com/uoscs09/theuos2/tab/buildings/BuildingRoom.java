package com.uoscs09.theuos2.tab.buildings;

import android.support.annotation.Nullable;

import com.uoscs09.theuos2.annotation.KeepName;
import com.uoscs09.theuos2.parse.IParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Root(name = "root", charset = "euc-kr")
public class BuildingRoom implements Serializable, IParser.IPostParsing {

    private static final long serialVersionUID = -2868782434037894130L;

    @ListContainer(name = "bdList")
    private List<BuildingInfo> buildingInfoList;
    @ListContainer(name = "roomList")
    private List<RoomInfo> roomInfoList;


    private transient Map<String, Pair> buildingRoomMap = new Hashtable<>(100);

    private static int compareInt(int lhs, int rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    @Override
    public void afterParsing() {
        if (buildingRoomMap == null) { // init from Serializable
            buildingRoomMap = new Hashtable<>(100);
        } else { // init from parser
            Collections.sort(roomInfoList, (lhs, rhs) -> {
                int buildingCodeCompared = compareInt(lhs.buildingCodeInt(), rhs.buildingCodeInt());

                if (buildingCodeCompared == 0)
                    return lhs.roomName().compareTo(rhs.roomName());
                else
                    return buildingCodeCompared;
            });
        }

        int currentRoomInfoListIndex = 0;

        final int N = roomInfoList.size();
        for (BuildingInfo buildingInfo : buildingInfoList) {
            int i = currentRoomInfoListIndex;
            for (; i < N; i++) {
                RoomInfo roomInfo = roomInfoList.get(i);
                if (!roomInfo.buildingCode.equals(buildingInfo.buildingNumber)) {
                    break;
                }
            }

            buildingRoomMap.put(buildingInfo.buildingNumber, new Pair(buildingInfo, new ArrayList<>(roomInfoList.subList(currentRoomInfoListIndex, i))));

            currentRoomInfoListIndex = i;
        }

    }

    /**
     * @param buildingNumber ex) 01
     */
    @Nullable
    public Pair roomInfoList(String buildingNumber) {
        return buildingRoomMap.get(buildingNumber);
    }


    public int size() {
        return buildingRoomMap.size();
    }

    public List<RoomInfo> roomInfoList() {
        return roomInfoList;
    }

    @Root(name = "list")
    public static class BuildingInfo implements Serializable {

        private static final long serialVersionUID = -8461609949183019523L;

        @Element(name = "building", cdata = true)
        private String buildingNumber;
        @Element(name = "building_nm", cdata = true)
        private String name;

        public String code() {
            return buildingNumber;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return String.format("Building [%s] : %s", buildingNumber, name);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + buildingNumber.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof BuildingInfo))
                return false;

            BuildingInfo b = (BuildingInfo) o;
            return this.buildingNumber.equals(b.buildingNumber) && this.name.equals(b.name);
        }
    }

    @KeepName
    @Root(name = "list")
    public static class RoomInfo implements Serializable {

        private static final long serialVersionUID = 8024683345112693051L;

        public RoomInfo(String code, String name, String buildingCode) {
            this.code = code;
            this.name = name;
            this.buildingCode = buildingCode;
        }

        public RoomInfo() {
        }

        @Element(name = "room_cd", cdata = true)
        private String code; // ex) 010001

        @Element(name = "room_nm", cdata = true)
        private String name;

        @Element(name = "building", cdata = true)
        private String buildingCode; // ex) 01

        public String code() {
            return code;
        }

        public String roomName() {
            return name;
        }

        public String buildingCode() {
            return buildingCode;
        }

        private transient int buildingCodeInt = 0;

        public int buildingCodeInt() {
            if (buildingCodeInt < 1)
                buildingCodeInt = Integer.parseInt(buildingCode);

            return buildingCodeInt;
        }

        @Override
        public String toString() {
            return String.format("Room [%s] : %s , Building : %s", code, name, buildingCode);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof RoomInfo) {
                RoomInfo info = (RoomInfo) o;
                return info.name.equals(this.name) && info.buildingCode.equals(this.buildingCode) && info.code.equals(this.code);
            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + code.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + buildingCode.hashCode();
            return result;
        }
    }

    public static class Pair implements Serializable {

        private static final long serialVersionUID = 794684982528501018L;

        private BuildingInfo first;
        private List<RoomInfo> second;

        public Pair(BuildingInfo first, List<RoomInfo> second) {
            this.first = first;
            this.second = second;
        }

        public BuildingInfo buildingInfo() {
            return first;
        }

        public List<RoomInfo> roomInfoList() {
            return second;
        }
    }
}
