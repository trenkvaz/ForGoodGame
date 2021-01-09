package org.trenkvaz.main;


//import org.bytedeco.javacpp.opencv_core.IplImage;
import javafx.scene.paint.Color;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/*import static org.bytedeco.javacpp.avformat.avformat_alloc_context;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;*/
//import static org.bytedeco.javacpp.opencv_videoio.cvCreateFileCapture;
import org.bytedeco.ffmpeg.global.avutil;
import org.trenkvaz.stats.MainStats;

//import static org.trenkvaz.database_hands.Work_DataBase.main_array_of_stats;
import static org.trenkvaz.main.CaptureVideo.Settings.get_StatsFromDataBase;
import static org.trenkvaz.ui.Controller_main_window.*;
import static org.trenkvaz.ui.StartAppLauncher.*;
import static org.trenkvaz.main.OcrUtils.*;

public class CaptureVideo {

   static final int[][] COORDS_NICKS = {{298,320},{15,253},{15,120},{264,67},{543,120},{543,253}};
   public static final int[][] COORDS_TABLES = {{0,0},{640,0},{1280,0},{0,469},{640,469},{1280,469}};
   static final int[][] COORDS_BUTTONS = {{382,287},{144,231},{156,133},{242,104},{459,133},{473,231}};
   static final int[][] COORDS_CARDS_HERO = {{287,286},{331,286}};
   static final int[][] COORDS_ACTIONS =  //{{302-25,267},{151,256},{118,175},{323-15,120},{459-15,175},{436-15,256}};
           {{277,267},{151,256},{118,175},{313,120},{444,175},{416,256}};
   static final int[][] coord_2_3_cards_flop = {{270,202},{318,202}};
   static final int[] CORRECTS_COORDS_NICKS = {1,2,2,2,1,1};
   static final int[][] COORDS_EMPTY_PLACES = {null,{40,258},{40,133},{279,72},{518,133},{518,258}};
   static final String[] NOMINALS_CARDS = {"2","3","4","5","6","7","8","9","T","J","Q","K","A"};
   public static final String[] DECK = {null,"Ac","Ad","Ah","As","Kc","Kd","Kh","Ks","Qc","Qd","Qh","Qs","Jc","Jd","Jh","Js","Tc","Td","Th","Ts","9c","9d","9h","9s","8c","8d","8h","8s",
            "7c","7d","7h","7s","6c","6d","6h","6s","5c","5d","5h","5s","4c","4d","4h","4s","3c","3d","3h","3s","2c","2d","2h","2s"};
   public static final String NICK_HERO = "trenkvaz";
   public static List<OCR> ocrList_1;
   static final int COUNT_TABLES = 6;
   static FFmpegFrameGrabber grabber;
   static CanvasFrame canvasFrame;


   static final UseTesseract[] use_tessearts = new UseTesseract[4];
   public static byte[] count_one_in_numbers;
   static HashMap<Long,String> hashmap_id_img_pix_nick = new HashMap<>();
   static SortedMap<Long,long[]> sortedmap_all_imgs_pix_of_nicks = new TreeMap<>();
   static long[][] _long_arr_cards_for_compare,shablons_text_sittingout_allin, shablon_text_poker_terms;
   static int[][] shablons_numbers_0_9_for_stacks, shablons_numbers_0_9_for_actions;
   public static ConcurrentHashMap[] current_map_stats;
   public static MainStats[] work_main_stats;
   public static boolean let_SaveTempHandsAndCountStatsCurrentGame = false;
   public static CaptureVideo.StartStopCapture startStopCapture;
   public record FrameTable(BufferedImage tableImg,boolean[] metaDates,int[] whoPlayOrNo){}


   public CaptureVideo(){
       for(int i=0; i<4; i++)use_tessearts[i] = new UseTesseract();
       //settings_capturevideo = new Settings();
       Settings.setting_capture_video();
       current_map_stats = get_StatsFromDataBase();
       if(isTest){
       System.out.println(RED+"START TEST NICKS IMG");
       System.out.println(RESET);}
      /* map_idplayers_nicks = work_dataBase.get_map_IdPlayersNicks();
       if(!map_idplayers_nicks.isEmpty()) id_for_nick = Collections.max(map_idplayers_nicks.values());*/
       //System.out.println("id_for_nick "+id_for_nick);
       /*canvasFrame = new CanvasFrame("Some Title");
       canvasFrame.setCanvasSize(600, 300);//задаем размер окна
       canvasFrame.setBounds(100,100,600,300);*/
       avutil.av_log_set_level (avutil.AV_LOG_ERROR);
   }



