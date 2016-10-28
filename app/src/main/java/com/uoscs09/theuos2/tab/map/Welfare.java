package com.uoscs09.theuos2.tab.map;


import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.OApiUtil.UnivBuilding;
/**
 * enum 순서는 R.array.tab_map_buildings_welfare 을 따름.
 * */
enum Welfare {
    CASH(
            new UnivBuilding[]{
                    UnivBuilding.University_Center,
                    UnivBuilding.Natural_Science,
                    UnivBuilding.Student_Hall,
                    UnivBuilding.The_21st_Century,
                    UnivBuilding.Library,
            },
            R.string.tab_map_buildings_welfare_cash
    ),
    BANK(
            new UnivBuilding[]{
                    UnivBuilding.University_Center,
            },
            R.string.tab_map_buildings_welfare_bank
    ),
    COPY(
            new UnivBuilding[]{
                    UnivBuilding.Student_Hall,
                    UnivBuilding.Library
            },
            R.string.tab_map_buildings_welfare_copy
    ),
    PRINT(
            new UnivBuilding[]{
                    UnivBuilding.Liberal_Arts,
                    UnivBuilding.The_21st_Century,
                    UnivBuilding.Law,
                    UnivBuilding.Library,
                    UnivBuilding.Mirae,
                    UnivBuilding.International,
            },
            R.array.tab_map_buildings_welfare_print,
            true
    ),
    SEARCH(
            new UnivBuilding[]{
                    UnivBuilding.Architecture_and_CivilEngineering,
                    UnivBuilding.Changgong,
                    UnivBuilding.Natural_Science,
                    UnivBuilding.Science_and_Technology,
                    UnivBuilding.IT,
                    UnivBuilding.Library,
                    UnivBuilding.Dormitory,
            },
            R.string.tab_map_buildings_welfare_search
    ),
    REST(
            new UnivBuilding[]{
                    UnivBuilding.Architecture_and_CivilEngineering,
                    UnivBuilding.Liberal_Arts,
                    UnivBuilding.Baebong,
                    UnivBuilding.Natural_Science,
                    UnivBuilding.Student_Hall,
                    UnivBuilding.The_21st_Century,
                    UnivBuilding.Design_and_Sculpture,
                    UnivBuilding.IT,
                    UnivBuilding.Law,
                    UnivBuilding.Library,
                    UnivBuilding.Dormitory,
                    UnivBuilding.Mirae,
            },
            R.string.tab_map_buildings_welfare_rest
    ),
    ELEVATOR(
            new UnivBuilding[]{
                    UnivBuilding.Architecture_and_CivilEngineering,
                    UnivBuilding.Liberal_Arts,
                    UnivBuilding.Baebong,
                    UnivBuilding.University_Center,
                    UnivBuilding.Student_Hall,
                    UnivBuilding.Science_and_Technology,
                    UnivBuilding.The_21st_Century,
                    UnivBuilding.IT,
                    UnivBuilding.Law,
                    UnivBuilding.Library,
                    UnivBuilding.Dormitory,
                    UnivBuilding.International,
            },
            R.string.tab_map_buildings_welfare_elevator
    ),
    HEALTH_CENTER(
            new UnivBuilding[]{
                    UnivBuilding.Dormitory,
                    UnivBuilding.Wellness,
            },
            R.string.tab_map_buildings_welfare_health_center
    ),
    POST(
            new UnivBuilding[]{
                    UnivBuilding.Student_Hall,
            },
            R.string.tab_map_buildings_welfare_post
    ),
    RESTAURANT(
            new UnivBuilding[]{
                    UnivBuilding.University_Center,
                    UnivBuilding.Natural_Science,
                    UnivBuilding.Student_Hall,
                    UnivBuilding.International,
            },
            R.string.tab_map_buildings_welfare_restaurant
    ),
    FAST_FOOD(
            new UnivBuilding[]{
                    UnivBuilding.Student_Hall,
            },
            R.string.tab_map_buildings_welfare_fast_food
    ),
    STAND(
            new UnivBuilding[]{
                    UnivBuilding.Student_Hall,
                    UnivBuilding.Library,
                    UnivBuilding.Mirae,
            },
            R.string.tab_map_buildings_welfare_stand
    ),
    EYE(
            new UnivBuilding[]{
                    UnivBuilding.Student_Hall,
            },
            R.string.tab_map_buildings_welfare_eye
    ),
    BOOK(
            new UnivBuilding[]{
                    UnivBuilding.Student_Hall,
            },
            R.string.tab_map_buildings_welfare_book
    ),

    WRITING(
            new UnivBuilding[]{
                    UnivBuilding.Student_Hall,
            },
            R.string.tab_map_buildings_welfare_writing
    ),

    SOUVENIR(
            new UnivBuilding[]{
                    UnivBuilding.Student_Hall,
            },
            R.string.tab_map_buildings_welfare_souvenir
    ),

    HEALTH(
            new UnivBuilding[]{
                    UnivBuilding.Dormitory,
                    UnivBuilding.Wellness,
            },
            R.string.tab_map_buildings_welfare_health
    ),

    TENNIS(
            new UnivBuilding[]{
                    UnivBuilding.Wellness,
            },
            R.string.tab_map_buildings_welfare_tennis
    );

    final UnivBuilding[] univBuildings;
    final int descriptionRes;
    final boolean isArrayRes;

    Welfare(UnivBuilding[] univBuildings, int descriptions) {
        this.univBuildings = univBuildings;
        this.descriptionRes = descriptions;
        isArrayRes = false;
    }

    Welfare(UnivBuilding[] univBuildings, int descriptions, boolean isArrayRes) {
        this.univBuildings = univBuildings;
        this.descriptionRes = descriptions;
        this.isArrayRes = isArrayRes;
    }
}
