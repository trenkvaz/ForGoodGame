package org.trenkvaz.main;

import net.coobird.thumbnailator.Thumbnails;
//import org.bytedeco.javacpp.opencv_core.IplImage;
//import org.bytedeco.opencv.opencv_core.IplImage;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/*import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.cvResetImageROI;*/
import static org.trenkvaz.main.CaptureVideo.*;
//import static org.trenkvaz.main.Settings.write_nicks_keys_img_pix;

public class OCR implements Runnable {

    boolean is_run = true;
    int table = -1;
    BufferedImage[] frame;
    int[] coord_of_table;
    Queue<BufferedImage[]> queue;
    BufferedImage bufferedImage_current_number_hand;
    CurrentHand currentHand;
    float sb = 0.5f;
    int[] poker_positions_of_numbers = new int[6];
    BufferedImage[] bufferedimage_current_position_actions = new BufferedImage[6];
    BufferedImage[] images_of_nicks_for_ocr = new BufferedImage[6];

    public OCR(int table){
        this.coord_of_table = coord_left_up_of_tables[table];
        this.table = table+1;
        queue = new LinkedList<>();


        new Thread(this).start();
    }

   public OCR(String test,int table,BufferedImage[] frame1){
       this.coord_of_table = coord_left_up_of_tables[table];
       this.table = table;
       frame = frame1;
   }




    @Override
    public void run() {
        while (is_run){
            if((frame = queue.poll())!=null){
                main_work_on_table();
               // frame = null;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(queue.size()>100){System.out.println("table "+table+"    "+queue.size());c++;
             //save_image(frame[0],"tables_img\\"+c);
            }
        }
    }


    public synchronized void set_image_for_ocr(BufferedImage[] frame){
        //System.out.println("t "+table+" set img");
        queue.offer(frame);
    }

    boolean start_hand = false;
    //boolean is_nicks_filled = false;
    static int S =0;
    static synchronized void show_total_hand(CurrentHand currentHand,OCR ocr,BufferedImage[] images_of_nicks_for_ocr){
        System.out.println("****** cards "+currentHand.cards_hero+" flop "+currentHand.is_start_flop+" table "+ocr.table);
        boolean save_hand_with_null_img = false;
        for(int i=0; i<6; i++) {
            System.out.print(currentHand.nicks[i]+"    "+currentHand.stacks[i]+"  ");
            if(i>0&&currentHand.nicks[i]==null&&!save_hand_with_null_img){
                save_hand_with_null_img =true;
                for(int s=1; s<6; s++)save_image(images_of_nicks_for_ocr[i],"test\\null_nicks"+(++S));
            }
            for(Float a:currentHand.preflop_by_positions.get(i)) System.out.print(a+"  ");
            System.out.println();
        }

        System.out.println("******************************************");
    }


    private void main_work_on_table(){
        if(table!=1)return;


        long check_number_hand = get_number_hand();
        //System.out.println(check_number_hand);
        if(check_number_hand==0)return;

        if(check_number_hand>0){
            if(currentHand!=null){
             show_total_hand(currentHand,this,images_of_nicks_for_ocr);
            }

           currentHand = new CurrentHand(table-1,sb);
            for(int i=0; i<6; i++)bufferedimage_current_position_actions[i]=null;
           /*current_cards_hero = "";
           current_position_of_sb = 0;*/
           start_hand = true;
           //for(int i=1; i<6; i++)current_nicks[i]=null;
           //is_nicks_filled = false;
        }
        //if(current_position_of_sb==0) current_position_of_sb = set_current_position_of_sb();
        if(currentHand.position_of_bu ==0) set_current_position_of_bu();

        //if(current_position_of_sb>0&&current_cards_hero.equals("")) set_cards_hero();
        if(currentHand.position_of_bu >0&&currentHand.cards_hero.equals("")) set_cards_hero();

        //if(current_position_of_sb>0&&!current_cards_hero.equals("")&&!is_nicks_filled) is_nicks_filled = get_nicks();

        if(currentHand.position_of_bu >0&&!currentHand.cards_hero.equals("")&&!currentHand.is_nicks_filled) {get_nicks();}

        if(currentHand.position_of_bu >0&&!currentHand.cards_hero.equals("")
                &&currentHand.is_nicks_filled&&!currentHand.is_preflop_end) {

            if(!currentHand.is_start_flop)check_start_flop();
            if(!currentHand.is_start_flop)get_start_stacks_and_preflop();
        }


        //System.out.println("bu "+currentHand.position_of_bu+" cards "+currentHand.cards_hero+" allnicks "+currentHand.is_nicks_filled);


        //System.out.println("bu "+currentHand.position_of_bu);


       /* if(currentHand.position_of_bu >0&&!currentHand.cards_hero.equals("")&&currentHand.is_nicks_filled&&start_hand&&currentHand.is_stacks_and_1_raund_actions_filled){
            System.out.println("nicks "+currentHand.is_nicks_filled);
            System.out.println("table "+table+" number "+currentHand.number_hand+" BU - "+currentHand.position_of_bu +",       "+currentHand.cards_hero); start_hand=false;


        }*/
    }




