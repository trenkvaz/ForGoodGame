package org.trenkvaz.newstats;

import org.trenkvaz.database_hands.Work_DataBase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;
import static org.trenkvaz.ui.StartAppLauncher.isTestDBandStats;

public class WorkStats implements Serializable {
    public boolean isInGame;

    Map<String, FilterStata> statsMap;
    Map<String,Map<String, DataStata>> mapNicksMapsNameFilterDataStata = new HashMap<>();


    public WorkStats(boolean isInGame1){ isInGame = isInGame1;
        readStatsMap();
        checkExistStructureDBofFilterStats();
    }

    public WorkStats(String type){
        //readStatsMap();
        if(type.equals("addAndCountNewStats")){
            statsMap = new HashMap<>();

        } else readStatsMap();
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
        }
       //recordNewStats(nicks,statsMap,mapNicksMapsNameFilterDataStata);
   }


    public void saveAllCountedStats(){ recordNewStats(null,statsMap,mapNicksMapsNameFilterDataStata); }



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
       // проверка на дубликат имени статы или перезапись статы
       if(statsMap.get(stata.getFullNameStata())!=null){ deleteFilterStata(stata); }
       stata.isCreateStructureDB = true;
       statsMap.put(stata.getFullNameStata(),stata);
       saveStatsMap();
       addStructureOneNewStataToDB(stata);
       writeDescriptionFilterStata(stata);
   }


   public void recoverFilterStata(FilterStata stata){
       stata.isCreateStructureDB = true;
       statsMap.put(stata.getFullNameStata(),stata);
       saveStatsMap();
   }

   public void deleteFilterStata(FilterStata stata){
       System.out.println("DELETE");
       List<String> listFilterStata =new ArrayList<>();
       try { BufferedReader br = new BufferedReader(new FileReader(new File(home_folder+"\\all_settings\\capture_video\\descriptions_filterstata.txt")));
           String line;while ((line = br.readLine()) != null) { listFilterStata.add(line); }br.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
       List<String> listFiltersStataForDelete = new ArrayList<>();
       if(stata!=null){
           listFiltersStataForDelete.add(stata.getFullNameStata());
           listFilterStata.removeIf(s -> s.contains(stata.getFullNameStata()));
           listFilterStata.forEach(System.out::println);
           System.out.println("for delete");
           for(String l:listFiltersStataForDelete) System.out.println("*"+l+"*");

       } else {
           Iterator<String> iterator = listFilterStata.listIterator();
           while (iterator.hasNext()){
               String line = iterator.next();
               if(line.contains("D ")){
                   listFiltersStataForDelete.add(line.split(" ")[1].replace("\"",""));
                   iterator.remove();
               }
           }
       }



       String writeLines = "";
       for(String line:listFilterStata)writeLines+=line+"\r\n";
       //System.out.println(writeLines);
       writeDescriptions(writeLines,false);
       for(String nameForDelete:listFiltersStataForDelete){
           deleteStructureOneNewStataToDB(statsMap.get(nameForDelete));
           statsMap.remove(nameForDelete);
       }
       if(stata==null)saveStatsMap();
   }


   private void checkExistStructureDBofFilterStats(){
       for(FilterStata filterStata:statsMap.values())
           if(!filterStata.isCreateStructureDB){
               filterStata.isCreateStructureDB = true;
               addStructureOneNewStataToDB(filterStata);
           }
   }

   public Map<String, DataStata> creatMapNameFilterDataStataOnePlayer(){
       Map<String, DataStata> result = new HashMap<>();
       for(String nameFilter:statsMap.keySet()){ result.put(nameFilter,new DataStata(statsMap.get(nameFilter))); }
       return result;
   }



   public int[] getValueOneStata(String nick, String nameFilter, int stata){
        if(mapNicksMapsNameFilterDataStata.get("$ю$"+nick+"$ю$")==null)return null;
        DataStata dataStata = mapNicksMapsNameFilterDataStata.get("$ю$"+nick+"$ю$").get(nameFilter);
        switch (stata){
            case 0->{ return  dataStata.mainSelCallRaise;}
            case 5->{ return  dataStata.W$WSF;}
            case 6->{ return  dataStata.WTSD;}
            case 7->{ return  dataStata.W$SD;}
            case 8->{ return  dataStata.VPIP_PFR;}
        }
        return null;
   }

    private static float procents(int stata, int select){
        if(select==0)return 0;
        return ((float)stata/(float)select)*100;
    }


    public void fullMapNicksMapsNameFilterDataStata(String mainORwork){
       mapNicksMapsNameFilterDataStata = getMapNicksMapsNameFilterDataStata(statsMap,mainORwork);

       if(isTestDBandStats)mapNicksMapsNameFilterDataStata = getMapNicksMapsNameFilterDataStataTest(statsMap,mainORwork);
    }





   public void readStatsMap(){
       String workOrTest = "\\all_settings\\capture_video\\statsMap.file";
       if(isTestDBandStats)workOrTest = "\\all_settings_test\\statsMap.file";
       try {	FileInputStream file=new FileInputStream(home_folder+workOrTest);
           ObjectInput out = new ObjectInputStream(file);
           statsMap = (Map<String, FilterStata>) out.readObject();
           out.close();
           file.close();
       } catch(IOException e) {
           System.out.println("HERE   "+e);
           statsMap = new HashMap<>();
       } catch (ClassNotFoundException e) {
           e.printStackTrace();
           statsMap = new HashMap<>();
       }
   }

    public void saveStatsMap(){
       String workOrTest = "\\all_settings\\capture_video\\statsMap.file";
       if(isTestDBandStats)workOrTest = "\\all_settings_test\\statsMap.file";
        try {
            FileOutputStream file=new FileOutputStream(home_folder+workOrTest);
            ObjectOutput out = new ObjectOutputStream(file);
            out.writeObject(statsMap);
            out.close();
            file.close();
        } catch(IOException e) {
            System.out.println(e);
        }
    }


    public void writeDescriptionFilterStata(FilterStata stata){
        String line = "";
        line+="\""+stata.getFullNameStata()+"\"  ";
        for(int i=0; i<stata.structureParametres.length; i++)
            if(stata.structureParametres[i])line+="\""+strStatsValues[i]+"\"  ";
        line+="\r\n";
        writeDescriptions(line,true);
    }


    private void writeDescriptions(String lines,boolean isAppend){
        System.out.println("WRITE");
        String workOrTest = "\\all_settings\\capture_video\\descriptions_filterstata.txt";
        if(isTestDBandStats)workOrTest = "\\all_settings_test\\descriptions_filterstata.txt";
        try (OutputStream os = new FileOutputStream(home_folder+workOrTest,isAppend)) {
            os.write(lines.getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
        } catch (IOException s) {
        }
    }


    static void initOldFilterStats(){
        new Work_DataBase();
        WorkStats workStats1 = new WorkStats("addAndCountNewStats");
        FilterStata filterStata = new FilterStata.Builder().setMainNameFilter("main_wsd_").setPosStata(new int[]{1,1,1,1,1,1},new int[][]{{1,1,1,1,1,1}}).setSpecStats(2).build();
        FilterStata filterStata1 = new FilterStata.Builder().setMainNameFilter("main_wtsd_").setPosStata(new int[]{1,1,1,1,1,1},new int[][]{{1,1,1,1,1,1}}).setSpecStats(1).build();
        FilterStata filterStata2 = new FilterStata.Builder().setMainNameFilter("main_wwsf_").setPosStata(new int[]{1,1,1,1,1,1},new int[][]{{1,1,1,1,1,1}}).setSpecStats(0).build();
       /* workStats1.recoverFilterStata(filterStata);
        workStats1.recoverFilterStata(filterStata1);
        workStats1.recoverFilterStata(filterStata2);*/
        workStats1.createOneNewStata(filterStata);
        workStats1.createOneNewStata(filterStata1);
        workStats1.createOneNewStata(filterStata2);
        close_DataBase();
    }


    static void addNewFilteStats(){
        new Work_DataBase();
        WorkStats workStats1 = new WorkStats(false);
        List<int[]> conditionsPreflopActions = new ArrayList<>();
        // ACT_PLAYER = 0,  LIMP = 1, LIMPS = 2, RAISER = 3, CALLERS = 4,  _3BET = 5, CALLERS_3BET= 6, _4BET = 7, CALLERS_4BET= 8, _5BET = 9, CALLERS_5BET= 10;
        conditionsPreflopActions.add(new int[]{0,-1,-1, 2, -1,-1,-1,-1,-1,-1,-1});
        String nameStata = "vRFI_";
        /*conditionsPreflopActions.add(new int[]{3,-1,-1, 2, -1,-1,-1,-1,-1,-1,-1});
        conditionsPreflopActions.add(new int[]{0,-1,-1, -1, -1,-1,-1,2,-1,-1,-1});
        String nameStata = "v4bet_";*/
        FilterStata filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,1,0,0,0,0},new int[][]{{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata);
        FilterStata filterStata1 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,1,0,0},new int[][]{{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata1);
        FilterStata filterStata2 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,1},new int[][]{{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata2);
        FilterStata filterStata3 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,1,0,0},new int[][]{{0,1,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata3);
        FilterStata filterStata4 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,1},new int[][]{{0,1,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata4);
        FilterStata filterStata5 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,1,0,0},new int[][]{{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata5);
        FilterStata filterStata6 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata6);
        FilterStata filterStata7 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata7);
        FilterStata filterStata8 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{0,0,0,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata8);
        FilterStata filterStata9 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,0,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata9);
        FilterStata filterStata10 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,0,0,1,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata10);
        /*FilterStata filterStata = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,1,0,0,0,0},new int[][]{{1,0,0,0,0,0},{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata);
        FilterStata filterStata1 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,1,0,0},new int[][]{{1,0,0,0,0,0},{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata1);
        FilterStata filterStata2 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,1},new int[][]{{1,0,0,0,0,0},{1,0,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata2);
        FilterStata filterStata3 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,1,1,0,0},new int[][]{{0,1,0,0,0,0},{0,1,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata3);
        FilterStata filterStata4 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,1},new int[][]{{0,1,0,0,0,0},{0,1,0,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata4);
        FilterStata filterStata5 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,1,0,0},new int[][]{{0,0,1,0,0,0},{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata5);
        FilterStata filterStata6 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{0,0,1,0,0,0},{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata6);
        FilterStata filterStata7 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,1,0,0,0},{0,0,1,0,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata7);
        FilterStata filterStata8 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,1,0},new int[][]{{0,0,0,1,0,0},{0,0,0,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata8);
        FilterStata filterStata9 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,0,1,0,0},{0,0,0,1,0,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata9);
        FilterStata filterStata10 = new FilterStata.Builder().setMainNameFilter(nameStata)
                .setPosStata(new int[]{0,0,0,0,0,1},new int[][]{{0,0,0,0,1,0},{0,0,0,0,1,0}}).setStreetOfActs(0).setConditionsPreflopActions(conditionsPreflopActions).build();
        workStats1.createOneNewStata(filterStata10);*/
        close_DataBase();
    }

    static void testGetStata(){
       new Work_DataBase();
      WorkStats  workStats = new WorkStats(false);
      workStats.fullMapNicksMapsNameFilterDataStata("work_");
        close_DataBase();
       /* squeeze_co_v_utg_
                squeeze_bu_v_utg_mp_
        squeeze_sb_v_co_
                squeeze_sb_v_utg_mp_
        squeeze_bb_v_co_
                squeeze_bb_v_utg_mp_
        squeeze_bb_v_bu_*/
      int[] stats = workStats.getValueOneStata("trenkvaz","main_wwsf_all_v_all",5);
        System.out.println("stata "+stats[0]+" "+stats[1]+" ");
        //System.out.println("stata "+stats[0]+" "+stats[1]+" "+stats[2]);
    }

    static void getNamesFilterStats(){
        WorkStats  workStats = new WorkStats("stats");
       for(Map.Entry<String,FilterStata> entry:workStats.statsMap.entrySet())
           System.out.println(entry.getKey());
    }

    public static void main(String[] args) {


        //initOldFilterStats();
        addNewFilteStats();
        getNamesFilterStats();
        //addNewFilteStats();
        //testGetStata();
    }


}


