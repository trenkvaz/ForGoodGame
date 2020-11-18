package org.trenkvaz.main;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static org.trenkvaz.main.CaptureVideo.current_map_stats;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;
import static org.trenkvaz.ui.StartAppLauncher.hud;

public class CreatingHUD {

     int table = -1;
     Object[][] arr_player_indstat_stata = new Object[6][];
     List<List<Text>> list_current_one_table_texts_huds_each_player;
     record SettingOneStata(String name_stata, int[] coord_text, int size_font, RangeColor rangeColor, int min_select, int condition_show, int[] show_on_poses_hero ){
         public Paint get_ColorByRangeOfStata(float stata){
             int range= -1;
             for(int i_range=0; i_range<rangeColor.ranges.length; i_range++){ if(rangeColor.ranges[i_range]<stata){range++; continue;}break; }
             return rangeColor.colors[range];
         }
         public boolean let_ShowStataOfPositionsHero(int position_hero){
             for(int pos:show_on_poses_hero)if(pos==position_hero)return true;
             return false;
         }
     }
     record RangeColor(int[] ranges, Paint[] colors){}
     static Map<String,Integer[]> map_descriptions_of_stats;
     static List<SettingOneStata> list_settings_one_stats;


    static DecimalFormat notZeroFormat = (DecimalFormat)NumberFormat.getNumberInstance(Locale.UK);


     public CreatingHUD(int table1){
         table = table1;
         //for(int i=0; i<6; i++) arr_player_indstat_stata[i] = new Object[current_map_stats.length][];
         list_current_one_table_texts_huds_each_player = set_ListCurrentOneTableTextsHudsEachPlayer();
         Setting.setting_CreatingHUD();
     }


     private List<List<Text>> set_ListCurrentOneTableTextsHudsEachPlayer(){
         List<List<Text>> result = new ArrayList<>(6); for(int i=0; i<6; i++)result.add(new ArrayList<>());return result; }



     public void clear_MapStats(){
         for(int i=0; i<6; i++) { arr_player_indstat_stata[i] = new Object[current_map_stats.length][];list_current_one_table_texts_huds_each_player.get(i).clear(); }
     }



     public void send_current_hand_to_creating_hud(String[] nicks, int[] inds_poker_pos_elements_places_table,int poker_position_of_hero){

         for(int table_place = 0; table_place<6; table_place++){
             if(nicks[table_place]==null)continue;
           // если ник и статы уже были преобразованы в текст и сохранены в текущем списке, то они берутся для нового списка текста
             // ПОКА ТАК !!! так как это может в будущем мешать сделать изменяемый по улицам и действиям ХАД
             // по идеи тогда не нужен массив nicks_for_hud , так как проверяется тоже самое
             if(!list_current_one_table_texts_huds_each_player.get(table_place).isEmpty())continue;
             list_current_one_table_texts_huds_each_player.get(table_place).add(get_NickText(nicks[table_place]));

           add_StatsToListForHUD(nicks[table_place],list_current_one_table_texts_huds_each_player.get(table_place),
                   get_ArrayIndex(inds_poker_pos_elements_places_table,table_place+1),table_place,poker_position_of_hero);
         }
         hud.set_hud(copy_ListCreatingHUDtoListHUD(list_current_one_table_texts_huds_each_player),table);
     }


    private List<List<Text>> copy_ListCreatingHUDtoListHUD(List<List<Text>> arr_one_table_texts_huds_each_player){
       List<List<Text>> result = new ArrayList<>(6);
        for(int player=0; player<6; player++){
            List<Text> copylist = new ArrayList<>(arr_one_table_texts_huds_each_player.get(player));
            result.add(copylist);
        }
        return result;
    }


      private Text get_NickText(String nick){
          if(nick.length()>5)nick = nick.substring(0,5);
          Text text_nick = new Text(1, 12, nick);
          text_nick.setFont(new Font(12));
          text_nick.setFill(Color.YELLOW);
          return text_nick;
      }



