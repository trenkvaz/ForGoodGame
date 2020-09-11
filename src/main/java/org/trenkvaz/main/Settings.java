package org.trenkvaz.main;



import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.trenkvaz.main.CaptureVideo.hashmap_id_img_pix_nick;
import static org.trenkvaz.main.CaptureVideo.sortedmap_all_imgs_pix_of_nicks;

public class Settings {


    private static File file_with_nicks;



    public  Settings(){
      read_file();

    }

    private static void read_file(){
        file_with_nicks = new File(System.getProperty("user.dir")+"\\nicks_img.txt");
        if(!file_with_nicks.isFile())return;
        try {
        BufferedReader br = new BufferedReader(new FileReader(file_with_nicks));
        String line;
        while ((line = br.readLine()) != null) {
          if(!(line.startsWith("*")&&line.endsWith("*")))break;
          String[] arr_line = line.substring(1,line.length()-1).split("%");
            //System.out.println("line "+arr_line.length);
            hashmap_id_img_pix_nick.put(Long.parseLong(arr_line[18]),arr_line[0]);
            long[] img_pix = new long[17];
            for(int i=2; i<19; i++){
                img_pix[i-2] = Long.parseLong(arr_line[i]);
            }
            sortedmap_all_imgs_pix_of_nicks.put(Long.parseLong(arr_line[1]),img_pix);
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void write_nicks_keys_img_pix(String nick,long key_in_treemap_img_pix,long[] imgs_pix_of_nick){
        StringBuilder line = new StringBuilder("*");
        line.append(nick);line.append('%');line.append(key_in_treemap_img_pix);line.append('%');
        for(long pixs:imgs_pix_of_nick){
            line.append(pixs);
            line.append('%');
        }
        line.deleteCharAt(line.length()-1);
        line.append("*\r\n");

        try (OutputStream os = new FileOutputStream(file_with_nicks,true)) {
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

    public static void main(String[] args) {
        new Settings();

    }
}