    public CaptureVideo(String a){}


   static boolean is_getting_frame = false;

   public class StartStopCapture implements Runnable{
       boolean is_run = true;


       public StartStopCapture(){
           ocrList_1 = new ArrayList<>();
           for(int i=0; i<COUNT_TABLES; i++){
               ocrList_1.add(new OCR(i));
           }
           new Thread(this).start();
       }

       public void run(){

           while (is_run){
               System.out.println("START CAPTURE");
               grabber = connect_stream();
               if(grabber!=null){
                   screen2(grabber);
               }
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
       }


       public synchronized  void stop_tread(){

           is_run = false;
           is_getting_frame = false;
           //canvasFrame.dispose();

       }

       public synchronized void removeOcrInOcrList_1(int table){
           ocrList_1.set(table,null);
       }
   }

   static FFmpegFrameGrabber connect_stream(){

        //FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(System.getProperty("user.dir")+"\\test_video9.avi");
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("rtmp://127.0.0.1/live/test");
       // FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("udp://192.168.0.129:1234");

        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            return null;
        }
        return grabber;
    }

   static void screen2(FFmpegFrameGrabber grabber){
       Frame frame = null;
       is_getting_frame = true;
       try {
           System.out.println(grabber.getFrameRate());
           System.out.println("start");
           controller_main_window.setMessage_work("Start capture", Color.GREEN);

           while(is_getting_frame){
               frame = grabber.grabImage();
               if(frame!=null){
                   //canvasFrame.showImage(frame);
                   //find_tables(frame);
                   allocationTables(frame);
               }
               else {System.out.println("null frame"); break; }
           }
       grabber.release();
       } catch (FrameGrabber.Exception e) {
           e.printStackTrace();
       }
       controller_main_window.setMessage_work("Stop capture",Color.RED);
       System.out.println("stop");
       //canvasFrame.dispose();
   }



   static synchronized String set_get_nicks_in_hashmap(long id_img_pix,String name){
       if(name==null) return hashmap_id_img_pix_nick.get(id_img_pix);
       try{
           hashmap_id_img_pix_nick.put(id_img_pix,name);
       } catch (IllegalArgumentException a){
           Settings.ErrorLog("error name "+name+" hash "+id_img_pix);
       }

       return null;
   }
    public static long testTime = 0;
    public static int testTimecount = 0;

