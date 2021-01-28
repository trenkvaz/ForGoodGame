package org.trenkvaz.database_hands;

import org.trenkvaz.stats.MainStats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//import static org.trenkvaz.database_hands.GetNicksForHands.reverse_MapIdplayersNicks;
import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.main.CaptureVideo.NICK_HERO;
import static org.trenkvaz.ui.StartAppLauncher.SCALE;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;

public class ReadHistoryGetStats {

    //static Map<String,Integer> map_nicks_idplayers;
    static List<List<Float>> preflopActions = new ArrayList<>(6);
    static List<List<Float>> flopActions = new ArrayList<>(6);
    static List<List<Float>> turnActions = new ArrayList<>(6);
    static List<List<Float>> riverActions = new ArrayList<>(6);
    static String[] nicks = new String[6]; static float[] stacks = new float[6];
    static int[] id_players = new int[6];
    static final String summary = "** Summary", dealing_flop = "** Dealing Flop", folds = " folds ", calls = " calls ", raises = " raises ", checks = " checks ",
    bets = " bets ", dealing_turn = "** Dealing Turn", dealing_river = "** Dealing River";
    static float[][] posActions;
    static byte[][][] preflop_players_actions_in_raunds;
    static MainStats[] mainstats;
    static HashMap<Long,Float> numHandResultHeroHistory = new HashMap<>();
    static boolean isRecordStats = true;


    static {  for(int f=0; f<6; f++){
        preflopActions.add(new ArrayList<Float>()); preflopActions.get(f).add(0.0f);
        flopActions.add(new ArrayList<Float>());
        turnActions.add(new ArrayList<Float>());
        riverActions.add(new ArrayList<Float>());
    } }