      private void add_StatsToListForHUD(String nick,List<Text> list_text_hud_one_player, int poker_position, int table_place,int poker_position_of_hero){

        for(SettingOneStata settingOneStata:list_settings_one_stats){
            //if(settingOneStata.name_stata.equals("fold_to_steal_BUvCO")) System.out.println("start");
            // если отображение статы привязано к позиции, но она не совпадает с текущей позой то пропускается
            if(settingOneStata.condition_show<6&&settingOneStata.condition_show!=poker_position)continue;
            //если отображение статы привязано к позиции, и она совпадает с текущей позой, но не соотвествует отображению относительно позы героя то пропускается
            if((settingOneStata.condition_show==poker_position||settingOneStata.condition_show==6)&&(!settingOneStata.let_ShowStataOfPositionsHero(poker_position_of_hero)))continue;


            // можно заранее подготовить текст так как в любом случае будет хотя бы ноль
            Text text = new Text(settingOneStata.coord_text[0], settingOneStata.coord_text[1],"" );
            text.setFont(new Font(settingOneStata.size_font));


            Integer[] description = map_descriptions_of_stats.get(settingOneStata.name_stata);

            Object main_stats = arr_player_indstat_stata[table_place][description[0]];
            if(main_stats==null){
                main_stats =current_map_stats[description[0]].get("$ю$"+nick+"$ю$");
                // статы может не быть в таком случае нужно выставить ноль в текст тогда по этому игроку не будет больше попыток получить стату так как сохранится текст
                // также нужно в текущий массив со статами нужно добавить некое значение ПУСТЫШКУ пусть Object[0], показывающее, что не нужно пытаться получить эту стату
                if(main_stats==null) {
                    //if(settingOneStata.name_stata.equals("fold_to_steal_BUvCO")) System.err.println("NULL  pos "+poker_position+" hero "+poker_position_of_hero+" table "+table);
                    text.setText("--"); text.setFill(Color.WHITE); list_text_hud_one_player.add(text);
                    arr_player_indstat_stata[table_place][description[0]] =  new Object[0];
                    continue;
                }
                arr_player_indstat_stata[table_place][description[0]] = main_stats;
            }
             // проверка на пустышку в текущем массиве стат
            if(main_stats instanceof Object[]&&((Object[]) main_stats).length==0){
                //if(settingOneStata.name_stata.equals("fold_to_steal_BUvCO")) System.err.println("NULL 222  pos "+poker_position+" hero "+poker_position_of_hero+" table "+table);
                text.setText("--");  text.setFill(Color.WHITE); list_text_hud_one_player.add(text); continue;
            }

              // ДОПИСАТЬ приведение для разных размерностей массивов стат
            float stata = -1;int select = -1;
            if(main_stats instanceof Object[][][] casting_stata){
                // общая стата может быть но по конкретной стате 0 выборки значит считай тоже нет
                select = (int)casting_stata[description[3]][description[4]][description[1]];
                if(select==0){ text.setText("--"); text.setFill(Color.WHITE); list_text_hud_one_player.add(text); continue; }
                // итог добавление статы
                stata = BigDecimal.valueOf(procents((int) casting_stata[description[3]][description[4]][description[2]],
                        (int) casting_stata[description[3]][description[4]][description[1]])).setScale(1, RoundingMode.HALF_UP).floatValue();
                // для отображения двузначных чисел целыми, а однозначных с дробью, плюс 0 без дроби и 100 как 99
                if(stata>=10)text.setText((stata>=99)? "99":Integer.toString(Math.round(stata)));
                else  text.setText((stata==0)? "0":notZeroFormat.format(stata));
                if(stata==0)text.setFill(Color.WHITE);else text.setFill(settingOneStata.get_ColorByRangeOfStata(stata));
            }

            if(stata==-1&&select==-1) {
                Object[][] casting_stata = (Object[][]) main_stats;
                select = (int)casting_stata[description[3]][description[1]];
                // общая стата может быть но по конкретной стате 0 выборки значит считай тоже нет
                if(select==0){ text.setText("--"); text.setFill(Color.WHITE); list_text_hud_one_player.add(text); continue; }
                // итог добавление статы
                stata = BigDecimal.valueOf(procents((int) casting_stata[description[3]][description[2]],
                        (int) casting_stata[description[3]][description[1]])).setScale(1, RoundingMode.HALF_UP).floatValue();
                // для отображения двузначных чисел целыми, а однозначных с дробью, плюс 0 без дроби и 100 как 99
                if(stata>=10)text.setText((stata>=99)? "99":Integer.toString(Math.round(stata)));
                else  text.setText((stata==0)? "0":notZeroFormat.format(stata));
                if(stata==0)text.setFill(Color.WHITE);else text.setFill(settingOneStata.get_ColorByRangeOfStata(stata));
            }


            list_text_hud_one_player.add(text);
            //System.out.println(stata+"   lengh text "+text.textProperty().length().get()+" ");
            //if(settingOneStata.name_stata.equals("fold_to_steal_BUvCO")) System.err.println("---------------      BUvCO "+text.getText()+"   stata "+stata+" table "+table);
            // проверка выборки
            if(select<settingOneStata.min_select){
                // если выборка меньше порога то справа от статы отображается маленькое число выборка для этого нужна длина текста статы
                int text_length = text.textProperty().length().get();
                text = new Text(settingOneStata.coord_text[0]+text_length*7, settingOneStata.coord_text[1]+3,"" );
                text.setFont(new Font(settingOneStata.size_font-4));
                text.setFill(Color.GRAY);
                text.setText(Integer.toString(select));
                    /*if(settingOneStata.name_stata.equals("fold_to_steal_BUvCO")) System.err.println("SELECT    ---------------      BUvCO "+text.getText()+"  select "
                            +(int)(casting_stata[description[3]][description[4]][description[1]])+" table "+table);*/
                list_text_hud_one_player.add(text);
            }
        }

      }






