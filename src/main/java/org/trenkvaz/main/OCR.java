package org.trenkvaz.main;

//import org.bytedeco.javacpp.opencv_core.IplImage;
//import org.bytedeco.opencv.opencv_core.IplImage;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/*import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.cvResetImageROI;*/
import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.main.OcrUtils.*;
import static org.trenkvaz.main.Testing.saveImageToFile;
import static org.trenkvaz.main.Testing.show_test_total_hand;
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
    List<List<int[]>> hashesNumsActionsForCompare = new ArrayList<>(6);
    float[] actionsForCompare = new float[6];
    int current_bu = -1;
    int current_position_hero = -1;
    String[] current_hero_cards = new String[]{"",""};
    long[][][] current_id_nicks_for_choose = new long[6][3][16];
    long[] zeros_for_clear_current_id = new long[16];
    CreatingHUD creatingHUD;
    static final int PREFLOP = 0, FLOP =1, TURN = 2, RIVER = 3, AllIn = 4;
    List<List<long[]>> list_of_lists_current_id_nicks_for_choose = new ArrayList<>(6);
    List<long[]> list_of_hashimgs_namberhand = new ArrayList<>(3);
    boolean startSecondHand = false;
    long[] currentHashNuberhand;
    int count_stop_signal = 0;
    float[] curActsOrInvests = new float[6];
    int[][] coordsWinMovePlayerAtPos = new int[6][2];
    int posMovingPlayer = -1;
    float maxRaise = 1;
    float[] currentStacks = new float[]{0,0,0,0,-0.5f,-1};
    int posPlayerRound = 0;
    int[] rounds = new int[]{0,0,0,0,0,1};
    int round = 1;



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
            hashesNumsActionsForCompare.add(new ArrayList<>());
        }

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
            if((frame = main_queue_with_frames.poll())!=null){ main_work_on_table(); }
            else { try { Thread.sleep(10);
                } catch (InterruptedException e) { e.printStackTrace(); }
            }
            if(main_queue_with_frames.size()>100){System.out.println("table "+table+"    "+ main_queue_with_frames.size());c++;
            if(frame[0]!=null) saveImageToFile(frame[0],"test4\\"+table+"_"+c);
            }
        }
    }


    public synchronized void set_image_for_ocr(BufferedImage[] frame){
        main_queue_with_frames.offer(frame);
    }




    boolean startlog = false;
    int count_cadres = 0;
    boolean TEST = true;
    boolean testRIT = false;
    boolean testStartByNumHand = false;

    private void main_work_on_table(){
        if(table!=1)return;
        if(!startlog){ startlog=true;Settings.ErrorLog("START"); }

        int check_start_or_end_hand = get_number_hand();
        //System.out.println(check_start_or_end_hand);
        if(check_start_or_end_hand==0){
            // обработка стоп сигнала для завершения последней раздачи
            if(count_stop_signal==200&&currentHand!=null) {
                currentHand.finalCurrendHand();
                show_test_total_hand(this);
                currentHand = null;
            }
            return;
        }

        if(check_start_or_end_hand==1) {
            if(currentHand!=null){
                currentHand.finalCurrendHand();
                show_test_total_hand(this);
                startSecondHand = true;
            }
            initNewHand();
        }

       /* if(counttest<3)save_image(frame[0],"test5\\_"+table+"_"+(++c));
        counttest++;*/
        //saveImageToFile(frame[0],"test5\\_"+table+"_"+(++c));

        worksPreflop();

       /* worksFlop();

        worksTurn();

        worksRiver();

        worksAllIn();*/

        //}

       /* if(currentHand.cards_hero[0].equals("3d")&&currentHand.cards_hero[1].equals("Ac"))
            //if(currentHand.is_start_flop&&!currentHand.is_start_turn)
                save_image(frame[0],"test5\\_"+table+"_"+(++c));*/
       /* if(currentHand.cards_hero[0].equals("3h")&&currentHand.cards_hero[1].equals("2h")&&table==3)
            System.out.println("MAIN ---------------------------------------  "+currentHand.nicks[4]);*/
        //System.out.println("bu "+currentHand.position_of_bu+" cards "+currentHand.cards_hero+" allnicks "+currentHand.is_nicks_filled);
    }


    private void initNewHand(){
        currentHand = new CurrentHand(this);
        list_by_poker_pos_current_list_arrnums_actions.forEach(List::clear);
        list_of_lists_current_id_nicks_for_choose.forEach(List::clear);
        hashesNumsActionsForCompare.forEach(List::clear);
        int[] correction_for_place_of_imgfold = {-31,97,97,97,-31,-31};
        for(int init=0; init<6; init++){
            coordsWinMovePlayerAtPos[init][0] = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[init]-1][0]
                    +correction_for_place_of_imgfold[poker_positions_index_with_numbering_on_table[init]-1];
            coordsWinMovePlayerAtPos[init][1] = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[init]-1][1]+7;
            actionsForCompare[init] =0;
            curActsOrInvests[init] = 0;
            currentStacks[init] = 0;
            rounds[init] = 0;
            if(init==4){curActsOrInvests[init] = 0.5f; currentStacks[init] = 0.5f;}
            if(init==5){curActsOrInvests[init] = 1f; currentStacks[init] = 1f;  rounds[init] = 1; }
            if(init>0)for(int n=0; n<3; n++) System.arraycopy(zeros_for_clear_current_id,0,current_id_nicks_for_choose[init][n],0,16);


        }
        posMovingPlayer = -1;
        maxRaise = 1;
        posPlayerRound = 0;
        round = 1;
        // TEST
        testRIT = false;
        testStartByNumHand = false;
    }


    private void worksPreflop(){

        //if(currentHand.is_nicks_filled){
        //if(currentHand.cards_hero[0].equals("7c")&&currentHand.cards_hero[1].equals("7h"))
        if(currentHand.is_start_flop||currentHand.is_allin)return;
        check_StartNewStreetANDreturnIsRIT(FLOP);

        if(currentHand.is_start_flop||currentHand.is_allin){
        set_arrs_PositionsWithContinueAndAllinerPlayers(FLOP);

        list_by_poker_pos_current_list_arrnums_actions.forEach(List::clear);
        actionsForCompare = new float[6];
        if(!currentHand.is_allin)currentHand.check_All_in(FLOP);
        return;
        }

        if(!currentHand.is_nicks_filled)get_nicks();
        if(!currentHand.is_stacks_filled)getStartStacks();
        if(currentHand.is_stacks_filled)getActionsAtStreet(PREFLOP);
        get_start_stacks_and_preflop();
        /*getPosMovingPlayer();
        if(posMovingPlayer!=-1) {
            if(testPosMove!=posMovingPlayer){  System.out.println("Position "+posMovingPlayer); testPosMove = posMovingPlayer;}

        }*/
    }

    int testPosMove = -1;

    private void worksFlop(){

        if(!currentHand.is_start_flop||currentHand.is_start_turn||currentHand.is_allin) return;
        check_StartNewStreetANDreturnIsRIT(TURN);


        if(currentHand.is_start_turn||currentHand.is_allin){
            set_arrs_PositionsWithContinueAndAllinerPlayers(TURN);
            list_by_poker_pos_current_list_arrnums_actions.forEach(List::clear);
            if(!currentHand.is_allin)currentHand.check_All_in(TURN);
            return;
        }
        getPostFlopActions(FLOP);
    }



    private void worksTurn(){

        if(!currentHand.is_start_turn||currentHand.is_start_river||currentHand.is_allin) return;
        check_StartNewStreetANDreturnIsRIT(RIVER);

        if(currentHand.is_start_river||currentHand.is_allin){
            set_arrs_PositionsWithContinueAndAllinerPlayers(RIVER);
            list_by_poker_pos_current_list_arrnums_actions.forEach(List::clear);
            if(!currentHand.is_allin)currentHand.check_All_in(RIVER);
            return;
        }
        getPostFlopActions(TURN);
    }


    private void worksRiver(){

        if(!currentHand.is_start_river||currentHand.is_allin) return;

        getPostFlopActions(RIVER);
    }


    private void worksAllIn(){
        if(!currentHand.is_allin)return;
        switch (currentHand.streetAllIn) {
            case PREFLOP -> getPostFlopActions(FLOP);
            case FLOP-> getPostFlopActions(TURN);
            case TURN -> getPostFlopActions(RIVER);
        }
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

            if(list_of_lists_current_id_nicks_for_choose.get(i).size()<3){
                // набор трех id изображений ников и добавление еще одного если ников два
                list_of_lists_current_id_nicks_for_choose.get(i).add(img_pix);
                if(list_of_lists_current_id_nicks_for_choose.get(i).size()<3)continue;
            }
            if(list_of_lists_current_id_nicks_for_choose.get(i).size()==3){
                boolean same_nicks = true;
                // когда набрано три изображения ника то сравниваются все между собой если равны то идет определение ника если нет удаляется первый ид и цикл продолжается
                for(int c1=1; c1<3; c1++){
                    if(compare_LongHashes(list_of_lists_current_id_nicks_for_choose.get(i).get(0),list_of_lists_current_id_nicks_for_choose.get(i).get(c1),10))continue;
                    same_nicks = false; break;
                }
                if(same_nicks){ System.arraycopy(list_of_lists_current_id_nicks_for_choose.get(i).get(0), 0, img_pix , 0, 16);
                } else {
                    list_of_lists_current_id_nicks_for_choose.get(i).remove(0);
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
                saveImageToFile(get_white_black_image(set_grey_and_inverse_or_no(cheked_img,true),105),"id_nicks\\"+currentHand.nicks[i]+" "+(-id_img_pix)+"_"+table);

                //test_is_ocr = true;
                //test_nick= cheked_img;
            }
        }
        currentHand.setIs_nicks_filled();
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
                if(Math.abs(_long_arr_cards_for_compare[nominal_ind_list][3]-card_hash_from_table[3])>=limit_error)continue;

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
           if(hero_cards[0].equals(current_hero_cards[0])&&hero_cards[1].equals(current_hero_cards[1])){
               if(startSecondHand) {
                   int samenumhand =  checkSameNumberHand(1);
                   //System.out.println(BLUE+checkSameNumberHand(1));
                   if(samenumhand==1){testStartByNumHand = true; counttest = 0;}
                   return samenumhand;
               } else return -1;
           }
           else return 0;
       }
       // если БУ определилась, то проверяет совпадение новых карт со старыми если да, то считается текущая раздача, если карты разные, то считается началом новой раздачи
       if(hero_cards[0].equals(current_hero_cards[0])&&hero_cards[1].equals(current_hero_cards[1])&&bu==current_bu){
           if(currentHand==null){
               current_hero_cards[0] = hero_cards[0];current_hero_cards[1] = hero_cards[1];
               current_bu = bu; set_PokerPositionsIndexWithNumberingOnTable(bu);
               hud.clear_hud(table-1);
               counttest = 0;
               if(startSecondHand) return checkSameNumberHand(0);
                   ///System.out.println(RED+checkSameNumberHand(0));
               return 1;
           } else {
               if(startSecondHand) {
               int samenumhand =  checkSameNumberHand(1);
               //System.out.println(BLUE+checkSameNumberHand(1));
               if(samenumhand==1){testStartByNumHand = true; counttest = 0;        }
               return samenumhand;
           } else return -1;
           }
       } else {
           current_hero_cards[0] = hero_cards[0];current_hero_cards[1] = hero_cards[1];
           if(bu!=current_bu){ current_bu = bu; set_PokerPositionsIndexWithNumberingOnTable(bu); }
           hud.clear_hud(table-1);
           counttest = 0;
           if(startSecondHand) return checkSameNumberHand(0);
               //System.out.println(RED+checkSameNumberHand(0));
           return 1;
       }
    }



    int checkSameNumberHand(int raund){
        //if(!startHand)return false;
        long[] hashNumberHand = get_longarr_HashImage(frame[1],0,0,26,5,3,80);
        // начало новой руки всегда возвращает 1
        if(raund==0){
            list_of_hashimgs_namberhand.clear();
            list_of_hashimgs_namberhand.add(hashNumberHand);
            currentHashNuberhand = null;
            return 1;
        } else {
            // по ходу руки если номер руки еще не выбран из трех изо, то заполняется список тремя номерами до тех пор пока все номера будут идентичны
            // тогда номер руки берется из этого списка возврат -1 считается что это правильная текущая рука
            if(currentHashNuberhand==null){
            if(list_of_hashimgs_namberhand.size()<3){list_of_hashimgs_namberhand.add(hashNumberHand);}
            if(list_of_hashimgs_namberhand.size()==3){ boolean same_number = true;
                for(int i=1; i<3; i++){ if(compare_LongHashes(list_of_hashimgs_namberhand.get(1),list_of_hashimgs_namberhand.get(i),6))continue;
                    same_number = false; break;
                }
                if(same_number){currentHashNuberhand = list_of_hashimgs_namberhand.get(1).clone(); list_of_hashimgs_namberhand.clear(); }
                else list_of_hashimgs_namberhand.remove(0);
            }
                return -1;
            }
            // если номер руки выбран, то он сравнивается с приходящими номерами если одинаково, то значит рука продолжается
            else {
                //System.out.println("compare");
               if(compare_LongHashes(currentHashNuberhand,hashNumberHand,6))return -1;
               // если нет равенства руки, то набирается список из трех номеров, до те пор пока все три номера не будут одинаковые
               // это значит, что началась новая рука и новые номера не равны текущему номеру
               // пока не наберутся три номера идет возврат 0, чтобы пропускались изображения, так как пока не ясно началась новая рука или нет
               // когда будет проверено что началась то иде возврат 1
               else {
                   if(list_of_hashimgs_namberhand.size()<3){list_of_hashimgs_namberhand.add(hashNumberHand);}
                   if(list_of_hashimgs_namberhand.size()==3){ boolean same_number = true;
                       for(int i=1; i<3; i++){ if(compare_LongHashes(list_of_hashimgs_namberhand.get(1),list_of_hashimgs_namberhand.get(i),6))continue;
                           same_number = false; break;
                       }
                       if(same_number){return 1; }
                       else list_of_hashimgs_namberhand.remove(0);
                   }
                   return 0;
               }
            }
        }
    }


    int counttest = 0;


    int C = 0;
    int pos = -1, gxa = -1, gya = -1, gwa = -1, gha = -1;
    void get_start_stacks_and_preflop(){
       C++;
        //System.out.println("get stacks");

        for(int poker_position=0; poker_position<6; poker_position++){
            // проверка последнего действия на префлопе на фолд берется последний индекс
            //if(!currentHand.preflop_by_positions.get(poker_position).isEmpty())
            if(currentHand.preflop_by_positions.get(poker_position).get(currentHand.preflop_by_positions.get(poker_position).size()-1)==-10)continue;

            //if(currentHand.cards_hero[0].equals("7c")&&currentHand.cards_hero[1].equals("7h")&&poker_position==5)save_image(frame[0],"test\\"+(c++)+"_"+poker_position+"_t_"+table);

           // если фолд то добавляется фолд
           if(is_Fold(poker_position)){
               /*if(currentHand.preflop_by_positions.get(poker_position).size()==1&&!(currentHand.preflop_by_positions.get(poker_position).get(0)>0))
                currentHand.preflop_by_positions.get(poker_position).set(0,1_000_000f);
            else */
                currentHand.preflop_by_positions.get(poker_position).add(-10f);
                continue;
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

                if(currentHand.oldStartStacks[poker_position]==0f)currentHand.oldStartStacks[poker_position]=-1f;continue;
            }


            //if(currentHand.cards_hero[0].equals("8d")&&poker_position==0)save_image(frame[0].getSubimage(xa,ya,wa,ha),"test2\\_act3_"+(c++));

            List<int[]> nums = get_list_intarr_HashNumberImg(frame[0],xa,ya+1,70,9,205,0,2,6,2);

            if(nums==null) {  if(currentHand.oldStartStacks[poker_position]==0)currentHand.oldStartStacks[poker_position]=-1f;

            Settings.ErrorLog(" hand "+currentHand.time_hand+" ERROR get_list_intarr_HashNumberImg ");
                saveImageToFile(frame[0].getSubimage(xa,ya,wa,ha),"test3\\"+(poker_positions_index_with_numbering_on_table[poker_position]-1)+"_"+table);

            continue;}

            // если рейз можно прочитать, а в стеке есть -1, то оно меняется на ноль, чтобы стек определялся
            if(currentHand.oldStartStacks[poker_position]==-1)currentHand.oldStartStacks[poker_position]=0f;

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
            if(actions==-1) {if(currentHand.oldStartStacks[poker_position]==0)currentHand.oldStartStacks[poker_position]=-1f; continue;}

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


        if(!currentHand.is_old_stacks_filled){
            int count_filled_stacks = 0;
            for(int poker_position =0; poker_position<6; poker_position++){
                // проверка на -1, чтобы избежать получения стека, так как не ясно какой был рейз
                if(currentHand.oldStartStacks[poker_position]==-1)continue;
                if(currentHand.oldStartStacks[poker_position]!=0){ count_filled_stacks++; continue;}
                float stack_without_action = getOneStack(poker_position);
                if(stack_without_action==-11)continue;
                else if(stack_without_action<0){
                    if(stack_without_action==-1)currentHand.oldStartStacks[poker_position] = Float.NaN;
                    if(stack_without_action==-2)currentHand.oldStartStacks[poker_position] = currentHand.preflop_by_positions.get(poker_position).
                            get(currentHand.preflop_by_positions.get(poker_position).size()-1);
                    count_filled_stacks++;
                    continue;
                }
                // если первое действие фолд или пустое
                if(currentHand.preflop_by_positions.get(poker_position).get(0)==-10)
                {currentHand.oldStartStacks[poker_position] = stack_without_action;
                count_filled_stacks++;
                continue;}    // fold = -10
                //System.out.println("p "+poker_position+" "+currentHand.preflop_by_positions.get(poker_position).get(currentHand.preflop_by_positions.get(poker_position).size()-1));
                // если есть действия и возможно фолд, фолд пропускается берется последний рейз и прибавляется к стеку
                for(int actions = currentHand.preflop_by_positions.get(poker_position).size()-1; actions>-1; actions-- ){
                    if(currentHand.preflop_by_positions.get(poker_position).get(actions)==-10)continue;
                    currentHand.oldStartStacks[poker_position] = stack_without_action+currentHand.preflop_by_positions.get(poker_position).get(actions);
                    count_filled_stacks++;
                    break;
                }
            }
            if(count_filled_stacks==6)currentHand.is_old_stacks_filled = true;
        }

        //test_list_imgStacks.add(imgStacks);
        //System.out.println("************************************");

    }



    private void getStartStacks(){

        int count_filled_stacks = 0;
        for(int poker_position =0; poker_position<6; poker_position++){

            if(currentHand.startStacks[poker_position]!=0){ count_filled_stacks++; continue;}
            float action = getOneAction(poker_position);
            if(action==-1)continue;

            float stack_without_action = getOneStack(poker_position);
            if(stack_without_action==-11)continue;
            else if(stack_without_action<0){
                if(stack_without_action==-1){currentHand.startStacks[poker_position] = Float.NaN; }
                // может быть ситуация что в стеке выставлен оллин, но в действии пока пусто поэтому нужно дождаться действия чтобы узнать стек оллина
                else if(stack_without_action==-2){
                    if(action==0)continue;
                    currentHand.startStacks[poker_position] = action;
                    currentStacks[poker_position] += currentHand.startStacks[poker_position]; // плюс чтобы в текущем стеке учитывались блайнды
                }

                count_filled_stacks++;
                continue;
            }
            // если действие пустое
            if(action==0)currentHand.startStacks[poker_position] = stack_without_action;
            else {
                currentHand.startStacks[poker_position] = stack_without_action+action;
                //curActsOrInvests[poker_position] = action;
            }

            currentStacks[poker_position] += currentHand.startStacks[poker_position];
            count_filled_stacks++;
        }
        if(count_filled_stacks==6)currentHand.is_stacks_filled = true;

    }


    private float getOneAction(int poker_position){
        int xa = coords_actions[poker_positions_index_with_numbering_on_table[poker_position]-1][0];
        int ya = coords_actions[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+2;
        int wa = 70;
        int ha = 11;
        // если поле пустое то действия вообще нет
        if(!(get_int_MaxBrightnessMiddleImg(frame[0],xa,ya,wa,ha)>200))return 0;
        // если действие есть но с помехами нет возможности прочитать
        if(!is_noCursorInterferenceImage(frame[0],xa,ya,wa,ha,240))return -1;
        // получении хеша числа и если нулл это ошибка получения хеша
        List<int[]> nums = get_list_intarr_HashNumberImg(frame[0],xa,ya+1,70,9,205,0,2,6,2);
        if(nums==null)return -1;
        if(!hashesNumsActionsForCompare.get(poker_position).isEmpty())
            // если лист не пустой то сравнивает его с текущим числом если они одинаковые, то значит не нужно распознавать
            if(compare_CurrentListNumsAndNewListNums(hashesNumsActionsForCompare.get(poker_position),nums,10))
                return actionsForCompare[poker_position];
            // если же числа разные, или лист пустой то сначало число распознается
        float action = get_OcrNum(nums,10,"actions");
        // ошибка распознавания
        if(action==-1)return -1;
        hashesNumsActionsForCompare.set(poker_position,nums);
        actionsForCompare[poker_position] = action;
        return action;
        // 0: пустое поле действия, -1: невозможно распознать
    }


    private float getOneStack(int poker_position){
        int[] correction_for_place_of_nicks = {1,2,2,2,1,1};
        int x = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][0]
                +3+correction_for_place_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1];
        int y = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+17;
        if(!is_GoodImageForOcrStack(frame[0],x,y,72,14,150))return  -11;
        float result = get_OcrNum(get_list_intarr_HashNumberImg(frame[0],x,y+1,72,12,175,
                5,3,8,3),10,"stacks");
        if(result==-1){
            long[] hash_for_compare = get_longarr_HashImage(frame[0],x,y+1,72,12,14,175);
            int shab = get_int_CompareLongHashesToShablons(hash_for_compare,shablons_text_sittingout_allin);
            if(shab==-1)return -11;
            if(shab==0)return -1;
            if(shab==1)return -2;
        }
        return result;
        // если не разпонается стек или шаблон то -11, если распознается только шаблон -1 = ситаут, -2 = аллин
    }



    private void getActionsAtStreet(int street){

        int pokerPos = posPlayerRound;
        for(;;pokerPos++){if(pokerPos==6){pokerPos=0;}
           if(curActsOrInvests[pokerPos]==-10||curActsOrInvests[pokerPos]==-100)continue;
           if(!(rounds[pokerPos]<round))break;// если текущий раунд игрока не меньше общего то пропуск значит игрок уже ходил

           float action = getRoundOfPlayer(pokerPos);
           if(action==-1)break;
           if(action==-10){curActsOrInvests[pokerPos] = -10;  currentHand.preflopActionsStats.get(pokerPos).add(Float.NEGATIVE_INFINITY);continue;}

           if(action>maxRaise){ maxRaise = action; round++;
            currentHand.preflopActionsStats.get(pokerPos).add(action);
           }
           else currentHand.preflopActionsStats.get(pokerPos).add(-(action-curActsOrInvests[pokerPos]));
            // ситуация когда сумму(текущего стека и вложженых средств) равны действию это значит оллин
            if(action==(curActsOrInvests[pokerPos]+currentStacks[pokerPos])) curActsOrInvests[pokerPos] = -100;
                // если нет значит обычный рейз
            else curActsOrInvests[pokerPos] = action;

            rounds[pokerPos] = round;
       }

        posPlayerRound = pokerPos;
    }


    private float getRoundOfPlayer(int pokerPos){
        int max = getMaxBrightWinMovePlayer(pokerPos);
        if(max<70)return -10;  // фолд
        if(max>100)return -1;  // ожидание хода
        float action = getOneAction(pokerPos);
        if(action==0||action==-1||action==curActsOrInvests[pokerPos])return -1;
        return action;
    }

    void getPostFlopActions(int street){

        ArrayList<ArrayList<Float>> actions = new ArrayList<>();
        int[] continueplayers = new int[6], allinersplayers=new int[6];
        if(street==FLOP){actions = currentHand.flop_by_positions;
        continueplayers = currentHand.arr_continue_players_flop;
        allinersplayers = currentHand.arr_alliner_players_flop;
        }
        else if(street==TURN){actions = currentHand.turn_by_positions;continueplayers = currentHand.arr_continue_players_turn;
        allinersplayers = currentHand.arr_alliner_players_turn;}
        else if(street==RIVER){actions = currentHand.river_by_positions;continueplayers = currentHand.arr_continue_players_river;
        allinersplayers = currentHand.arr_alliner_players_river;}


        int poker_position = 3;

        for(int init_pos=0;  init_pos<6; init_pos++){
            if(poker_position==5)poker_position=-1;poker_position++;

            if(continueplayers[poker_position]==-1||allinersplayers[poker_position]==1)continue;

            /*if(continueplayers[poker_position]==0){
                float stack = getStack(poker_position);
                if(stack==-1){continueplayers[poker_position]=-1; continue;}
                if(stack==-2){continueplayers[poker_position]=1; allinersplayers[poker_position] =1; continue;}
                if(stack>(currentHand.stacks[poker_position]-maxRaisePreStreet))
            }*/




            if(!actions.get(poker_position).isEmpty())
                if(actions.get(poker_position).get(actions.get(poker_position).size()-1)==-10)continue;

            if(is_Fold(poker_position)) {
                if(currentHand.firstBetPostflopPokerPos[street]==-1){
                    saveImageToFile(frame[0],"test5\\fold_befor_bet_"+currentHand.time_hand);
                }
                actions.get(poker_position).add(-10f);
                continue;}

            int xa = coords_actions[poker_positions_index_with_numbering_on_table[poker_position]-1][0];
            int ya = coords_actions[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+2;
            int wa = 70;
            int ha = 11;
            if(!(get_int_MaxBrightnessMiddleImg(frame[0],xa,ya,wa,ha)>200)||!is_noCursorInterferenceImage(frame[0],xa,ya,wa,ha,240)) continue;


            List<int[]> nums = get_list_intarr_HashNumberImg(frame[0],xa,ya+1,70,9,205,0,2,6,2);

            if(nums==null) {
                Settings.ErrorLog(" hand "+currentHand.time_hand+" ERROR get_list_intarr_HashNumberImg  "+street);
                saveImageToFile(frame[0].getSubimage(xa,ya,wa,ha),"test3\\"+(poker_positions_index_with_numbering_on_table[poker_position]-1)+"_"+table);
                continue;
            }

            if(list_by_poker_pos_current_list_arrnums_actions.get(poker_position).isEmpty()) list_by_poker_pos_current_list_arrnums_actions.set(poker_position,nums);
            else {
                //TEST
                //pos = poker_position;
                //gwa =wa; gha = ha; gxa = xa; gya = ya;
                if(compare_CurrentListNumsAndNewListNums(list_by_poker_pos_current_list_arrnums_actions.get(poker_position),nums,10))continue;
                list_by_poker_pos_current_list_arrnums_actions.set(poker_position,nums);
            }

            float act = get_OcrNum(nums,10,"actions");
            if(act==-1) continue;

            if(actions.get(poker_position).isEmpty())actions.get(poker_position).add(act);
            else if(actions.get(poker_position).get(actions.get(poker_position).size()-1)!=act)actions.get(poker_position).add(act);

            if(currentHand.firstBetPostflopPokerPos[street]==-1)currentHand.firstBetPostflopPokerPos[street] = poker_position;
        }


    }


    float maxRaisePreStreet =0;
    private void set_arrs_PositionsWithContinueAndAllinerPlayers(int street){
        maxRaisePreStreet =0; float action =0; int positionMaxRaise = 5;
        int[] correction_for_place_of_nicks = {1,2,2,2,1,1};

        ArrayList<ArrayList<Float>> actionsPreStreetByPoses = new ArrayList<>();

        int[] continuePlayersPreStreet = null, continuePlayPlayers = null, playersInAllines = null, playersInAllinesPreStreet = null;

        if(street==FLOP){actionsPreStreetByPoses = currentHand.preflop_by_positions;
                                                                             continuePlayPlayers = currentHand.arr_continue_players_flop;
                                                                             playersInAllines = currentHand.arr_alliner_players_flop;
        }
        else if(street==TURN){actionsPreStreetByPoses = currentHand.flop_by_positions;
            continuePlayersPreStreet = currentHand.arr_continue_players_flop; continuePlayPlayers = currentHand.arr_continue_players_turn;
            playersInAllinesPreStreet = currentHand.arr_alliner_players_flop; playersInAllines = currentHand.arr_alliner_players_turn;
        }
        else if(street==RIVER){actionsPreStreetByPoses = currentHand.turn_by_positions;
            continuePlayersPreStreet = currentHand.arr_continue_players_turn; continuePlayPlayers = currentHand.arr_continue_players_river;
            playersInAllinesPreStreet = currentHand.arr_alliner_players_turn; playersInAllines = currentHand.arr_alliner_players_river;
        }


        for(int poker_position=0; poker_position<6; poker_position++){

            // из-за того, что срабатывание фолда запаздывает изменил алгоритм определения оставшихся в раздаче
            // обработка предидущих массивов продолжаютиграть и олинеры, если игрок не играет или уже в оллине передается дальше это состояние
            if(street!=FLOP){
            if(playersInAllinesPreStreet[poker_position]==1){  playersInAllines[poker_position]=1;  continuePlayPlayers[poker_position] = 1; continue; }
            if(continuePlayersPreStreet[poker_position]==-1){  continuePlayPlayers[poker_position] = -1; continue; }
            }

            // может быть ситуация когда списки действий пустые если на улице были одни чеки, то указывается что игрок продолжает играть
            if(street!=FLOP)
            if(actionsPreStreetByPoses.get(poker_position).isEmpty()){
                if(continuePlayersPreStreet[poker_position]==1) continuePlayPlayers[poker_position] = 1;
                continue;
            }

            action = actionsPreStreetByPoses.get(poker_position).get(actionsPreStreetByPoses.get(poker_position).size()-1);

            if(action==-10){ continuePlayPlayers[poker_position] = -1; continue;}

            if(is_Fold(poker_position)){ continuePlayPlayers[poker_position] = -1; continue;}


            if(action>maxRaisePreStreet){maxRaisePreStreet=action; // positionMaxRaise = poker_position;
            }


            int x = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][0]+
                    correction_for_place_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1]-5;
            int y = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+1;
            long[] img_pix = get_longarr_HashImage(frame[0],x,y+2,86,11,15,150);
            /* if(currentHand.cards_hero[0].equals("6d")&&currentHand.cards_hero[1].equals("7d"))
             if(currentHand.is_start_flop&&!currentHand.is_start_turn&&poker_position==5)
                save_image(frame[0],"test5\\_"+table+"_"+(++c)+"_"+get_int_CompareLongHashesToShablons(img_pix,shablon_text_poker_terms));*/
            if(get_int_CompareLongHashesToShablons(img_pix,shablon_text_poker_terms)==3){ continuePlayPlayers[poker_position] = -1; continue;}


            continuePlayPlayers[poker_position]= 1;
        }

        //if(maxRaisePreStreet>1)continuePlayPlayers[positionMaxRaise] = 1;

        for(int poker_position=0; poker_position<6; poker_position++){
            if(continuePlayPlayers[poker_position]==-1||playersInAllines[poker_position]==1)continue;

            if(street==FLOP){
                //System.out.println("flop  "+currentHand.nicks[currentHand.poker_positions_by_pos_table_for_nicks[poker_position]-1]+"  "+continuePlayPlayers[poker_position]);
                currentHand.streetAllIn = PREFLOP;

                //  определение остатка стека у продолжающих играть для следующей улицы после действий на предидущей на основе максимальноге рейза
                //  если стек больше такого рейза то расчитывается разница рейза и стека как остаток для последеующих действий
                if(currentHand.startStacks[poker_position]>maxRaisePreStreet)currentHand.stacks_flop[poker_position] = currentHand.startStacks[poker_position]-maxRaisePreStreet;
                // если стек меньше или равен макс рейзу то это значит, что игрок в оллине
                else playersInAllines[poker_position] =1;
                continue;
            }
            if(street==TURN){
                currentHand.streetAllIn = FLOP;
                if(currentHand.stacks_flop[poker_position]>maxRaisePreStreet)currentHand.stacks_turn[poker_position] = currentHand.stacks_flop[poker_position]-maxRaisePreStreet;
                else playersInAllines[poker_position] =1;
                continue;
            }
            if(street==RIVER){
                currentHand.streetAllIn = TURN;
                if(currentHand.stacks_turn[poker_position]>maxRaisePreStreet)currentHand.stacks_river[poker_position] = currentHand.stacks_turn[poker_position]-maxRaisePreStreet;
                else playersInAllines[poker_position] =1;
            }
        }
    }




    private boolean is_Fold(int poker_position){
        if(poker_position==currentHand.poker_position_of_hero)return false;
        int x = coordsWinMovePlayerAtPos[poker_position][0], j = coordsWinMovePlayerAtPos[poker_position][1], max = 70;
        for(int i=x; i<x+15; i++){ j++; if(get_intGreyColor(frame[0],i,j)>max)return false; }
        return true;
    }


    private void getPosMovingPlayer(){
        if(posMovingPlayer !=-1){
            int max = getMaxBrightWinMovePlayer(posMovingPlayer);
            //System.out.println("Mov!=-1 "+posMovingPlayer+" "+max);
            if(max>240) { posMovingPlayer = -1; return;}  // помеха определения  ситуация пропускается так как вероятнее всего ход именно за этим игроком и проверять остальных нет смысла
            if(max<70){
                curActsOrInvests[posMovingPlayer] = -10;} // фолд значит ход перешел к другому
            else if(max>100)return; // подтвержение что ход остается на этом месте
            posMovingPlayer = -1;
        }

            for(int pokerPos = 0; pokerPos<6; pokerPos++){
                if(curActsOrInvests[pokerPos]<0)continue;
                int max = getMaxBrightWinMovePlayer(pokerPos);
                //System.out.println(pokerPos+" "+posMovingPlayer+" "+max);
                if(max>240)continue;
                if(max<70){
                    curActsOrInvests[pokerPos] = -10;continue;}
                if(max>100){ posMovingPlayer = pokerPos; break; }
            }

    }




    private int getMaxBrightWinMovePlayer(int pokerPos){
        int x = coordsWinMovePlayerAtPos[pokerPos][0], j = coordsWinMovePlayerAtPos[pokerPos][1], max = 0;
        for(int i=x; i<x+15; i++){ j++;int c = get_intGreyColor(frame[0],i,j);if(c>max)max=c; }
        return max;
    }


    private void check_StartNewStreetANDreturnIsRIT(int street){

        int xfloprit1 = 318, yfloprit1 = 179, xfloprit2 = 300, yfloprit2 = 200; // bright 150  correct_cards = 46;
        switch (street){
            case  FLOP -> {
                //System.out.println("check_start_flop");
                // проверка что херо не делал ход, кроме когда находится на ББ, где возможен чек, если не делал, то проверки на флоп нет
                if(currentHand.preflop_by_positions.get(currentHand.poker_position_of_hero).isEmpty()&&currentHand.poker_position_of_hero !=5) return;

                if(get_int_MaxBrightnessMiddleImg(frame[0],xfloprit1,yfloprit1,17,17)>150
                        &&get_int_MaxBrightnessMiddleImg(frame[0],xfloprit2,yfloprit2,17,17)>150){ testRIT = true; currentHand.is_allin = true;
                        return;}

                //System.out.print("check flop ");
                int x1 = coord_2_3_cards_flop[0][0];
                int x2 = coord_2_3_cards_flop[1][0];
                int y = coord_2_3_cards_flop[0][1];
                if(get_int_MaxBrightnessMiddleImg(frame[0],x1,y,17,17)>190
                        &&get_int_MaxBrightnessMiddleImg(frame[0],x2,y,17,17)>190)currentHand.is_start_flop = true;

            }
            case TURN -> {
                if(get_int_MaxBrightnessMiddleImg(frame[0],364,yfloprit1,17,17)>150
                        &&get_int_MaxBrightnessMiddleImg(frame[0],346,yfloprit2,17,17)>150){ testRIT = true;  currentHand.is_allin = true; return; }

                if(get_int_MaxBrightnessMiddleImg(frame[0],347,168,15,10)>175
                        &&get_int_MaxBrightnessMiddleImg(frame[0],363,212,15,10)>175)currentHand.is_start_turn = true;

                /*System.out.println("TURN "+currentHand.is_start_turn+"  "+get_int_MaxBrightnessMiddleImg(frame[0],347,168,15,10)+"  "
                +get_int_MaxBrightnessMiddleImg(frame[0],363,212,15,10));*/
            }

            case RIVER -> {
                if(get_int_MaxBrightnessMiddleImg(frame[0],410,yfloprit1,17,17)>150
                        &&get_int_MaxBrightnessMiddleImg(frame[0],392,yfloprit2,17,17)>150){ testRIT = true; currentHand.is_allin = true; return;}

                if(get_int_MaxBrightnessMiddleImg(frame[0],392,168,15,10)>175
                        &&get_int_MaxBrightnessMiddleImg(frame[0],408,212,15,10)>175)currentHand.is_start_river = true;
            }
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






    public synchronized void stop(){
        is_run = false;
        notify();
    }

}
