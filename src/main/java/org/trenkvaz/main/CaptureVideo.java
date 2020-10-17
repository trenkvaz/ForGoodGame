package org.trenkvaz.main;


//import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;


/*import static org.bytedeco.javacpp.avformat.avformat_alloc_context;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;*/
//import static org.bytedeco.javacpp.opencv_videoio.cvCreateFileCapture;
import org.bytedeco.ffmpeg.global.avutil;

import static org.trenkvaz.main.Testing.save_image;
import static org.trenkvaz.ui.Controller_main_window.*;

public class CaptureVideo implements Runnable{

   static int[][] coords_places_of_nicks = {{297,320},{15,253},{15,120},{264,67},{543,120},{543,253}};

   public static int[][] coord_left_up_of_tables = {{0,0},{640,0},{1280,0},{0,469},{640,469},{1280,469}};

   static int[][] coords_buttons = {{382,287},{144,231},{156,133},{242,104},{459,133},{473,231}};

   static int[][] coords_cards_hero = {{287,286},{331,286}};

   static int[][] coords_actions = {{302,267},{151,256},{118,175},{323,120},{459,175},{436,256}};

   static int[][] coord_2_3_cards_flop = {{270,202},{318,202}};

   static String[] nominals_cards = {"2","3","4","5","6","7","8","9","T","J","Q","K","A"};

   static String nick_hero = "trenkvaz";

   public static String home_folder = System.getProperty("user.dir");


   public List<OCR> ocrList_1;
   final int COUNT_TABLES = 6;
   boolean is_run = true;
   Thread thread;
   boolean is_getting_frame = true;
   FFmpegFrameGrabber grabber;
   CanvasFrame canvasFrame;
   //Java2DFrameConverter paintConverter;
   final static UseTesseract[] use_tessearts = new UseTesseract[4];

   static byte[] count_one_in_numbers = new byte[65536];
   static HashMap<Long,String> hashmap_id_img_pix_nick = new HashMap<>();
   static SortedMap<Long,long[]> sortedmap_all_imgs_pix_of_nicks = new TreeMap<>();
   static HashMap<String,BufferedImage>avirage_cards =new HashMap<>();
   static BufferedImage[] images_bu = new BufferedImage[6];

   static long[][] _long_arr_cards_for_compare = new long[52][5];



