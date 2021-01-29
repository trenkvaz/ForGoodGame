package org.trenkvaz.stats;

import org.trenkvaz.database_hands.Work_DataBase;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.trenkvaz.database_hands.Work_DataBase.addColumnsMainNicksStatsNew;
import static org.trenkvaz.database_hands.Work_DataBase.close_DataBase;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;

public class WorkStats implements Serializable {

    Map<String,Stata> statsMap = new HashMap<>();






   public void fillStataMapsInStatsForGame(){

   }

   public void fillStataMapsInStatsForHistory(){
       addColumnsMainNicksStatsNew(statsMap);
    }

    public void readStatsMap(){
       try {	FileInputStream file=new FileInputStream(home_folder+"\\all_settings\\capture_video\\statsMap.file");
           ObjectInput out = new ObjectInputStream(file);
           statsMap = (Map<String, Stata>) out.readObject();
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

       /*WorkStats workStats = new WorkStats();
       Stata stata = new Stata();
       stata.isInitMeansSetting(true);
       workStats.statsMap.put("test",stata);
       workStats.saveStatsMap();*/
       new Work_DataBase();
        WorkStats workStats1 = new WorkStats();
        workStats1.readStatsMap();
        //workStats1.statsMap.get("test").stataMap.put("hero",new MeansStata().select=1)
        Stata stata = new Stata.Builder().isMeans().isSelect().isPreflopRange().build();
        workStats1.statsMap.put("hero5",stata);
        workStats1.saveStatsMap();
        workStats1.fillStataMapsInStatsForHistory();
        close_DataBase();
    }


}


class MeansStata{
    int select = 0;
    int means = 0;
    int[] preflopRange;
}