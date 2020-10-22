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
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/*import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.cvResetImageROI;*/
import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.main.CaptureVideo.shablons_numbers_0_9;
import static org.trenkvaz.main.Testing.*;
//import static org.trenkvaz.main.Settings.write_nicks_keys_img_pix;

public class OCR implements Runnable {

    boolean is_run = true;
    int table = -1;
    BufferedImage[] frame;
    int[] coord_of_table;
    Queue<BufferedImage[]> main_queue_with_frames;
    BufferedImage bufferedImage_current_number_hand;
    CurrentHand currentHand;
    float sb = 0.5f;
    int[] poker_positions_index_with_numbering_on_table = new int[6];
    BufferedImage[] bufferedimage_current_position_actions = new BufferedImage[6];
    int current_bu = -1;
    String[] current_hero_cards = new String[]{"",""};


    // test
    BufferedImage[] images_of_nicks_for_ocr = new BufferedImage[6];
    BufferedImage[] test_cards = new BufferedImage[2];
    record ImgStacks(BufferedImage img,String stack){}
    ImgStacks[]  imgStacks = new ImgStacks[6];
    List<ImgStacks[]> test_list_imgStacks = new ArrayList<>();

    //Queue<Integer> testquer = new LinkedList<>();

    public OCR(int table){
        this.coord_of_table = coord_left_up_of_tables[table];
        this.table = table+1;
        main_queue_with_frames = new LinkedBlockingQueue<>();


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
            if((frame = main_queue_with_frames.poll())!=null){
                //numberc = testquer.poll();
                main_work_on_table();
               // frame = null;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(main_queue_with_frames.size()>100){System.out.println("table "+table+"    "+ main_queue_with_frames.size());c++;
            if(frame[0]!=null) save_image(frame[0],"tables_img\\"+table+"_"+c);
            }
        }
    }
    //private int numberc = -1;

    public synchronized void set_image_for_ocr(BufferedImage[] frame){
        main_queue_with_frames.offer(frame);
    }

    boolean start_hand = false;
    //boolean is_nicks_filled = false;
    static int S =0;
    static synchronized void show_total_hand(OCR ocr,BufferedImage[] test_cards){
        System.out.println("****** cards "+ocr.currentHand.cards_hero[0]+ocr.currentHand.cards_hero[1]+" flop "+ocr.currentHand.is_start_flop+
                " bu "+ocr.currentHand.position_bu_on_table +" table "+ocr.table);
        boolean save_hand_with_null_img = false;
        boolean is_save_test_list = false;
        for(int i=0; i<6; i++) {
            System.out.print(ocr.currentHand.nicks[i]+"    "+ocr.currentHand.stacks[i]+"  ");
            /*if(currentHand.stacks[i]==0){
                save_image(ocr.frame[0],"test5\\table_"+ocr.table);
                for(int s=0; s<ocr.test_list_imgStacks.size(); s++){
                    save_image(ocr.test_list_imgStacks.get(s)[i].img(),"test5\\table_"+ocr.table+"_stack_"+ocr.test_list_imgStacks.get(s)[i].stack());
                }
            }*/
            /*if(currentHand.nicks[i]==null&&!is_save_test_list){
                is_save_test_list = true;
                String namefolder = (S++)+"\\";
                *//*for(int l=0; l<list_test_numberhands.size(); l++){
                    BufferedImage[] img = list_test_numberhands.get(l);
                    String namehand = l+"";
                    if(img[1]!=null)namehand+="_FALSE";
                    Testing.save_image(img[0],"test3\\"+namefolder+"_"+namehand);
                }*//*

                for(int l=0; l<list_test_cards.size(); l++){
                    BufferedImage[] imgs = list_test_cards.get(i).card();
                    String cards = list_test_cards.get(i).nominal_card();
                    Testing.save_image(imgs[0],"test3\\"+namefolder+"_"+l+"_c1_"+cards);
                    Testing.save_image(imgs[1],"test3\\"+namefolder+"_"+l+"_c2_"+cards);
                }
            }*/

            /*if(i>0&&currentHand.nicks[i]==null&&!save_hand_with_null_img){
                save_hand_with_null_img =true;
                for(int s=1; s<6; s++)save_image(images_of_nicks_for_ocr[i],"test\\null_nicks"+(++S));
            }*/
            /*BufferedImage im = new BufferedImage(30, 17, BufferedImage.TYPE_INT_ARGB);
            im.getGraphics().drawImage(test_cards[0], 0, 0, null);
            im.getGraphics().drawImage(test_cards[1], 15, 0, null);
            save_image(im,"test4\\"+currentHand.cards_hero[0]+currentHand.cards_hero[1]);*/

            for(Float a:ocr.currentHand.preflop_by_positions.get(i)) System.out.print(a+"  ");
            System.out.println();

        }

        System.out.println("******************************************");
    }


    private void main_work_on_table(){
        if(table!=4)return;


        int check_start_or_end_hand = get_number_hand();
        //System.out.println(check_start_or_end_hand);
        if(check_start_or_end_hand==0) return;

        if(check_start_or_end_hand==1) {
            if(currentHand!=null){
                /*if(currentHand.cards_hero[0].equals("3h")&&currentHand.cards_hero[1].equals("2h")&&table==3){
                    System.out.println("TOTAL ---------------------------------------  "+currentHand.nicks[4]);

                }*/
                show_total_hand(this,test_cards);

            }
            //list_test_numberhands.clear();
            list_test_cards.clear();
            test_list_imgStacks.clear();



            currentHand = new CurrentHand(table-1,sb);
            for(int i=0; i<6; i++)bufferedimage_current_position_actions[i]=null;
           /*current_cards_hero = "";
           current_position_of_sb = 0;*/
            start_hand = true;
            test_cards[0] = null;
            test_cards[1] = null;
            //for(int i=1; i<6; i++)current_nicks[i]=null;
            //is_nicks_filled = false;
            currentHand.position_bu_on_table = current_bu;
            currentHand.cards_hero[0] = current_hero_cards[0];
            currentHand.cards_hero[1] = current_hero_cards[1];
        }

       /* if(check_start_or_end_hand==0){
            if(currentHand!=null){
                *//*if(currentHand.cards_hero[0].equals("3h")&&currentHand.cards_hero[1].equals("2h")&&table==3){
                    System.out.println("TOTAL ---------------------------------------  "+currentHand.nicks[4]);

                }*//*
             show_total_hand(this,test_cards);

            }
            //list_test_numberhands.clear();
            list_test_cards.clear();
            test_list_imgStacks.clear();
           return;
        }*/

        /*if(currentHand.position_bu_on_table ==0) set_current_position_of_bu();

        if(currentHand.position_bu_on_table >0&&(currentHand.cards_hero[0].equals("")||currentHand.cards_hero[1].equals(""))) set_cards_hero();*/

        /*if(currentHand.cards_hero[0].equals("3h")&&currentHand.cards_hero[1].equals("2h")&&table==3
        )save_image(frame[0],"test2\\_table_"+(++c));*/


        if(currentHand.position_bu_on_table >0&&!(currentHand.cards_hero[0].equals("")||currentHand.cards_hero[1].equals(""))&&!currentHand.is_nicks_filled) {get_nicks();}

        if(currentHand.position_bu_on_table >0&&!(currentHand.cards_hero[0].equals("")||currentHand.cards_hero[1].equals(""))
                &&currentHand.is_nicks_filled&&!currentHand.is_preflop_end) {

            if(!currentHand.is_start_flop)check_start_flop();
            if(!currentHand.is_start_flop)get_start_stacks_and_preflop();
        }

       /* if(currentHand.cards_hero[0].equals("3h")&&currentHand.cards_hero[1].equals("2h")&&table==3)
            System.out.println("MAIN ---------------------------------------  "+currentHand.nicks[4]);*/
        //System.out.println("bu "+currentHand.position_of_bu+" cards "+currentHand.cards_hero+" allnicks "+currentHand.is_nicks_filled);


        //System.out.println("bu "+currentHand.position_of_bu);


       /* if(currentHand.position_of_bu >0&&!currentHand.cards_hero.equals("")&&currentHand.is_nicks_filled&&start_hand&&currentHand.is_stacks_and_1_raund_actions_filled){
            System.out.println("nicks "+currentHand.is_nicks_filled);
            System.out.println("table "+table+" number "+currentHand.number_hand+" BU - "+currentHand.position_of_bu +",       "+currentHand.cards_hero); start_hand=false;


        }*/
    }




    void get_nicks(){

        //System.out.println("get nicks");
        //int p = 0;
        int[] correction_for_place_of_nicks = {1,2,2,2,1,1};
        int w = 86;
        int h = 14;

        for(int i=1; i<6; i++){
            if(currentHand.nicks[i]!=null)continue;
            int x = coords_places_of_nicks[i][0]+correction_for_place_of_nicks[i]-5;
            int y = coords_places_of_nicks[i][1]+1;
            /*boolean isplace =false;
            if(i==4&&currentHand.cards_hero[0].equals("3h")&&currentHand.cards_hero[1].equals("2h")&&table==3)
            {save_image(frame[0].getSubimage(x,y,w,h),"test4\\"+(c++)); isplace=true;}*/
            long[] img_pix = get_longarr_HashImage(frame[0],x,y+2,w,h-3,15,150);

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
                        //if(isplace) System.out.println("1 setnick+++++++++++++++++++++++++++++  "+currentHand.nicks[i]);

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
                BufferedImage cheked_img = frame[0].getSubimage(x,y,w,h);
                    while (true){
                        attempt++;
                        currentHand.nicks[i] = ocr_image(get_white_black_image(set_grey_and_inverse_or_no(get_scale_image(cheked_img,4),true),105),"nicks").trim();
                        //System.out.println("osr  "+currentHand.nicks[i]);
                        //if(isplace) System.out.println("2 setnick+++++++++++++++++++++++++++++  "+currentHand.nicks[i]);

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
                save_image(get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),105),"id_nicks\\"+currentHand.nicks[i]+" "+(-id_img_pix[0])+"_"+c+""+table);
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

            if(currentHand.nicks[i]==null) {
                System.err.println("DUBLIKATS IMG_PIX_NICK");
                BufferedImage cheked_img = frame[0].getSubimage(x,y,w,h);
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
            //images_of_nicks_for_ocr[i] = get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),limit_grey);
            //if(isplace) System.out.println("NICK    /////////////////////////////////////////    "+currentHand.nicks[i]);
        }
        currentHand.setIs_nicks_filled();
        /*if(currentHand.cards_hero[0].equals("Qc")&&currentHand.cards_hero[1].equals("2h")&&table==3){
            for(String n:currentHand.nicks) { if(n==null){
                System.out.println("null"); continue;
            }
                System.out.println("*"+n);}
        save_image(frame[0],"test\\"+(c++));
        }*/
        if(currentHand.is_nicks_filled) set_nick_by_positions_and_position_of_hero();
        //if(currentHand.cards_hero[0].equals("Qc")&&currentHand.cards_hero[1].equals("2h")) System.out.println("NICKS");;
    }


    void set_nick_by_positions_and_position_of_hero(){
        // расстановка ников по покерным позициям, и на основе этого инициализация позиции херо
        currentHand.nicks[0] = nick_hero;
        String[] nicks_by_positions = new String[6];
        for(int i=0; i<6; i++){
            nicks_by_positions[i] = currentHand.nicks[poker_positions_index_with_numbering_on_table[i]-1];
            if(nicks_by_positions[i].equals(nick_hero))currentHand.poker_position_of_hero = i;
        }
        currentHand.nicks = nicks_by_positions;
    }


    String[] set_cards_hero(){
        //System.out.println("set_cards_hero");
        //BufferedImage[] cards = new BufferedImage[2];
        String[] result = new String[]{"",""};

        for(int i=0; i<2; i++){
            //if(result[i].length()!=0)continue;
            int X = coords_cards_hero[i][0];
            int Y = coords_cards_hero[i][1];

            // проверка периметра карта на помеху курсором
           if(!is_noCursorInterferenceImage(frame[0],X+1,Y,15,17,240))continue;

           //test_cards[i] = frame[0].getSubimage(X+1,Y,w,h);

           long[] card_hash_from_table = get_longarr_HashImage(frame[0],X+1,Y+1,14,14,3,150);

            //show_img_from_arr_long(card_hash_from_table,14,14);
            int first_of_pair_error = 0, second_of_pair_error = 0, limit_error = 10, total_error = 0;
         out: for(int nominal_ind_list = 0; nominal_ind_list<52; nominal_ind_list++){
                // сравнение количества черных пикселей между хешем_имдж из массива номиналы_карт с хешем_имдж со стола
                //System.out.println("i "+i+" nom "+nominals_cards[nominal_ind_list/4]+"  err "+abs(_long_arr_cards_for_compare[nominal_ind_list][3]-card_hash_from_table[3]));
                if(abs(_long_arr_cards_for_compare[nominal_ind_list][3]-card_hash_from_table[3])>limit_error)continue;

                total_error = 0;

                for(int ind_num=0; ind_num<3; ind_num++){
                    total_error+= get_AmountOneBitInLong(_long_arr_cards_for_compare[nominal_ind_list][ind_num]^card_hash_from_table[ind_num]);
                    if(total_error>limit_error){ continue out;  }
                }

                //System.err.println("TOTAL ERROR "+total_error);
                // если нашлось совпадение, то берется номинал карты деление на 4 для получения индекса где 13 эелементов вместо 52
                result[i]=nominals_cards[nominal_ind_list/4];
                break;
            }

            if(result[i].length()<2)result[i]+=get_suit_of_card(frame[0],X+14,Y+16);

        }

        if(result[0].length()==2&&result[1].length()==2)return result;

        return null;
        /*Testing.Cards cards1 = new Testing.Cards(cards,currentHand.cards_hero);
        list_test_cards.add(cards1);*/
        //System.out.println("cards "+current_cards_hero+"  table "+table);
        //if(currentHand.cards_hero.length()!=4)currentHand.cards_hero = "";
    }


    private long abs(long a) { return (a < 0) ? -a : a; }


    public long[] get_longarr_HashImage(BufferedImage image,int X, int Y, int W, int H, int amount_64nums, int limit_grey){
        //System.out.println("new x "+X+" y "+Y+" w "+W+" H "+H);
        long _64_pixels =0, count_black_pix = 0, amount_pix = W*H;
        long[] longarr_hashimage = new long[amount_64nums+1]; int index_longarr_hashimage = -1, count_64_pix = 0;
        int count_all_pix = 0;
      out:for (int x = X; x < X+W; x++){
            for (int y = Y; y < Y+H; y++) {
                count_all_pix++;
                count_64_pix++;
                _64_pixels<<=1;
                if(get_intGreyColor(image,x,y)>limit_grey){ _64_pixels+=1; count_black_pix++; }
                if(count_64_pix==64||count_all_pix==amount_pix){
                    index_longarr_hashimage++;
                    longarr_hashimage[index_longarr_hashimage] = _64_pixels;
                    count_64_pix = 0;
                    _64_pixels = 0;
                }
                if(index_longarr_hashimage==amount_64nums-1)break out;
            }
        }
        longarr_hashimage[amount_64nums] = count_black_pix;
        return longarr_hashimage;
    }



    private String get_suit_of_card(BufferedImage image_card,int X,int Y){
        Color color = new Color(image_card.getRGB(X, Y));
        int blue = color.getBlue();
        int red = color.getRed();
        int green = color.getGreen();
        if(blue>100&&blue>red&&blue>green)return "d";
        if(red>100&&red>blue&&red>green)return "h";
        if(green>100&&green>blue&&green>red)return "c";
        return "s";
    }


    private int set_current_position_of_bu(){
        //System.out.println("set_current_bu");
            int result = -1;

            for(int i=0; i<6; i++){
                int x = coords_buttons[i][0];
                int y = coords_buttons[i][1];
                if(!is_noCursorInterferenceImage(frame[0],x,y,22,17,200))continue;
                if(get_int_MaxBrightnessMiddleImg(frame[0],x,y,22,17)>200){result = i+1; break;}
            }

            if(result==-1)return -1;
            // алгоритм определения соответсвия покерных позиций позициям за столом которые начинаются с херо, на основе того где на столе находится БУ
            int utg = result +3;
            if(utg>6) utg = utg-6;
            int positons_on_table = 0; boolean start = false; int i =-1;
            while (i!=5){
                positons_on_table++;
                if(positons_on_table==utg)start = true;
                if(start){
                    i++; poker_positions_index_with_numbering_on_table[i] = positons_on_table;
                }
                if(positons_on_table==6)positons_on_table=0;
            }
            return result;
    }

    int c =0;
    int P = 0;

    int get_number_hand(){

       /* if(frame[0]==null)return 0;
        else {
            if(frame[1]!=null)return 1;
            else return -1;
        }*/
       String[] hero_cards = set_cards_hero();
       // карты могут пропадать в конце текущей раздачи, отсутствие карт в новой раздаче пока не обнаружено
       if(hero_cards==null)return -1;
        //if(hero_cards[0].equals("7c")&&hero_cards[1].equals("7h"))save_image(frame[0],"test2\\"+(c++));
       int bu = set_current_position_of_bu();
       /*if(!(hero_cards[0].equals(current_hero_cards[0])&&hero_cards[1].equals(current_hero_cards[1]))&&bu==-1){
           System.err.println("ERROR");
           save_image(frame[0],"test\\"+(c++));
        }*/

        // БУ может отсутствовать и в начале новой и в конце старой раздачи
        // если БУ не определилась проверяет совпадение новых карт со старыми если да, то считается текущая раздача, если карты разные, то кадр пропускается
       if(bu==-1)if(hero_cards[0].equals(current_hero_cards[0])&&hero_cards[1].equals(current_hero_cards[1]))return -1;
                 else return 0;
       // если БУ определилась, то проверяет совпадение новых карт со старыми если да, то считается текущая раздача, если карты разные, то считается началом новой раздачи
       if(hero_cards[0].equals(current_hero_cards[0])&&hero_cards[1].equals(current_hero_cards[1])&&bu==current_bu){
           return -1;
       } else {
           current_hero_cards[0] = hero_cards[0];
           current_hero_cards[1] = hero_cards[1];
           current_bu = bu;
           return 1;
       }












    }


    void get_start_stacks_and_preflop(){

        //System.out.println("get stacks");
        ImgStacks[] imgStacks = new ImgStacks[6];
        int[] correction_for_place_of_nicks = {1,2,2,2,1,1};

        for(int poker_position=0; poker_position<6; poker_position++){
            // проверка последнего действия на префлопе на фолд берется последний индекс
            if(currentHand.preflop_by_positions.get(poker_position).get(currentHand.preflop_by_positions.get(poker_position).size()-1)==1_000_000)continue;

            //if(currentHand.cards_hero[0].equals("Kd")&&currentHand.cards_hero[1].equals("7c"))save_image(frame[0],"test\\"+(c++)+"_"+poker_position);




           if(is_Fold(poker_position)){if(currentHand.preflop_by_positions.get(poker_position).size()==1&&!(currentHand.preflop_by_positions.get(poker_position).get(0)>0))
                currentHand.preflop_by_positions.get(poker_position).set(0,1_000_000f);
            else currentHand.preflop_by_positions.get(poker_position).add(1_000_000f);
            }

            int xa = coords_actions[poker_positions_index_with_numbering_on_table[poker_position]-1][0];
            int ya = coords_actions[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+2;
            int wa = 54;
            int ha = 11;
            if(!(get_int_MaxBrightnessMiddleImg(frame[0],xa,ya,wa,ha)>220))continue;
            BufferedImage subimage_action = frame[0].getSubimage(xa,ya,wa,ha);
            if(!compare_buffred_images(bufferedimage_current_position_actions[poker_position],subimage_action,5)){
            bufferedimage_current_position_actions[poker_position] = subimage_action;
            //save_image(subimage_action,"test4\\"+poker_position+"_"+(c++));
            }


            //else
              /*  {
                int xa = coords_actions[poker_positions_of_numbers[i]-1][0];
                int ya = coords_actions[poker_positions_of_numbers[i]-1][1];
                int wa = 54;
                int ha = 11;

                BufferedImage subimage_action = frame.getSubimage(xa,ya,wa,ha);
                BufferedImage cheked_img_action = check_free_of_kursor(subimage_action,200);


                BufferedImage cheked_img_action = check_free_of_kursor(xa,ya,wa,ha,200,0,0,0,0);

                if(cheked_img_action==null)continue;

                if(i==4&&bufferedImage_current_sb!=null){if(compare_buffred_images(bufferedImage_current_sb,cheked_img_action,1))continue;}
                if(i==5&&bufferedImage_current_bb!=null){if(compare_buffred_images(bufferedImage_current_bb,cheked_img_action,1))continue;}
                BufferedImage inversedimage = set_grey_and_inverse_or_no(cheked_img_action,true);
                BufferedImage black_white_img_for_campare = get_white_black_image(inversedimage,150);
                if(!compare_buffred_images(bufferedimage_current_position_actions[i],black_white_img_for_campare,5)) {


                    if(i==5){save_image(cheked_img_action,"stacks_"+i);
                        System.out.println(currentHand.nicks[i]+"  "+get_max_brightness(cheked_img_action));}

                    if(get_max_brightness(cheked_img_action)>200){
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
                            c++;
                            save_image(black_white_img_for_campare,"test\\"+table+"_"+i+"_bwimg_"+c);
                            if(currentHand.preflop_by_positions.get(i).get(currentHand.preflop_by_positions.get(i).size()-1)<action_float) {
                                if(!(currentHand.preflop_by_positions.get(i).get(0)>0))currentHand.preflop_by_positions.get(i).set(0,action_float);
                                else currentHand.preflop_by_positions.get(i).add(action_float);
                            }
                        }
                    }
                }
            }*/


        }


        if(!currentHand.is_stacks_filled){
            int count_filled_stacks = 0;
            for(int poker_position =0; poker_position<6; poker_position++){
                if(currentHand.stacks[poker_position]!=0){ count_filled_stacks++; continue;}
                // массив покерные позиции индексы 0-5 это утг-бб, элементы это номера 1-6 позиции на столе начиная с херо
                int x = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][0]
                        +5+correction_for_place_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1];
                int y = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+17;

                if(!is_noCursorInterferenceImage(frame[0],x,y,72,14,100))continue;

                float stack_without_action = get_OcrNum(get_list_intarr_HashNumberImg(frame[0],x,y+1,72,12,175));
                //System.out.println("stack "+stack_without_action);
                if(currentHand.preflop_by_positions.get(poker_position).get(0)==1_000_000||currentHand.preflop_by_positions.get(poker_position).get(0)==0)
                {currentHand.stacks[poker_position] = stack_without_action; continue;}    // fold = -10
                float abc = Math.abs(currentHand.preflop_by_positions.get(poker_position).get(0));
                if(abc>0){currentHand.stacks[poker_position] = stack_without_action+abc;}
                count_filled_stacks++;
            }
            if(count_filled_stacks==6)currentHand.is_stacks_filled = true;
        }

        //test_list_imgStacks.add(imgStacks);
        //System.out.println("************************************");

    }


    private boolean is_Fold(int poker_position){
        if(poker_position==currentHand.poker_position_of_hero)return false;
        int[] correction_for_place_of_imgfold = {-31,97,97,97,-31,-31};
        int x = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][0]
                +correction_for_place_of_imgfold[poker_positions_index_with_numbering_on_table[poker_position]-1];
        int y = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+8;
        int j =y-1, max = 0;
        for(int i=x; i<x+15; i++){ j++;
            int grey = get_intGreyColor(frame[0],i,j);
            //if(get_intGreyColor(frame[0],i,j)>max)return false;
            if(grey>max)max=grey;
        }
        //if(currentHand.cards_hero[0].equals("Kd")&&currentHand.cards_hero[1].equals("7c"))save_image(frame[0],"test\\"+poker_position+"_"+(max));
        return !(max>70);
    }


    private float get_OcrNum(List<int[]> list_hash_nums){
        int limit_error = 10, total_error = 0;
        String res = "";
        int size = list_hash_nums.size();
        for(int hash_num=size-1;  hash_num>-1; hash_num--){
            if(list_hash_nums.get(hash_num)==null) {res+="."; continue;}
            /*for(int n:hash_num) System.out.print(n+" ");
            System.out.println();*/
            out: for(int number = 0; number<10; number++){

                total_error = 0;
                // boolean is_equal = true;
                for(int ind_num=0; ind_num<3; ind_num++){
                    //System.out.println(shablons_nushablons_numbers_0_9[number]mbers_0_9[number][ind_num]);
                /*System.out.println("shablon "+number);
                show_shortarr_HashShablonNumber(shablons_numbers_0_9[number]);
                System.out.println("+++++++++++++++++++");
                System.out.println("number ");
                show_shortarr_HashShablonNumber(list_hash_nums.get(hash_num));
                System.out.println("++++++++++++++++++++++++++++++");*/
                    total_error+= get_AmountOneBitInInt(shablons_numbers_0_9[number][ind_num]^list_hash_nums.get(hash_num)[ind_num]);
                    //System.out.println("total "+total_error);
                    if(total_error>limit_error){ continue out;  }
                }
                //System.err.println("TOTAL ERROR "+total_error);
                //if(!is_equal)continue;

                // если нашлось совпадение, то берется номинал карты деление на 4 для получения индекса где 13 эелементов вместо 52
                //System.out.println("num "+number);
                res+=number;
                break;
            }
        }
        //System.out.println(res);

        return Float.parseFloat(res);
    }


    private List<int[]> get_list_intarr_HashNumberImg(BufferedImage image_table, int X, int Y, int W, int H, int limit_grey){

        List<int[]> coords_line_x_for_one_num = new ArrayList<>();
        int[] start_end_num = null;
        boolean is_x_black = false; int count_black_x_line = 0, count_8_line_num = 0;
        for (int x = X+W-5; x > X+5; x--) {
            // определяется есть ли черный пиксель в текущей линии если есть то счетчик увеличивается
            is_x_black = false;
            for (int y = Y; y < Y+H; y++) { if(get_intGreyColor(image_table,x,y)>limit_grey){ is_x_black = true; break; } }
            if(is_x_black) { count_black_x_line++;  //System.out.println(x+" "+count_black_x_line);
            }
            // если линия белая, то проверяется сколько черных линий было до этого, если 3, а это точка, то все обнуляется в лист заносится нулл
            else {
                if(count_black_x_line==3){
                    coords_line_x_for_one_num.add(null);
                    count_black_x_line = 0;
                    continue;
                }
                // если счетчик черных линий равен нулю, то идет дальше если счетчик больше нуля, то идет вниз, это из-за того, что еденица меньше 8 черных линий
                // и чтобы белая линия не сбивала подсчет линий числа
                if(count_black_x_line==0) continue;
            }
            // проверяется условие есть ли начало числа
            if(count_black_x_line==1){
                start_end_num = new int[2];
                start_end_num[0] = x;
                count_8_line_num = 1;   // начинается счетчик линий числа
                continue;
            }
            count_8_line_num++;
            // есть счетчик линий дошел до 8 то обнуляются все счетчики и завершается получение кординат числа
            if(count_8_line_num==8){
                assert start_end_num != null;
                start_end_num[1] = x;
                coords_line_x_for_one_num.add(start_end_num);
                count_8_line_num = 0;
                count_black_x_line = 0;
            }
        }
        List<int[]> result = new ArrayList<>();
        for(int[] num:coords_line_x_for_one_num){
            if(num==null) { result.add(null);
                //System.out.println("DOT");
                continue;}
            int start = num[1], end = num[0];
            //System.out.println(num[0]+"  "+num[1]);
            int _32_pixels =0;
            int[] intarr_hashimage = new int[3]; int index_intarr_hashimage = -1, count_32_pix = 0;
            for (int x = start; x < end+1; x++){
                for (int y = Y; y < Y+H; y++) {
                    _32_pixels<<=1;
                    count_32_pix++;
                    if(get_intGreyColor(image_table,x,y)>limit_grey){ _32_pixels+=1;
                        // System.out.print("1");
                    }
                    /*else System.out.print("0");
                    System.out.print(" ");*/
                    if(count_32_pix==32){
                        index_intarr_hashimage++;
                        intarr_hashimage[index_intarr_hashimage] = _32_pixels;
                        _32_pixels = 0;
                        count_32_pix = 0;
                    }
                }
                //System.out.println();
            }
            result.add(intarr_hashimage);
        }
        return result;
    }

    static int get_AmountOneBitInInt(int lng){ return count_one_in_numbers[(short)(lng>>16)+32768]+count_one_in_numbers[(short)(lng)+32768]; }


    void check_start_flop(){
        //System.out.println("check_start_flop");
        // проверка что херо не делал ход, кроме когда находится на ББ, где возможен чек, если не делал, то проверки на флоп нет
        if(!(currentHand.preflop_by_positions.get(currentHand.poker_position_of_hero).get(0)>0)&&currentHand.poker_position_of_hero !=5) return;
        /*int x1 = coord_of_table[0]+coord_2_3_cards_flop[0][0];
        int x2 = coord_of_table[0]+coord_2_3_cards_flop[1][0];
        int y = coord_of_table[1]+coord_2_3_cards_flop[0][1];*/
        int x1 = coord_2_3_cards_flop[0][0];
        int x2 = coord_2_3_cards_flop[1][0];
        int y = coord_2_3_cards_flop[0][1];

        if(get_int_MaxBrightnessMiddleImg(frame[0],x1,y,17,17)>220
                &&get_int_MaxBrightnessMiddleImg(frame[0],x2,y,17,17)>220)currentHand.is_start_flop = true;
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



    int get_int_MaxBrightnessMiddleImg(BufferedImage image,int X,int Y,int W,int H){
        int max = 0, y = Y+H/2;
        for(int x=X; x<X+W; x++){
            int grey = get_intGreyColor(image,x,y);
            if(grey>max)max=grey;
        }
        return max;
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

        if(!is_noCursorInterferenceImage(frame[0],X,Y,w,h,limit_grey))return null;
        // System.out.println();
        return frame[0].getSubimage(X+cutX1,Y+cutY1,w+cutX2,h+cutY2);
    }


    BufferedImage set_grey_and_inverse_or_no(BufferedImage  source, boolean isnverse){
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        //System.out.println("tip "+source.getType());
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                int grey = get_intGreyColor(source,x,y);
                if(isnverse) grey = 255-grey;
                result.setRGB(x, y, new Color(grey, grey, grey).getRGB());
            }
        }
        return result;
    }



    static int get_intGreyColor(BufferedImage img,int x, int y){
        int val = img.getRGB(x, y);
        return  (int) (((val >> 16) & 0xff) * 0.299 + ((val >> 8) & 0xff) * 0.587 + (val & 0xff) * 0.114);
    }



    private boolean is_noCursorInterferenceImage(BufferedImage image,int X, int Y, int W, int H, int limit_grey){
        for(int x=X; x<W+X; x++) for(int y=Y; y<H+Y; y+=H-1) if(get_intGreyColor(image,x,y)>limit_grey)return false;
        for(int y=Y; y<H+Y; y++) for(int x=X; x<W+X; x+=W-1) if(get_intGreyColor(image,x,y)>limit_grey)return false;
        return true;
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
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < source.getWidth(); x++)
            for (int y = 0; y < source.getHeight(); y++) {
                int grey = get_intGreyColor(source,x,y);
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
