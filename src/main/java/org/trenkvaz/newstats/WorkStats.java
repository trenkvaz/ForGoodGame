package org.trenkvaz.newstats;

import org.trenkvaz.database_hands.Work_DataBase;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;

public class WorkStats implements Serializable {
    public boolean isInGame;

    Map<String, FilterStata> statsMap = new HashMap<>();

    public WorkStats(boolean isInGame1){ isInGame = isInGame1; }




   public void fillStataMapsInStatsForGame(){

   }

   public void countOneHand(String[] cards, String[] nicks, float[] stacks, float[] resultsHand, List<List<List<Float>>> actionsStreetsStats, int[]alliners){
        boolean isWin = false, isShowDown = false, isInitDataStatsOneHand = false;
        for(int i=0; i<6; i++){
            if(nicks[i]==null)continue;
            nicks[i] = "$ю$"+nicks[i]+"$ю$";
            // вин и щоудаун на игрока нужны для разных фильтров поэтому нужно один раз определить заранее
            isWin = isWin(resultsHand[i]);
            isShowDown = isShowDown(i,alliners[i],actionsStreetsStats.get(3));
            for(FilterStata filterStata:statsMap.values()){
               if(isInGame&&!isInitDataStatsOneHand){ filterStata.dataStatsOneHand = new FilterStata.DataStata[6]; isInitDataStatsOneHand = true;}
               filterStata.countOnePlayerStata(isInGame,i,nicks[i],stacks[i],actionsStreetsStats,isWin,isShowDown);
            }
        }
   }


   private boolean isWin(float resultPlayer){ return resultPlayer>0; }

   private boolean isShowDown(int indPlayer, int alliner, List<List<Float>> actionsRiver){
        if(alliner>0)return true;
        if(actionsRiver.get(indPlayer).isEmpty())return false;
        if(actionsRiver.get(indPlayer).get(actionsRiver.get(indPlayer).size()-1)==Float.NEGATIVE_INFINITY)return false;
        for(int pokPos=0; pokPos<6; pokPos++){
            if(pokPos==indPlayer)continue;
            if(actionsRiver.get(pokPos).isEmpty())continue;
            if(actionsRiver.get(pokPos).get(actionsRiver.get(pokPos).size()-1)==Float.NEGATIVE_INFINITY)continue;
            return true;
        }
        return false;
   }

   private int getPreflopRange(String[] cards){
       String nom1 = cards[0].substring(0,1);
       String nom2 = cards[1].substring(0,1);
       String mast1 =  cards[0].substring(1,2);
       String mast2 =  cards[1].substring(1,2);
       if(Arrays.asList(NOMINALS_CARDS).indexOf(nom1)<Arrays.asList(NOMINALS_CARDS).indexOf(nom2)){ String savecard = nom2; nom2 = nom1; nom1 = savecard; }
       String strRange = "";
       if(nom1.equals(nom2))strRange = nom1+nom2;
       else { if(mast1.equals(mast2))strRange = nom1+nom2+"s";
              else strRange = nom1+nom2+"o"; }
       return Arrays.asList(PREFLOP_RANGE).indexOf(strRange);
   }

   public void createOneNewStata(FilterStata stata){
       statsMap.put(stata.getFullNameStata(),stata);
       saveStatsMap();
       addStructureOneNewStataToDB(stata);
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
       /* FilterStata filterStata = new FilterStata.Builder().setMainNameFilter("v4bet").setPosStata(new int[][]{{0,1,1,1,1,1},{1,1,1,1,1,1}}).isRange().build();
        FilterStata filterStata2 = new FilterStata.Builder().setMainNameFilter("v4bet").setPosStata(new int[][]{{0,0,1,1,1,1},{1,1,1,1,1,1}}).isRange().build();
        workStats1.createOneNewStata(filterStata);
        workStats1.createOneNewStata(filterStata2);*/
        System.out.println(workStats1.getPreflopRange(new String[]{"2c","As"}));
        //close_DataBase();
    }


}


