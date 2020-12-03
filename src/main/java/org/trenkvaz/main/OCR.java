package org.trenkvaz.main;

//import org.bytedeco.javacpp.opencv_core.IplImage;
//import org.bytedeco.opencv.opencv_core.IplImage;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/*import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.cvResetImageROI;*/
import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.main.CaptureVideo.shablons_numbers_0_9_for_stacks;
import static org.trenkvaz.main.CurrentHand.creat_HandForSaving;
import static org.trenkvaz.ui.StartAppLauncher.*;
//import static org.trenkvaz.main.Settings.write_nicks_keys_img_pix;

public class OCR implements Runnable {

    boolean is_run = true, start_hud = false, end_hud = false, show_text_in_hud = false, stop_show_text_in_hud = false;
    int table = -1;
    BufferedImage[] frame;
    int[] coord_of_table;
    Queue<BufferedImage[]> main_queue_with_frames;
    CurrentHand currentHand;
    int[] poker_positions_index_with_numbering_on_table = new int[6];
    List<List<int[]>> list_by_poker_pos_current_list_arrnums_actions = new ArrayList<>(6);
    int current_bu = -1;
    int current_position_hero = -1;
    String[] current_hero_cards = new String[]{"",""};
    long[][][] current_id_nicks_for_choose = new long[6][3][16];
    long[] zeros_for_clear_current_id = new long[16];
    int[] count_nicks = new int[6];
    CreatingHUD creatingHUD;
    static final int FLOP =1, TURN = 2, RIVER = 3;
    List<List<long[]>> list_of_lists_current_id_nicks_for_choose = new ArrayList<>(6);

    // test

    record TestRecPlayer(List<BufferedImage> imges_nick,List<BufferedImage> imges_stack){}
    Queue<BufferedImage> cadres = new LinkedList<>();
    TestRecPlayer[]  testRecPlayers = new TestRecPlayer[6];
    List<List<String>> test_nicks = new ArrayList<>(6);

    List<List<BufferedImage>> images_nicks = new ArrayList<>(6);

    //Queue<Integer> testquer = new LinkedList<>();

    public OCR(int table){
        creatingHUD = new CreatingHUD(table);
        this.coord_of_table = coord_left_up_of_tables[table];
        this.table = table+1;
        main_queue_with_frames = new LinkedBlockingQueue<>();
        for(int i=0; i<6; i++){
            list_by_poker_pos_current_list_arrnums_actions.add(new ArrayList<>());
            list_of_lists_current_id_nicks_for_choose.add(new ArrayList<>());
        }


        //test
       /* for(int i=0; i<6; i++){
            //test_nicks.add(new ArrayList<>());
            //images_nicks.add(new ArrayList<>());
            testRecPlayers[i] = new TestRecPlayer(new ArrayList<>(),new ArrayList<>());
        }*/

        new Thread(this).start();
    }

   public OCR(String test,int table,BufferedImage[] frame1){
       this.coord_of_table = coord_left_up_of_tables[table];
       this.table = table;
       frame = frame1;
   }