    static synchronized long get_number_img_nicks(long[] img_nick_for_compare,int privat_error){
        // img_nick_for_compare 15 чисел изо, 16-у количество черных пикселей
        // умножается на миллион, чтобы получить индексы в сортируемом мепе, по ним будет отбираться диапазон по количеству черных пикселей
        long s = System.nanoTime();
        long count_pix_in_ = img_nick_for_compare[15]*1_000_000;
        int general_error = 15;
        long min = count_pix_in_-general_error*1_000_000, max = count_pix_in_+(general_error+1)*1_000_000;

        Map<Long,long[]> submap_imgs_with_min_error = sortedmap_all_imgs_pix_of_nicks.subMap(min,max);

        List<long[]> equal_imgs = new ArrayList<>(); int first_of_pair_error = 0, second_of_pair_error = 0;
        int total_error = 0, min_error = 500;
        long[] img_with_min_error = null;
       out: for(long[] img_min_error:submap_imgs_with_min_error.values()){
           total_error = 0;first_of_pair_error = 0; second_of_pair_error = 0;
            //boolean is_equal = true;
            for(int i=0; i<15; i++){
                /*count_error_in_compare+= get_count_one_in_numbers(img_min_error[i]^img_nick_for_compare[i]);
                if(count_error_in_compare>privat_error){is_equal = false; break;}*/
                if(i%2==0)first_of_pair_error = Long.bitCount(img_min_error[i]^img_nick_for_compare[i]);
                        //get_AmountOneBitInLong(img_min_error[i]^img_nick_for_compare[i]);
                if(i%2!=0)second_of_pair_error = Long.bitCount(img_min_error[i]^img_nick_for_compare[i]);
                        //get_AmountOneBitInLong(img_min_error[i]^img_nick_for_compare[i]);
                int local_error = first_of_pair_error+second_of_pair_error;
                if(i>0&&local_error>privat_error){ continue out;  }
                else total_error+=local_error;
            }
            //if(!is_equal)continue;
           if(total_error<min_error){
               min_error = total_error;
               img_with_min_error = img_min_error;
           }
            //equal_imgs.add(img_min_error);
        }

       /* System.out.println("**********************************************************************");
        System.out.println("min "+min+" max "+max+" count_pix "+count_pix_in_);
        for(Map.Entry<Long,long[]> entry:submap_imgs_with_min_error.entrySet())
            System.out.println("count_in_sub   "+entry.getKey());
        System.out.println("equl_img size "+equal_imgs.size());
        System.out.println("**********************************************************************");*/
        testTime+=(System.nanoTime()-s);
        testTimecount++;
       // если не нашлось в мепе такого же изо, то создается новый ИД для изо и записывается на место количества черных пикселей
        if(img_with_min_error==null){//long id_img_pix = get_HandTime();
            // проверка наличия изо с таким же количеством пикселей и индексом если есть то добавляется единица и снова проверяется, пока такого индекса не будет в списке,
            // тогда он присваевается новому изо
            boolean is_contain = true;
            while (is_contain){
                is_contain = submap_imgs_with_min_error.containsKey(count_pix_in_);
                if(is_contain){count_pix_in_++; //System.out.println("is_countain "+is_contain+" count "+count_pix_in_);
                }
                else {
                    //System.out.println("id record "+img_nick_for_compare[15]);
                    img_nick_for_compare[15]= count_pix_in_;
                    sortedmap_all_imgs_pix_of_nicks.put(count_pix_in_,img_nick_for_compare); break; }
            }

            return -count_pix_in_;
        }

        //for(long r:equal_imgs.get(0)) System.out.println(r);

        // если нашлось похожее изо, то берется его ИД на вывод


        return img_with_min_error[15];
    }





    static long last_hand_time = 0;

    static synchronized long get_HandTime(){
       // long time = start_world_time+((System.nanoTime()-start_nano_timer)%100_000/100);
        long time = System.currentTimeMillis();
        if(last_hand_time==time)time+=1;
        last_hand_time = time;
        return time;
    }


   boolean save = false;
   static int c =0;


   static BufferedImage bufferedImageframe;