    public static int get_ArrayIndex(int[] arr,int value) {
        for(int i=0;i<arr.length;i++)
            if(arr[i]==value) return i;
        return -1;
    }

    private static float procents(int stata, int select){
        if(select==0)return 0;
        return ((float)stata/(float)select)*100;
    }


    public static class Setting{

        private static File file_setting_coords_hud;

        public static int[][] read_coords_hud(){
           file_setting_coords_hud = new File(home_folder+"\\all_settings\\hud\\coords_hud.txt");
           int[][] result = new int[][]{{262,357},{12,191},{12,50},{365,17},{519,57},{519,170}};

            try {
                BufferedReader br = new BufferedReader(new FileReader(file_setting_coords_hud));
                String text = br.readLine();
                if(text==null)return result;
                String[] line = text.split(",");
                int c =-1, i = 0;
                for(String coord:line){
                    //System.out.println("*"+coord+"*");
                    c++;
                    result[i][c] = Integer.parseInt(coord);
                    if(c==1){c=-1; i++;}
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        public static void write_coords_hud(int[][] coords){

            StringBuilder line = new StringBuilder();
            for (int[] p:coords) line.append(p[0]).append(",").append(p[1]).append(",");

            try (OutputStream os = new FileOutputStream(file_setting_coords_hud,false)) {
                os.write(line.toString().getBytes(StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                //ErrorLog("016 "+e);
            /*try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                //ErrorLog("032"+e1);
            }*/
            } catch (IOException s) {
                //ErrorLog("017 "+s);
            }
        }


        public static void setting_CreatingHUD(){
            map_descriptions_of_stats = get_map_DescriptionsOfStats();
            list_settings_one_stats = get_list_SettingOneStata();
            notZeroFormat.applyPattern("0.0");
        }





        public static Map<String,Integer[]> get_map_DescriptionsOfStats(){
            Map<String,Integer[]> map_descriptions_of_stats = new HashMap<>();
            // {new AgainstRFI(),new Against3bet(),new VpipPFR3bet(),new RFI(),new Alliners()};
            // Integer: 0 - индекс статы в конкурентмапе всех стат, 1 индекс выборки статы, 2 индекс статы, 3 позиция героя или индекс общей статы,
            // 4 возможные позиции оппонентов складываются результаты против нескольких позиций выборка тоже складывается
            // может вообще не быть ни одного индекса, это значит, что стата специфичная или только важна поза героя

            map_descriptions_of_stats.put("VPIP",new Integer[]{2,0,1,6});
            map_descriptions_of_stats.put("PFR",new Integer[]{2,0,2,6});
            map_descriptions_of_stats.put("RFI_UTG",new Integer[]{3,0,1,0});
            map_descriptions_of_stats.put("RFI_MP",new Integer[]{3,0,1,1});
            map_descriptions_of_stats.put("RFI_CO",new Integer[]{3,0,1,2});
            map_descriptions_of_stats.put("RFI_BU",new Integer[]{3,0,1,3});
            map_descriptions_of_stats.put("RFI_SB",new Integer[]{3,0,1,4});
            map_descriptions_of_stats.put("fold_to_steal_BUvCO",new Integer[]{0,0,1,3,2});
            map_descriptions_of_stats.put("fold_to_steal_SBvCO",new Integer[]{0,0,1,4,2});
            map_descriptions_of_stats.put("fold_to_steal_BBvCO",new Integer[]{0,0,1,5,2});

            map_descriptions_of_stats.put("fold_to_steal_SBvBU",new Integer[]{0,0,1,4,3});
            map_descriptions_of_stats.put("fold_to_steal_BBvBU",new Integer[]{0,0,1,5,3});
            map_descriptions_of_stats.put("fold_to_steal_BBvSB",new Integer[]{0,0,1,5,4});
            return map_descriptions_of_stats;
        }


        public static List<SettingOneStata> get_list_SettingOneStata(){
            List<SettingOneStata> result_list = new ArrayList<>();
            //SettingOneStata(String name_stata,int[] coord_text, int size_font, Paint color, int min_select, int condition_show, int befor_or_after_hero )
            // condition_show 0-5 при нахождении на позиции 6 всегда  , массив позиций херо который показывает когда можно показывать стату позы помечены своими номерами
            RangeColor vpipRangeColor = new RangeColor(new int[]{0,15,35,50,101},new Paint[]{Color.RED,Color.ORANGE,Color.GREEN,Color.PURPLE});
            RangeColor pfrRangeColor = new RangeColor(new int[]{0,12,25,35,101},new Paint[]{Color.RED,Color.ORANGE,Color.BLUE,Color.PURPLE});
            RangeColor rfiUtgRangeColor = new RangeColor(new int[]{0,10,20,101},new Paint[]{Color.RED,Color.ORANGE,Color.GREEN});
            RangeColor rfiMpRangeColor = new RangeColor(new int[]{0,12,22,101},new Paint[]{Color.RED,Color.ORANGE,Color.GREEN});
            RangeColor rfiCoRangeColor = new RangeColor(new int[]{0,22,30,101},new Paint[]{Color.RED,Color.ORANGE,Color.GREEN});
            RangeColor rfiBuRangeColor = new RangeColor(new int[]{0,30,45,101},new Paint[]{Color.RED,Color.ORANGE,Color.GREEN});
            RangeColor rfiSbRangeColor = new RangeColor(new int[]{0,32,45,101},new Paint[]{Color.RED,Color.ORANGE,Color.GREEN});
            RangeColor foldToStealBUvCORangeColor = new RangeColor(new int[]{0,80,101},new Paint[]{Color.RED,Color.GREEN});
            RangeColor foldToStealSBvCORangeColor = new RangeColor(new int[]{0,80,101},new Paint[]{Color.RED,Color.GREEN});
            RangeColor foldToStealBBvCORangeColor = new RangeColor(new int[]{0,80,101},new Paint[]{Color.RED,Color.GREEN});
            RangeColor foldToStealSBvBURangeColor = new RangeColor(new int[]{0,75,101},new Paint[]{Color.RED,Color.GREEN});
            RangeColor foldToStealBBvBURangeColor = new RangeColor(new int[]{0,70,101},new Paint[]{Color.RED,Color.GREEN});
            RangeColor foldToStealBBvSBRangeColor = new RangeColor(new int[]{0,65,101},new Paint[]{Color.RED,Color.GREEN});
            int[] allPositionsHero = {0,1,2,3,4,5};
            int[] coPosHero = {2};
            int[] buPosHero = {3};
            int[] sbPosHero = {4};
            result_list.add(new SettingOneStata("VPIP",new int[]{50,12},14,vpipRangeColor,10,6,allPositionsHero));
            result_list.add(new SettingOneStata("PFR",new int[]{75,12},14,pfrRangeColor,10,6,allPositionsHero));
            result_list.add(new SettingOneStata("RFI_UTG",new int[]{1,25},14,rfiUtgRangeColor,10,6,allPositionsHero));
            result_list.add(new SettingOneStata("RFI_MP",new int[]{23,25},14,rfiMpRangeColor,10,6,allPositionsHero));
            result_list.add(new SettingOneStata("RFI_CO",new int[]{45,25},14,rfiCoRangeColor,10,6,allPositionsHero));
            result_list.add(new SettingOneStata("RFI_BU",new int[]{67,25},14,rfiBuRangeColor,10,6,allPositionsHero));
            result_list.add(new SettingOneStata("RFI_SB",new int[]{89,25},14,rfiSbRangeColor,10,6,allPositionsHero));
            result_list.add(new SettingOneStata("fold_to_steal_BUvCO",new int[]{1,38},14,foldToStealBUvCORangeColor,10,3,coPosHero));

            result_list.add(new SettingOneStata("fold_to_steal_SBvCO",new int[]{1,38},14,foldToStealSBvCORangeColor,10,4,coPosHero));
            result_list.add(new SettingOneStata("fold_to_steal_BBvCO",new int[]{1,38},14,foldToStealBBvCORangeColor,10,5,coPosHero));
            result_list.add(new SettingOneStata("fold_to_steal_SBvBU",new int[]{1,38},14,foldToStealSBvBURangeColor,10,4,buPosHero));

            result_list.add(new SettingOneStata("fold_to_steal_BBvBU",new int[]{1,38},14,foldToStealBBvBURangeColor,10,5,buPosHero));
            result_list.add(new SettingOneStata("fold_to_steal_BBvSB",new int[]{1,38},14,foldToStealBBvSBRangeColor,10,5,sbPosHero));
            return result_list;
        }



    }

    public static void main(String[] args) {

    }
}