   public OCR(){}


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
            if(frame[0]!=null) save_image(frame[0],"test4\\"+table+"_"+c);
            }
        }
    }


    public synchronized void set_image_for_ocr(BufferedImage[] frame){
        main_queue_with_frames.offer(frame);
    }


    static synchronized void show_test_total_hand(OCR ocr){

        if(!ocr.currentHand.creat_PreflopActionsInHandForCountStats())return;
        //TEST
        //if(!ocr.currentHand.is_start_turn)return;

        Date d = new Date();
        DateFormat formatter= new SimpleDateFormat("HH.mm.ss");
        String Z = formatter.format(d);

        //if(ocr.currentHand.cards_hero[0].equals("7c")&&ocr.currentHand.cards_hero[1].equals("7h")) System.err.println("===================================================");
        if(ocr.currentHand.is_start_turn) System.out.print(RED);
        else System.out.print(RESET);
        System.out.println(Z+"  "+ocr.currentHand.time_hand+"     ****** cards "+ocr.currentHand.cards_hero[0]+ocr.currentHand.cards_hero[1]+" flop "+ocr.currentHand.is_start_flop+
                " bu "+ocr.currentHand.position_bu_on_table +" table "+ocr.table);

        String logtest = Z+"  "+ocr.currentHand.time_hand+"     ****** cards "+ocr.currentHand.cards_hero[0]+ocr.currentHand.cards_hero[1]+" flop "+ocr.currentHand.is_start_flop+
                " turn "+ocr.currentHand.is_start_turn+" bu "+ocr.currentHand.position_bu_on_table +" table "+ocr.table+" \r\n";

        boolean error = false;
        boolean is_save_test_list = false;
        System.out.print(RESET);
        for(int i=0; i<6; i++) {
            logtest += ocr.currentHand.nicks[i]+"    "+ocr.currentHand.stacks[i]+"  ";
            if(ocr.currentHand.poker_position_of_hero==i) System.out.print(BLUE+ocr.currentHand.nicks[i]+"    "+ocr.currentHand.stacks[i]+"  ");
            else System.out.print(ocr.currentHand.nicks[i]+"    "+ocr.currentHand.stacks[i]+"  ");

            if(ocr.currentHand.nicks[i]==null) { error = true;                   Settings.ErrorLog(" NO NICK  hand "+ocr.currentHand.time_hand+" t "+ocr.table+" p "+i);
            /*for(BufferedImage image:testRecPlayers[i].imges_nick)
            Testing.save_image(image,     "test5\\"+hand+"\\nick_"+i);*/
            }
            if(ocr.currentHand.cards_hero[0].equals(""))Settings.ErrorLog("NO CARDS hand "+ocr.currentHand.time_hand+" t "+ocr.table+" p "+i);

            if(ocr.currentHand.stacks[i]<=0){ Settings.ErrorLog(" NO STACK  hand "+ocr.currentHand.time_hand+" t "+ocr.table+" p "+i+" stack "+ocr.currentHand.stacks[i]+
                    " cards "+ocr.currentHand.cards_hero[0]+ocr.currentHand.cards_hero[1]);
               /* for(BufferedImage image:testRecPlayers[i].imges_stack)
                    Testing.save_image(image,     "test5\\"+hand+"\\stack_"+i);*/

            }

            for (int a=0; a<ocr.currentHand.preflop_by_positions.get(i).size(); a++) {
                logtest+=ocr.currentHand.preflop_by_positions.get(i).get(a)+"  ";
                if(ocr.currentHand.poker_position_of_hero==i) System.out.print(BLUE+ocr.currentHand.preflop_by_positions.get(i).get(a)+" ");
                else System.out.print(ocr.currentHand.preflop_by_positions.get(i).get(a)+"  ");
            }
            logtest+="    _______________         ";

            if(ocr.currentHand.poker_position_of_hero==i)System.out.print(BLUE+"    _______________         ");
            else System.out.print("    _______________         ");

            for(int a=1; a<ocr.currentHand.preflop_actions_for_stats.get(i).size(); a++) {
                logtest+=ocr.currentHand.preflop_actions_for_stats.get(i).get(a)+" ";
                if(ocr.currentHand.poker_position_of_hero==i) System.out.print(BLUE+ocr.currentHand.preflop_actions_for_stats.get(i).get(a)+" ");
                else System.out.print(ocr.currentHand.preflop_actions_for_stats.get(i).get(a)+" ");
            }

            /*if(ocr.currentHand.poker_position_of_hero==i)System.out.println(BLUE);
            else */
            if(ocr.currentHand.is_start_flop&&ocr.currentHand.arr_continue_players_flop[i]==1) System.out.print(GREEN+"    "+ocr.currentHand.stacks_flop[i]);

            if(ocr.currentHand.is_start_turn&&ocr.currentHand.arr_continue_players_turn[i]==1) System.out.print(GREEN+"    "+ocr.currentHand.stacks_turn[i]);

                System.out.println(RESET);
            logtest+="\r\n";
        }
        if(ocr.currentHand.is_allin) {System.out.println(RED+"ALLIN PREFLOP ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
            Settings.ErrorLog("ALLIN PREFLOP "+ocr.currentHand.time_hand+" t "+ocr.table+" p ");
            logtest+="ALLIN PREFLOP |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\r\n";
        }
        System.out.println(RESET+"******************************************");

        logtest+="****************************************** \r\n";
        Testing.write_LogTest(logtest);



        //if(!error)creat_HandForSaving(ocr.currentHand);



        /*int f =0;
        if(error)for(BufferedImage img:cadres)Testing.save_image(img,"test5\\"+hand+"\\frame_"+(f++));*/

        /*for(int i=0; i<6; i++){
            System.out.print("pos "+i+" ");
            if(ocr.currentHand.nicks[i]==null)continue;
            System.out.print(ocr.currentHand.nicks[i]+"   ");
            for(int a=1; a<ocr.currentHand.preflop_actions_for_stats.get(i).size(); a++)
                System.out.print(ocr.currentHand.preflop_actions_for_stats.get(i).get(a)+" ");
            System.out.println();
        }*/

    }

    boolean startlog = false;
    int count_cadres = 0;
    boolean TEST = true;

    private void main_work_on_table(){
        //if(table!=1)return;
        if(!startlog){
            startlog=true;
            Settings.ErrorLog("START");
        }

        int check_start_or_end_hand = get_number_hand();
        //System.out.println(check_start_or_end_hand);
        if(check_start_or_end_hand==0){
            // обработка стоп сигнала для завершения последней раздачи
            if(count_stop_signal==200&&currentHand!=null) {
                currentHand.set_NicksByPositions();
                if(let_SaveTempHandsAndCountStatsCurrentGame)creat_HandForSaving(this.currentHand);
                  show_test_total_hand(this);
                 currentHand = null;
            }
            return;
        }

        if(check_start_or_end_hand==1) {
            if(currentHand!=null){
                /*if(currentHand.cards_hero[0].equals("3h")&&currentHand.cards_hero[1].equals("2h")&&table==3){
                    System.out.println("TOTAL ---------------------------------------  "+currentHand.nicks[4]);

                }*/

                currentHand.set_NicksByPositions();
                if(let_SaveTempHandsAndCountStatsCurrentGame)creat_HandForSaving(this.currentHand);

               //if(currentHand.is_start_flop)
                   show_test_total_hand(this);

            }
            //list_test_numberhands.clear();
            //list_test_cards.clear();

            currentHand = new CurrentHand(table-1,creatingHUD);
            for(int i=0; i<6; i++){list_by_poker_pos_current_list_arrnums_actions.get(i).clear();
                list_of_lists_current_id_nicks_for_choose.get(i).clear();
            }

            currentHand.position_bu_on_table = current_bu;
            currentHand.nicks[0] = nick_hero;
            currentHand.poker_position_of_hero = current_position_hero;
            currentHand.cards_hero[0] = current_hero_cards[0];
            currentHand.cards_hero[1] = current_hero_cards[1];
            currentHand.poker_positions_by_pos_table_for_nicks = poker_positions_index_with_numbering_on_table.clone();
            count_nicks = new int[6];

            for(int i=1; i<6; i++)
                for(int n=0; n<3; n++)
                    System.arraycopy(zeros_for_clear_current_id,0,current_id_nicks_for_choose[i][n],0,16);
                    //for(int h=0; h<16; h++)current_id_nicks_for_choose[i][n][h] = 0;


            // TEST

       /*     for(int i=0; i<6; i++){
               *//* test_nicks.get(i).clear();
                images_nicks.get(i).clear();*//*
                testRecPlayers[i].imges_nick.clear();
                testRecPlayers[i].imges_stack.clear();
            }*/

            //cadres.clear();
            //save_image(frame[0],"test5\\"+(c++));
            //count_cadres = 0;
        }
        //save_image(frame[0],"test5\\_"+(++c));
        /* count_cadres++;
         if(count_cadres<4) cadres.add(frame[0]);
         else {
             cadres.poll();
             cadres.add(frame[0]);
         }*/

        /*if(currentHand.cards_hero[0].equals("Qc")&&currentHand.cards_hero[1].equals("2h")
        )Testing.save_image(frame[0],"test2\\Qc2h\\_table_"+(++c));*/


        //if(currentHand.position_bu_on_table >0&&!(currentHand.cards_hero[0].equals("")||currentHand.cards_hero[1].equals(""))&&!currentHand.is_nicks_filled) {get_nicks();}



        /*if(currentHand.position_bu_on_table >0&&!(currentHand.cards_hero[0].equals("")||currentHand.cards_hero[1].equals(""))
                &&currentHand.is_nicks_filled) {


        }*/
        if(!currentHand.is_nicks_filled)get_nicks();
        //if(currentHand.is_nicks_filled){
        //if(currentHand.cards_hero[0].equals("7c")&&currentHand.cards_hero[1].equals("7h"))
        if(!currentHand.is_start_flop)check_StartPostFlop(FLOP);
        if(!currentHand.is_start_flop)get_start_stacks_and_preflop();

        if(currentHand.is_start_flop&&!currentHand.is_preflop_end){
            set_arrs_PositionsWithContinueAndAllinerPlayers(currentHand.arr_continue_players_flop,currentHand.arr_alliner_players_flop,FLOP);
            currentHand.is_preflop_end = true;
            currentHand.is_allin = currentHand.check_All_in(FLOP);
        }



        if(!currentHand.is_allin){
            if(currentHand.is_start_flop&&!currentHand.is_start_turn)check_StartPostFlop(TURN);

            if(currentHand.is_start_turn&&!currentHand.is_flop_end){
                set_arrs_PositionsWithContinueAndAllinerPlayers(currentHand.arr_continue_players_turn,currentHand.arr_alliner_players_turn,TURN);
                currentHand.is_flop_end = true;
                currentHand.is_allin = currentHand.check_All_in(TURN);
            }


        }


        //}


       /* if(currentHand.cards_hero[0].equals("3h")&&currentHand.cards_hero[1].equals("2h")&&table==3)
            System.out.println("MAIN ---------------------------------------  "+currentHand.nicks[4]);*/
        //System.out.println("bu "+currentHand.position_of_bu+" cards "+currentHand.cards_hero+" allnicks "+currentHand.is_nicks_filled);

    }


    void get_nicks(){

        int[] correction_for_place_of_nicks = {1,2,2,2,1,1};
        int w = 86, h = 14;

        for(int i=1; i<6; i++){
            //test_is_ocr = false;
            if(currentHand.nicks[i]!=null)continue;
            int x = coords_places_of_nicks[i][0]+correction_for_place_of_nicks[i]-5;
            int y = coords_places_of_nicks[i][1]+1;
            /*boolean isplace =false;
            if(i==4&&currentHand.cards_hero[0].equals("3h")&&currentHand.cards_hero[1].equals("2h")&&table==3)
            {save_image(frame[0].getSubimage(x,y,w,h),"test4\\"+(c++)); isplace=true;}*/
            long[] img_pix = get_longarr_HashImage(frame[0],x,y+2,w,h-3,15,150);

            if(get_int_CompareLongHashesToShablons(img_pix,shablon_text_poker_terms)!=-1)continue;
            //BufferedImage test_nick = frame[0].getSubimage(x,y,w,h);;

            if(count_nicks[i]<3){
                // набор трех id изображений ников и добавление еще одного если ников два
                list_of_lists_current_id_nicks_for_choose.get(i).add(img_pix);
                count_nicks[i]++;
                if(count_nicks[i]<3)continue;
            }
            if(count_nicks[i]==3){
                boolean same_nicks = true;
                // когда набрано три изображения ника то сравниваются все между собой если равны то идет определение ника если нет удаляется первый ид и цикл продолжается
                for(int c1=1; c1<3; c1++){
                    if(compare_LongHashes(list_of_lists_current_id_nicks_for_choose.get(i).get(0),list_of_lists_current_id_nicks_for_choose.get(i).get(c1),10))continue;
                    same_nicks = false; break;
                }
                if(same_nicks){ System.arraycopy(list_of_lists_current_id_nicks_for_choose.get(i).get(0), 0, img_pix , 0, 16);
                } else {
                    list_of_lists_current_id_nicks_for_choose.get(i).remove(0);
                    count_nicks[i]--;
                    continue;
                }
            }


            long id_img_pix = get_number_img_nicks(img_pix,10);
            //System.out.println("time id "+(System.currentTimeMillis()-s));
            //System.out.println("id "+i+"    "+id_img_pix[0]);
            //int id_img_pix_length = id_img_pix.length;
            c++;
            // ид ника найдено в базе изображений ников и поэтому текст ника берется в мапе по ид
            if(id_img_pix>0){
                    while (true){ // могло прийти одновременно несколько одинаковых ника первый идет на распознание, другие сюда и получают распознаный ник это может занять время поэтому цикл
                        currentHand.nicks[i] = set_get_nicks_in_hashmap(id_img_pix,null);
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

    // если нет похожих изображений в базе массивов изображений
            // и надо распознать, то возвращает два числа, первое ИД, второе ключ для сортированного массива, чтобы его можно было записать в файл
            if(id_img_pix<0){
                int attempt = 0;
                BufferedImage cheked_img = frame[0].getSubimage(x,y,w,h);
                    while (true){
                        attempt++;
                        currentHand.nicks[i] = ocr_image(get_white_black_image(set_grey_and_inverse_or_no(get_scale_image(cheked_img,4),true),105)).trim();
                        //System.out.println("osr  "+currentHand.nicks[i]);
                        //if(isplace) System.out.println("2 setnick+++++++++++++++++++++++++++++  "+currentHand.nicks[i]);

                        if(currentHand.nicks[i]!=null)break;
                        // проверка на невозможность распознования, дается несколько попыток, если все равно приходит нулл, то присваивается ник в виде текущего ИД
                        if(attempt>2){ currentHand.nicks[i]=Long.toString(-id_img_pix); break; }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    set_get_nicks_in_hashmap(-id_img_pix,currentHand.nicks[i]);

                    CaptureVideo.Settings.write_nicks_keys_img_pix(currentHand.nicks[i],img_pix);
                //System.out.println("id "+-id_img_pix[0]+" id in arr "+img_pix[16]);
                   // save_image(get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),limit_grey),"id_nicks\\"+currentHand.nicks[i]+" "+(-id_img_pix[0]));
                save_image(get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),105),"id_nicks\\"+currentHand.nicks[i]+" "+(-id_img_pix)+"_"+table);

                //test_is_ocr = true;
                //test_nick= cheked_img;
            }


            //save_image(get_white_black_image(set_grey_and_inverse_or_no(frame[0].getSubimage(x-3,y,w+5,h),true),105),"test4\\"+currentHand.nicks[i]+"_"+i+"_"+c+"_"+table);
         if(count_nicks[i]==3&&currentHand.nicks[i]==null){
             Settings.ErrorLog(" i "+i+" NOT CHOOSED "+table);
             //count_nicks[i]=0;
         }
        }
        currentHand.setIs_nicks_filled();
        /*if(currentHand.cards_hero[0].equals("Qc")&&currentHand.cards_hero[1].equals("2h")&&table==3){
            for(String n:currentHand.nicks) { if(n==null){
                System.out.println("null"); continue;
            }
                System.out.println("*"+n);}
        save_image(frame[0],"test\\"+(c++));
        }*/
        //if(currentHand.is_nicks_filled) set_nick_by_positions_and_position_of_hero();

        //if(currentHand.cards_hero[0].equals("Qc")&&currentHand.cards_hero[1].equals("2h")) System.out.println("NICKS");;
    }



    private String[] set_cards_hero(){
        //System.out.println("set_cards_hero");
        //BufferedImage[] cards = new BufferedImage[2];
        String[] result = new String[]{"",""};
        c++;
        for(int i=0; i<2; i++){
            //if(result[i].length()!=0)continue;
            int X = coords_cards_hero[i][0];
            int Y = coords_cards_hero[i][1];
         //save_image(frame[0].getSubimage(X+1,Y,15,17),"test\\"+(c++)+"_"+i);


        /*    if(currentHand!=null)
                if(currentHand.cards_hero[0].equals("Ks")&&currentHand.cards_hero[1].equals("5c")) */
                    //System.out.println("start "+(c)+"_"+i);

            if(get_int_MaxBrightnessMiddleImg(frame[0],X+1,Y,15,17)<220||get_int_MaxBrightnessMiddleImg(frame[0],X+1,Y+17,15,17)<220)break;

          /*  if(currentHand!=null)
                if(currentHand.cards_hero[0].equals("Ks")&&currentHand.cards_hero[1].equals("5c")) */
                    //System.out.println("after midle "+(c)+"_"+i);
            // проверка периметра карта на помеху курсором
           if(!is_noCursorInterferenceImage(frame[0],X+1,Y,15,17,240))break;

          /*  if(currentHand!=null)
            if(currentHand.cards_hero[0].equals("Ks")&&currentHand.cards_hero[1].equals("5c"))*/
                //System.out.println("after cursor "+(c)+"_"+i);
           //test_cards[i] = frame[0].getSubimage(X+1,Y,w,h);

           long[] card_hash_from_table = get_longarr_HashImage(frame[0],X+1,Y+1,14,14,3,150);

         /*   if(currentHand!=null)
                if(currentHand.cards_hero[0].equals("Ks")&&i==1)
           Testing.get_card(card_hash_from_table);*/

            //System.out.println("*********************************************************");

            //show_img_from_arr_long(card_hash_from_table,14,14);
            int first_of_pair_error = 0, second_of_pair_error = 0, limit_error = 15, total_error = 0,
            number_with_min_error = -1, min_error = 15;
         out: for(int nominal_ind_list = 0; nominal_ind_list<52; nominal_ind_list++){
                // сравнение количества черных пикселей между хешем_имдж из массива номиналы_карт с хешем_имдж со стола
                //System.out.println("i "+i+" nom "+nominals_cards[nominal_ind_list/4]+"  err "+abs(_long_arr_cards_for_compare[nominal_ind_list][3]-card_hash_from_table[3]));
                if(abs(_long_arr_cards_for_compare[nominal_ind_list][3]-card_hash_from_table[3])>=limit_error)continue;

                total_error = 0;

                for(int ind_num=0; ind_num<3; ind_num++){
                    total_error+= get_AmountOneBitInLong(_long_arr_cards_for_compare[nominal_ind_list][ind_num]^card_hash_from_table[ind_num]);
                   /* if(currentHand!=null)
                        if(currentHand.cards_hero[0].equals("Ks")&&i==1)
                            System.out.println(nominal_ind_list+"  "+total_error);*/
                    if(total_error>=limit_error){ continue out;  }
                }
             if(total_error<min_error){
                 min_error = total_error;
                 number_with_min_error = nominal_ind_list;
             }
                //System.err.println("TOTAL ERROR "+total_error);
                // если нашлось совпадение, то берется номинал карты деление на 4 для получения индекса где 13 эелементов вместо 52
             //result[i]=nominals_cards[nominal_ind_list/4];

                //break;
            }

            if(number_with_min_error==-1)return null;
            result[i]=nominals_cards[number_with_min_error/4];

            if(result[i].length()<2)result[i]+=get_suit_of_card(frame[0],X+14,Y+16);

            /*  if(currentHand!=null)
                 if(currentHand.cards_hero[0].equals("Ks")&&i==1) System.out.println("reult "+result[i]);*/


            /*if(currentHand!=null)
            if(currentHand.cards_hero[0].equals("Ks")&&currentHand.cards_hero[1].equals("5c")
            )Testing.save_image(frame[0].getSubimage(X+1,Y,15,17),"test2\\Ks5c_hand\\_result_"+(c)+"_"+result[i]+"_"+i);*/

                    //Testing.save_image(frame[0].getSubimage(X+1,Y,15,17),"test2\\Ks5c_hand\\_result_"+(c)+"_"+i);;
        }

        //System.out.println("result "+(c)+"_"+result[0]+"_"+result[1]);

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
            return result;
    }


    private void set_PokerPositionsIndexWithNumberingOnTable(int current_bu){

        // алгоритм определения соответсвия покерных позиций позициям за столом которые начинаются с херо, на основе того где на столе находится БУ
        // также определяется позиция героя по его известной позиции на столе
        //System.out.println("BU "+current_bu);
        int utg = current_bu +3;
        if(utg>6) utg = utg-6;
        int positons_on_table = 0; boolean start = false; int i =-1;
        while (i!=5){
            positons_on_table++;
            if(positons_on_table==utg)start = true;
            if(start){
                // массив покерные позиции индексы 0-5 это утг-бб, элементы это номера 1-6 позиции на столе начиная с херо
                i++;
                poker_positions_index_with_numbering_on_table[i] = positons_on_table;
                if(positons_on_table==1)current_position_hero = i;
            }
            if(positons_on_table==6)positons_on_table=0;
        }
    }

    int c =0;
    int P = 0;
    private int count_stop_signal = 0;

    int get_number_hand(){

        if(frame[0]!=null&&frame[1]==null){
            //System.out.println(RED+"IS TABLE NO NUMBER |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
            // если стол есть, но нет номера раздачи, то хад очищается от текста, но такая ситуация еще говорит о том, что раздача завершена и это сигнал стопа
            if(!stop_show_text_in_hud){
                // но такая ситуация еще говорит о том, что раздача завершена и это сигнал стопа, начало счетчика
                count_stop_signal = 0;stop_show_text_in_hud = true;show_text_in_hud = false; hud.clear_hud(table-1);
            }
            count_stop_signal++;
            return 0;
        }
        // если нет номера раздачи, то есть нет вообще стола хад выключается
        if(frame[1]==null){
            //System.out.println("NO NUMBER");
            if(!end_hud){ end_hud = true;start_hud = false;hud.stop_show_hud(table-1); }
            return 0;
        }
        // если есть номер раздачи то хад включается
        else {
            //System.out.println(GREEN+" IS NUMBER |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
            if(!start_hud){ end_hud = false;start_hud = true;hud.show_hud(table-1); }
            // если номер есть, но нет хода раздачи, то хад очищается от текста
            if(frame[0]==null) {
                if(!stop_show_text_in_hud){ stop_show_text_in_hud = true;show_text_in_hud = false;hud.clear_hud(table-1); }
                return 0;
             // если стол есть и раздача идет то текст в хаде появляется
            } else {
                if(!show_text_in_hud){ stop_show_text_in_hud = false;show_text_in_hud = true;hud.refresh_hud(table-1); }
            }
        }

       String[] hero_cards = set_cards_hero();
       // карты могут пропадать в конце текущей раздачи, отсутствие карт в новой раздаче пока не обнаружено
        // ОБНАРУЖЕО !!!

       if(hero_cards==null) return 0;
        //if(hero_cards[0].equals("7c")&&hero_cards[1].equals("7h"))save_image(frame[0],"test2\\"+(c++));
       int bu = set_current_position_of_bu();
       /*if(!(hero_cards[0].equals(current_hero_cards[0])&&hero_cards[1].equals(current_hero_cards[1]))&&bu==-1){
           System.err.println("ERROR");
           save_image(frame[0],"test\\"+(c++));
        }*/

        // БУ может отсутствовать и в начале новой и в конце старой раздачи,новая это раздача или старая проверяется по картам
        // важно наличие БУ в начале новой раздачи, если раздача новая без БУ то кадр пропускается
        // если БУ нет в конце раздачи, то такой кадр обрабатывается

       if(bu==-1){
           if(currentHand==null)return 0;
           if(hero_cards[0].equals(current_hero_cards[0])&&hero_cards[1].equals(current_hero_cards[1]))return -1;
                 else return 0;}
       // если БУ определилась, то проверяет совпадение новых карт со старыми если да, то считается текущая раздача, если карты разные, то считается началом новой раздачи
       if(hero_cards[0].equals(current_hero_cards[0])&&hero_cards[1].equals(current_hero_cards[1])&&bu==current_bu){
           return -1;
       } else {
           current_hero_cards[0] = hero_cards[0];current_hero_cards[1] = hero_cards[1];
           if(bu!=current_bu){ current_bu = bu; set_PokerPositionsIndexWithNumberingOnTable(bu); }
           hud.clear_hud(table-1);
           return 1;
       }
    }

    int C = 0;
    int pos = -1, gxa = -1, gya = -1, gwa = -1, gha = -1;
    void get_start_stacks_and_preflop(){
       C++;
        //System.out.println("get stacks");

        int[] correction_for_place_of_nicks = {1,2,2,2,1,1};

        for(int poker_position=0; poker_position<6; poker_position++){
            // проверка последнего действия на префлопе на фолд берется последний индекс
            if(!currentHand.preflop_by_positions.get(poker_position).isEmpty())
            if(currentHand.preflop_by_positions.get(poker_position).get(currentHand.preflop_by_positions.get(poker_position).size()-1)==-10)continue;

            //if(currentHand.cards_hero[0].equals("7c")&&currentHand.cards_hero[1].equals("7h")&&poker_position==5)save_image(frame[0],"test\\"+(c++)+"_"+poker_position+"_t_"+table);

           // если фолд то добавляется фолд
           if(is_Fold(poker_position)){
               /*if(currentHand.preflop_by_positions.get(poker_position).size()==1&&!(currentHand.preflop_by_positions.get(poker_position).get(0)>0))
                currentHand.preflop_by_positions.get(poker_position).set(0,1_000_000f);
            else */
                currentHand.preflop_by_positions.get(poker_position).add(-10f);
            }

            int xa = coords_actions[poker_positions_index_with_numbering_on_table[poker_position]-1][0];
            int ya = coords_actions[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+2;
            int wa = 70;
            int ha = 11;

            //save_image(frame[0].getSubimage(xa,ya,wa,ha),"test3\\_"+(poker_positions_index_with_numbering_on_table[poker_position])+"_"+C);
            // фильтр на пустое место без рейза
            //if(currentHand.cards_hero[0].equals("8d")&&poker_position==0)save_image(frame[0].getSubimage(xa,ya,wa,ha),"test2\\_act1_"+(c++));
            if(!(get_int_MaxBrightnessMiddleImg(frame[0],xa,ya,wa,ha)>200)){
                //save_image(frame[0].getSubimage(xa,ya,wa,ha),"test\\_"+(poker_positions_index_with_numbering_on_table[poker_position])+"_"+C);
                continue;}
            //if(currentHand.cards_hero[0].equals("8d")&&poker_position==0)save_image(frame[0].getSubimage(xa,ya,wa,ha),"test2\\_act2_"+(c++));
            // если есть первый рейз, но его нельзя прочитать из-за помехи, продолжение цикла, если стек = нулю то в стек ставится -1, из-за этого не будет определятся стек
            if(!is_noCursorInterferenceImage(frame[0],xa,ya,wa,ha,240)){
                //save_image(frame[0].getSubimage(xa,ya,wa,ha),"test2\\_"+(poker_positions_index_with_numbering_on_table[poker_position])+"_"+C);

                if(currentHand.stacks[poker_position]==0f)currentHand.stacks[poker_position]=-1f;continue;
            }


            //if(currentHand.cards_hero[0].equals("8d")&&poker_position==0)save_image(frame[0].getSubimage(xa,ya,wa,ha),"test2\\_act3_"+(c++));

            List<int[]> nums = get_list_intarr_HashNumberImg(frame[0],xa,ya+1,70,9,205,0,2,6,2);

            if(nums==null) {  if(currentHand.stacks[poker_position]==0)currentHand.stacks[poker_position]=-1f;

            Settings.ErrorLog(" hand "+currentHand.time_hand+" ERROR get_list_intarr_HashNumberImg ");
            save_image(frame[0].getSubimage(xa,ya,wa,ha),"test3\\"+(poker_positions_index_with_numbering_on_table[poker_position]-1)+"_"+table);

            continue;}

            // если рейз можно прочитать, а в стеке есть -1, то оно меняется на ноль, чтобы стек определялся
            if(currentHand.stacks[poker_position]==-1)currentHand.stacks[poker_position]=0f;

            // проверяется есть ли в листе сохраняющем предидущие числа действий по позициям сохраненное число, если нет то вносит новое число и идет дальше для распознавания
            if(list_by_poker_pos_current_list_arrnums_actions.get(poker_position).isEmpty()) list_by_poker_pos_current_list_arrnums_actions.set(poker_position,nums);
            else {
                // если в листе есть число то сравнивает его с текущим числом если они одинаковые, то значит не нужно распознавать в цикл продолжается
                //TEST
                //pos = poker_position;
                //gwa =wa; gha = ha; gxa = xa; gya = ya;
                if(compare_CurrentListNumsAndNewListNums(list_by_poker_pos_current_list_arrnums_actions.get(poker_position),nums,10))continue;
                // если же числа разные, то старое число меняется на новое и распознается
                list_by_poker_pos_current_list_arrnums_actions.set(poker_position,nums);
            }

            // test
            //pos = poker_position;
            float actions = get_OcrNum(nums,10,"actions");

           /* if(currentHand.cards_hero[0].equals("Kd")&&currentHand.cards_hero[1].equals("Qh")
            )Testing.save_image(frame[0].getSubimage(xa,ya,wa,ha),"test2\\KdQh\\_table_"+(++c)+"_"+actions);*/


             // если не смолго определится, и стек также еще не определен, то стек также не определяется до следующего раза поэтому -1
            if(actions==-1) {if(currentHand.stacks[poker_position]==0)currentHand.stacks[poker_position]=-1f; continue;}

            // если список действий пустой то добавляется новое определенное действие ЭТО в версии когда блайнды пусты
            /*if(currentHand.preflop_by_positions.get(poker_position).isEmpty()) currentHand.preflop_by_positions.get(poker_position).add(actions);
            else {*/


                // если не пустой проверяется если последнее действие равно новое, то продолжение цикла, если не равно то вносится новое определенное действие
                if(currentHand.preflop_by_positions.get(poker_position).get(currentHand.preflop_by_positions.get(poker_position).size()-1)==actions)continue;
                else currentHand.preflop_by_positions.get(poker_position).add(actions);
           // }

            /*System.out.println(C+" "+(poker_positions_index_with_numbering_on_table[poker_position])+" act "+currentHand.preflop_by_positions.get(poker_position).get(0)
                    +" stack "+currentHand.stacks[poker_position]);*/
            //System.out.println(" "+actions);
            /*String blind = "";
            if(poker_position==4||poker_position==5)blind="bl";*/

            //save_image(frame[0].getSubimage(xa,ya,wa,ha),"test3\\_"+(poker_positions_index_with_numbering_on_table[poker_position])+"_"+actions+"_"+(c++));

        }


        if(!currentHand.is_stacks_filled){
            int count_filled_stacks = 0;
            for(int poker_position =0; poker_position<6; poker_position++){
                // проверка на -1, чтобы избежать получения стека, так как не ясно какой был рейз
                if(currentHand.stacks[poker_position]==-1)continue;
                if(currentHand.stacks[poker_position]!=0){ count_filled_stacks++; continue;}

                //if(currentHand.preflop_by_positions.get(poker_position).get(0)==-1)continue;


                int x = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][0]
                        +3+correction_for_place_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1];
                int y = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+17;

                //testRecPlayers[poker_positions_index_with_numbering_on_table[poker_position]-1].imges_stack.add(frame[0].getSubimage(x,y,72,14));

                if(!is_GoodImageForOcrStack(frame[0],x,y,72,14,150))continue;


                float stack_without_action = get_OcrNum(get_list_intarr_HashNumberImg(frame[0],x,y+1,72,12,175,
                        5,3,8,3),10,"stacks");

                /*if(currentHand.cards_hero[0].equals("3d")&&poker_position==5){save_image(frame[0]
                                //.getSubimage(x,y+1,72,12)
                        ,"test2\\"+(c++)+"1_"+stack_without_action+"_"+currentHand.preflop_by_positions.get(5).get(0));   }*/

                // если стек не определен проверяется на ситтаут и оллин, или вообще не определится из-за помех
                if(stack_without_action==-1){
                    long[] hash_for_compare = get_longarr_HashImage(frame[0],x,y+1,72,12,14,175);
                    /*int first_of_pair_error = 0, second_of_pair_error = 0, error = 10;
                    int choosed_shablon_text = -1;
                   out: for(int ind_shablon=0; ind_shablon<2; ind_shablon++) {
                    for(int i=0; i<14; i++){
                        if(i%2==0)first_of_pair_error = get_AmountOneBitInLong(shablons_text_sittingout_allin[ind_shablon][i]^hash_for_compare[i]);
                        if(i%2!=0)second_of_pair_error = get_AmountOneBitInLong(shablons_text_sittingout_allin[ind_shablon][i]^hash_for_compare[i]);
                        if(i>0&&(first_of_pair_error+second_of_pair_error)>error){ continue out;  }
                    }
                    choosed_shablon_text = ind_shablon;
                    }*/
                    int choosed_shablon_text = get_int_CompareLongHashesToShablons(hash_for_compare,shablons_text_sittingout_allin);
                    if(choosed_shablon_text==-1)continue;
                    if(choosed_shablon_text==0)currentHand.stacks[poker_position] = Float.NaN;
                    if(choosed_shablon_text==1)currentHand.stacks[poker_position] = currentHand.preflop_by_positions.get(poker_position).
                            get(currentHand.preflop_by_positions.get(poker_position).size()-1);
                    count_filled_stacks++;
                    continue;
                }

                // если первое действие фолд или пустое
                if(currentHand.preflop_by_positions.get(poker_position).get(0)==-10)
                {currentHand.stacks[poker_position] = stack_without_action;
                count_filled_stacks++;
                continue;}    // fold = -10
                //System.out.println("p "+poker_position+" "+currentHand.preflop_by_positions.get(poker_position).get(currentHand.preflop_by_positions.get(poker_position).size()-1));
                // если есть действия и возможно фолд, фолд пропускается берется последний рейз и прибавляется к стеку
                for(int actions = currentHand.preflop_by_positions.get(poker_position).size()-1; actions>-1; actions-- ){
                    if(currentHand.preflop_by_positions.get(poker_position).get(actions)==-10)continue;
                    currentHand.stacks[poker_position] = stack_without_action+currentHand.preflop_by_positions.get(poker_position).get(actions);
                    count_filled_stacks++;
                    break;
                }
            }
            if(count_filled_stacks==6)currentHand.is_stacks_filled = true;
        }

        //test_list_imgStacks.add(imgStacks);
        //System.out.println("************************************");

    }


    private void set_arrs_PositionsWithContinueAndAllinerPlayers(int[] arrContinuePlayers,int[] arrAllinPlayers,int street){
        float max_raise =0, raise =0;
        for(int poker_position=0; poker_position<6; poker_position++){
            // определение продолжающих играть на основе Нет Фолда для любой улицы, так как поступает свой массив ПродолжающихИграть
            if(is_Fold(poker_position))continue;
            arrContinuePlayers[poker_position]=1;
            // нужно получить максимальный рейз на улице, чтобы по нему определить потом оллинеров
            if(street==FLOP)raise = currentHand.preflop_by_positions.get(poker_position).get(currentHand.preflop_by_positions.get(poker_position).size()-1);
            if(street==TURN){ // на улице может быть чек чек значит нет действий
                if(currentHand.flop_by_positions.get(poker_position).isEmpty())continue;
                raise = currentHand.flop_by_positions.get(poker_position).get(currentHand.flop_by_positions.get(poker_position).size()-1);
            }
            if(raise>max_raise)max_raise=raise;

        }

        for(int poker_position=0; poker_position<6; poker_position++){
            if(arrContinuePlayers[poker_position]==0)continue;

            if(street==FLOP){
                //  определение остатка стека у продолжающих играть для следующей улицы после действий на предидущей на основе максимальноге рейза
                //  если стек больше такого рейза то расчитывается разница рейза и стека как остаток для последеующих действий
                if(currentHand.stacks[poker_position]>max_raise)currentHand.stacks_flop[poker_position] = currentHand.stacks[poker_position]-max_raise;
                // если стек меньше или равен макс рейзу то это значит, что игрок в оллине
                if(currentHand.stacks_flop[poker_position]==0)arrAllinPlayers[poker_position] =1;
                continue;
            }
            if(street==TURN){
                //  определение остатка стека у продолжающих играть для следующей улицы после действий на предидущей на основе максимальноге рейза
                //  если стек больше такого рейза то расчитывается разница рейза и стека как остаток для последеующих действий
                if(currentHand.stacks_flop[poker_position]>max_raise)currentHand.stacks_turn[poker_position] = currentHand.stacks_flop[poker_position]-max_raise;
                // если стек меньше или равен макс рейзу то это значит, что игрок в оллине
                if(currentHand.stacks_turn[poker_position]==0)arrAllinPlayers[poker_position] =1;
                continue;
            }

        }

    }


    int get_int_CompareLongHashesToShablons(long[] hash_for_compare,long[][] shablons){
        int first_of_pair_error = 0, second_of_pair_error = 0, error = 10, size_shbalons = shablons.length, amount_nums = shablons[0].length;
        int choosed_shablon_text = -1;
        out: for(int ind_shablon=0; ind_shablon<size_shbalons; ind_shablon++) {
            for(int i=0; i<amount_nums; i++){
                if(i%2==0)first_of_pair_error = get_AmountOneBitInLong(shablons[ind_shablon][i]^hash_for_compare[i]);
                if(i%2!=0)second_of_pair_error = get_AmountOneBitInLong(shablons[ind_shablon][i]^hash_for_compare[i]);
                if(i>0&&(first_of_pair_error+second_of_pair_error)>error){ continue out;  }
            }
            choosed_shablon_text = ind_shablon;
        }
        return choosed_shablon_text;
    }



    private boolean is_Fold(int poker_position){
        if(poker_position==currentHand.poker_position_of_hero)return false;
        int[] correction_for_place_of_imgfold = {-31,97,97,97,-31,-31};
        int x = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][0]
                +correction_for_place_of_imgfold[poker_positions_index_with_numbering_on_table[poker_position]-1];
        int y = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+8;
        int j =y-1, max = 70;
        for(int i=x; i<x+15; i++){ j++;
            if(get_intGreyColor(frame[0],i,j)>max)return false;
        }
        //if(currentHand.cards_hero[0].equals("Kd")&&currentHand.cards_hero[1].equals("7c"))save_image(frame[0],"test\\"+poker_position+"_"+(max));
        return true;
    }


    public float get_OcrNum(List<int[]> list_hash_nums,int max_error,String type_shablon){
        if(list_hash_nums==null||list_hash_nums.isEmpty()||list_hash_nums.get(0)==null)return -1;
        int[][] shablons = shablons_numbers_0_9_for_stacks;
        if(type_shablon.equals("actions")) shablons = shablons_numbers_0_9_for_actions;
        int total_error = 0, number_with_min_error = -1, min_error = max_error;
        String res = "";
        int size = list_hash_nums.size(), size_of_num = list_hash_nums.get(0).length;

        // числа берутся справа налево
        for(int hash_num=size-1;  hash_num>-1; hash_num--){
            // точка
            if(list_hash_nums.get(hash_num)==null) { res+="."; continue;}
            number_with_min_error = -1;
            min_error = max_error;
            /*for(int n:hash_num) System.out.print(n+" ");
            System.out.println();*/
            out: for(int number = 0; number<10; number++){

                total_error = 0;
                // boolean is_equal = true;
                for(int ind_num=0; ind_num<size_of_num; ind_num++){
                    //System.out.println(shablons_nushablons_numbers_0_9[number]mbers_0_9[number][ind_num]);
                /*System.out.println("shablon "+number);
                show_shortarr_HashShablonNumber(shablons_numbers_0_9[number]);
                System.out.println("+++++++++++++++++++");
                System.out.println("number ");
                show_shortarr_HashShablonNumber(list_hash_nums.get(hash_num));
                System.out.println("++++++++++++++++++++++++++++++");*/
                    total_error+= get_AmountOneBitInInt(shablons[number][ind_num]^list_hash_nums.get(hash_num)[ind_num]);

                    /*if(currentHand.cards_hero[0].equals("Kd")&&currentHand.cards_hero[1].equals("Qh")&&type_shablon.equals("actions")&&pos==3)
                        System.err.println("TOTAL ERROR "+total_error+" number "+number+" minerr "+min_error);*/

                    //System.out.println("total "+total_error);
                    if(total_error>=max_error){ continue out;  }
                }

                // находится индекс в шаблоне числе с минимальным количеством ошибок
                if(total_error<min_error){
                    min_error = total_error;
                    number_with_min_error = number;
                }


            }
            /*if(currentHand.cards_hero[0].equals("Kd")&&currentHand.cards_hero[1].equals("Qh")&&type_shablon.equals("actions")&&pos==3){
                System.out.println("num "+number_with_min_error);
                //if(number_with_min_error==-1)
                Testing.show_HashShablonNumber(list_hash_nums.get(hash_num),6,9);
            }*/
            if(number_with_min_error==-1)return -1;
            res+=number_with_min_error;
        }
       /* if(currentHand.cards_hero[0].equals("Kd")&&currentHand.cards_hero[1].equals("Qh")&&type_shablon.equals("actions")&&pos==3)
        System.out.println("res "+res);*/



        float result = -1;
        try{
            result = Float.parseFloat(res);
        } catch (Exception a){
            return -1;
        }
        return result;
    }



    static List<int[]> get_list_intarr_HashNumberImg(BufferedImage image_table, int X, int Y, int W, int H, int limit_grey,
                                                     int indents_left_right, int size_dot_in_pix, int size_symbol, int size_intarr_hashimage){
        long s =System.currentTimeMillis();
        // создается списко с координатами начала Х линии символа и конца Х линии, точки обозначаются НУЛЛ
        List<int[]> coords_line_x_for_one_num = new ArrayList<>();
        int[] start_end_num = null;
        boolean is_x_black = false; int count_black_x_line = 0, count_size_num = 0;
        for (int x = X+W-indents_left_right-1; x > X+indents_left_right+1; x--) {
            // определяется есть ли черный пиксель в текущей линии если есть то счетчик увеличивается
            is_x_black = false;
            for (int y = Y; y < Y+H; y++) { if(get_intGreyColor(image_table,x,y)>limit_grey){ is_x_black = true; break; } }
            if(is_x_black) { count_black_x_line++; }
            // если линия белая, то проверяется сколько черных линий было до этого, если 3, а это точка, то все обнуляется в лист заносится нулл
            else {
                if(count_black_x_line==size_dot_in_pix){
                    coords_line_x_for_one_num.add(null);
                    count_black_x_line = 0;
                    continue;
                }
                // если счетчик черных линий равен нулю, при определении белой линиий значит, что символы не начинались и идет пробел это возврат цикла
                // если счетчик черных линий больше нуля, значит начался символ, но некоторые символы короче заявленных поэтому белая линия игнорируется и цикл идет вниз,
                // к примеру еденица меньше стандартных для символа количества черных линий
                // сделано чтобы белая линия не сбивала подсчет линий числа
                if(count_black_x_line==0) continue;
            }
            // проверяется условие есть ли начало числа
            if(count_black_x_line==1){
                start_end_num = new int[2];
                start_end_num[0] = x;
                count_size_num = 1;   // начинается счетчик линий числа
                continue;
            }
            count_size_num++;
            // есть счетчик линий дошел до размеров символов то обнуляются все счетчики и завершается получение кординат числа
            if(count_size_num==size_symbol){
                assert start_end_num != null;
                start_end_num[1] = x;
                coords_line_x_for_one_num.add(start_end_num);
                count_size_num = 0;
                count_black_x_line = 0;
            }
        }
        List<int[]> result = new ArrayList<>();
        boolean is_first_dot = false;
        for(int[] num:coords_line_x_for_one_num){
            // для записи точки, отмечается только первая точка, чтобы исключить попадание точек длинных чисел больше 1000
            if(num==null) { if(!is_first_dot){result.add(null); is_first_dot=true;}
                //System.out.println("DOT");
                continue;}
            int start = num[1], end = num[0];
            //System.out.println(num[0]+"  "+num[1]);
            int _32_pixels =0;
            int[] intarr_hashimage = new int[size_intarr_hashimage]; int index_intarr_hashimage = -1, count_32_pix = 0,
                    amount_pix = (end-start+1)*H, count_all_pix = 0;
            //System.out.println(start+" end "+end);
            out: for (int x = start; x < end+1; x++){
                for (int y = Y; y < Y+H; y++) {
                    count_all_pix++;
                    _32_pixels<<=1;
                    count_32_pix++;
                    if(get_intGreyColor(image_table,x,y)>limit_grey){ _32_pixels+=1;
                        //System.out.print("1");
                    }
                    /*else System.out.print("0");
                    System.out.print(" ");*/
                    // если последнее число имеет больше битов, чем нужно для оставшихся в цикле пикселей, то проверяется условие на общее количество пройденных пикселей,
                    // если оно равно общему количество пикселей в изображении то число с битами обрабатывается досрочно
                    if(count_32_pix==32||count_all_pix==amount_pix){
                        // сдвиг влево на недостающее количество раз если битов в числе больше чем оставшихся пикселей
                        // если так не сделать слева числа будут нули, и потом в получении изображения из битов нужно будет в послденем числе изменять смещение для битов
                        if(count_32_pix<32)_32_pixels<<=(amount_pix%count_32_pix);
                        index_intarr_hashimage++;
                        intarr_hashimage[index_intarr_hashimage] = _32_pixels;
                        _32_pixels = 0;
                        count_32_pix = 0;
                    }
                    // на случай если изображение больше чем битов в числе
                    if(index_intarr_hashimage==size_intarr_hashimage-1)break out;
                }
                //System.out.println();
            }
            //System.out.println(count_all_pix+"  "+amount_pix);
            result.add(intarr_hashimage);
        }

        if(result.isEmpty()||result.get(0)==null) return null;

        return result;
    }




    static int get_AmountOneBitInInt(int lng){ return count_one_in_numbers[(short)(lng>>16)+32768]+count_one_in_numbers[(short)(lng)+32768]; }


    void check_start_flop(){
        //System.out.println("check_start_flop");
        // проверка что херо не делал ход, кроме когда находится на ББ, где возможен чек, если не делал, то проверки на флоп нет
        if(currentHand.preflop_by_positions.get(currentHand.poker_position_of_hero).isEmpty()&&currentHand.poker_position_of_hero !=5) return;
        //System.out.print("check flop ");
        int x1 = coord_2_3_cards_flop[0][0];
        int x2 = coord_2_3_cards_flop[1][0];
        int y = coord_2_3_cards_flop[0][1];
        if(get_int_MaxBrightnessMiddleImg(frame[0],x1,y,17,17)>190
                &&get_int_MaxBrightnessMiddleImg(frame[0],x2,y,17,17)>190)currentHand.is_start_flop = true;

       /* System.out.println(currentHand.is_start_flop+"  c1 "+get_int_MaxBrightnessMiddleImg(frame[0],x1,y,17,17)+
                " c2 "+get_int_MaxBrightnessMiddleImg(frame[0],x2,y,17,17));
        if(currentHand.is_start_flop){save_image(frame[0].getSubimage(x1,y,17,17),"test2\\c1");
        save_image(frame[0].getSubimage(x2,y,17,17),"test2\\c2"); }*/
    }

    private void check_StartPostFlop(int street){
        switch (street){
            case  1 -> {
                //System.out.println("check_start_flop");
                // проверка что херо не делал ход, кроме когда находится на ББ, где возможен чек, если не делал, то проверки на флоп нет
                if(currentHand.preflop_by_positions.get(currentHand.poker_position_of_hero).isEmpty()&&currentHand.poker_position_of_hero !=5) return;
                //System.out.print("check flop ");
                int x1 = coord_2_3_cards_flop[0][0];
                int x2 = coord_2_3_cards_flop[1][0];
                int y = coord_2_3_cards_flop[0][1];
                if(get_int_MaxBrightnessMiddleImg(frame[0],x1,y,17,17)>190
                        &&get_int_MaxBrightnessMiddleImg(frame[0],x2,y,17,17)>190)currentHand.is_start_flop = true; }

            case 2 -> { if(get_int_MaxBrightnessMiddleImg(frame[0],347,168,15,10)>175
                        &&get_int_MaxBrightnessMiddleImg(frame[0],363,212,15,10)>175)currentHand.is_start_turn = true;

                /*System.out.println("TURN "+currentHand.is_start_turn+"  "+get_int_MaxBrightnessMiddleImg(frame[0],347,168,15,10)+"  "
                +get_int_MaxBrightnessMiddleImg(frame[0],363,212,15,10));*/
            }

            case 3 -> { if(get_int_MaxBrightnessMiddleImg(frame[0],392,168,15,10)>175
                        &&get_int_MaxBrightnessMiddleImg(frame[0],408,212,15,10)>175)currentHand.is_start_river = true; }
        }


    }


    String ocr_image(BufferedImage bufferedImage){
       String result = null;
       while (true){
           for(UseTesseract use_tesseart:use_tessearts){
               result = use_tesseart.get_ocr(bufferedImage);
               if(result!=null)return result;
           }
       }
    }



    public int get_int_MaxBrightnessMiddleImg(BufferedImage image,int X,int Y,int W,int H){
        int max = 0, y = Y+H/2;
        for(int x=X; x<X+W; x++){
            int grey = get_intGreyColor(image,x,y);
            if(grey>max)max=grey;
        }
        return max;
    }


    /*BufferedImage get_scale_image(BufferedImage img,double scale){
        try {
            return Thumbnails.of(img).scale(scale).asBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }*/


    BufferedImage get_scale_image(BufferedImage img, int size){ return Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, img.getWidth()*size,img.getHeight()*size); }



   /* BufferedImage check_free_of_kursor(int X, int Y, int w, int h, int limit_grey,int cutX1, int cutY1,int cutX2, int cutY2){

        if(!is_noCursorInterferenceImage(frame[0],X,Y,w,h,limit_grey))return null;
        // System.out.println();
        return frame[0].getSubimage(X+cutX1,Y+cutY1,w+cutX2,h+cutY2);
    }*/


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



    static boolean is_noCursorInterferenceImage(BufferedImage image,int X, int Y, int W, int H, int limit_grey){
        for(int x=X; x<W+X; x++) for(int y=Y; y<H+Y; y+=H-1) if(get_intGreyColor(image,x,y)>limit_grey)return false;
        for(int y=Y; y<H+Y; y++) for(int x=X; x<W+X; x+=W-1) if(get_intGreyColor(image,x,y)>limit_grey)return false;
        return true;
    }


    static boolean is_GoodImageForOcrStack(BufferedImage image,int X, int Y, int W, int H, int limit_grey){
        int count_permit_error =0;
        for(int x=X; x<W+X; x++) for(int y=Y; y<H+Y; y+=H-1) {
            if(get_intGreyColor(image,x,y)>limit_grey)count_permit_error++;
            if(count_permit_error>2)return false;
        }
        for(int y=Y; y<H+Y; y++) for(int x=X; x<W+X; x+=W-1) if(get_intGreyColor(image,x,y)>limit_grey)return false;
        return true;
    }


    boolean compare_CurrentListNumsAndNewListNums(List<int[]> current_list_nums,List<int[]> _new_list_nums, int limit_error){

        if(current_list_nums.size()!=_new_list_nums.size())return false;
        for(int ind_nums = 0; ind_nums<current_list_nums.size(); ind_nums++){
            // проверка на равенство наличия и положения точки в двух списках чисел
            if(current_list_nums.get(ind_nums)==null){ if(_new_list_nums.get(ind_nums)==null)continue; else return false; }
            int total_error_in_num = 0;
            for(int ind_of_num=0; ind_of_num<2; ind_of_num++){
                if(_new_list_nums.get(ind_nums)==null) {
                    // TEST
                    //save_image(frame[0].getSubimage(gxa,gya,gwa,gha),"test5\\_"+(c++));
                return false;}
                total_error_in_num += get_AmountOneBitInInt(current_list_nums.get(ind_nums)[ind_of_num]^_new_list_nums.get(ind_nums)[ind_of_num]);
                if(total_error_in_num>limit_error)return false;
            }
        }
        //System.out.println("true "+error);
        return true;
    }


    boolean compare_LongHashes(long[] current_list_nums,long[] _new_list_nums, int limit_error){

        //if(current_list_nums.size()!=_new_list_nums.size())return false;
        //System.out.println(current_list_nums[15]+" "+_new_list_nums[15]);
        if(abs(current_list_nums[15]-_new_list_nums[15])>15)return false;
        int first_of_pair_error = 0, second_of_pair_error = 0;
      for(int ind_nums = 0; ind_nums<15; ind_nums++){
            if(ind_nums%2==0)first_of_pair_error = get_AmountOneBitInLong(current_list_nums[ind_nums]^_new_list_nums[ind_nums]);
            if(ind_nums%2!=0)second_of_pair_error = get_AmountOneBitInLong(current_list_nums[ind_nums]^_new_list_nums[ind_nums]);
          //System.out.println((first_of_pair_error+second_of_pair_error));
            if(ind_nums>0&&(first_of_pair_error+second_of_pair_error)>limit_error){ return false;  }
        }
        //System.out.println("true "+error);
        return true;
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




    public synchronized void stop(){
        is_run = false;
        notify();
    }

}
