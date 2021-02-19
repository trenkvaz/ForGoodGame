package org.trenkvaz.newstats;

import java.io.*;
import java.util.*;

import static org.trenkvaz.database_hands.Work_DataBase.recordAllMapStats;
import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;

public class WorkStats implements Serializable {
    public boolean isInGame;

    Map<String, FilterStata> statsMap = new HashMap<>();
    Map<String,Map<String, DataStata>> mapNicksMapsNameFilterDataStata = new HashMap<>();

    public WorkStats(boolean isInGame1){ isInGame = isInGame1;
        readStatsMap();
    }




   public void fillStataMapsInStatsForGame(){

   }

   public static int countSD = 0;

   public synchronized void countOneHand(String[][] cards, String[] nicks, float[] stacks, float[] resultsHand, List<List<List<Float>>> sizeActionsStreetsStats,
                            int[]alliners,int posHero){
        boolean isWin = false, isShowDown = false; int rangePlayer = 0;
        Map<String, DataStata> mapNameFilterDataStata = null; FilterStata filterStata = null; DataStata dataStata = null;
        List<int[][]> listPokerActionsInRoundsByPositions = getListPokerActionsInRoundsByPositions(sizeActionsStreetsStats);
        for(int pokPos=0; pokPos<6; pokPos++){
            if(nicks[pokPos]==null)continue;
            rangePlayer = 0;
            if(!isInGame){ if(cards[pokPos][0]!=null) rangePlayer = getPreflopRange(cards[pokPos]);  } // карты обрабатываются только вне игры
            nicks[pokPos] = "$ю$"+nicks[pokPos]+"$ю$";
            // вин и щоудаун на игрока нужны для разных фильтров поэтому нужно один раз определить заранее
            isWin = isWin(resultsHand[pokPos]);
            isShowDown = isShowDown(pokPos,alliners,sizeActionsStreetsStats.get(3),cards);
           /* if(nicks[pokPos].equals("$ю$trenkvaz$ю$")){System.out.println(rangePlayer+" sd "+isShowDown+" win "+isWin);
            if(isShowDown)countSD++;
            }*/

            mapNameFilterDataStata = mapNicksMapsNameFilterDataStata.get(nicks[pokPos]);
            if(mapNameFilterDataStata==null){ mapNameFilterDataStata = creatMapNameFilterDataStataOnePlayer();mapNicksMapsNameFilterDataStata.put(nicks[pokPos],mapNameFilterDataStata);}

            for(String nameFilter:statsMap.keySet()){
                filterStata = statsMap.get(nameFilter);
                dataStata = mapNameFilterDataStata.get(nameFilter);
                filterStata.countOnePlayerStata(isInGame,pokPos,nicks[pokPos],stacks[pokPos],sizeActionsStreetsStats,isWin,isShowDown,
                        cards[pokPos],rangePlayer,posHero,dataStata,listPokerActionsInRoundsByPositions);
            }
            recordAllMapStats(statsMap,mapNicksMapsNameFilterDataStata);
        }
   }

    public List<int[][]> getListPokerActionsInRoundsByPositions(List<List<List<Float>>> sizeActionsStreetsStats){
        List<int[][]> result = new ArrayList<>(4);
        for(int street=0; street<4; street++){
        int maxSizeListActions = sizeActionsStreetsStats.get(street).stream().mapToInt(List::size).max().getAsInt();
        int cor = 0; if(street==0)cor=1;
        int[][] roundsPosAct = new int[maxSizeListActions-cor][6];
        int raise = cor;
        for(int act=cor; act<maxSizeListActions; act++)
            for(int pokPos=0; pokPos<6; pokPos++){
                if(sizeActionsStreetsStats.get(street).get(pokPos).size()-1<act)continue;
                float action = sizeActionsStreetsStats.get(street).get(pokPos).get(act);
                if(action==Float.NEGATIVE_INFINITY)roundsPosAct[act-cor][pokPos]=-10;
                else if(action==Float.POSITIVE_INFINITY)roundsPosAct[act-cor][pokPos]= 10;
                else if(action!=Float.NEGATIVE_INFINITY&&action<0)roundsPosAct[act-cor][pokPos]= -(raise);
                else if(action!=Float.POSITIVE_INFINITY&&action>0){ if(raise==5)roundsPosAct[act-cor][pokPos] = raise;else roundsPosAct[act-cor][pokPos] = ++raise;}
            }
        result.add(roundsPosAct);
        }
        return result;
    }


   private boolean isWin(float resultPlayer){ return resultPlayer>0; }

