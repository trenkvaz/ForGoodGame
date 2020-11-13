package org.trenkvaz.database_hands;

import org.trenkvaz.stats.MainStats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Array;
import java.sql.SQLException;
import java.util.*;

//import static org.trenkvaz.database_hands.GetNicksForHands.reverse_MapIdplayersNicks;
import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.main.CaptureVideo.nick_hero;

public class ReadHistoryGetStats {

    //static Map<String,Integer> map_nicks_idplayers;
    static List<List<Float>> preflop_actions = new ArrayList<>(6);
    static String[] nicks = new String[6]; static float[] stacks = new float[6];
    static int[] id_players = new int[6];
    static final String summary = "** Summary", dealing_flop = "** Dealing Flop", folds = " folds ", calls = " calls ", raises = " raises ", checks = " checks ";
    static float[][] posActions;
    static byte[][][] preflop_players_actions_in_raunds;
    static MainStats[] mainstats;

    static {  for(int f=0; f<6; f++){ preflop_actions.add(new ArrayList<Float>()); preflop_actions.get(f).add(0.0f); } }


    static void start_ReadFilesInFolder(String folder){
        for(File a: Objects.requireNonNull(new File(folder).listFiles())){
            if(a.isFile()&&a.getName().endsWith(".txt")){
                read_File(a.getPath());
            }
        }
    }