   public CaptureVideo(){
       for(int i=0; i<4; i++)use_tessearts[i] = new UseTesseract();
       //settings_capturevideo = new Settings();
       Settings.setting_cupture_video();
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

   /*public CaptureVideo(int a){
      // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
       ocrList_1 = new ArrayList<>();

       for(int i=0; i<COUNT_TABLES; i++){
           ocrList_1.add(new OCR(i));
           //ocrList_2.add(new OCR(i*10));
       }
       //bufferedImage_shablon_F = get_buffimage_from_shablon(shablon_F);
       get_files_from_folder("b_w_nominal",2);
      // posts_blinds = read_image("images_post_blinds\\wb3sb");
       set_count_one_in_numbers();
   }*/

   public CaptureVideo(String a){}

   public void run(){

       //FFmpegFrameGrabber grabber = null;

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



        //grabber.setFrameRate(30);
        try {
            grabber.start();
            //grabber.setFrameNumber(200);
            //grabber.setFrameRate(30);
        } catch (FrameGrabber.Exception e) {
            return null;
        }
        return grabber;
    }

   void screen2(FFmpegFrameGrabber grabber){
       Frame frame = null;


       is_getting_frame = true;
       int last = -1;
       try {
           System.out.println(grabber.getFrameRate());
           System.out.println("start");
           controller_main_window.setMessage_work("start");
           int count_space_frame = 0;
           while(is_getting_frame){
               /*int num = grabber.getFrameNumber();
               System.out.println(grabber.getFrameNumber()+"   "+(num%2));
               //if(num%2==0){ //System.out.println(num%2);
               //continue;}*/
               frame = grabber.grabImage();
               //System.out.println("*"+num+"*");
               //frame = grabAt(grabber.getFrameNumber(),30);
               if(frame!=null){ count_space_frame++;
                   canvasFrame.showImage(frame);
                   //System.out.println(canvasFrame.isDisplayable()+" "+canvasFrame.isActive()+" "+canvasFrame.isValid());
                   /*if(count_space_frame<0) continue;
                   if(find_tables(frame))continue;
                   count_space_frame = -30;
                   for(int o=0; o<COUNT_TABLES; o++)countcheks[o]=0;
                   System.out.println("count_space "+count_space_frame);
                   System.gc();*/
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
           System.err.println("error name "+name+" hash "+id_img_pix+" oldhash "+hashmap_id_img_pix_nick.get(name));
       }

       return null;
   }


    static synchronized long[] get_number_img_nicks(long[] img_nick_for_compare,int error){
        // img_nick_for_compare 15 чисел изо, 16-у количество черных пикселей
        // умножается на миллион, чтобы получить индексы в сортируемом мепе, по ним будет отбираться диапазон по количеству черных пикселей

        long count_pix_in_ = img_nick_for_compare[15]*1_000_000;
        long min = count_pix_in_-error*1_000_000, max = count_pix_in_+(error+1)*1_000_000;

        Map<Long,long[]> submap_imgs_with_min_error = sortedmap_all_imgs_pix_of_nicks.subMap(min,max);

        List<long[]> equal_imgs = new ArrayList<>(); int first_of_pair_error = 0, second_of_pair_error = 0;
        for(long[] img_min_error:submap_imgs_with_min_error.values()){
            int count_error_in_compare = 0;
            boolean is_equal = true;
            for(int i=0; i<15; i++){
                /*count_error_in_compare+= get_count_one_in_numbers(img_min_error[i]^img_nick_for_compare[i]);
                if(count_error_in_compare>error){is_equal = false; break;}*/
                if(i%2==0)first_of_pair_error = get_count_one_in_numbers(img_min_error[i]^img_nick_for_compare[i]);
                if(i%2!=0)second_of_pair_error = get_count_one_in_numbers(img_min_error[i]^img_nick_for_compare[i]);
                if(i>0&&(first_of_pair_error+second_of_pair_error)>error){ is_equal = false; break;  }
            }
            if(!is_equal)continue;
            equal_imgs.add(img_min_error);
        }

        /*System.out.println("**********************************************************************");
        System.out.println("min "+min+" max "+max+" count_pix "+count_pix_in_);
        for(Map.Entry<Long,long[]> entry:submap_imgs_with_min_error.entrySet())
            System.out.println("count_in_sub   "+entry.getKey());
        System.out.println("equl_img size "+equal_imgs.size());
        System.out.println("**********************************************************************");*/


       // если не нашлось в мепе такого же изо, то создается новый ИД для изо и записывается на место количества черных пикселей
        if(equal_imgs.isEmpty()){long id_img_pix = System.nanoTime(); img_nick_for_compare[15]= id_img_pix;
            // проверка наличия изо с таким же количеством пикселей и индексом если есть то добавляется единица и снова проверяется, пока такого индекса не будет в списке,
            // тогда он присваевается новому изо
            boolean is_contain = true;
            while (is_contain){
                is_contain = submap_imgs_with_min_error.containsKey(count_pix_in_);
                if(is_contain){count_pix_in_++; //System.out.println("is_countain "+is_contain+" count "+count_pix_in_);
                }
                else {
                   // System.out.println("count_pix "+count_pix_in_);
                    sortedmap_all_imgs_pix_of_nicks.put(count_pix_in_,img_nick_for_compare); break; }
            }

            return new long[]{-id_img_pix,count_pix_in_};}

        // если нашлось похожее изо, то берется его ИД на вывод
        int size = equal_imgs.size();
        long[] result = new long[size];
        for(int i=0; i<size; i++)
            result[i] = equal_imgs.get(i)[15];

        return result;
    }


    static int get_count_one_in_numbers(long lng){
        return (count_one_in_numbers[(short)(lng>>48)+32768]+count_one_in_numbers[(short)(lng>>32)+32768]
                +count_one_in_numbers[(short)(lng>>16)+32768]+count_one_in_numbers[(short)(lng)+32768]);
    }



   boolean save = false;
   static int c =0;
    //BufferedImage bufferedImageframe;
   int[] countcheks = new int[COUNT_TABLES];
   BufferedImage bufferedImageframe;

  public static Map<Integer,List<String>> checknicktest_table = new HashMap<>();
   static List<String> checknicktest_nick;


   boolean find_tables(Frame frame){

       //BufferedImage bufferedImageframe = paintConverter.getBufferedImage(frame);
       //long start = System.currentTimeMillis();
       //Java2DFrameConverter paintConverter = new Java2DFrameConverter().convert(frame);
       if(bufferedImageframe==null)
        bufferedImageframe = new Java2DFrameConverter().getBufferedImage(frame);
       //bufferedImageframe = Java2DFrameUtils.toBufferedImage(frame);
       //System.out.println((System.currentTimeMillis()-start));
        createBufferedImage(frame, bufferedImageframe);

       int x_of_number_hand = 579,y_of_number_hand = 56,width_of_number_hand = 53,height_of_number_hand = 11, width_nick = 87, height_nick = 14;
       boolean is_correct_number_hand = false, is_correct_nicks = false;
       int[] correction_for_place_of_nicks = {1,2,2,2,1,1};
       for(int index_table=0; index_table<COUNT_TABLES; index_table++){

          //long s = System.currentTimeMillis();
            //  проверка правильности изо номера раздачи
           //checknicktest_nick = new ArrayList<>();
           //checknicktest_nick.add("---------------------------------------------KURSOR");
           is_correct_number_hand = is_CorrectImageOfNumberHandAndNicks(coord_left_up_of_tables[index_table][0]+x_of_number_hand,
                   coord_left_up_of_tables[index_table][1]+y_of_number_hand,width_of_number_hand,height_of_number_hand,100,100,bufferedImageframe);
           //c++;
           //if(index_table==0){save_image(is_correct_number_hand,"tables_img\\t_"+(c)+"_"+(is_correct_number_hand!=null));}
           //c++;
           //checknicktest_nick.add("---------------------------------------------KURSOR   "+is_correct_number_hand+"  TABLE "+index_table);
           if(is_correct_number_hand){
               // проверка правильности изо ников
               is_correct_nicks = true;

              for(int img_nicks=0; img_nicks<6; img_nicks++ ){
                  int x_of_nick = coord_left_up_of_tables[index_table][0]+coords_places_of_nicks[img_nicks][0]+correction_for_place_of_nicks[img_nicks]-5;
                  int y_of_nick = coord_left_up_of_tables[index_table][1]+coords_places_of_nicks[img_nicks][1]+1;
                  //checknicktest_nick.add("++++++++++++++++++++++++++++++++++++"+img_nicks);
                  if(is_CorrectImageOfNumberHandAndNicks(x_of_nick,y_of_nick,width_nick,height_nick,240,210,bufferedImageframe))continue;
                  is_correct_nicks = false;
                  break;
              }

               //System.out.println("time "+(System.currentTimeMillis()-s));
              if(is_correct_nicks){
                  //System.out.println("is_correct_nicks");
                  /*checknicktest_table.put(c,checknicktest_nick);
                  if(index_table==0)save_image(bufferedImageframe,"tables_img\\_");*/
                   ocrList_1.get(index_table).set_image_for_ocr(
                           new BufferedImage[]{
                                   cut_SubImage(bufferedImageframe,coord_left_up_of_tables[index_table][0],coord_left_up_of_tables[index_table][1],639,468),
                                   cut_SubImage(bufferedImageframe,coord_left_up_of_tables[index_table][0]+x_of_number_hand+25,
                                           coord_left_up_of_tables[index_table][1]+y_of_number_hand+3,26,5)
                           });


               }

           }

           //if(is_correct_number_hand==null) save_image(bufferedImageframe.getSubimage(coord_left_up_of_tables[index_table][0],coord_left_up_of_tables[index_table][1],639,468),"tables_img\\t_nokurs"+(++c));
           //if(is_correct_number_hand==null)countcheks[index_table]++;
       }
       // в массив коунтчек где элемент отвечает за один стол накапливаются ситуации когда нет номера руки если у все элементов больше 30 раз отсутствует номер руки, то
       // это возвращает фалсе в метод где создается кадр, что вызвает в этом методе пазу на 30 кадров
       boolean is_info_for_orc = true;
       /*for(int i=0; i<COUNT_TABLES; i++){
           if(countcheks[i]>30)continue;
           is_info_for_orc = true;
           break;
       }*/

       //bufferedImageframe = null;
       //paintConverter = null;

       //return count_cheks != COUNT_TABLES;
       //System.out.println(is_info_for_orc);
       return is_info_for_orc;


       //DataBuffer dataBuffer = bufferedImageframe.getData().getDataBuffer();

// Each bank element in the data buffer is a 32-bit integer
      /* long sizeBytes = ((long) dataBuffer.getSize()) * 4L;
       long sizeMB = sizeBytes / (1024L * 1024L);
       System.out.println("sizeimage "+sizeMB);*/

   }


   private BufferedImage[] get_NicksImages(BufferedImage image_window,int index_table){
       int[] correction_for_place_of_nicks = {1,2,2,2,1,1};
       int width_nick = 86, height_nick = 14;
       BufferedImage[] nicks_images = new BufferedImage[6];
       for(int img_nicks=0; img_nicks<6; img_nicks++ ){
           int x_of_nick = coord_left_up_of_tables[index_table][0]+coords_places_of_nicks[img_nicks][0]+correction_for_place_of_nicks[img_nicks]-5;
           int y_of_nick = coord_left_up_of_tables[index_table][1]+coords_places_of_nicks[img_nicks][1]+1;

           BufferedImage img = image_window.getSubimage(x_of_nick, y_of_nick, width_nick, height_nick); //fill in the corners of the desired crop location here
           nicks_images[img_nicks] = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
           Graphics g = nicks_images[img_nicks].createGraphics();
           g.drawImage(img, 0, 0, null);
       }
       return nicks_images;
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


    /*private BufferedImage createBufferedImage(Frame frame, BufferedImage image) {
        ByteBuffer buffer = (ByteBuffer) frame.image[0].position(0);

            if(image == null) {
                ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

                ColorModel cm = new ComponentColorModel(cs, false,false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
                // this assumes BGR format
                DataBuffer dataBuffer = new DataBufferByte(buffer.limit());
                WritableRaster wr = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_BYTE, frame.imageWidth, frame.imageHeight, frame.imageChannels, frame.imageStride, new int[] {2, 1, 0}), dataBuffer,null);
                byte[] bufferPixels = ((DataBufferByte) wr.getDataBuffer()).getData();

                buffer.get(bufferPixels);

                return new BufferedImage(cm, wr, false, null);
            }
            else {
        WritableRaster wr = image.getRaster();
        byte[] bufferPixels = ((DataBufferByte) wr.getDataBuffer()).getData();
        buffer.get(bufferPixels);

        return image;
          }
    }*/









  public static boolean is_CorrectImageOfNumberHandAndNicks(int X, int Y, int w, int h, int brightness_of_perimeter,int max_brightness_of_text, BufferedImage frame){
       //save_image(frame.getSubimage(X,Y,w,h),"tables_img\\t_"+(c));
        // проверка отсутстивя белых пикселей по периметру номера раздачи
        for(int x=X; x<w+X; x++){
            for(int y=Y; y<h+Y; y+=h-1){
                int val = frame.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                //System.out.println("1 grey "+grey);
                //checknicktest_nick.add("1 "+grey);
                if(grey>brightness_of_perimeter)return false;
            }
        }
        for(int y=Y; y<h+Y; y++)
            for(int x=X; x<w+X; x+=w-1){
                int val = frame.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                //System.out.println("2 grey "+grey);
                //checknicktest_nick.add("2 "+grey);
                if(grey>brightness_of_perimeter)return false;
            }
       // проверка яркости текста
        int max = 0, y = Y+h/2;
        for(int x=X; x<w+X; x++){
            int val = frame.getRGB(x, y);
            int r = (val >> 16) & 0xff;
            int g = (val >> 8) & 0xff;
            int b = val & 0xff;
            int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
            if(grey>max)max=grey;
        }
      //System.out.println("max "+max);
        //checknicktest_nick.add("max "+max);

        return max >= max_brightness_of_text;

        //return frame.getSubimage(X+25,Y+3,26,5);
    }










    public static class Settings {

       private static File file_with_nicks;

      public static void setting_cupture_video(){
            read_file_with_nicks_and_img_pixs();
            //set_avirage_img_nominal_cards();
            set_long_arr_CardsForCompare();
            set_count_one_of_bite_in_mumber();
            set_images_bu();

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



        private static void set_avirage_img_nominal_cards(){
            for(File file: Objects.requireNonNull(new File(home_folder + "\\all_settings\\capture_video\\b_w_nominal").listFiles())){
                try {
                    avirage_cards.put(file.getName().substring(0,1),ImageIO.read(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static void set_count_one_of_bite_in_mumber(){
            try {	FileInputStream file=new FileInputStream(home_folder+"\\all_settings\\capture_video\\count_one_in_numbers.file");
                ObjectInput out = new ObjectInputStream(file);
                count_one_in_numbers = (byte[]) out.readObject();
                out.close();
                file.close();
            } catch(IOException e) {
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        private static void set_images_bu(){
            int i = -1;
            for(File file: Objects.requireNonNull(new File(home_folder + "\\all_settings\\capture_video\\images_bu").listFiles())){
                try {
                    i++;
                    images_bu[i] = ImageIO.read(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        private static void set_long_arr_CardsForCompare(){
            try {	FileInputStream file=new FileInputStream(home_folder+"\\all_settings\\capture_video\\_long_arr_cards_for_compare.file");
                ObjectInput out = new ObjectInputStream(file);
                _long_arr_cards_for_compare = (long[][]) out.readObject();
                out.close();
                file.close();
            } catch(IOException e) {
                System.out.println(e);
            } catch (ClassNotFoundException e) {
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