   static void find_tables(Frame frame){


       if(bufferedImageframe==null)
        bufferedImageframe = new Java2DFrameConverter().getBufferedImage(frame);

       //System.out.println((System.currentTimeMillis()-start));
        createBufferedImage(frame, bufferedImageframe);
       BufferedImage image_number_hand = null;
       int x_of_number_hand = 579,y_of_number_hand = 56,width_of_number_hand = 53,height_of_number_hand = 11, width_nick = 91, height_nick = 14;
       boolean is_correct_number_hand = false, is_correct_nicks = false, is_correct_hero_nick = false;


       long s =System.currentTimeMillis();
       for(int index_table=0; index_table<COUNT_TABLES; index_table++){
          if(ocrList_1.get(index_table)==null)continue;
          /*if(!isFastTable(index_table)){
              Testing.saveImageToFile(cut_SubImage(bufferedImageframe,coord_left_up_of_tables[index_table][0],coord_left_up_of_tables[index_table][1],639,468)
                      ,"test2\\win_"+index_table+"_"+(c++));
          } else Testing.saveImageToFile(cut_SubImage(bufferedImageframe,coord_left_up_of_tables[index_table][0],coord_left_up_of_tables[index_table][1],639,468)
                  ,"test3\\win_"+index_table+"_"+(c++));*/
           if(!isFastTable(index_table))continue;
           if(!isCardsHero(index_table))continue;
           /*if(!isCardsHero(index_table))Testing.saveImageToFile(cut_SubImage(bufferedImageframe,coord_left_up_of_tables[index_table][0],coord_left_up_of_tables[index_table][1],639,468)
                  ,"test4\\win_"+index_table+"_"+(c++));*/
           /*else Testing.saveImageToFile(cut_SubImage(bufferedImageframe,coord_left_up_of_tables[index_table][0],coord_left_up_of_tables[index_table][1],639,468)
               ,"test5\\win_"+index_table+"_"+(c++));*/
           getWhoPlayOrNo(index_table);
          //long s = System.currentTimeMillis();
            //  проверка правильности изо номера раздачи
           //checknicktest_nick = new ArrayList<>();
           //checknicktest_nick.add("---------------------------------------------KURSOR");
           is_correct_number_hand = is_CorrectImageOfNumberHandAndNicks(COORDS_TABLES[index_table][0]+x_of_number_hand,
                   COORDS_TABLES[index_table][1]+y_of_number_hand,width_of_number_hand,height_of_number_hand,100,100,100,bufferedImageframe);
           //c++;
           //if(index_table==0){save_image(is_correct_number_hand,"tables_img\\t_"+(c)+"_"+(is_correct_number_hand!=null));}
           //c++;
           //checknicktest_nick.add("---------------------------------------------KURSOR   "+is_correct_number_hand+"  TABLE "+index_table);
           image_number_hand = null;
           is_correct_hero_nick = false;
           if(is_correct_number_hand){
               image_number_hand = cut_SubImage(bufferedImageframe, COORDS_TABLES[index_table][0]+x_of_number_hand+25,
                       COORDS_TABLES[index_table][1]+y_of_number_hand+3,26,5);


               // проверка правильности изо ников
               is_correct_nicks = true;

              for(int img_nicks=0; img_nicks<6; img_nicks++ ){
                  int x_of_nick = COORDS_TABLES[index_table][0]+ COORDS_NICKS[img_nicks][0]+ CORRECTS_COORDS_NICKS[img_nicks]-8;
                  int y_of_nick = COORDS_TABLES[index_table][1]+ COORDS_NICKS[img_nicks][1]+1;
                  //checknicktest_nick.add("++++++++++++++++++++++++++++++++++++"+img_nicks);
                  if(is_CorrectImageOfNumberHandAndNicks(x_of_nick,y_of_nick,width_nick,height_nick,
                          220,220,210,bufferedImageframe))
                  { if(img_nicks==0){is_correct_hero_nick = true;} continue; }
                  //if(index_table==4)
             /*      c++;
                      save_image(ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no
                              (bufferedImageframe.getSubimage(x_of_nick,y_of_nick,width_nick,height_nick),true),35),"test3\\"+(c)+"_a_"+img_nicks);
                 *//* save_image(ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no
                          (bufferedImageframe.getSubimage(x_of_nick,y_of_nick,width_nick,height_nick),true),105),"test3\\"+(c)+"_b_"+img_nicks);*//*
                  save_image(bufferedImageframe.getSubimage(x_of_nick,y_of_nick,width_nick,height_nick),"test3\\"+(c)+"_b_"+img_nicks);*/
                  is_correct_nicks = false;
                  break;
              }
               //System.out.println("time "+(System.currentTimeMillis()-s));
              if(is_correct_nicks){
                  //System.out.println("is_correct_nicks");
                  /*checknicktest_table.put(c,checknicktest_nick);
                  if(index_table==0)save_image(bufferedImageframe,"tables_img\\_");*/
                  /*if(!is_start_tables[index_table]){
                      image_number_hand = cut_SubImage(bufferedImageframe,coord_left_up_of_tables[index_table][0]+x_of_number_hand+25,
                              coord_left_up_of_tables[index_table][1]+y_of_number_hand+3,26,5);
                      is_start_tables[index_table] = true;
                      is_end_tables[index_table] = false;

                  } else image_number_hand = null;*/

                 /* if(index_table==2)
                      if(image_number_hand==null) System.out.println("hand null is_start "+is_start_tables[index_table]);
                      else System.out.println("hand start is_start "+is_start_tables[index_table]);*/
                   ocrList_1.get(index_table).set_image_for_ocr(
                           new BufferedImage[]{
                                   cut_SubImage(bufferedImageframe, COORDS_TABLES[index_table][0], COORDS_TABLES[index_table][1],639,468),
                                   image_number_hand
                           });
               }
              else {
                  // если все ники не определяются или определяется только ник героя, нужно для завершения последней раздачи
                  if(!is_correct_hero_nick)ocrList_1.get(index_table).set_image_for_ocr(new BufferedImage[]{null, image_number_hand});
                  else ocrList_1.get(index_table).set_image_for_ocr(new BufferedImage[]{image_number_hand,null });

                  /*Testing.saveImageToFile(cut_SubImage(bufferedImageframe,coord_left_up_of_tables[index_table][0],
                          coord_left_up_of_tables[index_table][1],639,468),"test2\\win_"+index_table+"_"+(c++));*/
              }

           }
           else ocrList_1.get(index_table).set_image_for_ocr(new BufferedImage[]{null, null});
           //if(is_correct_number_hand==null) save_image(bufferedImageframe.getSubimage(coord_left_up_of_tables[index_table][0],coord_left_up_of_tables[index_table][1],639,468),"tables_img\\t_nokurs"+(++c));
           //if(is_correct_number_hand==null)countcheks[index_table]++;
       }
     alltime+=System.currentTimeMillis()-s; counttime++;
   }
   public static long alltime = 0;
   public static int counttime = 0;




