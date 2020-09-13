package org.trenkvaz.main;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.trenkvaz.main.CaptureVideo.home_folder;
import static org.trenkvaz.ui.StartAppLauncher.hud;

public class CreatingHUD {




     public CreatingHUD(){

     }


     public synchronized void send_current_hand_to_creating_hud(String[] nicks,int table){

         Text[][] arr_one_table_texts_huds_each_player = new Text[6][];
         for(int player = 0; player<6; player++){
             Text text_nick = new Text(1, 12, nicks[player]);
             text_nick.setFont(new Font(12));
             text_nick.setFill(Color.YELLOW);
             arr_one_table_texts_huds_each_player[player] = new Text[]{text_nick};
         }
         hud.set_hud(arr_one_table_texts_huds_each_player,table);
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

            StringBuilder line = new StringBuilder("");
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
