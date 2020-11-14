package org.trenkvaz.main;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.trenkvaz.main.CaptureVideo.current_map_stats;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;
import static org.trenkvaz.ui.StartAppLauncher.hud;

public class CreatingHUD {




     public CreatingHUD(){

     }


     public synchronized void send_current_hand_to_creating_hud(String[] nicks, int table, int[] inds_poker_pos_elements_places_table, CurrentHand.CurrentStats currentStats){

         Text[][] arr_one_table_texts_huds_each_player = new Text[6][];
         for(int player = 0; player<6; player++){
             if(nicks[player]==null)continue;

             arr_one_table_texts_huds_each_player[player] =
                     new Text[]{get_NickText(nicks[player]),
                             get_RFI_by_positions(inds_poker_pos_elements_places_table,player,currentStats)};
         }
         hud.set_hud(arr_one_table_texts_huds_each_player,table);
     }





      private static Text get_NickText(String nick){
          Text text_nick = new Text(1, 12, nick);
          text_nick.setFont(new Font(12));
          text_nick.setFill(Color.YELLOW);
          return text_nick;
      }

      private static Text get_RFI_by_positions(int[] inds_poker_pos_elements_places_table, int place_table, CurrentHand.CurrentStats currentStats){
          String stats = "0";
          int position = get_ArrayIndex(inds_poker_pos_elements_places_table,place_table+1);
          if(currentStats.stats_rfi[place_table]!=null&&position!=5) stats = String.format("%.1f",(procents((int)currentStats.stats_rfi[place_table][position][1],
                  (int)currentStats.stats_rfi[place_table][position][0])));
          Text result = new Text(1, 12, stats );
          result.setFont(new Font(12));
          result.setFill(Color.WHITE);
          return result;
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

    }

    public static void main(String[] args) {

    }
}
