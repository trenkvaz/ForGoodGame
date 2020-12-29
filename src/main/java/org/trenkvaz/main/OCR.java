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
import static org.trenkvaz.main.Testing.*;
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
    //float[] currentStacks = new float[]{0,0,0,0,-0.5f,-1};
    int posPlayerRound = 0;
    int[] rounds = new int[]{0,0,0,0,0,1};
    int round = 1;
    int amountContPlay = 6;
    boolean isActionPreflop = false;



    // test
    record TestRecFrameTimeHand(BufferedImage imges_frame,long timehand){}
    List<TestRecFrameTimeHand> images_framestimehands = new ArrayList<>(3);
    TestCurrentHand testCurrentHand;


    public OCR(int table){
        creatingHUD = new CreatingHUD(table);
        this.coord_of_table = coord_left_up_of_tables[table];
        this.table = table+1;
        main_queue_with_frames = new LinkedBlockingQueue<>();
        for(int i=0; i<6; i++){
            //list_by_poker_pos_current_list_arrnums_actions.add(new ArrayList<>());
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
        try {
            while (is_run){
                if((frame = main_queue_with_frames.poll())!=null){ main_work_on_table(); }
                else { try { Thread.sleep(10);
                } catch (InterruptedException e) { e.printStackTrace(); }
                }
                if(main_queue_with_frames.size()>100){System.out.println("table "+table+"    "+ main_queue_with_frames.size());c++;
                    if(frame[0]!=null) saveImageToFile(frame[0],"test4\\"+table+"_"+c);
                }
            }
        } catch (Exception e){

            show_test_total_hand(testCurrentHand,true);
            startStopCapture.removeOcrInOcrList_1(table-1);
            testSaveImgFrameTimeHand(images_framestimehands,"unknown");
            e.printStackTrace();

        }

    }


    public synchronized void set_image_for_ocr(BufferedImage[] frame){
        main_queue_with_frames.offer(frame);
    }




    boolean startlog = false;

    boolean TEST = true;
    boolean testRIT = false;
    boolean testStartByNumHand = false;

    private void main_work_on_table(){
        if(isTest){
        //if(table!=1&&table!=2)return;
        if(table!=2)return;
        }

        if(!startlog){ startlog=true;Settings.ErrorLog("START"); }

        int check_start_or_end_hand = get_number_hand();
        //System.out.println(check_start_or_end_hand);
        if(check_start_or_end_hand==0){
            // обработка стоп сигнала для завершения последней раздачи
            if(count_stop_signal==200&&currentHand!=null) {
                finishedAllStreetNextHand();
                currentHand.finalCurrendHand();
                show_test_total_hand(testCurrentHand,false);
                currentHand = null;
            }
            return;
        }

        if(check_start_or_end_hand==1) {
            if(currentHand!=null){
                finishedAllStreetNextHand();
                currentHand.finalCurrendHand();
                show_test_total_hand(testCurrentHand,false);
                startSecondHand = true;
            }
            initNewHand();
            testCurrentHand = new TestCurrentHand(this);
            testCurrentHand.setStartConditions(currentHand,testStartByNumHand);
        }

        //TEST
        if(images_framestimehands.size()!=3)images_framestimehands.add(new TestRecFrameTimeHand(frame[0],currentHand.time_hand));
        else { images_framestimehands.remove(0);images_framestimehands.add(new TestRecFrameTimeHand(frame[0],currentHand.time_hand)); }


        worksPreflop();

        worksFlop();

        worksTurn();

        //worksRiver();

        worksAllIn();


    }

    private void finishedAllStreetNextHand(){
        finishedActionsAtPreflop();
        if(currentHand.isStartStreets[FLOP])finishedActionsPostflop(FLOP);
    }

    private void initNewHand(){
        currentHand = new CurrentHand(this);
        list_of_lists_current_id_nicks_for_choose.forEach(List::clear);
        hashesNumsActionsForCompare.forEach(List::clear);
        int[] correction_for_place_of_imgfold = {-31,97,97,97,-31,-31};
        for(int init=0; init<6; init++){
            coordsWinMovePlayerAtPos[init][0] = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[init]-1][0]
                    +correction_for_place_of_imgfold[poker_positions_index_with_numbering_on_table[init]-1];
            coordsWinMovePlayerAtPos[init][1] = coords_places_of_nicks[poker_positions_index_with_numbering_on_table[init]-1][1]+7;
            actionsForCompare[init] =0;
            curActsOrInvests[init] = 0;
            rounds[init] = 0;
            if(init==4){curActsOrInvests[init] = 0.5f; }
            if(init==5){curActsOrInvests[init] = 1f;  rounds[init] = 1; }
            // с 1 потому что ник героя не определяется и его массивы не надо обнулять
            if(init>0)for(int n=0; n<3; n++) System.arraycopy(zeros_for_clear_current_id,0,current_id_nicks_for_choose[init][n],0,16);
        }
        posMovingPlayer = -1;maxRaise = 1;posPlayerRound = 0;round = 1;amountContPlay = 6; isActionPreflop = false;
        // TEST


        testStartByNumHand = false;
        counttest = 0;
    }
    int counttest = 0;

    private void clearForNewStreet(){
        hashesNumsActionsForCompare.forEach(List::clear);
        Arrays.fill(actionsForCompare,0);
        for(int init=0; init<6; init++){
            rounds[init] = 0;
            if(curActsOrInvests[init]==-10||curActsOrInvests[init]==-100)continue;
            curActsOrInvests[init] = 0;
        }
        maxRaise=0; round = 0; posPlayerRound = 4;
    }


    private void worksPreflop(){

        //if(currentHand.is_nicks_filled){
        //if(currentHand.cards_hero[0].equals("7c")&&currentHand.cards_hero[1].equals("7h"))
        if(currentHand.isStartStreets[FLOP]||currentHand.streetAllIn!=-1)return;
        check_StartNewStreetANDreturnIsRIT(FLOP);

        if(currentHand.isStartStreets[FLOP])testCurrentHand.setTestAllines(FLOP,"");
        if(currentHand.streetAllIn==0)testCurrentHand.setTestAllines(PREFLOP,"_ALL_RIT");

        if(currentHand.isStartStreets[FLOP]||currentHand.streetAllIn!=-1)return;


        if(!currentHand.is_nicks_filled)get_nicks();
        if(!currentHand.is_stacks_filled)getStartStacks();
        getActionsAtPreflop();

    }



    private void worksFlop(){

        if(!currentHand.isStartStreets[FLOP]||currentHand.isStartStreets[TURN]||currentHand.streetAllIn!=-1) return;
        if(!currentHand.isFinishedStreets[PREFLOP]){finishedActionsAtPreflop();if(currentHand.isFinishedStreets[PREFLOP]&&currentHand.streetAllIn==-1)clearForNewStreet();}
        // пока не завершится префлоп и не проверит что не было оллина префлоп дальше хода нет
        if(!currentHand.isFinishedStreets[PREFLOP]||currentHand.streetAllIn!=-1)return;
        check_StartNewStreetANDreturnIsRIT(TURN);

        //TEST
        if(currentHand.isStartStreets[TURN])testCurrentHand.setTestAllines(TURN,"");
        if(currentHand.streetAllIn==1)testCurrentHand.setTestAllines(FLOP,"_ALL_RIT");
        ////

        if(currentHand.isStartStreets[TURN]||currentHand.streetAllIn!=-1)return;
        getPostFlopActions(FLOP);
    }



    private void worksTurn(){

        if(!currentHand.isStartStreets[TURN]||currentHand.isStartStreets[RIVER]||currentHand.streetAllIn!=-1) return;
        if(!currentHand.isFinishedStreets[FLOP]){finishedActionsPostflop(FLOP);if(currentHand.isFinishedStreets[FLOP]&&currentHand.streetAllIn==-1)clearForNewStreet();}
        if(!currentHand.isFinishedStreets[FLOP]||currentHand.streetAllIn!=-1)return;
        check_StartNewStreetANDreturnIsRIT(RIVER);

        //TEST
        if(currentHand.isStartStreets[RIVER])testCurrentHand.setTestAllines(RIVER,"");
        if(currentHand.streetAllIn==2)testCurrentHand.setTestAllines(TURN,"_ALL_RIT");
        ////

        if(currentHand.isStartStreets[RIVER]||currentHand.streetAllIn!=-1)return;
        //getPostFlopActions(TURN);
    }


    private void worksRiver(){

        if(!currentHand.isStartStreets[RIVER]||currentHand.streetAllIn!=-1) return;

        //getPostFlopActions(RIVER);
    }


    private void worksAllIn(){
        if(currentHand.streetAllIn==-1)return;
        // здесь завершение улицы работает только если были РИТ, так как обычный оллин уже обработан в методах постфлопа

        finishedActionsAtPreflop();
        finishedActionsPostflop(FLOP);
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


        testCurrentHand.setNicks(currentHand.nicks);
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
        int utg = current_bu+3; if(utg>6) utg = utg-6;
        for(int c=0; c<6; c++, utg++){
            if(utg==7)utg=1;
            poker_positions_index_with_numbering_on_table[c] = utg;
            if(utg==1)current_position_hero = c;
        }
    }

    int c =0;


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
                   int samenumhand = checkSameNumberHand(1);
                   //System.out.println(BLUE+checkSameNumberHand(1));
                   if(samenumhand==1){testStartByNumHand = true;}
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

               if(startSecondHand) return checkSameNumberHand(0);
                   ///System.out.println(RED+checkSameNumberHand(0));
               return 1;
           } else {
               if(startSecondHand) {
               int samenumhand =  checkSameNumberHand(1);
               //TEST
               if(samenumhand==1)testStartByNumHand = true;

               return samenumhand;
           } else return -1;
           }
       } else {
           current_hero_cards[0] = hero_cards[0];current_hero_cards[1] = hero_cards[1];
           if(bu!=current_bu){ current_bu = bu; set_PokerPositionsIndexWithNumberingOnTable(bu); }
           hud.clear_hud(table-1);

           if(startSecondHand) return checkSameNumberHand(0);
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



    private void getStartStacks(){

        int count_filled_stacks = 0;
        for(int poker_position =0; poker_position<6; poker_position++){

            if(currentHand.startStacks[poker_position]!=0){ count_filled_stacks++; continue;}
            float action = getOneAction(poker_position);
            if(action==-1)continue;

            float stack_without_action = getOneStack(poker_position);
            if(stack_without_action==-11)continue;
            else if(stack_without_action<0){
                if(stack_without_action==-1){currentHand.startStacks[poker_position] = -1f; }
                // может быть ситуация что в стеке выставлен оллин, но в действии пока пусто поэтому нужно дождаться действия чтобы узнать стек оллина
                else if(stack_without_action==-2){
                    if(action==0)continue;
                    currentHand.startStacks[poker_position] = action;
                   // currentStacks[poker_position] += currentHand.startStacks[poker_position]; // плюс чтобы в текущем стеке учитывались блайнды
                    currentHand.startStacksAtStreets[PREFLOP][poker_position] = action;
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

            //currentStacks[poker_position] += currentHand.startStacks[poker_position];
            currentHand.startStacksAtStreets[PREFLOP][poker_position] = currentHand.startStacks[poker_position];
            count_filled_stacks++;
        }
        if(count_filled_stacks==6)currentHand.is_stacks_filled = true;


    }


    private float getOneAction(int poker_position){
        int xa = coords_actions[poker_positions_index_with_numbering_on_table[poker_position]-1][0];
        int ya = coords_actions[poker_positions_index_with_numbering_on_table[poker_position]-1][1]+2;
        int wa = 80;
        int ha = 11;
        // если поле пустое то действия вообще нет
        if(!(get_int_MaxBrightnessMiddleImg(frame[0],xa,ya,wa,ha)>200))return 0;
        // если действие есть но с помехами нет возможности прочитать
        if(!is_noCursorInterferenceImage(frame[0],xa,ya,wa,ha,240))return -1;
        // получении хеша числа и если нулл это ошибка получения хеша
        List<int[]> nums = get_list_intarr_HashNumberImg(frame[0],xa,ya+1,80,9,205,0,2,6,2);
        if(nums==null) {

            Settings.ErrorLog(" hand "+currentHand.time_hand+" ERROR get_list_intarr_HashNumberImg ");
            saveImageToFile(frame[0].getSubimage(xa,ya,wa,ha),"test3\\"+(poker_positions_index_with_numbering_on_table[poker_position]-1)+"_"+table);
            saveImageToFile(frame[0],"test3\\"+(poker_positions_index_with_numbering_on_table[poker_position]-1)+" hand "+currentHand.time_hand);
            return -1;

        }

        //if(nums==null)return -1;

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



    private void getActionsAtPreflop(){

        int pokerPos = posPlayerRound;
        for(;;pokerPos++){if(pokerPos==6){pokerPos=0;}
           if(curActsOrInvests[pokerPos]==-10||curActsOrInvests[pokerPos]==-100)continue;
            //если текущий раунд игрока равен общему значит он уже ходил,исключение игрок на ББ и 1 раунд, так как на ББ раунд изначально равен 1,
            // он еще имеет право зарейзить если до него были действия.Если действий до него не было, то и действовать он не может это фолд ту ББ
           if(rounds[pokerPos]==round){
               if(round>1)break;
               else if(!isActionPreflop)break;
           }

           float action = getTurnOfPlayer(pokerPos);
           if(action==-1||action==curActsOrInvests[pokerPos]){ testCurrentHand.setTestStreetTurnsPlayers(PREFLOP,pokerPos,"A:"+action+" ");
               // ситуация когда на блайндах коротыши со стеком равным или меньше блайндов
               if(action==currentHand.startStacksAtStreets[PREFLOP][pokerPos]) {  testSaveImgFrameTimeHand(images_framestimehands,"shortstacksblinds");
                   curActsOrInvests[pokerPos] = -100; amountContPlay--; continue;}
               break;}
           if(action==-10){curActsOrInvests[pokerPos] = -10; amountContPlay--;
           currentHand.preflopActionsStats.get(pokerPos).add(Float.NEGATIVE_INFINITY);
           testCurrentHand.setTestStreetTurnsPlayers(PREFLOP,pokerPos,"F ");

           continue;}

           testCurrentHand.setTestStreetTurnsPlayers(PREFLOP,pokerPos,"A:"+action+" ");


           // запись рейза или кола для статы
            isActionPreflop = true;
           if(action>maxRaise){ maxRaise = action; round++;
            currentHand.preflopActionsStats.get(pokerPos).add(action);
           }
           else currentHand.preflopActionsStats.get(pokerPos).add(-(action-curActsOrInvests[pokerPos]));
            //запись действия для учета
            // ситуация когда размер рейза равне стартовому стеку это значит оллин
            if(action==currentHand.startStacksAtStreets[PREFLOP][pokerPos]) {curActsOrInvests[pokerPos] = -100;
             //currentHand.startStacksAtStreets[FLOP][pokerPos] = 0;
             amountContPlay--; continue;}
                // если нет значит обычный рейз
            else curActsOrInvests[pokerPos] = action;

            currentHand.startStacksAtStreets[FLOP][pokerPos] = currentHand.startStacksAtStreets[PREFLOP][pokerPos]-action;
            rounds[pokerPos] = round;
       }
        posPlayerRound = pokerPos;


        //testCurrentHand.setPreflopActionsStats(currentHand.preflopActionsStats);

    }


    private void finishedActionsAtPreflop(){
        //System.out.println("FINISHED");
        if(currentHand.isFinishedStreets[PREFLOP])return;

        if(curActsOrInvests[currentHand.poker_position_of_hero]==-10){
            currentHand.isFinishedStreets[PREFLOP] = true;
            return;}//ситуация когда херо сфолдил но смотрит продолжение раздачи пока это не будет обрабатыватся

        if(!currentHand.isStartStreets[FLOP]){ // ситуация когда херо не сходил свой раунд значит сфолдил
            if(rounds[currentHand.poker_position_of_hero]<round){currentHand.preflopActionsStats.get(currentHand.poker_position_of_hero).add(Float.NEGATIVE_INFINITY);
                //System.out.println("FOLD");
            }
             // если херо сходил, значит все остальные сфолдили
            else for(int i=0; i<6; i++){
                if(curActsOrInvests[i]==-10||curActsOrInvests[i]==-100||currentHand.poker_position_of_hero==i)continue;
                currentHand.preflopActionsStats.get(i).add(Float.NEGATIVE_INFINITY);

                testCurrentHand.setTestStreetTurnsPlayers(PREFLOP,i,"fin_wns_F ");
            }
            currentHand.isFinishedStreets[PREFLOP] = true;
        }
        else {
            // ситуация чека на бб
            if(rounds[5]==1&&round==1){
            currentHand.preflopActionsStats.get(5).add(Float.POSITIVE_INFINITY);
                currentHand.isFinishedStreets[PREFLOP]=true; if(amountContPlay<2)currentHand.streetAllIn=PREFLOP;

            if(currentHand.streetAllIn==PREFLOP)testCurrentHand.setTestAllines(PREFLOP,"_ALL_PREFIN_chbb");

            return;
            }
            // ситуация когда остаются только два игрока, это автоматом значит что игрок колил
            if(amountContPlay==2){ finishedTwoPlayers(PREFLOP,currentHand.preflopActionsStats);return; }


            int pokerPos = posPlayerRound;
            for(;;pokerPos++){if(pokerPos==6){pokerPos=0;}
                if(curActsOrInvests[pokerPos]==-10||curActsOrInvests[pokerPos]==-100)continue;
                // ход доходит до последнего рейзера, чтобы не оставаться на этой позиции в следующей раз поза проходит дальше
                // это нужно если у игроков не определилось действие из-за помех нужно повторно их проверять в следующем заходе в этот метод
                if(rounds[pokerPos]==round) { pokerPos++;break;}
                finishedActionOnePosition(PREFLOP,pokerPos,currentHand.preflopActionsStats);
            }
            int amountFinished =0;
            for(int p=0; p<6; p++)if(curActsOrInvests[p]==-10||curActsOrInvests[p]==-100||rounds[p]==round)amountFinished++;
            if(amountFinished==6){currentHand.isFinishedStreets[PREFLOP]=true; if(amountContPlay<2)currentHand.streetAllIn=PREFLOP;

                if(currentHand.streetAllIn==PREFLOP)testCurrentHand.setTestAllines(PREFLOP,"_ALL_PREFIN_am>2");
            }

            posPlayerRound = pokerPos;


            testCurrentHand.setTestFinished(amountFinished);
        }

        //testCurrentHand.setPreflopActionsStats(currentHand.preflopActionsStats);
    }

   private void finishedTwoPlayers(int street,List<List<Float>> actionsStats){
       for(int p=0; p<6; p++){if(curActsOrInvests[p]==-10||curActsOrInvests[p]==-100)continue;
           if(!(rounds[p]<round))continue;
           if(maxRaise>=currentHand.startStacksAtStreets[street][p]) {  // если последнйи рейз больше или равен стартовому стеку(инвест+текущий стек) то кол равен остатку стека
               actionsStats.get(p).add(-currentHand.startStacksAtStreets[street+1][p]);
               currentHand.startStacksAtStreets[street+1][p]=0;
               curActsOrInvests[p] = -100;
               amountContPlay--;
               testCurrentHand.setTestStreetTurnsPlayers(street,p,"fin_Am2_C_all ");
           } else {  // если нет значит обычный кол
               actionsStats.get(p).add(-(maxRaise-curActsOrInvests[p]));
               currentHand.startStacksAtStreets[street+1][p]= currentHand.startStacksAtStreets[street][p]-maxRaise;
               curActsOrInvests[p] = maxRaise;
               //TEST
               testCurrentHand.setTestStreetTurnsPlayers(street,p,"fin_Am2_C ");
           }
       }
       currentHand.isFinishedStreets[street]=true; if(amountContPlay<2)currentHand.streetAllIn=street;
       ///TEST
       if(currentHand.streetAllIn==street)testCurrentHand.setTestAllines(street,"_ALL_PREFIN_am2");
   }


   private void finishedActionOnePosition(int street, int pokerPos,List<List<Float>> actionsStats){

       if(is_Fold(pokerPos)){  curActsOrInvests[pokerPos] = -10;  actionsStats.get(pokerPos).add(Float.NEGATIVE_INFINITY);
           amountContPlay--;
           testCurrentHand.setTestStreetTurnsPlayers(street,pokerPos,"fin_fold_F ");
           return; }
       float stack = getOneStack(pokerPos);
       if(stack==-11)return;
       if(stack==-1){  curActsOrInvests[pokerPos] = -10;  actionsStats.get(pokerPos).add(Float.NEGATIVE_INFINITY);
           amountContPlay--;

           testCurrentHand.setTestStreetTurnsPlayers(street,pokerPos,"fin_stack_sit_F ");
       }
       else if(stack==-2){  // ситуация аллина, но это может быть оллин уже на флопе поэтому сравнивается с последним рейзом префлопа
         // если последнйи рейз больше или равен стартовому стеку(инвест+текущий стек) то кол равен остатку стека
           if(maxRaise>=currentHand.startStacksAtStreets[street][pokerPos]) {
               actionsStats.get(pokerPos).add(-currentHand.startStacksAtStreets[street+1][pokerPos]);
               currentHand.startStacksAtStreets[street+1][pokerPos]=0;
               curActsOrInvests[pokerPos] = -100;
               amountContPlay--;

               testCurrentHand.setTestStreetTurnsPlayers(street,pokerPos,"fin_stack_allin_C_all ");
               return;
           } else {// если нет значит обычный кол
               actionsStats.get(pokerPos).add(-(maxRaise-curActsOrInvests[pokerPos]));
               currentHand.startStacksAtStreets[street+1][pokerPos]= currentHand.startStacksAtStreets[street][pokerPos]-maxRaise;
               curActsOrInvests[pokerPos] = maxRaise;

               testCurrentHand.setTestStreetTurnsPlayers(street,pokerPos,"fin_stack_allin_C ");
           }

       }
       else {
           // если стек равен текущему стеку, значит игрок не колил, а сфолдил
           //if(pokerPos==5) System.out.println("stack "+stack+" curstack "+currentStacks[pokerPos]+" maxraise "+maxRaise+" curinvest "+curActsOrInvests[pokerPos]);
           if(stack==currentHand.startStacksAtStreets[street][pokerPos]) {  curActsOrInvests[pokerPos] = -10;
               actionsStats.get(pokerPos).add(Float.NEGATIVE_INFINITY);
               amountContPlay--;

               testCurrentHand.setTestStreetTurnsPlayers(street,pokerPos,"fin_stack_F ");
               return;
           }
           else {
               actionsStats.get(pokerPos).add(-(maxRaise-curActsOrInvests[pokerPos]));
               currentHand.startStacksAtStreets[street+1][pokerPos]= currentHand.startStacksAtStreets[street][pokerPos]-maxRaise;
               curActsOrInvests[pokerPos] = maxRaise;

               testCurrentHand.setTestStreetTurnsPlayers(street,pokerPos,"fin_stack_C ");
           }
       }
       // раунды подтверждаются только у тех кто не фолдил или не оллинер
       rounds[pokerPos] = round;

   }




    private float getTurnOfPlayer(int pokerPos){
        if(is_Fold(pokerPos)){   return -10;}
        float action = getOneAction(pokerPos);
        if(action==0||action==-1)return -1;
        return action;
    }



    void getPostFlopActions(int street){
        List<List<Float>> actionsStats = null;
        if(street==FLOP)actionsStats=currentHand.flopActionsStats;
        assert actionsStats != null;
        int pokerPos = posPlayerRound; int countcycle = 0;
        for(;;pokerPos++){if(pokerPos==6){pokerPos=0;}if(countcycle==6)break;countcycle++;
            if(amountContPlay<2)break;
            if(rounds[pokerPos]>0&&rounds[pokerPos]==round){ pokerPos++; break;}
            if(curActsOrInvests[pokerPos]==-10||curActsOrInvests[pokerPos]==-100)continue;
            //System.out.println(pokerPos);
            float action = getTurnOfPlayer(pokerPos);
            if(action==-1||action==curActsOrInvests[pokerPos]){  testCurrentHand.setTestStreetTurnsPlayers(street,pokerPos,"A:"+action+" "); continue;}
            if(action==-10){curActsOrInvests[pokerPos] = -10; amountContPlay--;  actionsStats.get(pokerPos).add(Float.NEGATIVE_INFINITY);
                testCurrentHand.setTestStreetTurnsPlayers(street,pokerPos,"F "); continue;}

            testCurrentHand.setTestStreetTurnsPlayers(street,pokerPos,"A:"+action+" ");

            if(action>maxRaise){ maxRaise = action; round++;
                if(currentHand.firstBetPostflopPokerPos[street]==-1){
                    currentHand.firstBetPostflopPokerPos[street] = pokerPos;
                    int checkpokerPos = 4;
                    for(;;checkpokerPos++){if(checkpokerPos==6){checkpokerPos=0;}
                        if(curActsOrInvests[checkpokerPos]==-10||curActsOrInvests[checkpokerPos]==-100)continue;
                        if(currentHand.firstBetPostflopPokerPos[street]==checkpokerPos)break;
                        actionsStats.get(checkpokerPos).add(0,Float.POSITIVE_INFINITY);
                    }
                }
                actionsStats.get(pokerPos).add(action);
            }
            else actionsStats.get(pokerPos).add(-(action-curActsOrInvests[pokerPos]));

            if(action==currentHand.startStacksAtStreets[street][pokerPos]) {curActsOrInvests[pokerPos] = -100; amountContPlay--; continue;}
            // если нет значит обычный рейз
            else curActsOrInvests[pokerPos] = action;
            currentHand.startStacksAtStreets[street+1][pokerPos]= currentHand.startStacksAtStreets[street][pokerPos]-action;
            rounds[pokerPos] = round;
        }
        posPlayerRound=pokerPos;
    }


    private void finishedActionsPostflop(int street){
        if(currentHand.isFinishedStreets[street])return;
        List<List<Float>> actionsStats = null;
        if(street==FLOP)actionsStats=currentHand.flopActionsStats;
        assert actionsStats != null;
        if(currentHand.firstBetPostflopPokerPos[street]==-1){ // ситуация одних чеков
            for(int p=0; p<6; p++)if(curActsOrInvests[p]==0)actionsStats.get(p).add(Float.POSITIVE_INFINITY);
            currentHand.isFinishedStreets[street] = true; return;
        }
        //ситуация когда херо сфолдил но смотрит продолжение раздачи пока это не будет обрабатыватся
        if(curActsOrInvests[currentHand.poker_position_of_hero]==-10){ currentHand.isFinishedStreets[street] = true;return;}

        if(!currentHand.isStartStreets[street+1]){ // ситуация когда херо не сходил свой раунд значит сфолдил
            if(rounds[currentHand.poker_position_of_hero]<round){actionsStats.get(currentHand.poker_position_of_hero).add(Float.NEGATIVE_INFINITY);
                //System.out.println("FOLD");
            }
            // если херо сходил, значит все остальные сфолдили
            else for(int p=0; p<6; p++){
                if(curActsOrInvests[p]==-10||curActsOrInvests[p]==-100||currentHand.poker_position_of_hero==p)continue;
                actionsStats.get(p).add(Float.NEGATIVE_INFINITY);

                testCurrentHand.setTestStreetTurnsPlayers(street,p,"fin_wns_F ");
            }
            currentHand.isFinishedStreets[street] = true;
        } // Есть следующая улица
        else {
            if(amountContPlay==2){finishedTwoPlayers(street,actionsStats);return;}

            int countcycle = 0, pokerPos = posPlayerRound;
            for(;;pokerPos++){if(pokerPos==6){pokerPos=0;} countcycle++;if(countcycle==6)break;
                if(amountContPlay<2)break;
                if(rounds[pokerPos]==round){ pokerPos++; break;}
                if(curActsOrInvests[pokerPos]==-10||curActsOrInvests[pokerPos]==-100)continue;
                finishedActionOnePosition(street,pokerPos,actionsStats);
            }

            int amountFinished =0;
            for(int p=0; p<6; p++)if(curActsOrInvests[p]==-10||curActsOrInvests[p]==-100||rounds[p]==round)amountFinished++;
            if(amountFinished==6){currentHand.isFinishedStreets[street]=true; if(amountContPlay<2)currentHand.streetAllIn=street;

                if(currentHand.streetAllIn==street)testCurrentHand.setTestAllines(street,"_ALL_PREFIN_am>2");
            }

            posPlayerRound = pokerPos;


            testCurrentHand.setTestFinished(amountFinished);

        }

    }




    private boolean is_Fold(int poker_position){
        if(poker_position==currentHand.poker_position_of_hero)return false;
        int x = coordsWinMovePlayerAtPos[poker_position][0], j = coordsWinMovePlayerAtPos[poker_position][1], max = 70;
        for(int i=x; i<x+15; i++){ j++; if(get_intGreyColor(frame[0],i,j)>max)return false; }
        return true;
    }



    private void check_StartNewStreetANDreturnIsRIT(int street){

        int xfloprit1 = 318, yfloprit1 = 179, xfloprit2 = 300, yfloprit2 = 200; // bright 150  correct_cards = 46;
        switch (street){
            case  FLOP -> {
                //System.out.println("check_start_flop");
                // проверка что херо не делал ход, кроме когда находится на ББ, где возможен чек, если не делал, то проверки на флоп нет
                if(currentHand.preflopActionsStats.get(currentHand.poker_position_of_hero).size()==1&&currentHand.poker_position_of_hero !=5) return;

                if(get_int_MaxBrightnessMiddleImg(frame[0],xfloprit1,yfloprit1,17,17)>150
                        &&get_int_MaxBrightnessMiddleImg(frame[0],xfloprit2,yfloprit2,17,17)>150){ currentHand.streetAllIn = PREFLOP; currentHand.isStartStreets[FLOP] = true;
                        return;
                }
                //System.out.print("check flop ");
                int x1 = coord_2_3_cards_flop[0][0];
                int x2 = coord_2_3_cards_flop[1][0];
                int y = coord_2_3_cards_flop[0][1];
                if(get_int_MaxBrightnessMiddleImg(frame[0],x1,y,17,17)>190
                        &&get_int_MaxBrightnessMiddleImg(frame[0],x2,y,17,17)>190)currentHand.isStartStreets[FLOP] = true;

            }
            case TURN -> {
                if(get_int_MaxBrightnessMiddleImg(frame[0],364,yfloprit1,17,17)>150
                        &&get_int_MaxBrightnessMiddleImg(frame[0],346,yfloprit2,17,17)>150){ currentHand.streetAllIn = FLOP;  currentHand.isStartStreets[TURN] = true;
                        return;
                }
                if(get_int_MaxBrightnessMiddleImg(frame[0],347,168,15,10)>175
                        &&get_int_MaxBrightnessMiddleImg(frame[0],363,212,15,10)>175)currentHand.isStartStreets[TURN] = true;

                /*System.out.println("TURN "+currentHand.is_start_turn+"  "+get_int_MaxBrightnessMiddleImg(frame[0],347,168,15,10)+"  "
                +get_int_MaxBrightnessMiddleImg(frame[0],363,212,15,10));*/
            }

            case RIVER -> {
                if(get_int_MaxBrightnessMiddleImg(frame[0],410,yfloprit1,17,17)>150
                        &&get_int_MaxBrightnessMiddleImg(frame[0],392,yfloprit2,17,17)>150){ currentHand.streetAllIn = TURN; currentHand.isStartStreets[RIVER] = true;
                        return;
                }
                if(get_int_MaxBrightnessMiddleImg(frame[0],392,168,15,10)>175
                        &&get_int_MaxBrightnessMiddleImg(frame[0],408,212,15,10)>175)currentHand.isStartStreets[RIVER] = true;
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



    public synchronized void stop(){ is_run = false;notify();
    }

}