    void get_nicks(){
        //if(!current_cards_hero.equals("6sQc"))return false;
        //System.out.println("get nicks");
        int p = 0;
        for(int i=1; i<6; i++){
            if(currentHand.nicks[i]!=null)continue;
            //if(i!=2)continue;
            if(i==0||i==1||i==2||i==3)p=2;
            else p=1;
            int x = coords_places_of_nicks[i][0]+p-5;
            int y = coords_places_of_nicks[i][1]+1;

            /* old int w = 82;
            int h = 15;
            BufferedImage cheked_img = check_free_of_kursor(x,y,w,h,240,2,1,-3,-2);*/

            int w = 87;
            int h = 14;
            BufferedImage cheked_img = check_free_of_kursor(x,y,w,h,240,0,0,-1,0);
            if(cheked_img==null)continue;
            /*int bright = get_max_brightness(cheked_img);
            if(bright<200)continue;*/
            //if(is_error_image(cheked_img))continue;
             // проверка на надпись фолд пост сб бб синий будет не ярким
            //if(is_error_image(cheked_img)){ c++; save_image(cheked_img,"error_img\\"+table+" "+c);                continue;}
            int limit_grey = 105; c++;
            //save_image(cheked_img,"test2\\br"+c+"__"+bright);
            //if(bright<245) limit_grey = 155;
            // если синий из надписей фолд пост сб бб подмешался к светлым никам
            //if(limit_grey==155)if(is_error_image(cheked_img))continue;
            //System.out.println("limitgray "+limit_grey);
            long s = System.currentTimeMillis();
            long[] img_pix = get_img_pix(cheked_img,limit_grey);
            long[] id_img_pix = get_number_img_nicks(img_pix,10);
            //System.out.println("time id "+(System.currentTimeMillis()-s));
            //System.out.println("id "+id_img_pix[0]);
            int id_img_pix_length = id_img_pix.length;
            c++;
            if(id_img_pix[0]>0&&id_img_pix_length==1){
                    while (true){
                        currentHand.nicks[i] = set_get_nicks_in_hashmap(id_img_pix[0],null);
                        //System.out.println("campare  "+currentHand.nicks[i]);
                        //save_image(get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),limit_grey),"test\\"+currentHand.nicks[i]+" "+id_img_pix[0]);
                        //save_image(set_grey_and_inverse_or_no(cheked_img,true),"test\\"+currentHand.nicks[i]+"_"+id_img_pix[0]+"_"+limit_grey);
                        if(currentHand.nicks[i]!=null)break;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
            }
    // если нет похожих и надо распознать, то возвращает два числа, первое ИД, второе ключ для сортированного массива, чтобы его можно было записать в файл
            if(id_img_pix[0]<0){
                int attempt = 0;
                    while (true){
                        attempt++;
                        currentHand.nicks[i] = ocr_image(get_white_black_image(set_grey_and_inverse_or_no(get_scale_image(cheked_img,4),true),limit_grey),"nicks").trim();
                        //System.out.println("osr  "+currentHand.nicks[i]);
                        if(currentHand.nicks[i]!=null)break;
                        // проверка на невозможность распознования, дается несколько попыток, если все равно приходит нулл, то присваивается ник в виде текущего ИД
                        if(attempt>2){ currentHand.nicks[i]=Long.toString(-id_img_pix[0]); break; }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    set_get_nicks_in_hashmap(-id_img_pix[0],currentHand.nicks[i]);

                    CaptureVideo.Settings.write_nicks_keys_img_pix(currentHand.nicks[i],id_img_pix[1],img_pix);
                //System.out.println("id "+-id_img_pix[0]+" id in arr "+img_pix[16]);
                   // save_image(get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),limit_grey),"id_nicks\\"+currentHand.nicks[i]+" "+(-id_img_pix[0]));
                save_image(get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),limit_grey),"id_nicks\\"+currentHand.nicks[i]+" "+(-id_img_pix[0])+"_"+c+""+table);
            }

            if(id_img_pix[0]>0&&id_img_pix_length>1) {
                // если пришло больше одного ИД, проверка, что у них одинаковый ник, если ники разные сообщение об ошибки
                String[] str_nicks = new String[id_img_pix_length];
                while (true){
                    for(int n=0; n<id_img_pix_length; n++){
                        if(str_nicks[n]!=null)continue;
                        str_nicks[n] = set_get_nicks_in_hashmap(id_img_pix[n],null);
                    }

                    //System.out.println("campare  "+currentHand.nicks[i]);
                    //save_image(get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),limit_grey),"test\\"+currentHand.nicks[i]+" "+id_img_pix[0]);
                    if(!Arrays.asList(str_nicks).contains(null))break;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                currentHand.nicks[i]=str_nicks[0];
                for(int n=1; n<id_img_pix_length; n++){
                    if(!str_nicks[0].equals(str_nicks[n])){currentHand.nicks[i]=null; break;}
                }

            if(currentHand.nicks[i]==null) {System.err.println("DUBLIKATS IMG_PIX_NICK");
            int n=-1;
            for(long d:id_img_pix) { n++;     save_image(cheked_img,"dublicats_nicks\\"+str_nicks[n]+"_"+d); }
                System.out.println("**********************************");
            }


            }




            //assert currentHand.nicks[i] != null;
            if(currentHand.nicks[i] != null&&
                    (currentHand.nicks[i].equals("Posts SB")||currentHand.nicks[i].equals("Posts BB")||
                            currentHand.nicks[i].equals("Fold")||currentHand.nicks[i].equals("Check")||currentHand.nicks[i].equals("Raise")))currentHand.nicks[i]=null;
            //System.out.println(currentHand.nicks[i]);
            //assert currentHand.nicks[i] != null;
            /*if(i==4){
            if(currentHand.nicks[4]==null)save_image(cheked_img,"SittingD_null");
            else if(currentHand.nicks[4].equals("SittingD"))save_image(cheked_img,"SittingD");}*/
            //if(currentHand.nicks[i].equals("Posts SB")||currentHand.nicks[i].equals("Fold")){is_error_image(cheked_img); save_image(cheked_img,"error_img\\"+(++c));}
            //if(currentHand.nicks[i]!=null)save_image(cheked_img,"test2\\"+currentHand.nicks[i]+"_"+get_max_brightness(cheked_img));
            images_of_nicks_for_ocr[i] = get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),limit_grey);
        }