    private static void read_File(String files){
        final String start_line_of_hand = "***** Hand History";
        List<String> hand = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(files));
            String line;
            while ((line = br.readLine()) != null) {
                if(line.length()==0)continue;
                if(line.contains(start_line_of_hand)){
                    if(hand!=null) read_HandHistory(hand);
                    hand = new ArrayList<>();
                    hand.add(line);
                } else if(hand!=null)hand.add(line);
            }
            if(hand!=null) read_HandHistory(hand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void read_HandHistory(List<String> hand){
     float bb = read_BB(hand.get(1));
     read_StacksAndNicks(hand,bb);
     read_PreflopActions(hand,bb);
     //get_Idplayers();
     /*int position_hero = Arrays.asList(nicks).indexOf(nick_hero);
     if(position_hero!=-1)*/
     for(MainStats stats:mainstats)
         stats.count_Stats_for_map(preflop_players_actions_in_raunds,nicks,stacks,(byte) 6,posActions,false);

     // byte[][][] actions_hand,int[] idplayers,float[]stacks,int idHero,byte Seaters,float[][][]posactions,boolean isAdditional

     //test_show();


     clear_UsedArrays();

    }


    private static void read_PreflopActions(List<String> hand,float bb){

        for(int i_line = 13; i_line<hand.size(); i_line++){
            if(hand.get(i_line).startsWith(summary)||hand.get(i_line).startsWith(dealing_flop))break;
            for(int i_nick = 0; i_nick<6; i_nick++){
                if(hand.get(i_line).startsWith(nicks[i_nick])){
                     if(hand.get(i_line).contains(nicks[i_nick]+folds)) preflop_actions.get(i_nick).add(Float.NEGATIVE_INFINITY);
                     if(hand.get(i_line).contains(nicks[i_nick]+calls)) preflop_actions.get(i_nick).add(new BigDecimal((-Float.parseFloat(hand.get(i_line).
                             substring(nicks[i_nick].length()+8).replaceAll("[^0-9?!.]","")))/bb)
                             .setScale(2, RoundingMode.HALF_UP).floatValue());
                    if(hand.get(i_line).contains(nicks[i_nick]+raises)) preflop_actions.get(i_nick).add(new BigDecimal((Float.parseFloat(hand.get(i_line).
                            substring(hand.get(i_line).lastIndexOf(" to ")+4).replaceAll("[^0-9?!.]","")))/bb)
                            .setScale(2, RoundingMode.HALF_UP).floatValue());
                    if(hand.get(i_line).contains(nicks[i_nick]+checks)) preflop_actions.get(i_nick).add(Float.POSITIVE_INFINITY);
                }
            }
        }
        posActions = new float[6][];
        for (int v=0; v<6; v++){
            if(preflop_actions.get(v)!=null){
                if(preflop_actions.get(v).size()==0) {posActions[v] = null; continue;}
                posActions[v] = new float[preflop_actions.get(v).size()];
                for (int p=0; p<preflop_actions.get(v).size(); p++) posActions[v][p] = preflop_actions.get(v).get(p);
            }
        }

        preflop_players_actions_in_raunds = precount_to_PreflopActions(6,posActions);
    }


    static void test_show(){
        for(int i=0; i<6; i++){
            System.out.print(nicks[i]+"   ");
            for(int a=0; a<preflop_actions.get(i).size(); a++)
                System.out.print(preflop_actions.get(i).get(a)+" ");
            System.out.println();
        }
        System.out.println("===============================================");
    }


    private static void read_StacksAndNicks(List<String> hand, float bb){
        int position = -1;
        for (int i=0; i<6; i++) {
            if(i<3)position = i+3;
            else position = i-3;
            String line = hand.get(i+4);
            stacks[position]  = new BigDecimal(Float.parseFloat(line.substring(line.indexOf("$")+1,line.indexOf(")")))/bb).setScale(1, RoundingMode.HALF_UP).floatValue();
            nicks[position] = line.substring(8,line.indexOf("(")-1);
            //System.out.println("*"+nicks[position]+"*  "+stacks[position]);
        }
        //System.out.println("========================================================");
    }



    private static float read_BB(String line){ return Float.parseFloat(line.substring(line.indexOf("/")+1,line.indexOf(" "))); }



    //private static void get_Idplayers(){ for(int i=0; i<6; i++) if(map_nicks_idplayers.containsKey(nicks[i])) id_players[i] = map_nicks_idplayers.get(nicks[i]); }




    public static byte[][][] precount_to_PreflopActions(int Countplayers,float[][] posActions){

        float[][] PlayersActions = new float[7][1];
        int Raunds = 0;
        for(int i=0; i<6; i++){
            if(posActions[i]==null)continue;
            float[] playeraction = posActions[i];
            int pr = playeraction.length;
            if(pr>Raunds) Raunds=pr;
            PlayersActions[i+1] = playeraction;
        }
        --Raunds;

        int[][] RaundsActions = new int[Raunds][7];
        int Raise = 1;
        int popravka = 7-Countplayers;
        for(int pz=0; pz<Raunds; pz++){
            for(int p=popravka; p<7; p++){
                if(PlayersActions[p].length-1>pz){
                    if(PlayersActions[p][pz+1]==Float.NEGATIVE_INFINITY) RaundsActions[pz][p]= -10;
                    if(PlayersActions[p][pz+1]==Float.POSITIVE_INFINITY) RaundsActions[pz][p]= 10;
                    if(PlayersActions[p][pz+1]!=Float.NEGATIVE_INFINITY&&PlayersActions[p][pz+1]<0) RaundsActions[pz][p]= -(Raise);
                    if(PlayersActions[p][pz+1]!=Float.POSITIVE_INFINITY&&PlayersActions[p][pz+1]>0)
                        if(Raise==5)RaundsActions[pz][p]= Raise; else RaundsActions[pz][p]= ++Raise;
                }
            }
        }

        byte KolLimpers = 0, KolCallers = 0, KolCallers3beta = 0;
        byte[][][] BasaRaunds = new byte[Raunds][7][10];
        byte[] GeneralSituation = new byte[9];
        final byte FOLD = -10, LIMP = -1, RAISE = 2, CALL = -2, _3BET = 3, CALLv3BET = -3, _4BET = 4, CALLv4bet = -4, _5BET = 5, CALLv5bet = -5, CHEK = 10;
        final int indAction = 0, indPoz1Limpera = 0, indKolLimpers = 1, indPozRaiser = 2, indPoz1Callera = 3, indKolCallers = 4, indPoz3beter = 5, indPoz1Caller3beta = 6, indKolCallers3beta = 7, indPoz4betera = 8;
        for(int r=0; r<Raunds; r++){
            for(int p=popravka; p<7; p++){
                int D = RaundsActions[r][p];
                // System.out.print(" d "+D);
                switch (D) {
                    case -10 -> {
                        BasaRaunds[r][p][indAction] = FOLD;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                    }
                    case 10 -> {
                        BasaRaunds[r][p][indAction] = CHEK;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                        if (KolLimpers == 0) GeneralSituation[indPoz1Limpera] = (byte) p;
                        GeneralSituation[indKolLimpers] = ++KolLimpers;
                    }
                    case -1 -> {
                        BasaRaunds[r][p][indAction] = LIMP;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                        if (KolLimpers == 0) GeneralSituation[indPoz1Limpera] = (byte) p;
                        GeneralSituation[indKolLimpers] = ++KolLimpers;
                    }
                    case 2 -> {
                        BasaRaunds[r][p][indAction] = RAISE;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                        GeneralSituation[indPozRaiser] = (byte) p;
                    }
                    case -2 -> {
                        BasaRaunds[r][p][indAction] = CALL;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                        if (KolCallers == 0) GeneralSituation[indPoz1Callera] = (byte) p;
                        GeneralSituation[indKolCallers] = ++KolCallers;
                    }
                    case 3 -> {
                        BasaRaunds[r][p][indAction] = _3BET;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                        GeneralSituation[indPoz3beter] = (byte) p;
                    }
                    case -3 -> {
                        BasaRaunds[r][p][indAction] = CALLv3BET;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                        if (KolCallers3beta == 0) GeneralSituation[indPoz1Caller3beta] = (byte) p;
                        GeneralSituation[indKolCallers3beta] = ++KolCallers3beta;
                    }
                    case 4 -> {
                        BasaRaunds[r][p][indAction] = _4BET;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                        GeneralSituation[indPoz4betera] = (byte) p;
                    }
                    case -4 -> {
                        BasaRaunds[r][p][indAction] = CALLv4bet;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                    }
                    case 5 -> {
                        BasaRaunds[r][p][indAction] = _5BET;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                    }
                    case -5 -> {
                        BasaRaunds[r][p][indAction] = CALLv5bet;
                        System.arraycopy(GeneralSituation, 0, BasaRaunds[r][p], 1, GeneralSituation.length);
                    }
                }
            }
        }

        byte[][][] playerRaunds = new byte[6][Raunds][10];
        out: for(int p=1; p<7; p++){
            for(int r=0; r<Raunds; r++){
                if(Raunds<5) {
                    if(RaundsActions[r][p]==0){ continue out;}
                    if(BasaRaunds[r][p][0]!=0){ playerRaunds[p-1][r] = BasaRaunds[r][p];}
                }
                else {
                    for(int vr=4; vr<Raunds; vr++) {
                        if(RaundsActions[r][p]==0){ continue out;}
                        if(BasaRaunds[r][p][0]!=0){ playerRaunds[p-1][4] = BasaRaunds[r][p];}}
                    continue out;
                }
            }
        }

     /*  for(byte[][] pos:playerRaunds)
       {for (byte[] raund:pos) for (byte act:raund) System.out.print(" "+act+" ");
           System.out.println();}*/
        return playerRaunds;
    }



    private static void clear_UsedArrays(){

        for(int i=0; i<6; i++){
            nicks[i] = null;
            stacks[i] = 0;
            id_players[i] = 0;
            preflop_actions.get(i).clear();
            preflop_actions.get(i).add(0.0f);
        }
    }

    static final String[] positions_for_query = {null,"UTG","MP","CO","BU","SB","BB"};

    private static float procents(int stata, int select){
        if(select==0)return 0;
        return ((float)stata/(float)select)*100;
    }


    public static void main(String[] args) {
        Work_DataBase work_dataBase = new Work_DataBase();

        //for(Map.Entry<String,Integer> entry:map_nicks_idplayers.entrySet()) System.out.println(entry.getValue()+"   "+entry.getKey());

      /* List<Integer> sortlist = new ArrayList<>(map_nicks_idplayers.values());
       Collections.sort(sortlist);
        for(Integer a:sortlist) System.out.println(a);*/
        mainstats = work_dataBase.fill_MainArrayOfStatsFromDateBase();
        //start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\JavaProjects\\ForGoodGame\\test_party\\output");
        /*try {
            Array arraystata = connect_to_db.createArrayOf("integer",(Object[]) mainstats[0].getMap_of_Idplayer_stats().get(6));
            System.out.println(arraystata.toString());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }*/
        //record_MainArrayOfStatsToDateBase(mainstats);
        HashMap<Integer,Integer[][]> arr = mainstats[2].getMap_of_Idplayer_stats();
        Integer[][] stats = arr.get(2);

        if(stats==null) System.out.println("null");


        for (int i=0; i<6; i++)
            System.out.println(positions_for_query[i+1]+" vpip "+procents(stats[i][1], stats[i][0])+" pfr "+procents(stats[i][2], stats[i][0])+
                    " 3_bet "+procents(stats[i][4], stats[i][3])+" count pfr "+stats[i][2]+" count  select 3bet "+stats[i][3]+" count 3bet "+stats[i][4]+" count vpip "+stats[i][1]);
        System.out.println("Total vpip "+procents(stats[6][1], stats[6][0])+" pfr "+procents(stats[6][2], stats[6][0])+
                " 3_bet "+procents(stats[6][4], stats[6][3])+" count pfr "+stats[6][2]+" count  select 3bet "+stats[6][3]+" count 3bet "+stats[6][4]+" count vpip "+stats[6][1]);



        //delete_and_copy_WorkIdplayersStats();
       /* System.out.println("count hands "+c);
        for (HandFromHistory hand:list_handsfromhistory)
            System.out.println(hand.time_hand+"  "+get_str_Cards(hand.cards_hero));*/

        close_DataBase();
    }
}