   private boolean isShowDown(int pokPos, int[] alliner, List<List<Float>> sizeActionsRiver,String[][] cards){
        if(!isInGame){ return cards[pokPos][0] != null; }

        // ПОСМОТРЕТЬ в ОСР определение оллинера который рейзил не до конца стека !!!!!!!!!!!!!!!!!!!!!!!!
        if(alliner[pokPos]>0)return true;
        if(sizeActionsRiver.get(pokPos).isEmpty())return false;
        return sizeActionsRiver.get(pokPos).get(sizeActionsRiver.get(pokPos).size() - 1) != Float.NEGATIVE_INFINITY;
   }

   private int getPreflopRange(String[] cards){
       String nom1 = cards[0].substring(0,1), nom2 = cards[1].substring(0,1), mast1 =  cards[0].substring(1,2), mast2 =  cards[1].substring(1,2), strRange = "";
       if(Arrays.asList(NOMINALS_CARDS).indexOf(nom1)<Arrays.asList(NOMINALS_CARDS).indexOf(nom2)){ String savecard = nom2; nom2 = nom1; nom1 = savecard; }
       if(nom1.equals(nom2))strRange = nom1+nom2;
       else { if(mast1.equals(mast2))strRange = nom1+nom2+"s";
              else strRange = nom1+nom2+"o"; }
       return Arrays.asList(PREFLOP_RANGE).indexOf(strRange);
   }

   public void createOneNewStata(FilterStata stata){
       statsMap.put(stata.getFullNameStata(),stata);
       saveStatsMap();
       //addStructureOneNewStataToDB(stata);
   }


   public Map<String, DataStata> creatMapNameFilterDataStataOnePlayer(){
       Map<String, DataStata> result = new HashMap<>();
       for(String nameFilter:statsMap.keySet()){ result.put(nameFilter,new DataStata(statsMap.get(nameFilter))); }
       return result;
   }


   private void addNewStatsToMapNicksMaps(FilterStata stata){
        for(Map<String, DataStata> mapNameDataStata:mapNicksMapsNameFilterDataStata.values()){
            mapNameDataStata.put(stata.getFullNameStata(),new DataStata(stata));
        }
   }

   public <T> T getValueOneStata(String nick, String nameFilter, int stata){
        DataStata dataStata = mapNicksMapsNameFilterDataStata.get("$ю$"+nick+"$ю$").get(nameFilter);
        switch (stata){
            case 1->{ return (T) dataStata.mainSelCallRaise;}
            case 6->{ return (T) dataStata.W$WSF;}
            case 7->{ return (T) dataStata.WTSD;}
            case 8->{ return (T) dataStata.W$SD;}
            case 9->{ return (T) dataStata.VPIP_PFR;}
        }
        return null;
   }

    private float procents(int[] stata){
        if(stata[0]==0)return 0;
        return ((float)stata[1]/(float)stata[0])*100;
    }

   public void readStatsMap(){
       try {	FileInputStream file=new FileInputStream(home_folder+"\\all_settings\\capture_video\\statsMap.file");
           ObjectInput out = new ObjectInputStream(file);
           statsMap = (Map<String, FilterStata>) out.readObject();
           out.close();
           file.close();
       } catch(IOException e) {
           System.out.println(e);
       } catch (ClassNotFoundException e) {
           e.printStackTrace();
       }
   }

    public void saveStatsMap(){
        try {
            FileOutputStream file=new FileOutputStream(home_folder+"\\all_settings\\capture_video\\statsMap.file");
            ObjectOutput out = new ObjectOutputStream(file);
            out.writeObject(statsMap);
            out.close();
            file.close();
        } catch(IOException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {


        //new Work_DataBase();
        WorkStats workStats1 = new WorkStats(false);
        FilterStata filterStata = new FilterStata.Builder().setMainNameFilter("vpip_pfr").setPosStata(new int[][]{{1,1,1,1,1,1},{1,1,1,1,1,1}}).setSpecStats(3).build();
        //FilterStata filterStata1 = new FilterStata.Builder().setMainNameFilter("W$SD").setPosStata(new int[][]{{1,1,1,1,1,1},{1,1,1,1,1,1}}).setSpecStats(2).build();
        workStats1.createOneNewStata(filterStata);
        //workStats1.createOneNewStata(filterStata1);
        //System.out.println(workStats1.getPreflopRange(new String[]{"2c","As"}));
        //close_DataBase();
        //WorkStats workStats1 = new WorkStats(false);
        //int[] t= workStats1.getValueOneStata("","",0);
        String name = "WWSFall_v_all";
        //System.out.println(workStats1.statsMap.get("WWSFall_v_all"));
    }


}


