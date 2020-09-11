package org.trenkvaz.main;


import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.javacpp.*;
//import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.imgscalr.Scalr;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/*import static org.bytedeco.javacpp.avformat.avformat_alloc_context;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;*/
//import static org.bytedeco.javacpp.opencv_videoio.cvCreateFileCapture;
import org.bytedeco.ffmpeg.global.avutil;

import static org.trenkvaz.main.Settings.write_nicks_keys_img_pix;
import static org.trenkvaz.main.Testing.test_work_compare_nicks_img;

public class CaptureVideo implements Runnable{
    /*static Tesseract tesseract;

    static {
        tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Users\\Duduka\\.m2\\repository\\net\\sourceforge\\tess4j\\tess4j\\4.5.1\\tess4j-4.5.1\\tessdata");
        tesseract.setTessVariable("user_defined_dpi", "300");
        tesseract.setLanguage("eng");
    }*/
   static int[][] place_tables = {{24,9,31,19},{664,9,670,19},{1304,9,1310,19},
            {24,478,31,488},{664,478,670,488},{1304,478,1310,488}};
   static int[][] coord = {{0, 1, 2, 1},{2, 1, 2, 3},{2, 3, 0, 3},{0, 3, 0, 1}};
   static int[][] shablon_F = {{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
           {-1,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-1},
           {-1,-16777216,-1,-1,-1,-16777216,-1,-1,-1,-1},{-1,-16777216,-1,-1,-1,-16777216,-1,-1,-1,-1},
           {-1,-16777216,-1,-1,-1,-16777216,-1,-1,-1,-1},{-1,-16777216,-1,-1,-1,-1,-1,-1,-1,-1}};
   static int[][] shablon_SB = {{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
           {-1,-1,-1,-16777216,-16777216,-16777216,-1,-16777216,-16777216,-1,-1,-1},{-1,-1,-16777216,-1,-1,-16777216,-1,-1,-1,-16777216,-1,-1},
           {-1,-1,-16777216,-1,-1,-1,-16777216,-1,-1,-16777216,-1,-1},{-1,-1,-16777216,-1,-1,-1,-16777216,-1,-1,-16777216,-1,-1},
           {-1,-1,-1,-16777216,-1,-1,-16777216,-16777216,-16777216,-1,-1,-1},{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
           {-1,-1,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-1,-1},{-1,-1,-16777216,-1,-1,-16777216,-1,-1,-1,-16777216,-1,-1},
           {-1,-1,-16777216,-1,-1,-16777216,-1,-1,-1,-16777216,-1,-1},{-1,-1,-16777216,-1,-1,-16777216,-1,-1,-1,-16777216,-1,-1},
           {-1,-1,-1,-16777216,-16777216,-1,-16777216,-16777216,-16777216,-1,-1,-1},{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1}};
   static int[][] coords_places_of_nicks = {{297,320},{15,253},{15,120},{264,67},{543,120},{543,253}};

   static int[][] coord_left_up_of_tables = {{0,0},{640,0},{1280,0},{0,469},{640,469},{1280,469}};

   static int[][] coords_buttons = {{382,287},{144,231},{156,133},{242,104},{459,133},{473,231}};

   static int[][] coords_cards_hero = {{287,286},{331,286}};

   static int[][] coords_actions = {{302,267},{151,256},{118,175},{323,120},{459,175},{436,256}};

   static int[][] coord_2_3_cards_flop = {{270,202},{318,202}};

   static String nick_hero = "trenkvaz";

   static String home_folder = System.getProperty("user.dir");
   static BufferedImage[] images_bu = get_images_bu();
   //public static BufferedImage posts_blinds;
   public static BlockingQueue<String[]> drop = new ArrayBlockingQueue<>(6, true);
   public static List<BufferedImage> bufferedImageBlockingQueue = new ArrayList<>();
   public static List<String> names_of_cards = new ArrayList<>();
   public static HashMap<Integer,String> hashcodes_nicks_hashmap = new HashMap<>();
   public List<OCR> ocrList_1;
   final int COUNT_TABLES = 6;
   boolean is_run = true;
   Thread thread;
   FFmpegFrameGrabber grabber;
   CanvasFrame canvasFrame;
   Java2DFrameConverter paintConverter;
   final static UseTesseract[] use_tessearts = new UseTesseract[4];

   static byte[] count_one_in_numbers = new byte[65536];
   static HashMap<Long,String> hashmap_id_img_pix_nick = new HashMap<>();
   static SortedMap<Long,long[]> sortedmap_all_imgs_pix_of_nicks = new TreeMap<>();

   public CaptureVideo(){
       for(int i=0; i<4; i++)use_tessearts[i] = new UseTesseract();
       get_files_from_folder("b_w_nominal",2);
       set_count_one_in_numbers();
       new Settings();
   }



   public void start_thread(){
       ocrList_1 = new ArrayList<>();
       for(int i=0; i<COUNT_TABLES; i++){
           ocrList_1.add(new OCR(i));
       }
       thread = new Thread(this);
       thread.start();
   }

   public CaptureVideo(int a){
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
   }

   public CaptureVideo(String a){}

   public void run(){

       //FFmpegFrameGrabber grabber = null;

       avutil.av_log_set_level (avutil.AV_LOG_ERROR);

       while (is_run){
         grabber = connect_stream();

           if(grabber!=null){
               //getting_frames = new Getting_frames(grabber);
               //screen(grabber);
               //getting_frames.stop_getting_frames();
               screen2(grabber);
           }
           try {
               Thread.sleep(1000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
   }
   boolean is_getting_frame = true;

   public synchronized  void stop_tread(){

        is_run = false;
        is_getting_frame = false;

   }

    FFmpegFrameGrabber connect_stream(){

        //FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(System.getProperty("user.dir")+"\\test_video9.avi");
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("rtmp://127.0.0.1/live/test");
        grabber.setFrameRate(30);
        try {
            grabber.start();
            //grabber.setFrameNumber(200);
        } catch (FrameGrabber.Exception e) {
            return null;
        }
        return grabber;
    }

   void screen2(FFmpegFrameGrabber grabber){
       Frame frame = null;
       canvasFrame = new CanvasFrame("Some Title");
       canvasFrame.setCanvasSize(600, 300);//задаем размер окна
       canvasFrame.setBounds(100,100,600,300);
       is_getting_frame = true;
       int last = -1;
       try {
           System.out.println(grabber.getFrameRate());
           System.out.println("start");
           int count_space_frame = 0;
           while(is_getting_frame){
               frame = grabber.grabImage();
               if(frame!=null){ count_space_frame++;
                   canvasFrame.showImage(frame);
                   //System.out.println(canvasFrame.isDisplayable()+" "+canvasFrame.isActive()+" "+canvasFrame.isValid());
                   if(count_space_frame<0) continue;
                   if(find_tables(frame,0))continue;
                   count_space_frame = -30;
                   //System.out.println("count_space "+count_space_frame);
                   System.gc();
               }
               else {System.out.println("null frame"); break; }
           }
       grabber.release();
       } catch (FrameGrabber.Exception e) {
           e.printStackTrace();
       }
       System.out.println("stop");
       canvasFrame.dispose();
   }



   static BufferedImage[] get_images_bu(){
       BufferedImage[] result = new BufferedImage[6];
       for(int i=0; i<6; i++)result[i] = read_image("images_bu\\wb_bu_"+(i+1)+"_");
       return result;
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
        long count_pix_in_ = img_nick_for_compare[16]*1_000_000;
        long min = count_pix_in_-error*1_000_000, max = count_pix_in_+(error+1)*1_000_000;

        Map<Long,long[]> submap_imgs_with_min_error = sortedmap_all_imgs_pix_of_nicks.subMap(min,max);

        List<long[]> equal_imgs = new ArrayList<>();
        for(long[] img_min_error:submap_imgs_with_min_error.values()){
            int count_error_in_compare = 0;
            boolean is_equal = true;
            for(int i=0; i<16; i++){
                count_error_in_compare+= get_count_one_in_numbers(img_min_error[i]^img_nick_for_compare[i]);
                if(count_error_in_compare>error){is_equal = false; break;}
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
        if(equal_imgs.isEmpty()){long id_img_pix = System.nanoTime(); img_nick_for_compare[16]= id_img_pix;

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
            result[i] = equal_imgs.get(i)[16];

        return result;
    }


    static int get_count_one_in_numbers(long lng){
        return (count_one_in_numbers[(short)(lng>>48)+32768]+count_one_in_numbers[(short)(lng>>32)+32768]
                +count_one_in_numbers[(short)(lng>>16)+32768]+count_one_in_numbers[(short)(lng)+32768]);
    }

    static void set_count_one_in_numbers(){
        try {	FileInputStream file=new FileInputStream(System.getProperty("user.dir")+"\\count_one_in_numbers.file");
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

   boolean save = false;
   int c =0;
    //BufferedImage bufferedImageframe;
   boolean find_tables(Frame frame,int t){

       //BufferedImage bufferedImageframe = paintConverter.getBufferedImage(frame);
       //long start = System.currentTimeMillis();
       paintConverter = new Java2DFrameConverter();
       BufferedImage bufferedImageframe = paintConverter.getBufferedImage(frame);
       //bufferedImageframe = Java2DFrameUtils.toBufferedImage(frame);
       //System.out.println((System.currentTimeMillis()-start));
       


       int x = 579,y = 56,w = 53,h = 11;
       BufferedImage check_kursor = null;
       int count_cheks = 0;
       for(int i=0; i<COUNT_TABLES; i++){

            //  проверка отсутствия курсора на номере раздачи
           check_kursor = check_free_of_kursor(coord_left_up_of_tables[i][0]+x,coord_left_up_of_tables[i][1]+y,w,h,10,bufferedImageframe);

           if(check_kursor!=null){
               //if(i==4){save_image(bufferedImageframe.getSubimage(coord_left_up_of_tables[i][0],coord_left_up_of_tables[i][1],639,468),"tables_img\\t_"+(++c));}
               // проверка наличия числа в номере раздачи
               int hand_bright = get_max_brightness(check_kursor);
               //System.out.println("check "+hand_bright);
           if(hand_bright<100)check_kursor = null;
           else {
               // проверка наличия ников в раздаче по верхнему нику стола
               if(is_check_free_of_kursor(coord_left_up_of_tables[i][0]+264,coord_left_up_of_tables[i][1]+67,82,15,100,bufferedImageframe)){
                   ocrList_1.get(i).set_image_for_ocr(
                           new BufferedImage[]{bufferedImageframe.getSubimage(coord_left_up_of_tables[i][0],coord_left_up_of_tables[i][1],639,468),check_kursor} );
               }
               else check_kursor = null;
           }

           }

           if(check_kursor==null) count_cheks++;
       }
       bufferedImageframe = null;
       return count_cheks != COUNT_TABLES;


       //DataBuffer dataBuffer = bufferedImageframe.getData().getDataBuffer();

// Each bank element in the data buffer is a 32-bit integer
      /* long sizeBytes = ((long) dataBuffer.getSize()) * 4L;
       long sizeMB = sizeBytes / (1024L * 1024L);
       System.out.println("sizeimage "+sizeMB);*/

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



    BufferedImage check_free_of_kursor(int X, int Y, int w, int h, int limit_grey,BufferedImage frame){
        for(int x=X; x<w+X; x++){
            for(int y=Y; y<h+Y; y+=h-1){
                int val = frame.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                if(grey>limit_grey)return null;
            }
        }
        for(int y=Y; y<h+Y; y++)
            for(int x=X; x<w+X; x+=w-1){
                int val = frame.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                if(grey>limit_grey)return null;
            }
        return frame.getSubimage(X,Y,w,h);
    }


    boolean is_check_free_of_kursor(int X, int Y, int w, int h, int limit_grey,BufferedImage frame){

        for(int x=X; x<w+X; x++){
            for(int y=Y; y<h+Y; y+=h-1){
                int val = frame.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                if(grey>limit_grey)return false;
            }
        }
        for(int y=Y; y<h+Y; y++)
            for(int x=X; x<w+X; x+=w-1){
                int val = frame.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                if(grey>limit_grey)return false;
            }
        return true;
    }



   /*IplImage toGray(IplImage img){
        IplImage currenframe = IplImage.create(img.width(),img.height(),IPL_DEPTH_8U,1);
        cvCvtColor(img,currenframe,CV_RGB2GRAY);
       opencv_imgproc.cvCvtColor(img,currenframe,CV_RGB2GRAY);
        return currenframe;
   }

    public IplImage getSubImageFromIpl(IplImage img, int x, int y, int w, int h) {
        IplImage resizeImage = IplImage.create(w-x, h-y, img.depth(), img.nChannels());
        cvSetImageROI(img, cvRect(x, y, w-x, h-y));
        cvCopy(img, resizeImage);
        cvResetImageROI(img);
        return resizeImage;
    }*/



    BufferedImage get_buffimage_from_shablon(int[][]shablon){
        BufferedImage result = new BufferedImage(shablon.length, shablon[0].length, 5);
        for (int x = 0; x < shablon.length; x++)
            for (int y = 0; y < shablon[0].length; y++) {
                result.setRGB(x, y, shablon[x][y]);
            }
        return result;
    }

   static boolean s = true;

    static BufferedImage inverse_text_background(BufferedImage  source){
       // BufferedImage  source = Java2DFrameUtils.toBufferedImage(iplImage);
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        if(s) System.out.print("{");
        for (int x = 0; x < source.getWidth(); x++) {
            if(s) System.out.print("{");
            for (int y = 0; y < source.getHeight(); y++) {
                Color color = new Color(source.getRGB(x, y));
                int blue = color.getBlue();
                int red = color.getRed();
                int green = color.getGreen();
                int grey = (int) (red * 0.299 + green * 0.587 + blue * 0.114);
                if(grey>100){ grey = 0;}
                else grey = 255;
                int newRed = grey;
                int newGreen = grey;
                int newBlue = grey;
                Color newColor = new Color(newRed, newGreen, newBlue);
                result.setRGB(x, y, newColor.getRGB());
                if(s) {System.out.print(newColor.getRGB());
                if(y<source.getHeight()-1) System.out.print(",");
                }
            }
            if(s) System.out.print("},");
        }
        if(s) System.out.println("}");
        s = false;
        return result;
    }





    static int compare_buffred_images(BufferedImage current_image,BufferedImage _new_image){
        int error = 0;
        for (int i = 0; i < current_image.getWidth(); i++) {
            for (int j = 0; j < current_image.getHeight(); j++) {
                int rgb = Math.abs(current_image.getRGB(i, j) - _new_image.getRGB(i, j));
                if (rgb != 0) {
                    error++;
                }
            }
        }
        return error;
    }


    public static void save_image(BufferedImage image,String name_file){
        try {
            ImageIO.write(image ,"png",new File(home_folder+"\\"+name_file+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static BufferedImage read_image(String name_file){
        BufferedImage result = null;
        try {
         result = ImageIO.read(new File(home_folder+"\\"+name_file+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    static void get_image_buttons(){
        String[] colors = {"green","blue","red","black"};
        for(int i=1; i<5; i++){
            BufferedImage card = read_image("lastcards3\\card_"+(i)+"_");
            Color color = new Color(card.getRGB(7, 1));
            int blue = color.getBlue();
            int red = color.getRed();
            int green = color.getGreen();
            int grey = (int) (red * 0.299 + green * 0.587 + blue * 0.114);
            //System.out.println(colors[i-1]+" "+" blue "+blue+" red "+red+" green "+green+" grey "+grey);
            if(i==1) System.out.println(colors[i-1]+" "+green);
            if(i==2) System.out.println(colors[i-1]+" "+blue);
            if(i==3) System.out.println(colors[i-1]+" "+red);
            if(i==4) System.out.println(colors[i-1]+" "+red);
        }
    }

   static String get_suit_of_card(BufferedImage image_card){
       Color color = new Color(image_card.getRGB(15, 16));
       int blue = color.getBlue();
       int red = color.getRed();
       int green = color.getGreen();
       if(blue==red&&red==green)return "black";
       if(blue>red&&blue>green)return "blue";
       if(red>blue&&red>green)return "red";
       if(green>blue&&green>red)return "green";
       return "unknown";
   }

   static int get_grey_corner(BufferedImage image_card){
       Color color = new Color(image_card.getRGB(15, 16));
       int blue = color.getBlue();
       int red = color.getRed();
       int green = color.getGreen();
       return (int) (red * 0.299 + green * 0.587 + blue * 0.114);
   }


   static BufferedImage get_scale_image(BufferedImage img, int w,int h){ return Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, w,h); }

   static void get_files_from_folder(String name_folder,int type) {
        String folder = home_folder+"\\"+name_folder;
        for(File file:new File(folder).listFiles()){
            try {
             if(type==1) {bufferedImageBlockingQueue.add(ImageIO.read(file));names_of_cards.add(file.getName().substring(5,7));}
             if(type==2) avirage_cards.put(file.getName().substring(0,1),ImageIO.read(file));
             if(type==4) test_for_campare.put(file.getName(),((DataBufferByte) ImageIO.read(file).getRaster().getDataBuffer()).getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static HashMap<String,byte[]> test_for_campare = new HashMap<>();
    static HashMap<String,BufferedImage>avirage_cards =new HashMap<>();

    static BufferedImage get_white_black_image(BufferedImage  source,int limit_grey){
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        for (int x = 0; x < source.getWidth(); x++)
            for (int y = 0; y < source.getHeight(); y++) {
                Color color = new Color(source.getRGB(x, y));
                int blue = color.getBlue();
                int red = color.getRed();
                int green = color.getGreen();
                int grey = (int) (red * 0.299 + green * 0.587 + blue * 0.114);
                if(grey<limit_grey){ grey = 0;}
                else grey = 255;
                int newRed = grey;
                int newGreen = grey;
                int newBlue = grey;
                result.setRGB(x, y, new Color(newRed, newGreen, newBlue).getRGB());
            }
        //System.out.println("error "+error);
        return result;
    }

    static BufferedImage avirage_image_from_one_nominal_cards(BufferedImage[] one_nominal,int limit_grey){
        BufferedImage result = new BufferedImage(one_nominal[0].getWidth(), one_nominal[0].getHeight(), one_nominal[0].getType());

        //int[] black_white = new int[4];
        int[] greys = new int[4];
        int avirage = 0;
        for (int x = 0; x < one_nominal[0].getWidth(); x++)
            for (int y = 0; y < one_nominal[0].getHeight(); y++) {
                for(int i=0; i<4; i++){
                Color color = new Color(one_nominal[i].getRGB(x, y));
                int blue = color.getBlue();
                int red = color.getRed();
                int green = color.getGreen();
                greys[i] = (int) (red * 0.299 + green * 0.587 + blue * 0.114);

                }
                avirage =(int)((greys[0]+greys[1]+greys[2]+greys[3])/4);
                if(avirage<limit_grey){ avirage = 0;}
                else avirage = 255;
               // if(black_white[0]==0||black_white[1]==0||black_white[2]==0||black_white[3]==0)avirage=0;
                //else avirage=255;
                result.setRGB(x, y, new Color(avirage, avirage, avirage).getRGB());
            }
        return result;
    }






   synchronized void stop(){
        is_run = false;
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
