package org.trenkvaz.newstats;

import org.trenkvaz.database_hands.Work_DataBase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.main.OCR.FLOP;
import static org.trenkvaz.ui.StartAppLauncher.*;

public class WorkStats implements Serializable {
    public boolean isInGame;
    public static boolean isRecoverStats = false;
    private static final int[] preflopPoses = {0,1,2,3,4,5};
    private static final int[] postflopPoses = {4,5,0,1,2,3};

    Map<String, FilterStata> statsMap;
    Map<String,Map<String, DataStata>> mapNicksMapsNameFilterDataStata = new HashMap<>();


    public WorkStats(boolean isInGame1){ isInGame = isInGame1;
        readStatsMap();
        if(!isRecoverStats){
        checkExistStructureDBofFilterStats();
        }
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

        // TEST
        //int mypos = IntStream.range(0,6).filter(c->nicks[c]!=null).filter(c->nicks[c].equals("trenkvaz")).findFirst().getAsInt();


        for(int pokPos=0; pokPos<6; pokPos++){
            if(nicks[pokPos]==null)continue;
            rangePlayer = 0;
            if(!isInGame){ if(cards[pokPos][0]!=null) rangePlayer = getPreflopRange(cards[pokPos]);
                // вин и щоудаун на игрока нужны для разных фильтров поэтому нужно один раз определить заранее
                isWin = isWin(resultsHand[pokPos]);
                isShowDown = isShowDown(pokPos,alliners,sizeActionsStreetsStats.get(3),cards);
            } // карты обрабатываются только вне игры вин и шоудаун пока только вне игры
            nicks[pokPos] = "$ю$"+nicks[pokPos]+"$ю$";

           /* int testMypos = -1;
            if(nicks[pokPos].equals("$ю$trenkvaz$ю$")){ testMypos = pokPos; }*/

            mapNameFilterDataStata = mapNicksMapsNameFilterDataStata.get(nicks[pokPos]);
            if(mapNameFilterDataStata==null){ mapNameFilterDataStata = creatMapNameFilterDataStataOnePlayer();mapNicksMapsNameFilterDataStata.put(nicks[pokPos],mapNameFilterDataStata);}

            for(String nameFilter:statsMap.keySet()){
                filterStata = statsMap.get(nameFilter);
                dataStata = mapNameFilterDataStata.get(nameFilter);
                filterStata.countOnePlayerStata(isInGame,pokPos,nicks[pokPos],stacks[pokPos],sizeActionsStreetsStats,isWin,isShowDown,
                        cards[pokPos],rangePlayer,posHero,dataStata,listPokerActionsInRoundsByPositions,null);
            }
        }

       if(isInGame)recordNewStats(nicks,statsMap,mapNicksMapsNameFilterDataStata);
   }


    public void saveAllCountedStats(){ recordNewStats(null,statsMap,mapNicksMapsNameFilterDataStata); }



    public List<int[][]> getListPokerActionsInRoundsByPositions(List<List<List<Float>>> sizeActionsStreetsStats){
        List<int[][]> result = new ArrayList<>(4);
        int[] placePos = null;
        for(int street=0; street<4; street++){
        int maxSizeListActions = sizeActionsStreetsStats.get(street).stream().mapToInt(List::size).max().getAsInt();
        //if(street==0) System.out.println(RED+"max "+maxSizeListActions+RESET);
        placePos = postflopPoses;
        int cor = 0; if(street==0){cor=1; placePos = preflopPoses; }
        int[][] roundsPosAct = new int[maxSizeListActions-cor][6];
        int raise = cor;
        for(int act=cor; act<maxSizeListActions; act++)
            for(int pokPos=0; pokPos<6; pokPos++){
                if(sizeActionsStreetsStats.get(street).get( placePos[pokPos]).size()-1<act)continue;
                float action = sizeActionsStreetsStats.get(street).get( placePos[pokPos]).get(act);
                if(action==Float.NEGATIVE_INFINITY)roundsPosAct[act-cor][ placePos[pokPos]]=-10;
                else if(action==Float.POSITIVE_INFINITY)roundsPosAct[act-cor][ placePos[pokPos]]= 10;
                else if(action!=Float.NEGATIVE_INFINITY&&action<0)roundsPosAct[act-cor][ placePos[pokPos]]= -(raise);
                else if(action!=Float.POSITIVE_INFINITY&&action>0){ if(raise==5)roundsPosAct[act-cor][ placePos[pokPos]] = raise;
                else roundsPosAct[act-cor][ placePos[pokPos]] = ++raise;}
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
       if(isRecoverStats){
           stata.isCreateStructureDB = true;
           statsMap.put(stata.getFullNameStata(),stata);
           saveStatsMap();
           return;
       }

       // проверка на дубликат имени статы или перезапись статы
       if(statsMap.get(stata.getFullNameStata())!=null){ deleteFilterStata(stata); }
       stata.isCreateStructureDB = true;
       statsMap.put(stata.getFullNameStata(),stata);
       saveStatsMap();
       addStructureOneNewStataToDB(stata);
       writeDescriptionFilterStata(stata);
   }



   public void deleteFilterStata(FilterStata stata){
       String workOrTest = "\\all_settings\\capture_video\\descriptions_filterstata.txt";
       if(isTestDBandStats)workOrTest = "\\all_settings_test\\descriptions_filterstata.txt";
       System.out.println("DELETE");
       List<String> listFilterStata =new ArrayList<>();
       try { BufferedReader br = new BufferedReader(new FileReader(new File(home_folder+workOrTest)));
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
       for(FilterStata filterStata:statsMap.values()){
           if(!filterStata.isCreateStructureDB){
               filterStata.isCreateStructureDB = true;
               addStructureOneNewStataToDB(filterStata);
           }                                                                          // TEST !!!!!!!!!!!!!!!!!!!!!

           /*filterStata.isCreateStructureDB = true;
           addStructureOneNewStataToDB(filterStata);*/
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

    public static float procents(int stata, int select){
        if(select==0)return 0;
        return ((float)stata/(float)select)*100;
    }


    public void fullMapNicksMapsNameFilterDataStata(String mainORwork){
       //mapNicksMapsNameFilterDataStata = getMapNicksMapsNameFilterDataStata(statsMap,mainORwork);

       //if(isTestDBandStats)
           mapNicksMapsNameFilterDataStata = getMapNicksMapsNameFilterDataStataTest(statsMap,mainORwork);
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








    public static void main(String[] args) {


        //initOldFilterStats();
        //addNewFilteStats();
        //getNamesFilterStats();
        //addNewFilteStats();
        //testGetStata();


    }


}


