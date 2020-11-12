package org.trenkvaz.main;


//import org.bytedeco.javacpp.opencv_core.IplImage;
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


/*import static org.bytedeco.javacpp.avformat.avformat_alloc_context;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;*/
//import static org.bytedeco.javacpp.opencv_videoio.cvCreateFileCapture;
import org.bytedeco.ffmpeg.global.avutil;

import static org.trenkvaz.main.OCR.get_intGreyColor;
import static org.trenkvaz.ui.Controller_main_window.*;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;
import static org.trenkvaz.ui.StartAppLauncher.work_dataBase;

public class CaptureVideo implements Runnable{

   static final int[][] coords_places_of_nicks = {{298,320},{15,253},{15,120},{264,67},{543,120},{543,253}};

   public static final int[][] coord_left_up_of_tables = {{0,0},{640,0},{1280,0},{0,469},{640,469},{1280,469}};

   static final int[][] coords_buttons = {{382,287},{144,231},{156,133},{242,104},{459,133},{473,231}};

   static final int[][] coords_cards_hero = {{287,286},{331,286}};

   static final int[][] coords_actions = {{302-25,267},{151,256},{118,175},{323-15,120},{459-15,175},{436-15,256}};

   static final int[][] coord_2_3_cards_flop = {{270,202},{318,202}};

   static final String[] nominals_cards = {"2","3","4","5","6","7","8","9","T","J","Q","K","A"};

   public static final String[] Deck = {null,"Ac","Ad","Ah","As","Kc","Kd","Kh","Ks","Qc","Qd","Qh","Qs","Jc","Jd","Jh","Js","Tc","Td","Th","Ts","9c","9d","9h","9s","8c","8d","8h","8s",
            "7c","7d","7h","7s","6c","6d","6h","6s","5c","5d","5h","5s","4c","4d","4h","4s","3c","3d","3h","3s","2c","2d","2h","2s"};

   public static final String nick_hero = "trenkvaz";




   public List<OCR> ocrList_1;
   final int COUNT_TABLES = 6;
   boolean is_run = true;
   Thread thread;
   boolean is_getting_frame = true;
   FFmpegFrameGrabber grabber;
   CanvasFrame canvasFrame;


   static final UseTesseract[] use_tessearts = new UseTesseract[4];

   static byte[] count_one_in_numbers;
   static HashMap<Long,String> hashmap_id_img_pix_nick = new HashMap<>();
   static SortedMap<Long,long[]> sortedmap_all_imgs_pix_of_nicks = new TreeMap<>();
   static long[][] _long_arr_cards_for_compare,shablons_text_sittingout_allin, shablon_text_poker_terms;
   static int[][] shablons_numbers_0_9_for_stacks, shablons_numbers_0_9_for_actions;



   static Map<String, Integer> map_idplayers_nicks;
   static int id_for_nick = 0;
   public record IdPlayer_Nick(int idplayer,String nick){}



   public CaptureVideo(){
       for(int i=0; i<4; i++)use_tessearts[i] = new UseTesseract();
       //settings_capturevideo = new Settings();
       Settings.setting_cupture_video();
       map_idplayers_nicks = work_dataBase.get_map_IdPlayersNicks();
       if(!map_idplayers_nicks.isEmpty()) id_for_nick = Collections.max(map_idplayers_nicks.values());
       //System.out.println("id_for_nick "+id_for_nick);
       canvasFrame = new CanvasFrame("Some Title");
       canvasFrame.setCanvasSize(600, 300);//задаем размер окна
       canvasFrame.setBounds(100,100,600,300);
   }



   public void start_thread(){
       ocrList_1 = new ArrayList<>();
       for(int i=0; i<COUNT_TABLES; i++){
           ocrList_1.add(new OCR(i));
       }
       thread = new Thread(this);
       thread.start();
   }


   public CaptureVideo(String a){}

