package org.trenkvaz.database_hands;

import org.trenkvaz.stats.MainStats;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.main.OcrUtils.get_AmountOneBitInLong;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;

public class CorrectNicksStats {

    static HashMap<Long,String> hashmap_id_img_pix_NEWnick = new HashMap<>();
    public static SortedMap<Long,long[]> sortedmap_all_imgs_pix_of_NEWnicks = new TreeMap<>();
    static HashMap<Long,String> hashmap_id_img_pix_OLDnick = new HashMap<>();
    static SortedMap<Long,long[]> sortedmap_all_imgs_pix_of_OLDnicks = new TreeMap<>();


    // берет хеш изо в новом списке ников и в старом корректируемом списке называемом делет
    // находит одинаковые хеши, но разные ники это значит, что игроки одинаковые, но распознаные по разному можно объеденить их статы и удалить из старого списка такой ник
    // берет статы по старому нику и прибавляет их к статам по новому нику
    // дальше удаляет из списка старых ников такие дубликаты и удаляет эти ники их статы из базы
    // Новый список ников не затрагивается.По нему только определяются наличие дубликатов в базе данных на основе старого списка ников
    // который изменяется удалением из него ников
    // МЕНЯТЬ НАЗВАНИЯ ФАЙЛОВ у нового файла на делетед а старый делетев удалять

