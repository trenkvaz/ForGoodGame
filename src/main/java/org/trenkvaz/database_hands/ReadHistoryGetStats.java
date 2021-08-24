package org.trenkvaz.database_hands;

import org.trenkvaz.newstats.FilterStata;
import org.trenkvaz.newstats.WorkStats;


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
import static org.trenkvaz.main.CaptureVideo.DECK;
import static org.trenkvaz.main.CaptureVideo.NICK_HERO;
import static org.trenkvaz.ui.StartAppLauncher.*;

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
    static final int PREFLOP = 0, FLOP =1, TURN = 2, RIVER = 3;
    static HashMap<String,Double> boostersMap = new HashMap<>();
    static HashMap<String,Integer> countBoostersMap = new HashMap<>();
    static double winBooster = 0;

    static HashMap<Long,Float> numHandResultHeroHistory = new HashMap<>();
    static boolean isRecordStats = false;
    static FilterStata filterStata;
    static WorkStats workStats;
    static boolean isNewStatsCount = false;

    static boolean isHeroCard =false;

    static boolean isAllHandsHistory = false;                                 // ВАЖНО !!!!!!!!!!!!!!!!!!!!!!!!!!!!1

    static {  for(int f=0; f<6; f++){
        preflopActions.add(new ArrayList<Float>()); preflopActions.get(f).add(0.0f);
        flopActions.add(new ArrayList<Float>());
        turnActions.add(new ArrayList<Float>());
        riverActions.add(new ArrayList<Float>());
    } }


    static void initCountFilterStata(){
       workStats = new WorkStats(false);
                                                                                                             // НА ЧИСТУЮ !!!!!!!!!!!
       if(!isAllHandsHistory)workStats.fullMapNicksMapsNameFilterDataStata("main_");
       isNewStatsCount = true;
       /* int[] vpip = workStats.getValueOneStata("trenkvaz","vpip_pfrall_v_all",8);
        System.out.println("main 1 "+vpip[0]+"  "+vpip[1]+" "+vpip[2]);
        System.out.println(procents(vpip[1]+vpip[2],vpip[0]));*/
    }




    static void start_ReadFilesInFolder(String folder){
        Work_DataBase work_dataBase = new Work_DataBase();
        initCountFilterStata();

        boolean isAllowRec = true;
        for(File a: Objects.requireNonNull(new File(folder).listFiles())){
            if(a.isFile()&&a.getName().endsWith(".txt")){
                if(a.getName().endsWith("_recstats.txt")) { isAllowRec = false; }
               // System.out.println(RED+a.getName()+RESET);

                read_File(a.getPath(),a.getName());
                if(!a.getName().endsWith("_recstats.txt")){
                File newFile = new File(folder+"\\"+a.getName().replaceFirst("[.][^.]+$", "")+"_recstats.txt");
                if(a.renameTo(newFile)){
                    System.out.println("Файл переименован успешно");
                }else{
                    System.out.println("Файл не был переименован");
                }
                }


            }
        }


       if(isNewStatsCount){
           workStats.saveAllCountedStats();

       }




        close_DataBase();

        System.out.println("speed record hand "+time+"  "+count);
    }


    private static void read_File(String files,String nameFile){
        final String start_line_of_hand = "***** Hand History";
        List<String> hand = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(files));
            String line;
            while ((line = br.readLine()) != null) {
                if(line.length()==0)continue;
                if(line.contains(start_line_of_hand)){
                    if(hand!=null) read_HandHistory(hand,nameFile);
                    hand = new ArrayList<>();
                    hand.add(line);
                } else if(hand!=null)hand.add(line);
            }
            if(hand!=null) read_HandHistory(hand,nameFile);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static float totalHero = 0;



    private static void read_HandHistory(List<String> hand,String nameFile){
     /*if(hand.get(0).equals("***** Hand History For Game 1613331263329 *****"))return;
        if(hand.get(0).equals("***** Hand History For Game 1613331334538 *****"))return;
        if(hand.get(0).equals("***** Hand History For Game 1613331755858 *****"))return;*/
     //long numHand = hand.get(0).substring(28,13);
        //System.out.println("*"+hand.get(0).substring(28,41)+"*");
        /*for(String line:hand) System.out.println(line);
        System.out.println();
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");*/
     //checkBooster(hand);
     //if(true)return;
     float bb = read_BB(hand.get(1));
     int amountPlayers = read_StacksAndNicks(hand,bb);
     int startLine = amountPlayers+7;

     for(int street = 0; street<4; street++) {startLine = read_PreflopActions(hand,bb,startLine,street);
     if(startLine==-1)break;
     }
     String hero = NICK_HERO;
     int posHero = Arrays.asList(nicks).indexOf(hero);
     if(posHero==-1){
         System.out.println(hand.get(0)); return;
         //hero = "Hero";
     }
     float[] resultHand = getResultHand(hand,bb,amountPlayers);
     float resulHero = resultHand[Arrays.asList(nicks).indexOf(hero)];

     String[][] cards = getCardsShowDown(hand,amountPlayers);

     if(isHeroCard){
         if(cards[posHero][0]==null){
             cards[posHero] = read_CardsHeroForHistoryHand(hand);
         }
        // System.out.println("cardsHero "+Arrays.toString(cards[posHero]));
     }

     totalHero+=resulHero;
       // System.out.println(Arrays.toString(resultHand)+"  "+resulHero+"  "+totalHero);
     numHandResultHeroHistory.put(Long.parseLong(hand.get(0).substring(28,41)),resulHero);
     String[] nicksOldStata = new String[6];
        for(int i=0; i<6; i++){
            if(nicks[i]==null)continue;
            nicksOldStata[i] = "$ю$"+nicks[i]+"$ю$";
        }

    /* for(MainStats stats:mainstats)
         stats.count_Stats_for_map(preflop_players_actions_in_raunds,nicksOldStata,stacks,(byte) amountPlayers,posActions,false);*/

     //testStata(posHero,hand);
        //System.out.println(nameFile+"   "+hand.get(0));
     if(isNewStatsCount)workStats.countOneHand(cards,nicks,stacks,resultHand,unionActionsStreetsStats(),null,posHero);
     //test_show(hand.get(0));
     clear_UsedArrays();

    }

    static void checkBooster(List<String> hand){
        String typeBooster = "";
        for(int i=hand.size()-1; i>10; i--)if(hand.get(i).contains("booster value: ")){
            typeBooster = hand.get(i).substring(0,hand.get(i).indexOf(" "));

            //System.out.println("*"+hand.get(i).substring(hand.get(i).indexOf("$")+1)+"*");
            double booster = Double.parseDouble(hand.get(i).substring(hand.get(i).indexOf("$")+1));
            if(!boostersMap.containsKey(typeBooster)){boostersMap.put(typeBooster,booster);  countBoostersMap.put(typeBooster,1);                          }
            else {boostersMap.put(typeBooster,boostersMap.get(typeBooster)+booster); countBoostersMap.put(typeBooster,countBoostersMap.get(typeBooster)+1);                                   }



            for(int h=hand.size()-1; h>10; h--) if(hand.get(h).contains("Hero receives $")||hand.get(h).contains(NICK_HERO+" receives $")){
                int indexHero = hand.get(h).indexOf(NICK_HERO+" receives $");
                if(indexHero!=-1)indexHero = (NICK_HERO+" receives $").length()+indexHero;
                else indexHero = hand.get(h).indexOf("Hero receives $")+("Hero receives $").length();
                int lastInd = hand.get(h).indexOf(",",indexHero);
                if(lastInd!=-1) winBooster+= Double.parseDouble(hand.get(h).substring(indexHero,lastInd));
                else winBooster+= Double.parseDouble(hand.get(h).substring(indexHero));
                   // winBooster = Double.parseDouble(hand.get(h).substring(hand.get(h).indexOf("$")+1));

                break;
            }


            break;
       }


    }

    private static String[] read_CardsHeroForHistoryHand(List<String> hand){
        int i_deal = -1;
        for(int i=7; i<hand.size(); i++ )if(hand.get(i).startsWith("** Dealing down cards **")){i_deal = i+1;break;}
        int indStart = hand.get(i_deal).indexOf('[');
        return new String[]{hand.get(i_deal).substring(indStart+2,indStart+4),hand.get(i_deal).substring(indStart+6,indStart+8)};
    }


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

       /* if(street==PREFLOP){
        posActions = new float[6][];
        for (int v=0; v<6; v++){
            if(preflopActions.get(v)!=null){
                if(preflopActions.get(v).size()==0) {posActions[v] = null; continue;}
                posActions[v] = new float[preflopActions.get(v).size()];
                for (int p = 0; p< preflopActions.get(v).size(); p++) posActions[v][p] = preflopActions.get(v).get(p);
            }
        }
        preflop_players_actions_in_raunds = precount_to_PreflopActions(6,posActions);
        }*/

        return result;
    }

   static float[] getResultHand(List<String> hand,float bb,int amountPlayers){
        float[] result = new float[6];
        int startLine = hand.size()-amountPlayers;
        for(int line=startLine; line<hand.size(); line++){
            for(int i_nick = 0; i_nick<6; i_nick++){
                if(nicks[i_nick]==null)continue;
                if(hand.get(line).startsWith(nicks[i_nick]+" balance ")){
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

    static String[][] getCardsShowDown(List<String> hand,int amountPlayers){
        String[][] result = new String[6][2];
        int startLine = hand.size()-amountPlayers;
        int startRIT = 0;
        for(int line=hand.size()-1; line>0; line--){
            if(hand.get(line).startsWith("SECOND Board: ")){ startRIT = line+1; break;}
            if(hand.get(line).startsWith("** Summary **")){ break;}
        }
        if(startRIT!=0)startLine = startRIT;
        for(int line=startLine; line<hand.size(); line++){
            for(int i_nick = 0; i_nick<6; i_nick++){
                if(nicks[i_nick]==null)continue;
                if(hand.get(line).startsWith(nicks[i_nick])){
                    int indStart = hand.get(line).indexOf('[');
                    if(indStart>0){ result[i_nick][0] = hand.get(line).substring(indStart+2,indStart+4); result[i_nick][1] = hand.get(line).substring(indStart+6,indStart+8);
                        //System.out.println("*"+result[i_nick][0]+result[i_nick][1]+"*");
                    }
                }
            }
        }
     return result;
    }

    static void test_show(String firstLine){
        //if(!firstLine.equals("***** Hand History For Game 1611334751632 *****"))return;

        List<int[][]> listActionsInRoundsByPositions = workStats.getListPokerActionsInRoundsByPositions(unionActionsStreetsStats());
        System.out.println(firstLine);
        for(int i=0; i<6; i++){
            System.out.print(nicks[i]+"   pre ");
            for(int a = 0; a< preflopActions.get(i).size(); a++)
                System.out.print(preflopActions.get(i).get(a)+" ");
            System.out.print("  ////  ");
            for(int act=0; act<listActionsInRoundsByPositions.get(0).length; act++)
                System.out.print(listActionsInRoundsByPositions.get(0)[act][i]+" ");
            if(!flopActions.get(i).isEmpty()) {System.out.print(" flop ");
            for(int a = 0; a< flopActions.get(i).size(); a++)
                System.out.print(flopActions.get(i).get(a)+" ");
                System.out.print("  ////  ");
                for(int act=0; act<listActionsInRoundsByPositions.get(1).length; act++)
                    System.out.print(listActionsInRoundsByPositions.get(1)[act][i]+" ");
            }
            if(!turnActions.get(i).isEmpty()) {System.out.print(" turn ");
            for(int a = 0; a< turnActions.get(i).size(); a++)
                System.out.print(turnActions.get(i).get(a)+" ");
                System.out.print("  ////  ");
                for(int act=0; act<listActionsInRoundsByPositions.get(2).length; act++)
                    System.out.print(listActionsInRoundsByPositions.get(2)[act][i]+" ");
            }
            if(!riverActions.get(i).isEmpty()) {System.out.print(" river ");
            for(int a = 0; a< riverActions.get(i).size(); a++)
                System.out.print(riverActions.get(i).get(a)+" ");
                System.out.print("  ////  ");
                for(int act=0; act<listActionsInRoundsByPositions.get(3).length; act++)
                    System.out.print(listActionsInRoundsByPositions.get(3)[act][i]+" ");
            }

            System.out.println();
        }
        System.out.println("===============================================");
    }


    private static List<List<List<Float>>> unionActionsStreetsStats(){ return new ArrayList<>(Arrays.asList(preflopActions,flopActions,turnActions,riverActions));}

    private static void testStata(int posHero,List<String> hand){
        /*if(filterStata.countPreflop(null,preflopActions,posHero,null,0,0,0)){
            for(String line:hand) System.out.println(line);
            System.out.println();
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        }*/

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


    static HashMap<Long,float[]> numHandResultHeroTest = new HashMap<>();

    static void readResultHero(){

        try {
            BufferedReader br = new BufferedReader(new FileReader(home_folder+"\\test\\resultHero.txt"));
            String line; float res =0; int stNumHand = 0, endNumHand = 0, stStrRes = 0; String[] resStr = null;
            while ((line = br.readLine()) != null) {
                if(line.startsWith("TOTAL"))continue;
                //long numHand = Long.parseLong()line.substring(0,13);
                //res+=Float.parseFloat(line.substring(19));
                //System.out.println("*"+line.substring(19)+"*");
                if(!line.startsWith("LAST HAND")){
                    stNumHand = 0; endNumHand = 13; stStrRes = 19;
                } else { stNumHand = 10; endNumHand = 23; stStrRes = 29;             }

                resStr = line.substring(stStrRes).split("    ");

                numHandResultHeroTest.put(Long.parseLong(line.substring(stNumHand,endNumHand)),
                        new float[]{BigDecimal.valueOf(Float.parseFloat(resStr[0])).setScale(SCALE, RoundingMode.HALF_UP).floatValue(),
                                BigDecimal.valueOf(Float.parseFloat(resStr[1])).setScale(SCALE, RoundingMode.HALF_UP).floatValue()});

            }
           // System.out.println(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {

    // бустер НЛ2 с 11 мая 2021

        //start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\Poker\\PartyPokerHands\\PokerStars\\party_nicks_right");
        readResultHero();

       start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\JavaProjects\\ForGoodGame\\test_party\\output");

      /* for(Map.Entry<String,Double> entry:boostersMap.entrySet())
            System.out.println(entry.getKey()+" "+entry.getValue());

        for(Map.Entry<String,Integer> entry:countBoostersMap.entrySet())
            System.out.println(entry.getKey()+" "+entry.getValue());

        System.out.println("win "+winBooster);*/
       // System.out.println("total win "+totalHero);
       float totalResProga =0, totalResProga1 = 0;
       for(Map.Entry<Long,float[]> entry:numHandResultHeroTest.entrySet()){
           Float res = numHandResultHeroHistory.get(entry.getKey());

           if(res!=null){
               totalResProga+=entry.getValue()[1]; totalResProga1+=entry.getValue()[0];
               if(res!=entry.getValue()[1]) System.out.println(entry.getKey()+" "+res+"  "+entry.getValue()[1]);
           }
       }
      //  System.out.println(totalResProga+"  "+totalResProga1);
    }
}
