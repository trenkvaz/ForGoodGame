package org.trenkvaz.newstats;

import org.trenkvaz.database_hands.Work_DataBase;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;

public class WorkStats implements Serializable {

    Map<String, FilterStata> statsMap = new HashMap<>();






   public void fillStataMapsInStatsForGame(){

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


        new Work_DataBase();
        /*WorkStats workStats1 = new WorkStats();
        FilterStata filterStata = new FilterStata.Builder().setMainNameFilter("v4bet").setPosStata(new int[][]{{0,1,1,1,1,1},{1,1,1,1,1,1}}).isRange().build();
        FilterStata filterStata2 = new FilterStata.Builder().setMainNameFilter("v4bet").setPosStata(new int[][]{{0,0,1,1,1,1},{1,1,1,1,1,1}}).isRange().build();
        workStats1.createOneNewStata(filterStata);
        workStats1.createOneNewStata(filterStata2);*/
        close_DataBase();
    }


}


