package org.trenkvaz.main;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.trenkvaz.main.CaptureVideo.current_map_stats;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;
import static org.trenkvaz.ui.StartAppLauncher.hud;

public class CreatingHUD {

     int table = -1;
     Map<Integer,Map<Integer,Object>> map_player_map_stats = new HashMap<>();
     Map<Integer,Map<Integer,Integer>> map_controling_exists_stats = new HashMap<>();
     List<List<Text>> list_current_one_table_texts_huds_each_player = new ArrayList<>(6);
     record SettingOneStata(String name_stata, int[] coord_text, int size_font, Paint color, int min_select, int condition_show, int befor_or_after_hero ){}
     static Map<String,Integer[]> map_descriptions_of_stats;
     static List<SettingOneStata> list_settings_one_stats;

     public CreatingHUD(int table1){
         table = table1;
         //for(int i=0; i<6; i++) map_player_map_stats.put(i,new HashMap<>());
         Setting.setting_CreatingHUD();
     }

     public void clear_MapStats(){
         list_current_one_table_texts_huds_each_player.clear();
         for(int i=0; i<6; i++) {
             map_player_map_stats.put(i,new HashMap<>());
             map_controling_exists_stats.put(i,new HashMap<>());
             list_current_one_table_texts_huds_each_player.add(new ArrayList<>());
         }

     }

     public void send_current_hand_to_creating_hud(String[] nicks, int[] inds_poker_pos_elements_places_table, boolean[] nicks_for_hud,int poker_position_of_hero){

        //Text[][] arr_one_table_texts_huds_each_player = new Text[6][];
         List<List<Text>> list_one_table_texts_huds_each_player = new ArrayList<>(6);
         for(int table_place = 0; table_place<6; table_place++){
             list_one_table_texts_huds_each_player.add(new ArrayList<>());
             if(nicks[table_place]==null)continue;

           // если ник и статы уже были преобразованы в текст и сохранены в текущем списке, то они берутся для нового списка текста
             // ПОКА ТАК !!! так как это может в будущем мешать сделать изменяемый по улицам и действиям ХАД
             // по идеи тогда не нужен массив nicks_for_hud , так как проверяется тоже самое


           if(!list_current_one_table_texts_huds_each_player.get(table_place).isEmpty())
               list_one_table_texts_huds_each_player.set(table_place,list_current_one_table_texts_huds_each_player.get(table_place));

           list_one_table_texts_huds_each_player.get(table_place).add(get_NickText(nicks[table_place]));

           //List<Text> one_player_texts = list_one_table_texts_huds_each_player.get(table_place);

           add_StatsToListForHUD(nicks[table_place],list_one_table_texts_huds_each_player.get(table_place),
                   get_ArrayIndex(inds_poker_pos_elements_places_table,table_place+1),table_place,nicks_for_hud,poker_position_of_hero);

           //list_one_table_texts_huds_each_player.set(table_place,one_player_texts);
           list_current_one_table_texts_huds_each_player.set(table_place,list_one_table_texts_huds_each_player.get(table_place));
         }
         hud.set_hud(list_one_table_texts_huds_each_player,table);
     }





      private Text get_NickText(String nick){
          if(nick.length()>5)nick = nick.substring(0,5);
          Text text_nick = new Text(1, 12, nick);
          text_nick.setFont(new Font(12));
          text_nick.setFill(Color.YELLOW);
          return text_nick;
      }

      private static Text get_RFI_by_positions(int position, int place_table, CurrentHand.CurrentStats currentStats){
          String stats = "0";

          if(currentStats.stats_rfi[place_table]!=null&&position!=5) stats = String.format("%.1f",(procents((int)currentStats.stats_rfi[place_table][position][1],
                  (int)currentStats.stats_rfi[place_table][position][0])));
          Text result = new Text(1+50, 12, stats );
          result.setFont(new Font(12));
          result.setFill(Color.WHITE);
          return result;
      }



