package org.trenkvaz.newstats;

import org.trenkvaz.database_hands.Work_DataBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.trenkvaz.database_hands.Work_DataBase.close_DataBase;
import static org.trenkvaz.main.OCR.FLOP;
import static org.trenkvaz.newstats.WorkStats.procents;

public class AddNewFilterStata {














    static void addOldFilterStats(){

        WorkStats workStats1 = new WorkStats("addAndCountNewStats");
        WorkStats.isRecoverStats = true;
        FilterStata filterStata = new FilterStata.Builder().setMainNameFilter("main_wsd_").setPosStata(new int[]{1,1,1,1,1,1},new int[][]{{1,1,1,1,1,1}}).setSpecStats(2).build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter("main_wtsd_").setPosStata(new int[]{1,1,1,1,1,1},new int[][]{{1,1,1,1,1,1}}).setSpecStats(1).build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter("main_wwsf_").setPosStata(new int[]{1,1,1,1,1,1},new int[][]{{1,1,1,1,1,1}}).setSpecStats(0).build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter("main_vpip_pfr_").setPosStata(new int[]{1,1,1,1,1,1},new int[][]{{1,1,1,1,1,1}}).setSpecStats(3).
                isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);

        List<int[]> conditionsPreflopActions = new ArrayList<>();
        // ACT_PLAYER = 0,  LIMP = 1, LIMPS = 2, RAISER = 3, CALLERS = 4,  _3BET = 5, CALLERS_3BET= 6, _4BET = 7, CALLERS_4BET= 8, _5BET = 9, CALLERS_5BET= 10;
        String nameStata = "";
        // СДЕЛАТЬ СТАТЫ ДОСТУПНЫМИ ДЛЯ РАСЧЕТА ВО ВРЕМЯ ИГРЫ !!!