    static void start_ReadFilesInFolder(String folder){
        Work_DataBase work_dataBase = new Work_DataBase();
        mainstats = work_dataBase.fill_MainArrayOfStatsFromDateBase("main_nicks_stats");
        boolean isAllowRec = true;
        for(File a: Objects.requireNonNull(new File(folder).listFiles())){
            if(a.isFile()&&a.getName().endsWith(".txt")){
                if(isRecordStats&&a.getName().endsWith("_recstats.txt")) { isAllowRec = false; break; }
                read_File(a.getPath());
                if(isRecordStats){
                File newFile = new File(folder+"\\"+a.getName().replaceFirst("[.][^.]+$", "")+"_recstats.txt");
                if(a.renameTo(newFile)){
                    System.out.println("Файл переименован успешно");
                }else{
                    System.out.println("Файл не был переименован");
                }
                }
            }
        }
        if(isRecordStats&&isAllowRec){
            System.out.println("RECORD");
        record_MainArrayOfStatsToDateBase(mainstats);
        delete_and_copy_WorkNicksStats();
        }
        close_DataBase();
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
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static float totalHero = 0;

    private static void read_HandHistory(List<String> hand){
     //if(!hand.get(0).equals("***** Hand History For Game 1611344734614 *****"))return;
     //long numHand = hand.get(0).substring(28,13);
        //System.out.println("*"+hand.get(0).substring(28,41)+"*");
     float bb = read_BB(hand.get(1));
     int amountPlayers = read_StacksAndNicks(hand,bb);
     int startLine = amountPlayers+7;

     for(int street = 0; street<4; street++) {startLine = read_PreflopActions(hand,bb,startLine,street);
     if(startLine==-1)break;
     }
     int posHero = Arrays.asList(nicks).indexOf(NICK_HERO);
     if(posHero==-1){
         System.out.println(hand.get(0)); return;
     }
     float resulHero = getResultHand(hand,bb,amountPlayers)[Arrays.asList(nicks).indexOf(NICK_HERO)];
     totalHero+=resulHero;
     numHandResultHeroHistory.put(Long.parseLong(hand.get(0).substring(28,41)),resulHero);

        for(int i=0; i<6; i++){
            if(nicks[i]==null)continue;
            nicks[i] = "$ю$"+nicks[i]+"$ю$";
        }

     for(MainStats stats:mainstats)
         stats.count_Stats_for_map(preflop_players_actions_in_raunds,nicks,stacks,(byte) amountPlayers,posActions,false);



     //test_show(hand.get(0));
     clear_UsedArrays();

    }

    static final int PREFLOP = 0, FLOP =1, TURN = 2, RIVER = 3;

    private static int read_PreflopActions(List<String> hand,float bb,int startLine,int street){
        String dealingStreet = summary; List<List<Float>> actions = null;
        if(street==PREFLOP){dealingStreet = dealing_flop;  actions = preflopActions;            }
        if(street==FLOP){dealingStreet = dealing_turn;actions = flopActions;}
        if(street==TURN){dealingStreet = dealing_river;actions = turnActions;}
        if(street==RIVER)actions = riverActions;
        int result = 0;
        for(int i_line = startLine; i_line<hand.size(); i_line++){
            //System.out.println(street+" "+i_line);
            if(hand.get(i_line).startsWith(summary)) { result=-1; break; }
            if(hand.get(i_line).startsWith(dealingStreet)){ result=i_line; break; }
            for(int i_nick = 0; i_nick<6; i_nick++){
                if(nicks[i_nick]==null)continue;
                if(hand.get(i_line).startsWith(nicks[i_nick])){
                     if(hand.get(i_line).contains(nicks[i_nick]+folds)) actions.get(i_nick).add(Float.NEGATIVE_INFINITY);
                     if(hand.get(i_line).contains(nicks[i_nick]+calls)) actions.get(i_nick).add(new BigDecimal((-Float.parseFloat(hand.get(i_line).
                             substring(nicks[i_nick].length()+8).replaceAll("[^0-9?!.]","")))/bb)
                             .setScale(2, RoundingMode.HALF_UP).floatValue());
                    if(hand.get(i_line).contains(nicks[i_nick]+raises)) actions.get(i_nick).add(new BigDecimal((Float.parseFloat(hand.get(i_line).
                            substring(hand.get(i_line).lastIndexOf(" to ")+4).replaceAll("[^0-9?!.]","")))/bb)
                            .setScale(2, RoundingMode.HALF_UP).floatValue());
                    if(hand.get(i_line).contains(nicks[i_nick]+checks)) actions.get(i_nick).add(Float.POSITIVE_INFINITY);
                    if(hand.get(i_line).contains(nicks[i_nick]+bets)) actions.get(i_nick).add(new BigDecimal((Float.parseFloat(hand.get(i_line).
                            substring(nicks[i_nick].length()+7).replaceAll("[^0-9?!.]","")))/bb)
                            .setScale(2, RoundingMode.HALF_UP).floatValue());
                }
            }
        }

        if(street==PREFLOP){
        posActions = new float[6][];
        for (int v=0; v<6; v++){
            if(preflopActions.get(v)!=null){
                if(preflopActions.get(v).size()==0) {posActions[v] = null; continue;}
                posActions[v] = new float[preflopActions.get(v).size()];
                for (int p = 0; p< preflopActions.get(v).size(); p++) posActions[v][p] = preflopActions.get(v).get(p);
            }
        }
        preflop_players_actions_in_raunds = precount_to_PreflopActions(6,posActions);
        }

        return result;
    }

   static float[] getResultHand(List<String> hand,float bb,int amountPlayers){
        float[] result = new float[6];
        int startLine = hand.size()-amountPlayers;
        for(int line=startLine; line<hand.size(); line++){
            for(int i_nick = 0; i_nick<6; i_nick++){
                if(nicks[i_nick]==null)continue;
                if(hand.get(line).startsWith(nicks[i_nick])){
                    //String balance = hand.get(line).substring(nicks[i_nick].length()+10,hand.get(line).indexOf(',',nicks[i_nick].length()+10));
                    //System.out.println(nicks[i_nick]+" *"+balance+"*");
                    result[i_nick] = BigDecimal.valueOf(Float.parseFloat(hand.get(line).substring(nicks[i_nick].length()+10,
                            hand.get(line).indexOf(',',nicks[i_nick].length()+10)))/bb-stacks[i_nick])
                            .setScale(2, RoundingMode.HALF_UP).floatValue();
                }
            }
        }
       //System.out.println("******************************************************");
        return result;
    }

    static void test_show(String firstLine){
        //if(!firstLine.equals("***** Hand History For Game 1611334751632 *****"))return;
        System.out.println(firstLine);
        for(int i=0; i<6; i++){
            System.out.print(nicks[i]+"   pre ");
            for(int a = 0; a< preflopActions.get(i).size(); a++)
                System.out.print(preflopActions.get(i).get(a)+" ");
            if(!flopActions.get(i).isEmpty()) System.out.print(" flop ");
            for(int a = 0; a< flopActions.get(i).size(); a++)
                System.out.print(flopActions.get(i).get(a)+" ");
            if(!turnActions.get(i).isEmpty()) System.out.print(" turn ");
            for(int a = 0; a< turnActions.get(i).size(); a++)
                System.out.print(turnActions.get(i).get(a)+" ");
            if(!riverActions.get(i).isEmpty()) System.out.print(" river ");
            for(int a = 0; a< riverActions.get(i).size(); a++)
                System.out.print(riverActions.get(i).get(a)+" ");
            System.out.println();

        }
        System.out.println("===============================================");
    }


    private static int read_StacksAndNicks(List<String> hand, float bb){
        int p = 0;
        for(int i=4; i<10; i++){ p++;
            if(hand.get(i).startsWith("Seat "+p+": "))continue;
            p--;
            break;}

        //if(p==6)return;
        int cor = 6-p;
        int position = -1;
        for (int i=0; i<p; i++) {
            if(i<3)position = i+3;
            else position = i-3+cor;
            String line = hand.get(i+4);
            stacks[position]  = new BigDecimal(Float.parseFloat(line.substring(line.indexOf("$")+1,line.indexOf(")")))/bb).setScale(1, RoundingMode.HALF_UP).floatValue();
            nicks[position] = line.substring(8,line.indexOf("(")-1);
            //System.out.println("pos "+position+" *"+nicks[position]+"*  "+stacks[position]);
        }
        //System.out.println("========================================================");
        return p;
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
            preflopActions.get(i).clear();
            preflopActions.get(i).add(0.0f);
            flopActions.get(i).clear();
            turnActions.get(i).clear();
            riverActions.get(i).clear();
        }
    }