   public void run(){

       avutil.av_log_set_level (avutil.AV_LOG_ERROR);
       while (is_run){
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

   }

    FFmpegFrameGrabber connect_stream(){

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

   void screen2(FFmpegFrameGrabber grabber){
       Frame frame = null;
       is_getting_frame = true;
       try {
           System.out.println(grabber.getFrameRate());
           System.out.println("start");
           controller_main_window.setMessage_work("start");

           while(is_getting_frame){
               frame = grabber.grabImage();
               if(frame!=null){
                   //canvasFrame.showImage(frame);
                   find_tables(frame);
               }
               else {System.out.println("null frame"); break; }
           }
       grabber.release();
       } catch (FrameGrabber.Exception e) {
           e.printStackTrace();
       }
       controller_main_window.setMessage_work("stop");
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


    static synchronized long[] get_number_img_nicks(long[] img_nick_for_compare,int privat_error){
        // img_nick_for_compare 15 чисел изо, 16-у количество черных пикселей
        // умножается на миллион, чтобы получить индексы в сортируемом мепе, по ним будет отбираться диапазон по количеству черных пикселей

        long count_pix_in_ = img_nick_for_compare[15]*1_000_000;
        int general_error = 15;
        long min = count_pix_in_-general_error*1_000_000, max = count_pix_in_+(general_error+1)*1_000_000;

        Map<Long,long[]> submap_imgs_with_min_error = sortedmap_all_imgs_pix_of_nicks.subMap(min,max);

        List<long[]> equal_imgs = new ArrayList<>(); int first_of_pair_error = 0, second_of_pair_error = 0;
       out: for(long[] img_min_error:submap_imgs_with_min_error.values()){

            //boolean is_equal = true;
            for(int i=0; i<15; i++){
                /*count_error_in_compare+= get_count_one_in_numbers(img_min_error[i]^img_nick_for_compare[i]);
                if(count_error_in_compare>privat_error){is_equal = false; break;}*/
                if(i%2==0)first_of_pair_error = get_AmountOneBitInLong(img_min_error[i]^img_nick_for_compare[i]);
                if(i%2!=0)second_of_pair_error = get_AmountOneBitInLong(img_min_error[i]^img_nick_for_compare[i]);
                if(i>0&&(first_of_pair_error+second_of_pair_error)>privat_error){ continue out;  }
            }
            //if(!is_equal)continue;
            equal_imgs.add(img_min_error);
        }

       /* System.out.println("**********************************************************************");
        System.out.println("min "+min+" max "+max+" count_pix "+count_pix_in_);
        for(Map.Entry<Long,long[]> entry:submap_imgs_with_min_error.entrySet())
            System.out.println("count_in_sub   "+entry.getKey());
        System.out.println("equl_img size "+equal_imgs.size());
        System.out.println("**********************************************************************");*/


       // если не нашлось в мепе такого же изо, то создается новый ИД для изо и записывается на место количества черных пикселей
        if(equal_imgs.isEmpty()){long id_img_pix = get_HandTime(); img_nick_for_compare[15]= id_img_pix;
            // проверка наличия изо с таким же количеством пикселей и индексом если есть то добавляется единица и снова проверяется, пока такого индекса не будет в списке,
            // тогда он присваевается новому изо
            boolean is_contain = true;
            while (is_contain){
                is_contain = submap_imgs_with_min_error.containsKey(count_pix_in_);
                if(is_contain){count_pix_in_++; //System.out.println("is_countain "+is_contain+" count "+count_pix_in_);
                }
                else {
                    //System.out.println("id record "+img_nick_for_compare[15]);
                    sortedmap_all_imgs_pix_of_nicks.put(count_pix_in_,img_nick_for_compare); break; }
            }

            return new long[]{-id_img_pix,count_pix_in_};}

        //for(long r:equal_imgs.get(0)) System.out.println(r);

        // если нашлось похожее изо, то берется его ИД на вывод
        int size = equal_imgs.size();
        long[] result = new long[size];
        for(int i=0; i<size; i++)
            result[i] = equal_imgs.get(i)[15];

        return result;
    }



    static int get_AmountOneBitInLong(long lng){
        return (count_one_in_numbers[(short)(lng>>48)+32768]+count_one_in_numbers[(short)(lng>>32)+32768]
                +count_one_in_numbers[(short)(lng>>16)+32768]+count_one_in_numbers[(short)(lng)+32768]);
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


   BufferedImage bufferedImageframe;



   void find_tables(Frame frame){


       if(bufferedImageframe==null)
        bufferedImageframe = new Java2DFrameConverter().getBufferedImage(frame);

       //System.out.println((System.currentTimeMillis()-start));
        createBufferedImage(frame, bufferedImageframe);
       BufferedImage image_number_hand = null;
       int x_of_number_hand = 579,y_of_number_hand = 56,width_of_number_hand = 53,height_of_number_hand = 11, width_nick = 87+4, height_nick = 14;
       boolean is_correct_number_hand = false, is_correct_nicks = false, is_correct_hero_nick = false;

       int[] correction_for_place_of_nicks = {1,2,2,2,1,1};
       for(int index_table=0; index_table<COUNT_TABLES; index_table++){

          //long s = System.currentTimeMillis();
            //  проверка правильности изо номера раздачи
           //checknicktest_nick = new ArrayList<>();
           //checknicktest_nick.add("---------------------------------------------KURSOR");
           is_correct_number_hand = is_CorrectImageOfNumberHandAndNicks(coord_left_up_of_tables[index_table][0]+x_of_number_hand,
                   coord_left_up_of_tables[index_table][1]+y_of_number_hand,width_of_number_hand,height_of_number_hand,100,100,100,bufferedImageframe);
           //c++;
           //if(index_table==0){save_image(is_correct_number_hand,"tables_img\\t_"+(c)+"_"+(is_correct_number_hand!=null));}
           //c++;
           //checknicktest_nick.add("---------------------------------------------KURSOR   "+is_correct_number_hand+"  TABLE "+index_table);
           image_number_hand = null;
           is_correct_hero_nick = false;
           if(is_correct_number_hand){
               image_number_hand = cut_SubImage(bufferedImageframe,coord_left_up_of_tables[index_table][0]+x_of_number_hand+25,
                       coord_left_up_of_tables[index_table][1]+y_of_number_hand+3,26,5);


               // проверка правильности изо ников
               is_correct_nicks = true;

              for(int img_nicks=0; img_nicks<6; img_nicks++ ){
                  int x_of_nick = coord_left_up_of_tables[index_table][0]+coords_places_of_nicks[img_nicks][0]+correction_for_place_of_nicks[img_nicks]-5-3;
                  int y_of_nick = coord_left_up_of_tables[index_table][1]+coords_places_of_nicks[img_nicks][1]+1;
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
                                   cut_SubImage(bufferedImageframe,coord_left_up_of_tables[index_table][0],coord_left_up_of_tables[index_table][1],639,468),
                                   image_number_hand
                           });
               }
              else {
                  // если все ники не определяются или определяется только ник героя, нужно для завершения последенй раздачи
                  if(!is_correct_hero_nick)ocrList_1.get(index_table).set_image_for_ocr(new BufferedImage[]{null, image_number_hand});
                  else ocrList_1.get(index_table).set_image_for_ocr(new BufferedImage[]{image_number_hand,null });
              }

           }
           ocrList_1.get(index_table).set_image_for_ocr(new BufferedImage[]{null, null});
           //if(is_correct_number_hand==null) save_image(bufferedImageframe.getSubimage(coord_left_up_of_tables[index_table][0],coord_left_up_of_tables[index_table][1],639,468),"tables_img\\t_nokurs"+(++c));
           //if(is_correct_number_hand==null)countcheks[index_table]++;
       }

   }



   private BufferedImage cut_SubImage(BufferedImage image_window,int X, int Y, int W, int H){
       BufferedImage img = image_window.getSubimage(X, Y, W, H);
       BufferedImage cut_subimage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
       Graphics g = cut_subimage.createGraphics();
       g.drawImage(img, 0, 0, null);
       return cut_subimage;
   }


    private BufferedImage createBufferedImage(Frame frame, BufferedImage image) {
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
                //System.out.println("1 grey "+grey);
                //checknicktest_nick.add("1 "+grey);
                if(get_intGreyColor(frame,x,y)>brightness_of_perimeter_up_down)return false;
            }
        }
        // правые и левые линии периметра ограничены на 10 пикселй снизу
            for(int x=X; x<w+X; x+=w-1)
                for(int y=Y; y<h+Y-10; y++){
                //System.out.println("2 grey "+grey);
                //checknicktest_nick.add("2 "+grey);
                if(get_intGreyColor(frame,x,y)>brightness_of_perimeter_left_right)return false;
            }
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



    public static synchronized Integer[] get_and_write_NewIdPlayersForNicks(String[] nicks){
        Integer[] IdNiks = new Integer[6];
        int IDplyer = -1;
        List<IdPlayer_Nick> list_idplayer_nicks = new ArrayList<>(6);
        for (int n = 0; n < 6; n++) {
            if (nicks[n] != null) {
                if (!map_idplayers_nicks.containsKey(nicks[n])) {
                    id_for_nick++;
                    map_idplayers_nicks.put(nicks[n], id_for_nick);
                    IdNiks[n] = id_for_nick;
                    list_idplayer_nicks.add(new IdPlayer_Nick(id_for_nick,nicks[n]));
                } else {
                    IDplyer = map_idplayers_nicks.get(nicks[n]);
                    IdNiks[n] = IDplyer;
                }
            } else IdNiks[n] = 0;
        }
        if(!list_idplayer_nicks.isEmpty())work_dataBase.record_listrec_to_TableIdPlayersNicks(list_idplayer_nicks);

        return IdNiks;
    }






    public static class Settings {

        private static File file_with_nicks;

        public static void setting_cupture_video(){
            read_file_with_nicks_and_img_pixs();
            _long_arr_cards_for_compare = read_ObjectFromFile("_long_arr_cards_for_compare");
            shablons_numbers_0_9_for_stacks = read_ObjectFromFile("shablons_numbers_0_9");
            count_one_in_numbers = read_ObjectFromFile("count_one_in_numbers");
            shablons_numbers_0_9_for_actions = read_ObjectFromFile("shablons_numbers_0_9_for_actions");
            shablons_text_sittingout_allin = read_ObjectFromFile("shablons_text_sittingout_allin");
            shablon_text_poker_terms = read_ObjectFromFile("shablon_text_poker_terms");

        }

        private static void read_file_with_nicks_and_img_pixs(){
            file_with_nicks = new File(home_folder+"\\all_settings\\capture_video\\nicks_img.txt");
            if(!file_with_nicks.isFile())return;
            try {
                BufferedReader br = new BufferedReader(new FileReader(file_with_nicks));
                String line;
                while ((line = br.readLine()) != null) {
                    if(!(line.startsWith("*")&&line.endsWith("*")))break;
                    String[] arr_line = line.substring(1,line.length()-1).split("%");
                    //System.out.println("line "+arr_line.length);
                    hashmap_id_img_pix_nick.put(Long.parseLong(arr_line[17]),arr_line[0]);
                    long[] img_pix = new long[16];
                    for(int i=2; i<18; i++){
                        img_pix[i-2] = Long.parseLong(arr_line[i]);
                    }
                    sortedmap_all_imgs_pix_of_nicks.put(Long.parseLong(arr_line[1]),img_pix);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        static <T> T read_ObjectFromFile(String name_file){
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



        public static void ErrorLog(String er){
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



   /* public static synchronized void write_shablon(String term,BufferedImage image,long[] img_pix){
       String[] terms = {"Posts SB","Posts BB","Call","Fold","Raise"};
       int indx = Arrays.asList(terms).indexOf(term);
       if(shablon_text_poker_terms[indx]!=null)return;
       save_image(image,"test\\"+term);

       shablon_text_poker_terms[indx] = Arrays.copyOfRange(img_pix,0,15);

        System.err.println("TERM "+term);
        for(long[] l:shablon_text_poker_terms)if(l==null)return;

        Testing.save_ObjectInFile(shablon_text_poker_terms,"shablon_text_poker_terms");
        System.err.println("SAVE SHABLON !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }*/
























    public static void main(String[] args) throws IOException, InterruptedException {
       /*CaptureVideo captureVideo = new CaptureVideo(1);
       //captureVideo.test_cap_video();
       for(OCR ocr:captureVideo.ocrList_1)ocr.stop();
        //for(OCR ocr:captureVideo.ocrList_2)ocr.stop();
       System.out.println("stop");

       //test_cam2();
       for(Map.Entry<Integer,String> entry:hashcodes_nicks_hashmap.entrySet())
           System.out.println(entry.getValue()+"    "+entry.getKey());*/
        CaptureVideo captureVideo = null;
        String name = null;
        Scanner in = new Scanner(System.in);
        System.out.print("Start y/n ");
        do {
            name = in.nextLine();
            if (name.equals("y")) {
                captureVideo = new CaptureVideo();
                captureVideo.start_thread();
            }
            if (name.equals("n")) System.exit(0);
            System.out.println("if stop push s");
            System.out.print("Start y/n ");
            if (name.equals("s") && captureVideo != null) {
                captureVideo.stop_tread();
                for (OCR ocr : captureVideo.ocrList_1) ocr.stop();
                //for(OCR ocr:captureVideo.ocrList_2)ocr.stop();
                System.out.println("stop");
            }
        } while (!name.equals("c"));




        
    }

}