   static void allocationTables(Frame frame){
       if(bufferedImageframe==null) bufferedImageframe = new Java2DFrameConverter().getBufferedImage(frame);
       createBufferedImage(frame, bufferedImageframe);
       boolean[] metaDates = null; // есть стол, есть раздача, есть помехи
       int[] whoPlayOrNo = null;
       for(int indTable=0; indTable<COUNT_TABLES; indTable++){
           if(ocrList_1.get(indTable)==null)continue;
           metaDates = new boolean[4];

           if(!isFastTable(indTable)){ ocrList_1.get(indTable).addFrameTableToQueue(new FrameTable(null,metaDates,null));
               //System.out.println("NO TABLE");
           continue;}
           metaDates[0] = true;
           if(!isCardsHero(indTable)){ ocrList_1.get(indTable).addFrameTableToQueue(new FrameTable(null,metaDates,null));
               //System.out.println("NO CARD");
           continue;}

           whoPlayOrNo = getWhoPlayOrNo(indTable);
           if(whoPlayOrNo==null){ metaDates[2] = true; ocrList_1.get(indTable).addFrameTableToQueue(new FrameTable(null,metaDates,null));
               //System.out.println("WHOPLAY NULL");
           continue;}
           // первый индекс показывает что есть только ник херо если 0
           if(whoPlayOrNo[0]==0){ ocrList_1.get(indTable).addFrameTableToQueue(new FrameTable(null,metaDates,null));
               //System.out.println("WHOPLAY 0");
           continue;}
           metaDates[1] = true;
           ocrList_1.get(indTable).addFrameTableToQueue(new FrameTable(cutImageTable(indTable),metaDates,whoPlayOrNo));

       }

   }




    private static boolean isFastTable(int indTable){
       // проверка буквы Ф в названии стола Фаст
        if(get_longarr_HashImage(bufferedImageframe, COORDS_TABLES[indTable][0]+25,
                COORDS_TABLES[indTable][1]+9,6,10,1,200)[0] !=8976692374933504L)return false;
        // проверка номера раздачи
        if(!is_CorrectImageOfNumberHandAndNicks(COORDS_TABLES[indTable][0]+579, COORDS_TABLES[indTable][1]+56,53,11,
                100,100,100,bufferedImageframe))return false;
        // проверка чтения ника херо
        return is_CorrectImageOfNumberHandAndNicks(COORDS_TABLES[indTable][0]+291, COORDS_TABLES[indTable][1]+321,91,14,
                220,220,210,bufferedImageframe);
    }


    private static boolean isCardsHero(int indTable){
        for(int i=0; i<2; i++){
            int X = COORDS_CARDS_HERO[i][0]+ COORDS_TABLES[indTable][0];
            int Y = COORDS_CARDS_HERO[i][1]+ COORDS_TABLES[indTable][1];
            if(get_int_MaxBrightnessMiddleImg(bufferedImageframe,X+1,Y,15,17)<220||
                    get_int_MaxBrightnessMiddleImg(bufferedImageframe,X+1,Y+17,15,17)<220)return false;
            // проверка периметра карта на помеху курсором
            if(!is_noCursorInterferenceImage(bufferedImageframe,X+1,Y,15,17,240))return false;
        }
       return true;
    }



    private static int[] getWhoPlayOrNo(int indTable){
        int[] result = new int[6];c++;
        for(int placePlayer=1; placePlayer<6; placePlayer++ ){
            if(is_CorrectImageOfNumberHandAndNicks(COORDS_TABLES[indTable][0]+COORDS_NICKS[placePlayer][0]+CORRECTS_COORDS_NICKS[placePlayer]-8,
                    COORDS_TABLES[indTable][1]+COORDS_NICKS[placePlayer][1]+1,91,14,
                    220,220,210,bufferedImageframe)) {  result[0] =1; result[placePlayer] =1; continue;}
            else if(isEmptyPlace(indTable,placePlayer))continue;
            return null;
        }
       return result;
    }