    public static void read_Newfile_with_nicks_and_img_pixs(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(home_folder+"\\all_settings\\capture_video\\nicks_img.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                if(!(line.startsWith("*")&&line.endsWith("*")))break;
                String[] arr_line = line.substring(1,line.length()-1).split("%");
                //System.out.println("line "+arr_line.length);
                hashmap_id_img_pix_NEWnick.put(Long.parseLong(arr_line[16]),arr_line[0]);
                long[] img_pix = new long[16];
                for(int i=1; i<17; i++){
                    img_pix[i-1] = Long.parseLong(arr_line[i]);
                }
                sortedmap_all_imgs_pix_of_NEWnicks.put(img_pix[15],img_pix);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void read_OLDfile_with_nicks_and_img_pixs(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(home_folder+"\\arhive_nicks\\nicks_img_deleted.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                if(!(line.startsWith("*")&&line.endsWith("*")))break;
                String[] arr_line = line.substring(1,line.length()-1).split("%");
                //System.out.println("line "+arr_line.length);
                hashmap_id_img_pix_OLDnick.put(Long.parseLong(arr_line[16]),arr_line[0]);
                long[] img_pix = new long[16];
                for(int i=1; i<17; i++){
                    img_pix[i-1] = Long.parseLong(arr_line[i]);
                }
                sortedmap_all_imgs_pix_of_OLDnicks.put(img_pix[15],img_pix);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void rewrite_nicks_keys_img_pix(String nick,long[] imgs_pix_of_nick){
        StringBuilder line = new StringBuilder("*");
        line.append(nick);line.append('%');
        for(long pixs:imgs_pix_of_nick){
            line.append(pixs);
            line.append('%');
        }
        line.deleteCharAt(line.length()-1);
        line.append("*\r\n");

        try (OutputStream os = new FileOutputStream(new File(home_folder+"\\arhive_nicks\\nicks_img_new.txt"),true)) {
            os.write(line.toString().getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
        } catch (IOException s) {
        }
    }

    // коррекция главного списка ников убирание дубликатов и объединение их стат
    // полученный новый файл ников переименовывать в стандартный и переносить в папку настроеек и заменять текущий файл с никами
    static void checkDublicatHashLikeImg(){
        read_Newfile_with_nicks_and_img_pixs();
        //read_OLDfile_with_nicks_and_img_pixs();
        count_one_in_numbers = Settings.read_ObjectFromFile("count_one_in_numbers");
        boolean printid = true;
        Map<String, List<String>> main_map_newnick_oldnicks = new HashMap<>();
        for(long id:sortedmap_all_imgs_pix_of_NEWnicks.keySet()){
            printid = false;
            String nid = hashmap_id_img_pix_NEWnick.get(id);
            for (long id2:sortedmap_all_imgs_pix_of_NEWnicks.keySet()){
                if(!compare_arrlong(sortedmap_all_imgs_pix_of_NEWnicks.get(id),sortedmap_all_imgs_pix_of_NEWnicks.get(id2),15))continue;
                String nid2 = hashmap_id_img_pix_NEWnick.get(id2);
                if(nid.equals(nid2))continue;
                if(main_map_newnick_oldnicks.get(nid2)!=null)continue;

                if(!printid){
                    main_map_newnick_oldnicks.put(nid,new ArrayList<>());
                    printid = true;}
                if(!main_map_newnick_oldnicks.get(nid).contains(nid2)) main_map_newnick_oldnicks.get(nid).add(nid2);
            }
        }
        main_map_newnick_oldnicks.remove("aenea");
        for(String nick:main_map_newnick_oldnicks.keySet()){
            System.out.print(nick+" : ");
            for(String nicks:main_map_newnick_oldnicks.get(nick)) System.out.print(nicks+" ");
            System.out.println();
        }
        boolean iswork = false;
        if(!iswork)return;

        rewriteStats(main_map_newnick_oldnicks);
        // удаление неправильных дубликатов из списка старых ников
        out:for(Map.Entry<Long,String> entry:hashmap_id_img_pix_NEWnick.entrySet()){
            for(List<String> deletednick:main_map_newnick_oldnicks.values())
                if(deletednick.contains(entry.getValue()))continue out;
            rewrite_nicks_keys_img_pix(entry.getValue(),sortedmap_all_imgs_pix_of_NEWnicks.get(entry.getKey()));
        }

    }





    static void select_AllNicks(){
        read_Newfile_with_nicks_and_img_pixs();
        read_OLDfile_with_nicks_and_img_pixs();
        count_one_in_numbers = Settings.read_ObjectFromFile("count_one_in_numbers");
        boolean printid = true;
        Map<String, List<String>> main_map_newnick_oldnicks = new HashMap<>();
        for(long id:sortedmap_all_imgs_pix_of_NEWnicks.keySet()){
            printid = false;
            String nid = hashmap_id_img_pix_NEWnick.get(id);
            for (long id2:sortedmap_all_imgs_pix_of_OLDnicks.keySet()){
                if(!compare_arrlong(sortedmap_all_imgs_pix_of_NEWnicks.get(id),sortedmap_all_imgs_pix_of_OLDnicks.get(id2),10))continue;
                String nid2 = hashmap_id_img_pix_OLDnick.get(id2);
                if(nid.equals(nid2))continue;
                if(!printid){
                    main_map_newnick_oldnicks.put(nid,new ArrayList<>());
                    printid = true;}
                if(!main_map_newnick_oldnicks.get(nid).contains(nid2)) main_map_newnick_oldnicks.get(nid).add(nid2);
            }
        }
        for(String nick:main_map_newnick_oldnicks.keySet()){
            System.out.print(nick+" : ");
            for(String nicks:main_map_newnick_oldnicks.get(nick)) System.out.print(nicks+" ");
            System.out.println();
        }
        boolean iswork = false;
        if(!iswork)return;

        rewriteStats(main_map_newnick_oldnicks);


        // удаление неправильных дубликатов из списка старых ников
      out:for(Map.Entry<Long,String> entry:hashmap_id_img_pix_OLDnick.entrySet()){
            for(List<String> deletednick:main_map_newnick_oldnicks.values())
                if(deletednick.contains(entry.getValue()))continue out;
          rewrite_nicks_keys_img_pix(entry.getValue(),sortedmap_all_imgs_pix_of_OLDnicks.get(entry.getKey()));
        }
    }


    static boolean compare_arrlong(long[] img_min_error, long[] img_nick_for_compare,int privat_error){
        int first_of_pair_error = 0, second_of_pair_error = 0;
        boolean result = true;
        for(int i=0; i<15; i++){
            if(i%2==0)first_of_pair_error = get_AmountOneBitInLong(img_min_error[i]^img_nick_for_compare[i]);
            if(i%2!=0)second_of_pair_error = get_AmountOneBitInLong(img_min_error[i]^img_nick_for_compare[i]);
            if(i>0&&(first_of_pair_error+second_of_pair_error)>privat_error){ return false;  }
        }
        return result;
    }


    static void rewriteStats(Map<String, List<String>> main_map_newnick_oldnicks){

        Work_DataBase work_dataBase = new Work_DataBase();
        MainStats[] mainstats = work_dataBase.fill_MainArrayOfStatsFromDateBase("main_nicks_stats");
        boolean is_cast = false;
        for(MainStats stata:mainstats){
            Map nick_stata = stata.getMap_of_Idplayer_stats();
            for(String Newnick:main_map_newnick_oldnicks.keySet()){
                Object stats_of_new_nick = nick_stata.get("$ю$"+Newnick+"$ю$");
                //System.out.print(Newnick+" ");
                is_cast = false;
                if(stats_of_new_nick instanceof Object[][][] New_stata){
                    is_cast = true;
                    for(String Oldnick:main_map_newnick_oldnicks.get(Newnick)){
                        Object[][][] Old_stata =(Object[][][])nick_stata.get("$ю$"+Oldnick+"$ю$");
                        if(Old_stata==null){
                            //System.out.println(Oldnick+" no stata");
                            continue;
                        }
                        //System.out.println(Oldnick+" is stata");

                        for(int A=0; A<New_stata.length; A++){
                            for(int B=0; B<New_stata[A].length; B++)
                                for(int C=0; C<New_stata[A][B].length; C++){
                                    if(New_stata[A][B][C]==null){
                                        //System.out.print("New==null"); if(Old_stata[A][B][C]==null) System.out.println( "   oldstata C=NULL");
                                        continue; }
                                    /*if(Old_stata[A][B][C]==null) {
                                        System.out.print("Old==null"); if(New_stata[A][B][C]==null) System.out.println( "   newstata C=NULL");
                                        continue; }*/
                                    New_stata[A][B][C]=(int)New_stata[A][B][C]+(int)Old_stata[A][B][C];
                                }
                        }
                    }
                }
                if(!is_cast&&stats_of_new_nick instanceof Object[][] New_stata){
                    for(String Oldnick:main_map_newnick_oldnicks.get(Newnick)){

                        Object[][] Old_stata =(Object[][])nick_stata.get("$ю$"+Oldnick+"$ю$");
                        if(Old_stata==null){
                            //System.out.println(Oldnick+" no stata");
                            continue;
                        }
                        //System.out.println(Oldnick+" is stata");
                        for(int A=0; A<New_stata.length; A++)
                            for(int B=0; B<New_stata[A].length; B++) New_stata[A][B]=(int)New_stata[A][B]+(int)Old_stata[A][B];
                    }
                }




                //if(Newnick.equals("jusder")) break;
            }

           //break;
        }
        record_MainArrayOfStatsToDateBase(mainstats);
        System.out.println("++++++++++++++++++++");
        List<String> nicks_for_delete = new ArrayList<>();
        for(String Newnick:main_map_newnick_oldnicks.keySet())
            for(String Oldnick:main_map_newnick_oldnicks.get(Newnick))nicks_for_delete.add(Oldnick);

        delete_LinesByNick(nicks_for_delete);
        //nicks_for_delete.forEach(System.out::println);

        delete_and_copy_WorkNicksStats();
        close_DataBase();
    }

    public static void main(String[] args) {
        select_AllNicks();
        //checkDublicatHashLikeImg();
        // смещение битов последнего числа
       /* read_OLDfile_with_nicks_and_img_pixs();
        for(Map.Entry<Long,long[]> entry:sortedmap_all_imgs_pix_of_OLDnicks.entrySet()){
            entry.getValue()[14] = entry.getValue()[14]<<14;
            String nid = hashmap_id_img_pix_OLDnick.get(entry.getKey());
            rewrite_nicks_keys_img_pix(nid,entry.getValue());
        }*/


    }
}
