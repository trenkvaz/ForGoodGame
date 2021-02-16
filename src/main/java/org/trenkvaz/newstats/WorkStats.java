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
    Map<String,Map<String, DataStata>> mapNicksMapsNameFilterDataStata = new HashMap<>();

    public WorkStats(boolean isInGame1){ isInGame = isInGame1;
        readStatsMap();
    }




   public void fillStataMapsInStatsForGame(){

   }

   public void countOneHand(String[][] cards, String[] nicks, float[] stacks, float[] resultsHand, List<List<List<Float>>> actionsStreetsStats,
                            int[]alliners,int posHero){
        boolean isWin = false, isShowDown = false, isInitDataStatsOneHand = false; int rangePlayer = 0;
       Map<String, DataStata> mapNameFilterDataStata = null; FilterStata filterStata2 = null; DataStata dataStata = null;
        for(int pokPos=0; pokPos<6; pokPos++){
            if(nicks[pokPos]==null)continue;
            rangePlayer = 0;
            if(!isInGame){ if(cards[pokPos]!=null) rangePlayer = getPreflopRange(cards[pokPos]);  } // карты обрабатываются только вне игры
            nicks[pokPos] = "$ю$"+nicks[pokPos]+"$ю$";
            // вин и щоудаун на игрока нужны для разных фильтров поэтому нужно один раз определить заранее
            isWin = isWin(resultsHand[pokPos]);
            isShowDown = isShowDown(pokPos,alliners[pokPos],actionsStreetsStats.get(3),cards[pokPos]);
           /* for(FilterStata filterStata:statsMap.values()){
               if(isInGame&&!isInitDataStatsOneHand){ filterStata.dataStatsOneHand = new FilterStata.DataStata[6]; isInitDataStatsOneHand = true;}
               filterStata.countOnePlayerStata(isInGame,pokPos,nicks[pokPos],stacks[pokPos],actionsStreetsStats,isWin,isShowDown,
                       cards[pokPos],rangePlayer,posHero);
            }*/

            mapNameFilterDataStata = mapNicksMapsNameFilterDataStata.get(nicks[pokPos]);
            if(mapNameFilterDataStata==null){ mapNameFilterDataStata = creatMapNameFilterDataStataOnePlayer();mapNicksMapsNameFilterDataStata.put(nicks[pokPos],mapNameFilterDataStata);}

            for(String nameFilter:statsMap.keySet()){
                filterStata2 = statsMap.get(nameFilter);
                dataStata = mapNameFilterDataStata.get(nameFilter);
                filterStata2.countOnePlayerStata(isInGame,pokPos,nicks[pokPos],stacks[pokPos],actionsStreetsStats,isWin,isShowDown,
                        cards[pokPos],rangePlayer,posHero,dataStata);
            }
        }
   }


   private boolean isWin(float resultPlayer){ return resultPlayer>0; }

   private boolean isShowDown(int indPlayer, int alliner, List<List<Float>> actionsRiver,String[] cards){
        if(!isInGame){ return cards != null; }

        // ПОСМОТРЕТЬ в ОСР определение оллинера который рейзил не до конца стека !!!!!!!!!!!!!!!!!!!!!!!!
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
       addStructureOneNewStataToDB(stata);
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

   float getValueOneStata(String nick,String nameFilter,String nameData,int CallOrRaise,int specStata){
        DataStata dataStata = mapNicksMapsNameFilterDataStata.get(nick).get(nameFilter);
        if(specStata!=-1) return procents(dataStata.getSpecStats(specStata));
        return 0;
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


        /*new Work_DataBase();
        WorkStats workStats1 = new WorkStats(false);
        FilterStata filterStata = new FilterStata.Builder().setMainNameFilter("WWSF").setPosStata(new int[][]{{1,1,1,1,1,1},{1,1,1,1,1,1}}).setSpecStats(0).build();

        workStats1.createOneNewStata(filterStata);

        //System.out.println(workStats1.getPreflopRange(new String[]{"2c","As"}));
        close_DataBase();*/
    }


}