      private void add_StatsToListForHUD(String nick,List<Text> list_text_hud_one_player, int poker_position, int table_place, boolean[] nicks_for_hud,int poker_position_of_hero){

        for(SettingOneStata settingOneStata:list_settings_one_stats){
            // если отображение статы привязано к позиции, но она не совпадает с текущей позой то пропускается
            if(settingOneStata.condition_show<6&&settingOneStata.condition_show!=poker_position)continue;
            //если отображение статы привязано к позиции, и она совпадает с текущей позой, но не соотвествует отображению относительно позы героя то пропускается
            if((settingOneStata.condition_show==poker_position||settingOneStata.condition_show==6)&&settingOneStata.befor_or_after_hero==-1&&poker_position_of_hero<poker_position)continue;
            //тоже самое, только первая поза находится до героя, а вторая после героя  также добавлена вероятность, что отображение статы не привязано к позе
            if((settingOneStata.condition_show==poker_position||settingOneStata.condition_show==6)&&settingOneStata.befor_or_after_hero==1&&poker_position_of_hero>poker_position)continue;

            // можно заранее подготовить текст так как в любом случае будет хотя бы ноль
            Text text = new Text(settingOneStata.coord_text[0], settingOneStata.coord_text[1],"" );
            text.setFont(new Font(settingOneStata.size_font));
            text.setFill(settingOneStata.color);

            Integer[] description = map_descriptions_of_stats.get(settingOneStata.name_stata);

            Integer control = map_controling_exists_stats.get(table_place).get(description[0]);
            Object main_stats = null;
            // если сонтрол нулл то значит самое первое обращение тогда обращение к главной мапе

            if(control==null) {
                main_stats =current_map_stats[description[0]].get("$ю$"+nick+"$ю$");

                // статы может не быть в таком случае нужно выставить ноль в текст тогда по этому игроку не будет больше попыток получить стату так как сохранится текст
                // также нужно в контрольную мапу нужно добавить некое значение пусть 0, показывающее, что не нужно пытаться получить эту стату
                if(main_stats==null) {
                    text.setText("0"); list_text_hud_one_player.add(text);
                    //System.out.println("stata no ");
                    Map<Integer,Integer> _control_map = map_controling_exists_stats.get(table_place);
                    _control_map.put(description[0],0);
                    map_controling_exists_stats.put(table_place,_control_map);
                    continue;
                }
                // если стата определилась то в контрольную заносится 1 чтобы потом было обращение в текущую мапу
                Map<Integer,Integer> _control_map = map_controling_exists_stats.get(table_place);
                _control_map.put(description[0],1);
                map_controling_exists_stats.put(table_place,_control_map);

                // заполнение текущей мапы если стата получена из главной
                Map<Integer,Object> _new_map = map_player_map_stats.get(table_place);
                _new_map.put(description[0],main_stats);
                map_player_map_stats.put(table_place,_new_map);

            }
            else if(control==0){
                //System.out.println("stata zero ");
                text.setText("0"); list_text_hud_one_player.add(text); continue;
            }
            else if(control==1)main_stats = map_player_map_stats.get(table_place).get(description[0]);

              // ДОПИСАТЬ приведение для разных размерностей массивов стат


                Object[][] casting_stata =(Object[][]) main_stats;
                // проверка выборки
                if((int)casting_stata[description[3]][description[1]]<10){
                    //System.out.println("stata select ");
                    text.setText("0"); list_text_hud_one_player.add(text); continue; }
                // итог добавление статы

                //System.out.println("stata total");
                text.setText(String.format("%.1f",(procents((int)casting_stata[description[3]][description[2]],
                        (int)casting_stata[description[3]][description[1]]))));

                list_text_hud_one_player.add(text);





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
            return map_descriptions_of_stats;
        }


        public static List<SettingOneStata> get_list_SettingOneStata(){
            List<SettingOneStata> result_list = new ArrayList<>();
            //SettingOneStata(String name_stata,int[] coord_text, int size_font, Paint color, int min_select, int condition_show, int befor_or_after_hero )
            // condition_show 0-5 при нахождении на позиции 6 всегда  befor_or_after_hero -1 до героя 0 всегда 1 после героя
            result_list.add(new SettingOneStata("VPIP",new int[]{50,12},12,Color.WHITE,10,6,0));
            result_list.add(new SettingOneStata("PFR",new int[]{75,12},12,Color.WHITE,10,6,0));
            return result_list;
        }

    }

    public static void main(String[] args) {

    }
}