        currentHand.setIs_nicks_filled();
        if(currentHand.is_nicks_filled) set_nick_by_positions_and_position_of_hero();

    }

    void set_nick_by_positions_and_position_of_hero(){
        // расстановка ников по покерным позициям, и на основе этого инициализация позиции херо
        currentHand.nicks[0] = nick_hero;
        String[] nicks_by_positions = new String[6];
        for(int i=0; i<6; i++){
            nicks_by_positions[i] = currentHand.nicks[poker_positions_of_numbers[i]-1];
            if(nicks_by_positions[i].equals(nick_hero))currentHand.position_of_hero = i;
        }
        currentHand.nicks = nicks_by_positions;
    }




    void set_cards_hero(){
        for(int i=0; i<2; i++){
            /*int x = coord_of_table[0]+coords_cards_hero[i][0];
            int y = coord_of_table[1]+coords_cards_hero[i][1];*/
            int x = coords_cards_hero[i][0];
            int y = coords_cards_hero[i][1];
            int w = 15;
            int h = 17;

            /*BufferedImage subimage = frame.getSubimage(x,y,w,h);
            BufferedImage cheked_img = check_free_of_kursor(subimage,200);*/

            BufferedImage cheked_img = check_free_of_kursor(x,y,w,h,200,0,0,0,0);
            if(cheked_img==null)continue;
            BufferedImage image = get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),100);
            for(Map.Entry<String,BufferedImage> entry:avirage_cards.entrySet()){
                if(compare_buffred_images(image,entry.getValue(),10)){
                    currentHand.cards_hero+=entry.getKey(); break;
                }
            }
            currentHand.cards_hero+=get_suit_of_card(cheked_img);
        }
        //System.out.println("cards "+current_cards_hero+"  table "+table);
        if(currentHand.cards_hero.length()<4)currentHand.cards_hero = "";
    }


    public long[] get_img_pix(BufferedImage image,int limit_grey){

        int count_64_pix = 0, W = image.getWidth(), H = image.getHeight()-1; // урезание картинки снизу на 1
        long _64_pixels =0; long[] result = new long[16]; int index_for_result = -1, start_get_pix = 0;long count_black_pix = 0;
        int count =0;
        for (int x = 0; x < W; x++) {
            // уразание картинки сверху на 2
            for (int y = 2; y < H; y++) {
                //if(start_get_pix<3){ start_get_pix++; continue;}
                int val = image.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = 255-(int) (r * 0.299 + g * 0.587 + b * 0.114);
                count_64_pix++;
                _64_pixels<<=1;
                if(grey<limit_grey){_64_pixels+=1; count_black_pix++;}
                if(count_64_pix==64){
                    index_for_result++;
                    result[index_for_result] = _64_pixels;
                    count_64_pix = 0;
                    _64_pixels = 0;
                }
                count++;
            }
        }
        result[15] = count_black_pix;

        return result;
    }

    String get_suit_of_card(BufferedImage image_card){
        Color color = new Color(image_card.getRGB(14, 16));
        int blue = color.getBlue();
        int red = color.getRed();
        int green = color.getGreen();
        //System.out.println("b "+blue+" r "+red+" g "+green);
        //if(blue==red&&red==green)return "s";
        if(blue>100&&blue>red&&blue>green)return "d";
        if(red>100&&red>blue&&red>green)return "h";
        if(green>100&&green>blue&&green>red)return "c";
        return "s";
    }


    void set_current_position_of_bu(){
            for(int i=0; i<6; i++){
                /*int x = coord_of_table[0]+coords_buttons[i][0];
                int y = coord_of_table[1]+coords_buttons[i][1];*/
                int x = coords_buttons[i][0];
                int y = coords_buttons[i][1];
                int w = 22;
                int h = 17;

                /*BufferedImage subimage = frame.getSubimage(x,y,w,h);
                BufferedImage cheked_img = check_free_of_kursor(subimage,200);*/
                BufferedImage cheked_img = check_free_of_kursor(x,y,w,h,200,0,0,0,0);
                if(cheked_img==null)continue;
                //save_image(cheked_img,"test\\bu"+(++c));
                /*BufferedImage sub_white_black_image = get_white_black_image(cheked_img,150);
                if(compare_part_of_buffred_images(sub_white_black_image,images_bu[i],9,5,16,12)){ currentHand.position_of_bu = i+1; break;}*/
                int bright = get_max_brightness(cheked_img);
                if(bright>200){currentHand.position_of_bu = i+1; break;}
                //System.out.println((i+1)+" "+bright);
            }
            // алгоритм определения соответсвия покерных позиций позициям за столом которые начинаются с херо, на основе того где на столе находится БУ
            int utg = currentHand.position_of_bu+3;
            if(utg>6) utg = utg-6;
            int positons_on_table = 0; boolean start = false; int i =-1;
            while (i!=5){
                positons_on_table++;
                if(positons_on_table==utg)start = true;
                if(start){
                    i++; poker_positions_of_numbers[i] = positons_on_table;
                }
                if(positons_on_table==6)positons_on_table=0;
            }
    }

    int c =0;


    long get_number_hand(){

        /*int x = coord_of_table[0]+579;
        int y = coord_of_table[1]+56;*/
        /*int x = 579;
        int y = 56;
        int w = 53;
        int h = 11;*/
        // возрат 0 если есть помеха или нельзя распознать, -1 если новая равна текущей картинке, 1 если новая картинка распозна


        /*BufferedImage subimage = frame.getSubimage(x,y,w,h);
        BufferedImage cheked_img = check_free_of_kursor(subimage,10);*/

        //BufferedImage cheked_img = check_free_of_kursor(x,y,w,h,10);

        //System.out.println("sub");
        //if(cheked_img==null){ return 0;}
        //System.out.println("check");

        int limit_grey = 175;
        if(get_max_brightness(frame[1])<150)limit_grey = 214;
        c++;
        //save_image(frame[1],"for_ocr_number\\osr_"+c+"_grey_"+limit_grey);
        //BufferedImage scaled_sub_bufferedImage = get_scale_image(set_grey_and_inverse_or_no(frame[1],true),2);
        //BufferedImage scaled_sub_bufferedImage = set_grey_and_inverse_or_no(get_scale_image(frame[1],3),true);
        BufferedImage black_white_image = get_white_black_image(set_grey_and_inverse_or_no(frame[1],true),limit_grey);

        if(compare_buffred_images(bufferedImage_current_number_hand,black_white_image,5))return -1;
        bufferedImage_current_number_hand = black_white_image;

        //save_image(black_white_image,"for_ocr_number\\osr_bw_"+c+"_grey_"+limit_grey);
        return 1;


        //System.out.println("ocr");
       /* String result = ocr_image(scaled_sub_bufferedImage, "hand");
        //if(result.trim().length()!=11)return 0;
        long res = 0;
        try{
             res = Long.parseLong(result.trim());
        } catch (Exception e){
          return 0;
        }
        if(res!=0){bufferedImage_current_number_hand = black_white_image;

        return  res; }

        return 0;*/
    }


    void get_start_stacks_and_preflop(){
       int p =0;
       //System.out.println("get stacks");
       for(int i=0; i<6; i++){
           if(currentHand.stacks[i]!=0&&currentHand.preflop_by_positions.get(i).get(currentHand.preflop_by_positions.get(i).size()-1)==1_000_000)continue;

           /*if(i==0||i==1||i==2||i==3)p=0;
           else p=-1;*/
           /*int x = coord_of_table[0]+coords_places_of_nicks[poker_positions_of_numbers[i]-1][0]+p+5;
           int y = coord_of_table[1]+coords_places_of_nicks[poker_positions_of_numbers[i]-1][1]+18;*/
           int x = coords_places_of_nicks[poker_positions_of_numbers[i]-1][0]+p+5;
           int y = coords_places_of_nicks[poker_positions_of_numbers[i]-1][1]+18;
           int w = 72;
           int h = 13;

           /*BufferedImage subimage = frame.getSubimage(x,y,w,h);
           BufferedImage cheked_img = check_free_of_kursor(subimage,200);*/


           /*System.out.println("oldm "+(System.currentTimeMillis()-s));
           s =System.currentTimeMillis();*/
           BufferedImage cheked_img = check_free_of_kursor(x,y,w,h,200,0,0,0,0);
           //System.out.println("newm "+(System.currentTimeMillis()-s));
          // if(currentHand.stacks[i]==0&&currentHand.first_round_preflop[i]==0){ }

           /*if(i==4){save_image(cheked_img,"new_stacks_"+i+" t "+table);
               System.out.println("stack "+currentHand.stacks[i]);}*/
           //if(i==3)save_image(Java2DFrameUtils.toBufferedImage(subimage),"notsee");
          // if((i>0&&(float)currentHand.preflop_by_positions.get(i-1).get(0)>0)||i==0||currentHand.preflop_by_positions.get(i).size()>2){
           if(currentHand.preflop_by_positions.get(i).get(currentHand.preflop_by_positions.get(i).size()-1)!=1_000_000){
               if(cheked_img==null)continue;
               int brightness = get_max_brightness(cheked_img);
              //if(i==5) System.out.println("brigh "+brightness);
               if(brightness<80){ currentHand.is_preflop_end = true; break;}
              //if(i==5) System.out.println(currentHand.nicks[i]+"      "+get_max_brightness(cheked_img)+" "+currentHand.preflop_by_positions.get(i).get(0));
              if(get_max_brightness(cheked_img)<150){if(currentHand.preflop_by_positions.get(i).size()==1&&!(currentHand.preflop_by_positions.get(i).get(0)>0))currentHand.preflop_by_positions.get(i).set(0,1_000_000f);
              else currentHand.preflop_by_positions.get(i).add(1_000_000f);
              }
              else {
                  /*int xa = coord_of_table[0]+coords_actions[poker_positions_of_numbers[i]-1][0];
                  int ya = coord_of_table[1]+coords_actions[poker_positions_of_numbers[i]-1][1];*/
                  int xa = coords_actions[poker_positions_of_numbers[i]-1][0];
                  int ya = coords_actions[poker_positions_of_numbers[i]-1][1];
                  int wa = 54;
                  int ha = 11;

                  /*BufferedImage subimage_action = frame.getSubimage(xa,ya,wa,ha);
                  BufferedImage cheked_img_action = check_free_of_kursor(subimage_action,200);*/


                  BufferedImage cheked_img_action = check_free_of_kursor(xa,ya,wa,ha,200,0,0,0,0);

                  if(cheked_img_action==null)continue;

                  /*if(i==4&&bufferedImage_current_sb!=null){if(compare_buffred_images(bufferedImage_current_sb,cheked_img_action,1))continue;}
                  if(i==5&&bufferedImage_current_bb!=null){if(compare_buffred_images(bufferedImage_current_bb,cheked_img_action,1))continue;}*/
                  BufferedImage inversedimage = set_grey_and_inverse_or_no(cheked_img_action,true);
                  BufferedImage black_white_img_for_campare = get_white_black_image(inversedimage,150);
                  if(!compare_buffred_images(bufferedimage_current_position_actions[i],black_white_img_for_campare,5)) {


                  /*if(i==5){save_image(cheked_img_action,"stacks_"+i);
                      System.out.println(currentHand.nicks[i]+"  "+get_max_brightness(cheked_img_action));}*/

                  if(get_max_brightness(cheked_img_action)>150){
                    // if(i==4) save_image(cheked_img_action,"img4");
                      String action = ocr_image(get_scale_image(inversedimage,4),"actions").trim();
                      //if(i==5) System.out.println("*"+action+"*");
                      float action_float = 0;
                      try{
                          action_float = Float.parseFloat(action);
                      } catch (Exception e){
                          continue;
                      }
                       //if(i==5) System.out.println(action_float);
                      if((i==4&&action_float==sb)||(i==5&&action_float==1)){bufferedimage_current_position_actions[i]=black_white_img_for_campare;} else {
                      bufferedimage_current_position_actions[i] = black_white_img_for_campare;
                     /* c++;
                      save_image(black_white_img_for_campare,"test\\"+table+"_"+i+"_bwimg_"+c);*/
                      if(currentHand.preflop_by_positions.get(i).get(currentHand.preflop_by_positions.get(i).size()-1)<action_float) {
                          if(!(currentHand.preflop_by_positions.get(i).get(0)>0))currentHand.preflop_by_positions.get(i).set(0,action_float);
                          else currentHand.preflop_by_positions.get(i).add(action_float);
                      }
                     }
                    }
                  }
              }
           }
           //System.out.println(i+"  "+currentHand.first_round_preflop[i]);
           //if(i==5) System.out.println("*"+currentHand.stacks[i]);
           if(currentHand.stacks[i]==0){

               if(cheked_img==null){ continue;}
               // нет стека есть действие
               float stack_without_action = 0;
               String stack = ocr_image(get_scale_image(set_grey_and_inverse_or_no(cheked_img,true),2),"stacks").trim();


               try{
                   stack_without_action = Float.parseFloat(stack);

               } catch (Exception e){
                   continue;
               }
               //System.out.println(i+"  "+stack_without_action);
               //if(i==0)System.out.println("p "+i+" stack "+stack+" "+currentHand.stacks[i]+" number "+stack_without_action);
               if(currentHand.preflop_by_positions.get(i).get(0)==1_000_000||currentHand.preflop_by_positions.get(i).get(0)==0){currentHand.stacks[i] = stack_without_action; continue;}    // fold = -10
               float abc = Math.abs(currentHand.preflop_by_positions.get(i).get(0));
               if(abc>0){currentHand.stacks[i] = stack_without_action+abc;}
           }
           //System.out.println(currentHand.stacks[i]);
       }
        //System.out.println("************************************");

    }

    void check_start_flop(){
        //System.out.println("check_start_flop");
        // проверка что херо не делал ход, кроме когда находится на ББ, где возможен чек, если не делал, то проверки на флоп нет
        if(!(currentHand.preflop_by_positions.get(currentHand.position_of_hero).get(0)>0)&&currentHand.position_of_hero!=5) return;
        /*int x1 = coord_of_table[0]+coord_2_3_cards_flop[0][0];
        int x2 = coord_of_table[0]+coord_2_3_cards_flop[1][0];
        int y = coord_of_table[1]+coord_2_3_cards_flop[0][1];*/
        int x1 = coord_2_3_cards_flop[0][0];
        int x2 = coord_2_3_cards_flop[1][0];
        int y = coord_2_3_cards_flop[0][1];

        if(get_max_brightness(frame[0].getSubimage(x1,y,17,17))>220
                &&get_max_brightness(frame[0].getSubimage(x2,y,17,17))>220)currentHand.is_start_flop = true;
        //if(currentHand.is_start_flop){save_image(frame[0].getSubimage(x1,y,17,17),"test2\\c1"); save_image(frame[0].getSubimage(x2,y,17,17),"test2\\c2"); }
    }


   String ocr_image(BufferedImage bufferedImage,String type){
       String result = null;
       while (true){
           for(UseTesseract use_tesseart:use_tessearts){
               result = use_tesseart.get_ocr(bufferedImage,type);
               if(result!=null)return result;
           }
       }
    }


    int get_max_brightness(BufferedImage image){
        int w = image.getWidth(); int y = image.getHeight()/2;
        int max = 0;
        for(int x=0; x<w; x++){
            int val = image.getRGB(x, y);
            int r = (val >> 16) & 0xff;
            int g = (val >> 8) & 0xff;
            int b = val & 0xff;
            int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
            if(grey>max)max=grey;
        }
        return max;
    }



    boolean is_error_image(BufferedImage image){
        int h = image.getHeight(), w = image.getWidth();
        int count_line_with_symbols = 0; boolean is_symbol_start = false; int count_white = 0;
        for(int i=18;i<w;i++) {
            for(int j=0;j<h;j++) {
                int val = image.getRGB(i, j);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                if(grey<100)continue;
                is_symbol_start = true;
                //System.out.println("er "+grey);
                if(grey>200)count_white++;
                //if(isBlue(new Color(val)))count_blue++;
            }
            if(is_symbol_start)count_line_with_symbols++;
            if(count_line_with_symbols==5)break;
        }
        return count_white <= 0;
    }



    public static boolean isBlue(Color c) {
        final float MIN_BLUE_HUE = 0.5f; // CYAN
        final float MAX_BLUE_HUE = 0.8333333f; // MAGENTA
        float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        float hue = hsv[0];
        return hue >= MIN_BLUE_HUE && hue <= MAX_BLUE_HUE;
    }

    BufferedImage get_scale_image(BufferedImage img,double scale){
        try {
            return Thumbnails.of(img).scale(scale).asBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    BufferedImage get_scale_image(BufferedImage img, int size){ return Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, img.getWidth()*size,img.getHeight()*size); }



    BufferedImage check_free_of_kursor(int X, int Y, int w, int h, int limit_grey,int cutX1, int cutY1,int cutX2, int cutY2){

        for(int x=X; x<w+X; x++){
            for(int y=Y; y<h+Y; y+=h-1){
                int val = frame[0].getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                //if(grey>limit_grey)System.out.println(grey);
                if(grey>limit_grey)return null;
            }
            //System.out.println();
        }
        for(int y=Y; y<h+Y; y++)
            for(int x=X; x<w+X; x+=w-1){
                int val = frame[0].getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                //if(grey>limit_grey)System.out.println(grey);
                if(grey>limit_grey)return null;
            }
        // System.out.println();
        return frame[0].getSubimage(X+cutX1,Y+cutY1,w+cutX2,h+cutY2);
    }


    BufferedImage set_grey_and_inverse_or_no(BufferedImage  source, boolean isnverse){
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Color newColor = null;

        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                // Получаем цвет текущего пикселя
                int val = source.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                // Применяем стандартный алгоритм для получения черно-белого изображения
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                // Если вы понаблюдаете, то заметите что у любого оттенка серого цвета, все каналы имеют
                // одно и то же значение. Так, как у нас изображение тоже будет состоять из оттенков серого
                // то, все канали будут иметь одно и то же значение.
                /*if(grey>150){ grey = 0;}
                else grey = 255;*/
                if(isnverse) grey = 255-grey;
                //  Cоздаем новый цвет
                newColor = new Color(grey, grey, grey);
                // И устанавливаем этот цвет в текущий пиксель результирующего изображения
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }


    boolean compare_buffred_images(BufferedImage current_image,BufferedImage _new_image, int limit_error){
       if(current_image==null||_new_image==null)return false;
       int error = 0;
        for (int i = 0; i < current_image.getWidth(); i++) {
            for (int j = 0; j < current_image.getHeight(); j++) {
                int rgb = Math.abs(current_image.getRGB(i, j) - _new_image.getRGB(i, j));
                if (rgb != 0) {
                   error++;
                    //if(limit_error==20) System.out.println(error);
                   if(error>limit_error){
                       //System.out.println("false "+error);
                       return false;}
                }
            }
        }
        //System.out.println("true "+error);
        return true;
    }


    boolean compare_part_of_buffred_images(BufferedImage current_image,BufferedImage _new_image,int x,int y, int w, int h){
        int error = 0;
        for (int i = x; i < w; i++) {
            for (int j = y; j < h; j++) {
                int rgb = Math.abs(current_image.getRGB(i, j) - _new_image.getRGB(i, j));
                if (rgb != 0) {
                   error++;
                }
            }
        }
        return error <= 1;
    }


    BufferedImage get_white_black_image(BufferedImage  source,int limit_grey){
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        for (int x = 0; x < source.getWidth(); x++)
            for (int y = 0; y < source.getHeight(); y++) {
                int val = source.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                if(grey<limit_grey){ grey = 0;}
                else grey = 255;
                result.setRGB(x, y, new Color(grey, grey, grey).getRGB());
            }
        //System.out.println("error "+error);
        return result;
    }



   static synchronized void save_image(BufferedImage image,String name_file){
        try {
            ImageIO.write(image ,"png",new File(home_folder+"\\"+name_file+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    int[] compute_right_gray_for_white_black(BufferedImage image_from_file,BufferedImage image_from_table){
        int min_error = 272;
        int error = 0;
        int result = 0;
        BufferedImage wb_image_from_file = null, wb_image_from_table = null;
        for(int a=100; a<200; a++){
            wb_image_from_file = get_white_black_image(image_from_file,a);
            wb_image_from_table = get_white_black_image(image_from_table,a);
            for (int i = 0; i < wb_image_from_file.getWidth(); i++) {
                for (int j = 0; j < wb_image_from_file.getHeight(); j++) {
                    int rgb = Math.abs(wb_image_from_file.getRGB(i, j) - wb_image_from_table.getRGB(i, j));
                    if (rgb != 0) {
                        error++;
                    }
                }
            }
            System.out.println("error "+error+" result "+a);
            if(error<min_error){min_error=error; result = a;}
            if(min_error==error)result =a;
            error = 0;
        }
        return new int[]{result,min_error};
    }


    public synchronized void stop(){
        is_run = false;
        notify();
    }

}
