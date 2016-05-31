package com.uoscs09.theuos2.tab.buildings;

import java.io.Serializable;
import java.util.List;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Root(name = "root", charset = "euc-kr")
public class BuildingRoom implements Serializable {

    private static final long serialVersionUID = -2868782434037894130L;

    @ListContainer(name = "bdList")
    private List<BuildingInfo> buildingInfoList;
    @ListContainer(name = "roomList")
    private List<RoomInfo> roomInfoList;

    public List<BuildingInfo> getBuildingInfoList() {
        return buildingInfoList;
    }

    public List<RoomInfo> getRoomInfoList() {
        return roomInfoList;
    }

    @Root(name = "list")
    public static class BuildingInfo implements Serializable {

        private static final long serialVersionUID = -8461609949183019523L;

        @Element(name = "building", cdata = true)
        private String code;
        @Element(name = "building_nm", cdata = true)
        private String name;

        public String code() {
            return code;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return String.format("Building [%s] : %s", code, name);
        }
    }

    @Root(name = "list")
    public static class RoomInfo implements Serializable {

        private static final long serialVersionUID = 8024683345112693051L;

        @Element(name = "room_cd", cdata = true)
        private String code;

        @Element(name = "room_nm", cdata = true)
        private String name;

        @Element(name = "building", cdata = true)
        private String building;

        public String code() {
            return code;
        }

        public String name() {
            return name;
        }

        public String building() {
            return building;
        }

        @Override
        public String toString() {
            return String.format("Room [%s] : %s , Building : %s", code, name, building);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof RoomInfo) {
                RoomInfo info = (RoomInfo) o;
                return info.name.equals(this.name) && info.building.equals(this.building) && info.code.equals(this.code);
            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + code.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + building.hashCode();
            return result;
        }
    }
}