    static final String[] positions_for_query = {null,"UTG","MP","CO","BU","SB","BB"};

    private static float procents(int stata, int select){
        if(select==0)return 0;
        return ((float)stata/(float)select)*100;
    }


    public static synchronized void count_StatsCurrentGame(ConcurrentHashMap[] current_map_stats,MainStats[] main_stats,String[] nicks,
                                                           float[] stacks, List<List<Float>> preflop_actions_for_stats,int startAmountPlayers){
        System.out.println("CREATE STATS");
       /* for(int i=0; i<6; i++){
            System.out.print(nicks[i]+"   ");
        for(int a=1; a<preflop_actions_for_stats.get(i).size(); a++) {
            System.out.print(preflop_actions_for_stats.get(i).get(a)+" ");
        }
            System.out.println();
        }*/
       String[] ercanning_nicks = new String[6];
       for(int i=0; i<6; i++){
           if(nicks[i]==null)continue;
           ercanning_nicks[i] = "$ю$"+nicks[i]+"$ю$";
       }


        float[][] posActions = new float[6][];
        for (int v=0; v<6; v++){
            if(preflop_actions_for_stats.get(v)!=null){
                if(preflop_actions_for_stats.get(v).size()==0) {posActions[v] = null; continue;}
                posActions[v] = new float[preflop_actions_for_stats.get(v).size()];
                for (int p=0; p<preflop_actions_for_stats.get(v).size(); p++) posActions[v][p] = preflop_actions_for_stats.get(v).get(p);
            }
        }



        for(int stata=0; stata<main_stats.length; stata++){
            main_stats[stata].count_Stats_for_map(precount_to_PreflopActions(startAmountPlayers,posActions),ercanning_nicks,stacks,(byte)startAmountPlayers,posActions,false);
            for(int player=0; player<6; player++){
            if(ercanning_nicks[player]==null)continue;
            current_map_stats[stata].put(ercanning_nicks[player],main_stats[stata].getMap_of_Idplayer_stats().get(ercanning_nicks[player]));
            }
        }

        record_StatsCurrentGame(main_stats,ercanning_nicks);

    }

