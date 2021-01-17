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
     record SettingOneStata(String name_stata, int[] coord_text, int size_font, RangeColor rangeColor, int min_select, int[][] show_on_poses_player_hero){
         public Paint get_ColorByRangeOfStata(float stata){
             int range= -1;
             for(int i_range=0; i_range<rangeColor.ranges.length; i_range++){ if(rangeColor.ranges[i_range]<stata){range++; continue;}break; }
             return rangeColor.colors[range];
         }
         public boolean let_ShowStataOfPositionsHero(int position_player,int position_hero){
             boolean is_player = false, is_hero = false;
             for(int pos:show_on_poses_player_hero[0])if(pos==position_player){is_player = true;break;}
             for(int pos:show_on_poses_player_hero[1])if(pos==position_hero){is_hero = true;break;}
             return is_player&&is_hero;
         }
     }
     record RangeColor(int[] ranges, Paint[] colors){}
     static Map<String,Integer[][]> map_descriptions_of_stats;
     static List<SettingOneStata> list_settings_one_stats;
     static final int UTG = 0, MP = 1, CO = 2, BU = 3, SB = 4, BB = 5;


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

           check_LinesOfTexts(list_current_one_table_texts_huds_each_player.get(table_place));
         }

         hud.set_hud(copy_ListCreatingHUDtoListHUD(list_current_one_table_texts_huds_each_player),table);
     }


    private void check_LinesOfTexts(List<Text> list_text_hud_one_player){

         for (Text text:list_text_hud_one_player){ if(text.getY()==38)return; }
         for (Text text:list_text_hud_one_player){ if(text.getY()==51) text.setY(38);
             if(text.getY()==54) text.setY(41);
         }

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

            //проверка позиций игрока и херо на возможность отображения статы
            if((!settingOneStata.let_ShowStataOfPositionsHero(poker_position,poker_position_of_hero)))continue;


            // можно заранее подготовить текст так как в любом случае будет хотя бы ноль
            Text text = new Text(settingOneStata.coord_text[0], settingOneStata.coord_text[1],"" );
            text.setFont(new Font(settingOneStata.size_font));


            Integer[][] description = map_descriptions_of_stats.get(settingOneStata.name_stata);

            Object main_stats = arr_player_indstat_stata[table_place][description[0][0]];
            if(main_stats==null){
                main_stats =current_map_stats[description[0][0]].get("$ю$"+nick+"$ю$");
                // статы может не быть в таком случае нужно выставить ноль в текст тогда по этому игроку не будет больше попыток получить стату так как сохранится текст
                // также нужно в текущий массив со статами нужно добавить некое значение ПУСТЫШКУ пусть Object[0], показывающее, что не нужно пытаться получить эту стату
                if(main_stats==null) {
                    //if(settingOneStata.name_stata.equals("fold_to_steal_BUvCO")) System.err.println("NULL  pos "+poker_position+" hero "+poker_position_of_hero+" table "+table);
                    text.setText("--"); text.setFill(Color.WHITE); list_text_hud_one_player.add(text);
                    arr_player_indstat_stata[table_place][description[0][0]] =  new Object[0];
                    continue;
                }
                arr_player_indstat_stata[table_place][description[0][0]] = main_stats;
            }
             // проверка на пустышку в текущем массиве стат
            if(main_stats instanceof Object[]&&((Object[]) main_stats).length==0){
                //if(settingOneStata.name_stata.equals("fold_to_steal_BUvCO")) System.err.println("NULL 222  pos "+poker_position+" hero "+poker_position_of_hero+" table "+table);
                text.setText("--");  text.setFill(Color.WHITE); list_text_hud_one_player.add(text); continue;
            }

              // ДОПИСАТЬ приведение для разных размерностей массивов стат
            float stata = -1;int select = -1;
            if(main_stats instanceof Object[][][] casting_stata){

                    select = 0; stata = 0;
                    // пробегание по всем сочетаниям складываемых стат
                    for(int pos_player = 0; pos_player<description[1].length; pos_player++)
                    for(int pos_opp = 0; pos_opp<description[2].length; pos_opp++){
                        select+=(int)casting_stata[description[1][pos_player]][description[2][pos_opp]][description[0][1]];
                    }
                    if(select==0){ text.setText("--"); text.setFill(Color.WHITE); list_text_hud_one_player.add(text); continue; }

                    for(int pos_player = 0; pos_player<description[1].length; pos_player++)
                    for(int pos_opp = 0; pos_opp<description[2].length; pos_opp++){
                        stata+=(int)casting_stata[description[1][pos_player]][description[2][pos_opp]][description[0][2]];
                    }
                    stata = BigDecimal.valueOf(procents((int) stata, select)).setScale(1, RoundingMode.HALF_UP).floatValue();


                // для отображения двузначных чисел целыми, а однозначных с дробью, плюс 0 без дроби и 100 как 99
                if(stata>=10)text.setText((stata>=99)? "99":Integer.toString(Math.round(stata)));
                else  text.setText((stata==0)? "0":notZeroFormat.format(stata));
                if(stata==0)text.setFill(Color.WHITE);else text.setFill(settingOneStata.get_ColorByRangeOfStata(stata));
            }
            // если было уже приведение к Объекту[][][] то стата и селект не будут -1 поэтому возможно приведение
            if(stata==-1&&select==-1) {
                Object[][] casting_stata = (Object[][]) main_stats;

                select = 0; stata = 0;
                for(int pos_player = 0; pos_player<description[1].length; pos_player++)
                        select+=(int)casting_stata[description[1][pos_player]][description[0][1]];

                if(select==0){ text.setText("--"); text.setFill(Color.WHITE); list_text_hud_one_player.add(text); continue; }

                for(int pos_player = 0; pos_player<description[1].length; pos_player++)
                    stata+=(int)casting_stata[description[1][pos_player]][description[0][2]];

                stata = BigDecimal.valueOf(procents((int) stata, select)).setScale(1, RoundingMode.HALF_UP).floatValue();


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
            //{2,0,1,6}
            // TEST
            /*if(description[0]==2&&description[1]==0&&description[2]==1){
                text = new Text(20,38,"" );
                text.setFont(new Font(14));
                text.setFill(Color.WHITE);
                text.setText(Integer.toString(select));
                    *//*if(settingOneStata.name_stata.equals("fold_to_steal_BUvCO")) System.err.println("SELECT    ---------------      BUvCO "+text.getText()+"  select "
                            +(int)(casting_stata[description[3]][description[4]][description[1]])+" table "+table);*//*
                list_text_hud_one_player.add(text);
            }*/
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





        public static Map<String,Integer[][]> get_map_DescriptionsOfStats(){
            Map<String,Integer[][]> map_descriptions_of_stats = new HashMap<>();
            // {new AgainstRFI(),new Against3bet(),new VpipPFR3bet(),new RFI(),new Alliners()};
            // Integer 0: 0 - индекс статы в конкурентмапе всех стат, 1 индекс выборки статы, 2 индекс статы
            // Integer 1: позиции статы или индекс общей статы
            // Integer 2: позиции оппонентов



            map_descriptions_of_stats.put("VPIP",new Integer[][]{{2,0,1},{6}});
            map_descriptions_of_stats.put("PFR",new Integer[][]{{2,0,2},{6}});
            map_descriptions_of_stats.put("RFI_UTG",new Integer[][]{{3,0,1}, {UTG}});
            map_descriptions_of_stats.put("RFI_MP",new Integer[][]{{3,0,1},{MP}});
            map_descriptions_of_stats.put("RFI_CO",new Integer[][]{{3,0,1},{CO}});
            map_descriptions_of_stats.put("RFI_BU",new Integer[][]{{3,0,1},{BU}});
            map_descriptions_of_stats.put("RFI_SB",new Integer[][]{{3,0,1},{SB}});
            map_descriptions_of_stats.put("fold_to_steal_BUvCO",new Integer[][]{{0,0,1},{BU},{CO}});
            map_descriptions_of_stats.put("fold_to_steal_SBvCO",new Integer[][]{{0,0,1},{SB},{CO}});
            map_descriptions_of_stats.put("fold_to_steal_BBvCO",new Integer[][]{{0,0,1},{BB},{CO}});
            map_descriptions_of_stats.put("fold_to_steal_SBvBU",new Integer[][]{{0,0,1},{SB},{BU}});
            map_descriptions_of_stats.put("fold_to_steal_BBvBU",new Integer[][]{{0,0,1},{BB},{BU}});
            map_descriptions_of_stats.put("fold_to_steal_BBvSB",new Integer[][]{{0,0,1},{BB},{SB}});


            map_descriptions_of_stats.put("fold_to_3bet_UTGvMP",new Integer[][]{{1,0,1},{UTG},{MP}});
            map_descriptions_of_stats.put("fold_to_3bet_UTGvCO_BU",new Integer[][]{{1,0,1},{UTG},{CO,BU}});
            map_descriptions_of_stats.put("fold_to_3bet_UTGvSB_BB",new Integer[][]{{1,0,1},{UTG},{SB,BB}});
            map_descriptions_of_stats.put("fold_to_3bet_MPvCO",new Integer[][]{{1,0,1},{MP},{CO}});
            map_descriptions_of_stats.put("fold_to_3bet_MPvBU",new Integer[][]{{1,0,1},{MP},{BU}});
            map_descriptions_of_stats.put("fold_to_3bet_MPvSB_BB",new Integer[][]{{1,0,1},{MP},{SB,BB}});
            map_descriptions_of_stats.put("fold_to_3bet_COvBU",new Integer[][]{{1,0,1},{CO},{BU}});
            map_descriptions_of_stats.put("fold_to_3bet_COvSB",new Integer[][]{{1,0,1},{CO},{SB}});
            map_descriptions_of_stats.put("fold_to_3bet_COvBB",new Integer[][]{{1,0,1},{CO},{BB}});
            map_descriptions_of_stats.put("fold_to_3bet_BUvSB",new Integer[][]{{1,0,1},{BU},{SB}});
            map_descriptions_of_stats.put("fold_to_3bet_BUvBB",new Integer[][]{{1,0,1},{BU},{BB}});
            map_descriptions_of_stats.put("fold_to_3bet_SBvBB",new Integer[][]{{1,0,1},{SB},{BB}});

            map_descriptions_of_stats.put("3bet_MPvUTG",new Integer[][]{{0,0,2},{MP},{UTG}});
            map_descriptions_of_stats.put("3bet_CO_BUvUTG",new Integer[][]{{0,0,2},{CO,BU},{UTG}});
            map_descriptions_of_stats.put("3bet_SB_BBvUTG",new Integer[][]{{0,0,2},{SB,BB},{UTG}});
            map_descriptions_of_stats.put("3bet_CO_BUvMP",new Integer[][]{{0,0,2},{CO,BU},{MP}});
            map_descriptions_of_stats.put("3bet_SB_BBvMP",new Integer[][]{{0,0,2},{SB,BB},{MP}});
            map_descriptions_of_stats.put("3bet_BUvCO",new Integer[][]{{0,0,2},{BU},{CO}});
            map_descriptions_of_stats.put("3bet_SBvCO",new Integer[][]{{0,0,2},{SB},{CO}});
            map_descriptions_of_stats.put("3bet_BBvCO",new Integer[][]{{0,0,2},{BB},{CO}});
            map_descriptions_of_stats.put("3bet_SBvBU",new Integer[][]{{0,0,2},{SB},{BU}});
            map_descriptions_of_stats.put("3bet_BBvBU",new Integer[][]{{0,0,2},{BB},{BU}});
            map_descriptions_of_stats.put("3bet_BBvSB",new Integer[][]{{0,0,2},{BB},{SB}});

            map_descriptions_of_stats.put("4bet_UTGvMP",new Integer[][]{{1,0,2},{UTG},{MP}});

            map_descriptions_of_stats.put("4bet_UTGvCO_BU",new Integer[][]{{1,0,2},{UTG},{CO,BU}});
            map_descriptions_of_stats.put("4bet_UTGvSB_BB",new Integer[][]{{1,0,2},{UTG},{SB,BB}});
            map_descriptions_of_stats.put("4bet_MPvCO",new Integer[][]{{1,0,2},{MP},{CO}});
            map_descriptions_of_stats.put("4bet_MPvBU",new Integer[][]{{1,0,2},{MP},{BU}});
            map_descriptions_of_stats.put("4bet_MPvSB_BB",new Integer[][]{{1,0,2},{MP},{SB,BB}});
            map_descriptions_of_stats.put("4bet_COvBU",new Integer[][]{{1,0,2},{CO},{BU}});
            map_descriptions_of_stats.put("4bet_COvSB",new Integer[][]{{1,0,2},{CO},{SB}});
            map_descriptions_of_stats.put("4bet_COvBB",new Integer[][]{{1,0,2},{CO},{BB}});
            map_descriptions_of_stats.put("4bet_BUvSB",new Integer[][]{{1,0,2},{BU},{SB}});
            map_descriptions_of_stats.put("4bet_BUvBB",new Integer[][]{{1,0,2},{BU},{BB}});
            map_descriptions_of_stats.put("4bet_SBvBB",new Integer[][]{{1,0,2},{SB},{BB}});



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
            RangeColor foldTo3betRangeColor = new RangeColor(new int[]{0,68,101},new Paint[]{Color.RED,Color.GREEN});
            RangeColor _4bet = new RangeColor(new int[]{0,101},new Paint[]{Color.BLUE});
            Paint[] RedOrangePurpule = new Paint[]{Color.RED,Color.ORANGE,Color.PURPLE};



            int[] allPositions = {0,1,2,3,4,5};


            /*int[] co_buPosHero = {2,3};
            int[] sb_bbPosHero = {4,5};*/
            result_list.add(new SettingOneStata("VPIP",new int[]{50,12},14,vpipRangeColor,10,new int[][]{allPositions,allPositions}));
            result_list.add(new SettingOneStata("PFR",new int[]{75,12},14,pfrRangeColor,10,new int[][]{allPositions,allPositions}));
            result_list.add(new SettingOneStata("RFI_UTG",new int[]{1,25},14,rfiUtgRangeColor,10,new int[][]{allPositions,allPositions}));
            result_list.add(new SettingOneStata("RFI_MP",new int[]{23,25},14,rfiMpRangeColor,10,new int[][]{allPositions,allPositions}));
            result_list.add(new SettingOneStata("RFI_CO",new int[]{45,25},14,rfiCoRangeColor,10,new int[][]{allPositions,allPositions}));
            result_list.add(new SettingOneStata("RFI_BU",new int[]{67,25},14,rfiBuRangeColor,10,new int[][]{allPositions,allPositions}));
            result_list.add(new SettingOneStata("RFI_SB",new int[]{89,25},14,rfiSbRangeColor,10,new int[][]{allPositions,allPositions}));
            result_list.add(new SettingOneStata("fold_to_steal_BUvCO",new int[]{45,51},14,foldToStealBUvCORangeColor,10,new int[][]{{BU},{CO}}));
            result_list.add(new SettingOneStata("fold_to_steal_SBvCO",new int[]{45,51},14,foldToStealSBvCORangeColor,10,new int[][]{{SB},{CO}}));
            result_list.add(new SettingOneStata("fold_to_steal_BBvCO",new int[]{45,51},14,foldToStealBBvCORangeColor,10,new int[][]{{BB},{CO}}));
            result_list.add(new SettingOneStata("fold_to_steal_SBvBU",new int[]{45,51},14,foldToStealSBvBURangeColor,10,new int[][]{{SB},{BU}}));
            result_list.add(new SettingOneStata("fold_to_steal_BBvBU",new int[]{45,51},14,foldToStealBBvBURangeColor,10,new int[][]{{BB},{BU}}));
            result_list.add(new SettingOneStata("fold_to_steal_BBvSB",new int[]{45,51},14,foldToStealBBvSBRangeColor,10,new int[][]{{BB},{SB}}));

            result_list.add(new SettingOneStata("fold_to_3bet_UTGvMP",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{UTG},{MP}}));
            result_list.add(new SettingOneStata("fold_to_3bet_UTGvCO_BU",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{UTG},{CO, BU}}));
            result_list.add(new SettingOneStata("fold_to_3bet_UTGvSB_BB",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{UTG},{SB, BB}}));
            result_list.add(new SettingOneStata("fold_to_3bet_MPvCO",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{MP},{CO}}));
            result_list.add(new SettingOneStata("fold_to_3bet_MPvBU",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{MP},{BU}}));
            result_list.add(new SettingOneStata("fold_to_3bet_MPvSB_BB",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{MP},{SB, BB}}));
            result_list.add(new SettingOneStata("fold_to_3bet_COvBU",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{CO},{BU}}));
            result_list.add(new SettingOneStata("fold_to_3bet_COvSB",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{CO},{SB}}));
            result_list.add(new SettingOneStata("fold_to_3bet_COvBB",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{CO},{BB}}));
            result_list.add(new SettingOneStata("fold_to_3bet_BUvSB",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{BU},{SB}}));
            result_list.add(new SettingOneStata("fold_to_3bet_BUvBB",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{BU},{BB}}));
            result_list.add(new SettingOneStata("fold_to_3bet_SBvBB",new int[]{1,38},14,foldTo3betRangeColor,10,new int[][]{{SB},{BB}}));


            result_list.add(new SettingOneStata("3bet_MPvUTG",new int[]{23,51},14,new RangeColor(new int[]{0,2,3,101},RedOrangePurpule),10,new int[][]{{MP},{UTG}}));
            result_list.add(new SettingOneStata("3bet_CO_BUvUTG",new int[]{23,51},14,new RangeColor(new int[]{0,3,4,101},RedOrangePurpule),10,new int[][]{{CO,BU},{UTG}}));
            result_list.add(new SettingOneStata("3bet_SB_BBvUTG",new int[]{23,51},14,new RangeColor(new int[]{0,3,4,101},RedOrangePurpule),10,new int[][]{{SB,BB},{UTG}}));
            result_list.add(new SettingOneStata("3bet_CO_BUvMP",new int[]{23,51},14,new RangeColor(new int[]{0,3,5,101},RedOrangePurpule),10,new int[][]{{CO,BU},{MP}}));
            result_list.add(new SettingOneStata("3bet_SB_BBvMP",new int[]{23,51},14,new RangeColor(new int[]{0,3,5,101},RedOrangePurpule),10,new int[][]{{SB,BB},{MP}}));
            result_list.add(new SettingOneStata("3bet_BUvCO",new int[]{23,51},14, new RangeColor(new int[]{0,5,10,101},RedOrangePurpule),10,new int[][]{{BU},{CO}}));
            result_list.add(new SettingOneStata("3bet_SBvCO",new int[]{23,51},14,new RangeColor(new int[]{0,5,10,101},RedOrangePurpule),10,new int[][]{{SB},{CO}}));
            result_list.add(new SettingOneStata("3bet_BBvCO",new int[]{23,51},14,new RangeColor(new int[]{0,5,7,101},RedOrangePurpule),10,new int[][]{{BB},{CO}}));
            result_list.add(new SettingOneStata("3bet_SBvBU",new int[]{23,51},14,new RangeColor(new int[]{0,5,12,101},RedOrangePurpule),10,new int[][]{{SB},{BU,BB}}));
            result_list.add(new SettingOneStata("3bet_BBvBU",new int[]{23,51},14,new RangeColor(new int[]{0,5,11,101},RedOrangePurpule),10,new int[][]{{BB},{BU}}));
            result_list.add(new SettingOneStata("3bet_BBvSB",new int[]{23,51},14,new RangeColor(new int[]{0,5,10,101},RedOrangePurpule),10,new int[][]{{BB},{SB}}));


            result_list.add(new SettingOneStata("4bet_UTGvMP",new int[]{23,38},14,_4bet,10,new int[][]{{UTG},{MP}}));
            result_list.add(new SettingOneStata("4bet_UTGvCO_BU",new int[]{23,38},14,_4bet,10,new int[][]{{UTG},{CO, BU}}));
            result_list.add(new SettingOneStata("4bet_UTGvSB_BB",new int[]{23,38},14,_4bet,10,new int[][]{{UTG},{SB, BB}}));
            result_list.add(new SettingOneStata("4bet_MPvCO",new int[]{23,38},14,_4bet,10,new int[][]{{MP},{CO}}));
            result_list.add(new SettingOneStata("4bet_MPvBU",new int[]{23,38},14,_4bet,10,new int[][]{{MP},{BU}}));
            result_list.add(new SettingOneStata("4bet_MPvSB_BB",new int[]{23,38},14,_4bet,10,new int[][]{{MP},{SB, BB}}));
            result_list.add(new SettingOneStata("4bet_COvBU",new int[]{23,38},14,_4bet,10,new int[][]{{CO},{BU}}));
            result_list.add(new SettingOneStata("4bet_COvSB",new int[]{23,38},14,_4bet,10,new int[][]{{CO},{SB}}));
            result_list.add(new SettingOneStata("4bet_COvBB",new int[]{23,38},14,_4bet,10,new int[][]{{CO},{BB}}));
            result_list.add(new SettingOneStata("4bet_BUvSB",new int[]{23,38},14,_4bet,10,new int[][]{{BU},{SB}}));
            result_list.add(new SettingOneStata("4bet_BUvBB",new int[]{23,38},14,_4bet,10,new int[][]{{BU},{BB}}));
            result_list.add(new SettingOneStata("4bet_SBvBB",new int[]{23,38},14,_4bet,10,new int[][]{{SB},{BB}}));


            return result_list;
        }



    }

    public static void main(String[] args) {

    }
}