    private static boolean isEmptyPlace(int indTable,int placePlayer){
        final int X = COORDS_TABLES[indTable][0]+ COORDS_EMPTY_PLACES[placePlayer][0],
                Y = COORDS_TABLES[indTable][1]+ COORDS_EMPTY_PLACES[placePlayer][1], W = X+82, H = Y+28, max = 110, min = 25;
        for(int y=Y; y<H; y+=27)
            for(int x=X+4; x<W-4; x++){ int bright = get_intGreyColor(bufferedImageframe,x,y);if(bright>max||bright<min)return false; }
        for(int x=X; x<W; x+=81)
            for(int y=Y+4; y<H-4; y++){ int bright = get_intGreyColor(bufferedImageframe,x,y);if(bright>max||bright<min)return false; }
        return true;
    }


   private static BufferedImage cutImageTable(int indTable){
       BufferedImage img = bufferedImageframe.getSubimage(COORDS_TABLES[indTable][0],  COORDS_TABLES[indTable][1],639,468);
       BufferedImage cut_subimage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
       Graphics g = cut_subimage.createGraphics();
       g.drawImage(img, 0, 0, null);
       return cut_subimage;
   }


   public static BufferedImage cut_SubImage(BufferedImage image_window,int X, int Y, int W, int H){
       BufferedImage img = image_window.getSubimage(X, Y, W, H);
       BufferedImage cut_subimage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
       Graphics g = cut_subimage.createGraphics();
       g.drawImage(img, 0, 0, null);
       return cut_subimage;
   }


    private static BufferedImage createBufferedImage(Frame frame, BufferedImage image) {
            ByteBuffer buffer = (ByteBuffer) frame.image[0].position(0);
            WritableRaster wr = image.getRaster();
            byte[] bufferPixels = ((DataBufferByte) wr.getDataBuffer()).getData();
            buffer.get(bufferPixels);
            return image;
    }




  public static boolean is_CorrectImageOfNumberHandAndNicks(int X, int Y, int w, int h, int brightness_of_perimeter_up_down,int brightness_of_perimeter_left_right,
                                                            int max_brightness_of_text, BufferedImage frame){
       //save_image(frame.getSubimage(X,Y,w,h),"tables_img\\t_"+(c));
        // вверхние и нижние линии периметра верхняя линия вся, поэтому мув1 и мув2 = 0, нижняя линия ограничена с начала и с конца на 3 и 2 пикселя соотвественно
            int p =0, move1 = 0, move2 = 0;
            for(int y=Y; y<h+Y; y+=h-1){ p++;
                if(p==2) { move1 = 3; move2 = 2; }
                for(int x=X+move1; x<w+X-move2; x++){
                //System.out.println("1 grey "+get_intGreyColor(frame,x,y));
                //checknicktest_nick.add("1 "+grey);
                if(get_intGreyColor(frame,x,y)>brightness_of_perimeter_up_down)return false;
            }
        }
        // правые и левые линии периметра ограничены на 10 пикселй снизу
            for(int x=X; x<w+X; x+=w-1)
                for(int y=Y; y<h+Y-10; y++){
                    //System.out.println("2 grey "+get_intGreyColor(frame,x,y));
                //checknicktest_nick.add("2 "+grey);
                if(get_intGreyColor(frame,x,y)>brightness_of_perimeter_left_right)return false;
            }
      //System.out.println("=========================================");
       // проверка яркости текста
        int max = 0, y = Y+h/2;
        for(int x=X; x<w+X; x++){
            int grey = get_intGreyColor(frame,x,y);
            if(grey>max)max=grey;
        }
      //System.out.println("max "+max);
        //checknicktest_nick.add("max "+max);

        return max >= max_brightness_of_text;

        //return frame.getSubimage(X+25,Y+3,26,5);
    }







    public static class Settings {

        private static File file_with_nicks;

        public static void setting_capture_video(){
            read_file_with_nicks_and_img_pixs();
            _long_arr_cards_for_compare = read_ObjectFromFile("_long_arr_cards_for_compare");
            shablons_numbers_0_9_for_stacks = read_ObjectFromFile("shablons_numbers_0_9");
            count_one_in_numbers = read_ObjectFromFile("count_one_in_numbers");
            shablons_numbers_0_9_for_actions = read_ObjectFromFile("shablons_numbers_0_9_for_actions");
            shablons_text_sittingout_allin = read_ObjectFromFile("shablons_text_sittingout_allin");
            shablon_text_poker_terms = read_ObjectFromFile("shablon_text_poker_terms");
        }