    static HashMap<Long,Float> numHandResultHeroTest = new HashMap<>();

    static void readResultHero(){

        try {
            BufferedReader br = new BufferedReader(new FileReader(home_folder+"\\test\\resultHero.txt"));
            String line; float res =0;
            while ((line = br.readLine()) != null) {
                if(line.startsWith("TOTAL"))continue;
                //long numHand = Long.parseLong()line.substring(0,13);
                //res+=Float.parseFloat(line.substring(19));
                //System.out.println("*"+line.substring(19)+"*");
                numHandResultHeroTest.put(Long.parseLong(line.substring(0,13)),BigDecimal.valueOf(Float.parseFloat(line.substring(19))).
                        setScale(SCALE, RoundingMode.HALF_UP).floatValue());
            }
            System.out.println(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        //mainstats = main_array_of_stats;
        //for(Map.Entry<String,Integer> entry:map_nicks_idplayers.entrySet()) System.out.println(entry.getValue()+"   "+entry.getKey());

      /* List<Integer> sortlist = new ArrayList<>(map_nicks_idplayers.values());
       Collections.sort(sortlist);
        for(Integer a:sortlist) System.out.println(a);*/
        //mainstats = work_dataBase.fill_MainArrayOfStatsFromDateBase();
        start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\JavaProjects\\ForGoodGame\\test_party\\output");
        /*try {
            Array arraystata = connect_to_db.createArrayOf("integer",(Object[]) mainstats[0].getMap_of_Idplayer_stats().get(6));
            System.out.println(arraystata.toString());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }*/

        //mainstats = work_dataBase.fill_MainArrayOfStatsFromDateBase();
       /* HashMap<String,Integer[][]> arr = mainstats[2].getMap_of_Idplayer_stats();
        Integer[][] stats = arr.get("trenkvaz");

        if(stats==null) System.out.println("null");


        for (int i=0; i<6; i++)
            System.out.println(positions_for_query[i+1]+" vpip "+procents(stats[i][1], stats[i][0])+" pfr "+procents(stats[i][2], stats[i][0])+
                    " 3_bet "+procents(stats[i][4], stats[i][3])+" count pfr "+stats[i][2]+" count  select 3bet "+stats[i][3]+" count 3bet "+stats[i][4]+" count vpip "+stats[i][1]);
        System.out.println("Total vpip "+procents(stats[6][1], stats[6][0])+" pfr "+procents(stats[6][2], stats[6][0])+
                " 3_bet "+procents(stats[6][4], stats[6][3])+" count pfr "+stats[6][2]+" count  select 3bet "+stats[6][3]+" count 3bet "+stats[6][4]+" count vpip "+stats[6][1]);*/

        System.out.println("Res "+totalHero);
      readResultHero();

      for(Map.Entry<Long,Float> entry:numHandResultHeroTest.entrySet()){
          if(!numHandResultHeroHistory.containsKey(entry.getKey()))continue;

          if(!entry.getValue().equals(numHandResultHeroHistory.get(entry.getKey())))
              System.out.println(entry.getKey()+" test "+entry.getValue()+" history "+numHandResultHeroHistory.get(entry.getKey()));

      }


    }
}
