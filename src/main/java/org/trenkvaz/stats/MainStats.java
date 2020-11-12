package org.trenkvaz.stats;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class MainStats {

    static final String[] positions_for_query = {null,"UTG","MP","CO","BU","SB","BB"};
    static final int UTG = 0, MP = 1, CO = 2, BU = 3, SB = 4, BB = 5;
    static final int indPoz1Limpera = 1, indKolLimpers = 2, indPozRaiser = 3, indPoz1Callera = 4, indKolCallers = 5, indPoz3beter = 6, indPoz1Caller3beta = 7, indKolCallers3beta = 8,
            indPoz4betera = 9, action = 0, FOLD = -10, LIMP = -1, RAISE = 2, CALL = -2, _3BET = 3, CALLv3BET = -3,
            _4BET = 4, CALLv4bet = -4, _5BET = 5, CALLv5bet = -5, CHEK = 10, raund_1 = 0, raund_2 = 1, raund_3 = 2;
    //static final StringBuilder[] type_data = {new StringBuilder("integer"),new StringBuilder("smallint")};
    /*private static final String[] rfi_3bet = {"rfi_v_3bet","integer[][][]"};
    private static final String[] v_rfi = {"v_rfi","integer[][][]"};
    private static final String[] vpip_pfr_3bet = {"vpip_pfr_3bet","integer[][]"};
    private static final String[] rfi = {"rfi","integer[][]"};
    private static final String[] alliners = {"alliners","integer[][]"};
    public static final String[][] list_of_stats = {{"IDplayer"},{"Nicks"},v_rfi,rfi_3bet,vpip_pfr_3bet,rfi,alliners};*/ // не забывать еще добавлять в Клиент Дата Баз получение стат
    public static long datehand =0;
    public static void setDate(long date1){datehand=date1;}

    public abstract String[] getName_of_stat();
    public abstract void count_Stats_for_map(byte[][][] actions_hand,int[] idplayers,float[]stacks,int idHero,byte Seaters,float[][] posaction,boolean isAdditional);
    public abstract HashMap getMap_of_Idplayer_stats();
    public abstract void setIdplayers_stats(Integer idplayer, Array statasql);



    public static int getUnknownId(float Stack){

        int[] Unknownstacks = {19,39,59,79,99,150,200};
        int res = 200;
        for(int i=0; i<7; i++){
            if(Stack>Unknownstacks[i])continue;
            res = Unknownstacks[i]; break;
        }

        return res;
    }


    public static void main(String[] args) {
        //MainStats[] stats = {new AgainstRFI(),new Against3bet()};
        //StringBuilder[] st = stats[0].getPositions_of_stats();
        //for(StringBuilder a:st) System.out.println(a);


    }
}