        public static void read_file_with_nicks_and_img_pixs(){
            String testnicks = "";
            if(isTest)testnicks = "_test";
            file_with_nicks = new File(home_folder+"\\all_settings\\capture_video\\nicks_img"+testnicks+".txt");
            if(!file_with_nicks.isFile())return;
            try {
                BufferedReader br = new BufferedReader(new FileReader(file_with_nicks));
                String line;
                while ((line = br.readLine()) != null) {
                    if(!(line.startsWith("*")&&line.endsWith("*")))break;
                    String[] arr_line = line.substring(1,line.length()-1).split("%");
                    //System.out.println("line "+arr_line.length);
                    hashmap_id_img_pix_nick.put(Long.parseLong(arr_line[16]),arr_line[0]);
                    long[] img_pix = new long[16];
                    for(int i=1; i<17; i++){
                        img_pix[i-1] = Long.parseLong(arr_line[i]);
                    }
                    sortedmap_all_imgs_pix_of_nicks.put(img_pix[15],img_pix);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        public static <T> T read_ObjectFromFile(String name_file){
            T type = null;
            try {	FileInputStream file=new FileInputStream(home_folder+"\\all_settings\\capture_video\\"+name_file+".file");
                ObjectInput out = new ObjectInputStream(file);
                type = (T) out.readObject();
                out.close();
                file.close();
            } catch(IOException e) {
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return type;
        }

        static ConcurrentHashMap[] get_StatsFromDataBase(){


            work_main_stats = work_dataBase.fill_MainArrayOfStatsFromDateBase("work_nicks_stats");
            ConcurrentHashMap[] result = new ConcurrentHashMap[work_main_stats.length];
            for(int i = 0; i< work_main_stats.length; i++)
                result[i] = new ConcurrentHashMap<> (work_main_stats[i].getMap_of_Idplayer_stats());


            return result;
        }

        public static synchronized void write_nicks_keys_img_pix(String nick,long[] imgs_pix_of_nick){
            StringBuilder line = new StringBuilder("*");
            line.append(nick);line.append('%');
            for(long pixs:imgs_pix_of_nick){
                line.append(pixs);
                line.append('%');
            }
            line.deleteCharAt(line.length()-1);
            line.append("*\r\n");

            try (OutputStream os = new FileOutputStream(file_with_nicks,true)) {
                os.write(line.toString().getBytes(StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
            } catch (IOException s) {
            }
        }



        public static synchronized void ErrorLog(String er){
            boolean estfile = false; String error = ""; boolean zapis = false;
            try {
                Date d = new Date();
                DateFormat formatter= new SimpleDateFormat("yyyy_MM_dd HH.mm.ss");
                String Z = formatter.format(d);
                error = Z+" "+er+"\r\n";
                do {
                    File homedir = new File(home_folder);
                    for (File myFile : new File(homedir.getPath()).listFiles())
                        if (myFile.isFile()) {
                            if (myFile.getName().startsWith("logerror")) {
                                estfile = true;
                                OutputStream os = new FileOutputStream(myFile.getPath(), true);
                                os.write(error.getBytes(StandardCharsets.UTF_8));
                                zapis = true;
                            }
                        }
                    if (!estfile) {
                        String adressfileloger = homedir.getPath() + "\\" + "logerror.txt";
                        File config = new File(adressfileloger);
                        config.createNewFile();
                    }
                } while (!zapis);
            } catch (NullPointerException ex){
                System.out.println(ex);
            } catch (IOException e) {
                System.out.println(e);
            }

        }


    }




























    public static void main(String[] args) throws IOException, InterruptedException {
       /*CaptureVideo captureVideo = new CaptureVideo(1);
       //captureVideo.test_cap_video();
       for(OCR ocr:captureVideo.ocrList_1)ocr.stop();
        //for(OCR ocr:captureVideo.ocrList_2)ocr.stop();
       System.out.println("stop");

       //test_cam2();
       for(Map.Entry<Integer,String> entry:hashcodes_nicks_hashmap.entrySet())
           System.out.println(entry.getValue()+"    "+entry.getKey());*/





        
    }

}