        conditionsPreflopActions.add(new int[]{0,-1,-1, 2, -1,-1,-1,-1,-1,-1,-1});
        nameStata = "v_rfi_";
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,1,0,0,0,0},new int[][]{{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,1,0,0},new int[][]{{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,1},new int[][]{{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,1,0,0},new int[][]{{0,1,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,1},new int[][]{{0,1,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,1,0,0},new int[][]{{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{0,0,0,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,0,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,0,0,1,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);



        conditionsPreflopActions = new ArrayList<>();
        conditionsPreflopActions.add(new int[]{3,-1,-1, 2, -1,-1,-1,-1,-1,-1,-1});
        conditionsPreflopActions.add(new int[]{0,-1,-1, -1, -1,-1,-1,2,-1,-1,-1});
        nameStata = "v4bet_";
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,1,0,0,0,0},new int[][]{{1,0,0,0,0,0},{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,1,0,0},new int[][]{{1,0,0,0,0,0},{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,1},new int[][]{{1,0,0,0,0,0},{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,1,0,0},new int[][]{{0,1,0,0,0,0},{0,1,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,1},new int[][]{{0,1,0,0,0,0},{0,1,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,1,0,0},new int[][]{{0,0,1,0,0,0},{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{0,0,1,0,0,0},{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,1,0,0,0},{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{0,0,0,1,0,0},{0,0,0,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,0,1,0,0},{0,0,0,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,0,0,1,0},{0,0,0,0,1,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);


        conditionsPreflopActions = new ArrayList<>();
        conditionsPreflopActions.add(new int[]{0,-1,-1, -1, -1,-1,-1,-1,-1,-1,-1});
        nameStata = "rfi_";
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{1,0,0,0,0,0},new int[][]{{1,1,1,1,1,1}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,1,0,0,0,0},new int[][]{{1,1,1,1,1,1}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,0,0,0},new int[][]{{1,1,1,1,1,1}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,1,0,0},new int[][]{{1,1,1,1,1,1}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{1,1,1,1,1,1}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);


        conditionsPreflopActions = new ArrayList<>();
        conditionsPreflopActions.add(new int[]{2,-1,-1, -1, -1,-1,-1,-1,-1,-1,-1});
        conditionsPreflopActions.add(new int[]{0,-1,-1, -1, -1,2,-1,-1,-1,-1,-1});
        nameStata = "v3bet_";
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{1,0,0,0,0,0},new int[][]{{1,1,1,1,1,1},{0,1,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{1,0,0,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,1,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{1,0,0,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,1,1}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,1,0,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,1,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,1,0,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,1,1}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,1,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,0,1}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,1,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,1,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,1,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,0,1}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,0,1}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);



        // close_DataBase();
    }



    static void addNewFilterStats(){

        WorkStats workStats1 = new WorkStats(false);
        WorkStats.isRecoverStats = true;
        List<int[]> conditionsPreflopActions = new ArrayList<>();
        List<int[]> conditionsFlopActions = new ArrayList<>();
        // ACT_PLAYER = 0,  LIMP = 1, LIMPS = 2, RAISER = 3, CALLERS = 4,  _3BET = 5, CALLERS_3BET= 6, _4BET = 7, CALLERS_4BET= 8, _5BET = 9, CALLERS_5BET= 10;
        String nameStata = "";
        // СДЕЛАТЬ СТАТЫ ДОСТУПНЫМИ ДЛЯ РАСЧЕТА ВО ВРЕМЯ ИГРЫ !!!

        conditionsPreflopActions.add(new int[]{2,-1,-1, -1, -1,-1,-1,-1,-1,-1,-1});
        conditionsPreflopActions.add(new int[]{0,-1,-1, -1, 2,-1,-1,-1,-1,-1,-1});
        conditionsFlopActions.add(new int[]{0,10});
        nameStata = "sraisepot_vs_caller_flop_ip_";
        FilterStata filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{1,1,0,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,1,1}}).setStreetOfActs(FLOP).
                        setConditionsPreflopActions(conditionsPreflopActions).setConditionsPostFlopActions(conditionsFlopActions,FLOP).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);

        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,1,1}}).setStreetOfActs(FLOP).
                        setConditionsPreflopActions(conditionsPreflopActions).setConditionsPostFlopActions(conditionsFlopActions,FLOP).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);

        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,1,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,1,1}}).setStreetOfActs(FLOP).
                        setConditionsPreflopActions(conditionsPreflopActions).setConditionsPostFlopActions(conditionsFlopActions,FLOP).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);


        conditionsFlopActions = new ArrayList<>();
        conditionsFlopActions.add(new int[]{0,0});
        nameStata = "sraisepot_vs_caller_flop_op_";
        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{1,1,0,0,0,0},new int[][]{{1,1,1,1,1,1},{0,1,1,0,0,0}}).setStreetOfActs(FLOP).
                        setConditionsPreflopActions(conditionsPreflopActions).setConditionsPostFlopActions(conditionsFlopActions,FLOP).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);

        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{1,1,0,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,1,0,0}}).setStreetOfActs(FLOP).
                        setConditionsPreflopActions(conditionsPreflopActions).setConditionsPostFlopActions(conditionsFlopActions,FLOP).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);

        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,0,0,0},new int[][]{{1,1,1,1,1,1},{0,0,0,1,0,0}}).setStreetOfActs(FLOP).
                        setConditionsPreflopActions(conditionsPreflopActions).setConditionsPostFlopActions(conditionsFlopActions,FLOP).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);

        filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{1,1,1,1,1,1},{0,0,0,0,0,1}}).setStreetOfActs(FLOP).
                        setConditionsPreflopActions(conditionsPreflopActions).setConditionsPostFlopActions(conditionsFlopActions,FLOP).isAllowInGame().build();
        workStats1.createOneNewStata(filterStata);

    }


    static void testGetStata(){

        WorkStats  workStats = new WorkStats(false);
        workStats.fullMapNicksMapsNameFilterDataStata("work_");

       /* squeeze_co_v_utg_
                squeeze_bu_v_utg_mp_
        squeeze_sb_v_co_
                squeeze_sb_v_utg_mp_
        squeeze_bb_v_co_
                squeeze_bb_v_utg_mp_
        squeeze_bb_v_bu_*/

        //int[] stats = workStats.getValueOneStata("trenkvaz","main_vpip_pfr_all_v_all",8);

        int[] stats = workStats.getValueOneStata("trenkvaz","sraisepot_vs_caller_flop_op_sb_v_all_v_bb_",0);
        //System.out.println("stata "+stats[0]+" "+stats[1]+" ");
        System.out.println("stata "+stats[0]+" "+stats[1]+" "+stats[2]);
        System.out.println("vpip "+procents(stats[1],stats[0])+" pfr "+procents(stats[2],stats[0]));
    }

    static void getNamesFilterStats(){
        WorkStats  workStats = new WorkStats("stats");
        for(Map.Entry<String,FilterStata> entry:workStats.statsMap.entrySet())
            System.out.println(entry.getKey());
    }

    public static void main(String[] args) {

        getNamesFilterStats();

       // new Work_DataBase();


        //addOldFilterStats();
        //addNewFilterStats();

       // testGetStata();

        //close_DataBase();
    }
}
