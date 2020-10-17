package org.trenkvaz.main;

import com.sun.jna.Pointer;
import net.coobird.thumbnailator.Thumbnails;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.ImageIOHelper;
import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.util.LoadLibs;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;

import static net.sourceforge.tess4j.TessAPI.*;
import static org.bytedeco.javacv.FFmpegFrameGrabber.getDeviceDescriptions;
import static org.trenkvaz.main.CaptureVideo.*;

public class Testing {



    public synchronized static void save_image(BufferedImage image, String name_file){
        int index = name_file.lastIndexOf("\\");
        if(index>0){
            new File(home_folder+"\\"+name_file.substring(0,index)).mkdirs();
        }
        try {
            ImageIO.write(image ,"png",new File(home_folder+"\\"+name_file+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static BufferedImage read_image(String name_file){
        BufferedImage result = null;
        try {
            result = ImageIO.read(new File(home_folder+"\\"+name_file+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static short[][] _short_arrs_shablons_numbers = new short[11][];

    static void save_ObjectInFile(){
        try {FileOutputStream file=new FileOutputStream(home_folder+"\\_short_arrs_shablons_numbers.file");
            ObjectOutput out = new ObjectOutputStream(file);
            out.writeObject(_short_arrs_shablons_numbers);
            out.close();
            file.close();
        } catch(IOException e) {
            System.out.println(e);
        }
    }

    static void read_ObjectFromFile(){
        try {	FileInputStream file=new FileInputStream(home_folder+"\\_short_arrs_shablons_numbers.file");
            ObjectInput out = new ObjectInputStream(file);
            _short_arrs_shablons_numbers = (short[][]) out.readObject();
            out.close();
            file.close();
        } catch(IOException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    static void write_TextToFile(List<String> strings,String name_file){
        int index = name_file.lastIndexOf("\\");
        if(index>0){
            new File(home_folder+"\\"+name_file.substring(0,index)).mkdirs();
        }

        try {
            BufferedWriter   bufferedWriter = new BufferedWriter(new FileWriter(home_folder+"\\"+name_file+".txt"));
            for(String text:strings){
                bufferedWriter.write(text);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void test_work_compare_nicks_img(){

        SortedMap<Long,long[]> sortedmap_all_imgs_pix_of_nicks_million = new TreeMap<>();
        long s =System.currentTimeMillis();
        Long[] keys = sortedmap_all_imgs_pix_of_nicks.keySet().toArray(new Long[0]); int c =-1;
        int size = keys.length; int tokey = 0;
        for(int i=0; i<1_000_000; i++){
            c++;
            long key = keys[c];
            long[] data = sortedmap_all_imgs_pix_of_nicks.get(key);
            if(i>keys.length-1)key += tokey+=1;
            sortedmap_all_imgs_pix_of_nicks_million.put(key,data);
            if(c==size-1)c=-1;
            //sortedmap_all_imgs_pix_of_nicks.put(key,id);
        }
        System.out.println("list random time "+(System.currentTimeMillis()-s));
        SortedMap<Long,long[]> saved = sortedmap_all_imgs_pix_of_nicks;
        sortedmap_all_imgs_pix_of_nicks = sortedmap_all_imgs_pix_of_nicks_million;
        System.out.println("size saved = "+saved.size());
        System.out.println("size mill = "+sortedmap_all_imgs_pix_of_nicks.size());
        s =System.currentTimeMillis();
        c =-1;
        for(int i=0; i<100; i++){
            c++;
            get_number_img_nicks(saved.get(keys[c]),6);
            if(c==size-1)c=-1;
        }
        System.out.println("time "+(System.currentTimeMillis()-s));


    }



    static BufferedImage get_white_black(BufferedImage image){
        int w= image.getWidth(), h = image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(w,h, BufferedImage.TYPE_INT_RGB);

            for(int i=0;i<w;i++) {
                for(int j=0;j<h;j++) {
                    //Get RGB Value
                    int val = image.getRGB(i, j);
                    //Convert to three separate channels
                    int r = (0x00ff0000 & val) >> 16;
                    int g = (0x0000ff00 & val) >> 8;
                    int b = (0x000000ff & val);
                    int m=(r+g+b);
                    //(255+255+255)/2 =283 middle of dark and light
                    if(m>=383) {
                        // for light color it set white
                        bufferedImage.setRGB(i, j, Color.WHITE.getRGB());
                    }
                    else{
                        // for dark color it will set black
                        bufferedImage.setRGB(i, j, 0);
                    }
                }
            }
            return bufferedImage;
    }

    static BufferedImage get_white_black_average(BufferedImage image){
        int w= image.getWidth(), h = image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(w,h, BufferedImage.TYPE_INT_RGB);
        int count_px = w*h;
        int sum_gray =0;
        int[][] arr_greys = new int[w][h];
        for(int i=0;i<w;i++) {
            for(int j=0;j<h;j++) {
                int val = image.getRGB(i, j);
                //Convert to three separate channels
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                arr_greys[i][j] = grey;
                sum_gray+=grey;
            }
        }
        int average = sum_gray/count_px;
        for(int i=0;i<w;i++) {
            for(int j=0;j<h;j++) {
              if(arr_greys[i][j]>average)bufferedImage.setRGB(i, j, Color.WHITE.getRGB());
              else bufferedImage.setRGB(i,j,0);

            }
        }
        //System.out.println("average "+(sum_gray/count_px));
        return bufferedImage;
    }








    public static boolean isBlue(Color c) {
         float MIN_BLUE_HUE = 0.5f; // CYAN
         float MAX_BLUE_HUE = 0.8333333f; // MAGENTA

        float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        float hue = hsv[0];
        return hue >= MIN_BLUE_HUE && hue <= MAX_BLUE_HUE;
    }


    static void show_img_from_img(BufferedImage img,int X, int Y){
        for(int y=0; y<Y; y++){
            for(int x=0; x<X; x++){
                int val = img.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                if(grey==255)System.out.print("0");else System.out.print("1");
                System.out.print(" ");
                //System.out.println("ind "+index_bit);
                //if(index_bit==63){index_bit=-1; index_in_arrlong++; }
            }
            System.out.println();
        }

        System.out.println();
        System.out.println();
    }



    static void show_img_from_arr_long(long[] arr_long,int X, int Y){
        int count_pix = 0;
        for(int y=0; y<Y; y++){
            for(int x=0; x<X; x++){
                //if(y<3&&x==0)continue;
                //System.out.println(y+" "+x);
                //count_pix++;
                int coord_in_arr_long = (y+Y*x);
                int index_bit = coord_in_arr_long%64;
                int index_in_arrlong = coord_in_arr_long/64;
                //index_bit++;
                //System.out.println(coord_in_arr_long+"  "+index_in_arrlong+"  "+index_bit);
                long pix = arr_long[index_in_arrlong];
                // 1<<число сдвига маска единицы 000001 двигаешь еденицу влево
                pix = pix&(long)1<<(63-index_bit);
                if(pix==0)System.out.print("0");else System.out.print("1");
                System.out.print(" ");
                //System.out.println("ind "+index_bit);
                //if(index_bit==63){index_bit=-1; index_in_arrlong++; }
                count_pix++;
            }
            System.out.println();
        }

        System.out.println();
        System.out.println();
        System.out.println(count_pix);
    }

   static int get_max_brightness(BufferedImage image){
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

    static BufferedImage getGrayScale(BufferedImage inputImage){
        BufferedImage img = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = img.getGraphics();
        g.drawImage(inputImage, 0, 0, null);
        g.dispose();
        return img;
    }

    static BufferedImage re_bright(BufferedImage inputImage,float scaleFactor){
        BufferedImage bufferedImage = new BufferedImage(inputImage.getWidth(),inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        RescaleOp op = new RescaleOp(scaleFactor, 0, null);
        return op.filter(inputImage,bufferedImage);
    }

   static BufferedImage check_free_of_kursor(int X, int Y, int w, int h, int limit_grey,BufferedImage frame){
        //save_image(frame.getSubimage(X,Y,w,h),"tables_img\\t_");
        for(int x=X; x<w+X; x++){
            for(int y=Y; y<h+Y; y+=h-1){
                int val = frame.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                System.out.println("1 grey "+grey);
                //if(grey>limit_grey)return null;
            }
        }
        for(int y=Y; y<h+Y; y++)
            for(int x=X; x<w+X; x+=w-1){
                int val = frame.getRGB(x, y);
                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;
                int grey = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                System.out.println("2 grey "+grey);
                //if(grey>limit_grey)return null;
            }
        return frame.getSubimage(X,Y,w,h);
    }

    static void show_canvas() throws FrameGrabber.Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("video=USB Video");

        grabber.setFormat("dshow");
        grabber.setVideoCodecName("mjpeg");

        grabber.setFrameRate(60);

        grabber.start();

        Frame frame = null;
        CanvasFrame canvasFrame = new CanvasFrame("");
        canvasFrame.setCanvasSize(600, 300);//задаем размер окна
        canvasFrame.setBounds(100,100,600,300);
        while (canvasFrame.isVisible()&&(frame =grabber.grabImage())!=null)canvasFrame.showImage(frame);
    }

    static void compare_binar_imgs(BufferedImage img1,BufferedImage img2,int limit_grey){
        int w = img1.getWidth(), h = img1.getHeight(); int error = 0;
        for(int x=0; x<w; x++) {
            for (int y = 0; y < h; y++) {
                int val1 = img1.getRGB(x, y);
                int r = (val1 >> 16) & 0xff;
                int g = (val1 >> 8) & 0xff;
                int b = val1 & 0xff;
                int grey1 = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                if(grey1<limit_grey)grey1=1;else grey1=0;
                int val2 = img2.getRGB(x, y);
                r = (val2 >> 16) & 0xff;
                g = (val2 >> 8) & 0xff;
                b = val2 & 0xff;
                int grey2 = (int) (r * 0.299 + g * 0.587 + b * 0.114);
                if(grey2<limit_grey)grey2=1;else grey2=0;
                if(grey1!=grey2)error++;
                //if(grey>limit_grey)return null;
            }
        }
        System.out.println("error "+error);
    }


   static boolean is_error_image(BufferedImage image){
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
                System.out.println("er "+grey);
                if(grey>200)count_white++;
                //if(isBlue(new Color(val)))count_blue++;
            }
            if(is_symbol_start)count_line_with_symbols++;
            if(count_line_with_symbols==5)break;
        }
        return count_white <= 0;
    }
   static int c=0;

   static long get_number_hand(BufferedImage img,OCR ocr,int grey){
        
        //save_image(frame[1],"for_ocr_number\\osr_"+c+"t"+table);
        int limit_grey = 175;
        if(get_max_brightness(img)<150)limit_grey = 214;
        //BufferedImage scaled_sub_bufferedImage = ocr.get_scale_image(ocr.set_grey_and_inverse_or_no(img,true),2);
        //BufferedImage scaled_sub_bufferedImage = set_grey_and_inverse_or_no(get_scale_image(frame[1],3),true);
        BufferedImage black_white_image = ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no(img,true),limit_grey);
        save_image(black_white_image,"test\\bwnum_"+grey+"_"+(c++));
        if(ocr.compare_buffred_images(ocr.bufferedImage_current_number_hand,black_white_image,5))return -1;
        ocr.bufferedImage_current_number_hand = black_white_image;

        //save_image(black_white_image,"test2\\osr_"+(c++));
        return 1;

    }

    record Cards(BufferedImage[] card,String nominal_card){}

    public static List<BufferedImage[]> list_test_numberhands = new ArrayList<>();


    public static List<Cards> list_test_cards = new ArrayList<>();



   static short[] get_shortarr_HashNumberImg(BufferedImage image_table,int X, int Y, int W, int H, int limit_grey){
       int first_x_black_pix = 0, last_x_black_pix =0;
     out:for (int x = X+5; x < X+W-5; x++)for(int y = Y; y < Y+H; y++) if(get_intGreyColor(image_table,x,y)>limit_grey){first_x_black_pix = x;break out;}
     out: for (int x = X+W-5; x > X+5; x--)for (int y = Y; y < Y+H; y++) if(get_intGreyColor(image_table,x,y)>limit_grey){last_x_black_pix = x; break out; }

     int amount_line_of_num = last_x_black_pix-first_x_black_pix+2;

       System.out.println("line "+amount_line_of_num);
       short _16_pixels =0;
       short[] shortarr_hashimage = new short[amount_line_of_num]; int index_shortarr_hashimage = -1;

       for (int x = first_x_black_pix; x < amount_line_of_num+first_x_black_pix; x++){
           index_shortarr_hashimage++;
           for (int y = Y; y < Y+H; y++) {
               _16_pixels<<=1;
               if(get_intGreyColor(image_table,x,y)>limit_grey){ _16_pixels+=1;
                   System.out.print("1"); } else System.out.print("0");
               System.out.print(" ");
           }
           shortarr_hashimage[index_shortarr_hashimage] = _16_pixels;
           _16_pixels = 0;
           System.out.println();
       }
       return shortarr_hashimage;
   }



   static short[] get_shortarr_HashShablonNumber(int amount_line_of_num, short[] shortarr_hashnumberimg,int start_line){
       short[] shortarr_shablon = new short[amount_line_of_num];
       for(int i=start_line, ind =0; i<amount_line_of_num+start_line; i++,  ind++) shortarr_shablon[ind] = shortarr_hashnumberimg[i];
       return shortarr_shablon;
   }


   static void show_shortarr_HashShablonNumber(short[] shortarr_shablon){

       for(int x=0; x<shortarr_shablon.length; x++){


       }
       System.out.println("4    "+shortarr_shablon[4]);
       for(int y=0; y<9; y++){
           for(int x=0; x<shortarr_shablon.length; x++){
               //if(y<3&&x==0)continue;
               //System.out.println(y+" "+x);
               //count_pix++;
               /*int coord_in_arr_long = (y+9*x);
               int index_bit = coord_in_arr_long%16;
               int index_in_arrlong = coord_in_arr_long/64;*/
               //index_bit++;
               //System.out.println(coord_in_arr_long+"  "+index_in_arrlong+"  "+index_bit);
               short pix = shortarr_shablon[x];
               // 1<<число сдвига маска единицы 000001 двигаешь еденицу влево
               // пикс пример число шорт 16 битов(0..01) маска единицы 0000000000000001 в ней сдвигается 1 на определенное число и по этой маске определяется какой бит
               // есть в числе на месте единицы, число закрывается маской в которой 1 это условная дырка
               //результат ноль или число отличное от нуля так как единица на любом месте дает произвольное число
               // операция побитовое И дает единицу бита если в исходном бите также единица в остальных случаях ноль
               int pixl = pix&(short)1<<(8-y);
               if(pixl==0)System.out.print("0");else System.out.print("1");
               System.out.print(" ");
               //System.out.println("ind "+index_bit);
               //if(index_bit==63){index_bit=-1; index_in_arrlong++; }

           }
           System.out.println();
       }

   }

    static int get_intGreyColor(BufferedImage img,int x, int y){
        int val = img.getRGB(x, y);
        return  (int) (((val >> 16) & 0xff) * 0.299 + ((val >> 8) & 0xff) * 0.587 + (val & 0xff) * 0.114);
    }

    public static void main(String[] args) throws Exception {
        /*static int[][] coords_places_of_nicks = {{297,320},{15,253},{15,120},{264,67},{543,120},{543,253}};

        static int[][] coord_left_up_of_tables = {{0,0},{640,0},{1280,0},{0,469},{640,469},{1280,469}};

        static int[][] coords_buttons = {{382,287},{144,231},{156,133},{242,104},{459,133},{473,231}};

        static int[][] coords_cards_hero = {{287,286},{331,286}};*/
        /*OCR ocr = new OCR("",1,new BufferedImage[]{read_image("Mtest\\wins5p2").
                getSubimage(coord_left_up_of_tables[4][0],coord_left_up_of_tables[4][1],639,468),null});*/
        OCR ocr = new OCR("",1,new BufferedImage[]{read_image("Mtest\\win3p5").
                getSubimage(coord_left_up_of_tables[2][0],coord_left_up_of_tables[2][1],639,468),null});
        UseTesseract useTesseract =new UseTesseract();
        UseTesseract useTesseract_ltsm = new UseTesseract(7);
        CaptureVideo captureVideo = new CaptureVideo("");
        Settings settings = new Settings();
        /*BufferedImage win = read_image("Mtest\\win_long_nick");
        save_image(win.getSubimage(640+12,469+120,87,15),"Mtest\\long_nick");
        System.out.println(useTesseract.get_ocr(ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no(
                ocr.get_scale_image(win.getSubimage(640+12,469+120,87,15),4),true),100),"nicks").trim());
        BufferedImage long_nick = win.getSubimage(640+12,469+120,87,15);
        save_image(Thumbnails.of(ocr.set_grey_and_inverse_or_no
                (long_nick,true)).size(long_nick.getWidth(),long_nick.getWidth()).asBufferedImage(),"Mtest\\img8x8");*/


      /*  List<long[]> testarr = new ArrayList();
        long time = 0;
        for(File a: new File("F:\\Moe_Alex_win_10\\JavaProjects\\ForGoodGame\\original").listFiles()){
            if(a.isFile()){

                BufferedImage image = ImageIO.read(a);
                long s = System.currentTimeMillis();
                System.out.println(a.getName()+"     "); ;
                testarr.add(get_arr_longs_of_img(image,100));
                time+=System.currentTimeMillis()-s;
            }
        }*/
        /*System.out.println("TIME "+time);

        String t = "1111111000000000100000000000111111000000000000001001111111111111";
        String t2 = "1000111000000000100000000000111111000000000000001111111111111100";
        String t_int = "10011111111111111111111111111111";

        String tim = "1111 1110 0000 0000      1000 0000 0000 1111        1100 0000 0000 0000       1001 1111 1111 1111";
        String t2im = "1000 1110 0000 0000     1000 0000 0000 1111        1100 0000 0000 0000       1111 1111 1111 1100";
        long a = Long.parseUnsignedLong(t,2);
        long a2 = Long.parseUnsignedLong(t2,2);
        int i = Integer.parseUnsignedInt(t_int,2);

        *//*a2 = a2&(long) Math.pow(2,63);
        System.out.println("a2 "+Long.toBinaryString((long) Math.pow(2,63)));*//*
        System.out.println("6 bit "+Long.toBinaryString(a2 & ((long) 1<<0)));
        System.out.println(Integer.toBinaryString(((byte)a2 & 0xFF) + 256).substring(1));
        System.out.println(Integer.toBinaryString(((byte)a2+256)%256));
        long caa2 = a^a2;
        System.out.println(Long.toBinaryString(caa2));
        //byte[] bytes = _long_to_arr_bytes(a2);
        System.out.println("bites");
        //for(byte by:bytes) System.out.println(Integer.toBinaryString((by + 256) % 256));
        System.out.println("last "+Long.toBinaryString((a2>>56&0x000000ff)));
        System.out.println("bite "+Integer.toBinaryString(((byte)(a2>>56&0x000000ff) + 256) % 256));
        System.out.println("int "+Integer.toBinaryString(i));
        System.out.println("m "+Integer.toBinaryString((i>>24&0x000000ff)));

        *//*get_count_one_in_numbers(a);
        get_count_one_in_numbers(a2);*//*
        set_count_one_in_numbers();
        System.out.println(get_count_one_in_numbers(0l));
        System.out.println(get_count_one_in_numbers(1l));
        System.out.println(get_count_one_in_numbers(3l));
        long[] arr_long = get_arr_longs_of_img(read_image("original\\B.A.Z.U.KA"),100);
        System.out.println();
        show_img_from_arr_long(arr_long);*/
        //for(long arr:arr_long) System.out.println(Long.toBinaryString(arr));


        /*long[][] array = testarr.toArray(new long[testarr.size()][]);

        Arrays.sort(array, Comparator.comparingInt(arr -> (int) arr[0]));
      for(long[] ls:array) System.out.println(ls[0]+"  "+ls[1]);
        System.out.println(Arrays.binarySearch(array[1],100));*/
       /* System.out.println((new File(System.getProperty("java.io.tmpdir"), "tess4j")).getPath());
        System.out.println(System.getProperty("jna.library.path"));*/
        /*int a =-1;
        System.out.println(-a);
        long s =System.currentTimeMillis();
        for(int i=0; i<1_000_000; i++){
            long[] id = new long[18];
            for(int in = 0; in<18; in++)
            if(i!=16)id[in]=System.nanoTime();
            id[16]=(long) (Math.random() * 600) + 100;
            System.out.println(id[16]);
            //long key =  (long)(Math.random() * (700_000_000L - 100_000_000L)) + 100_000_000L;
            long key = ThreadLocalRandom.current().nextLong(100_000_000,700_000_000);
            //System.out.println(key);
            sortedmap_all_imgs_pix_of_nicks.put(key,id);
        }
        System.out.println("list random time "+(System.currentTimeMillis()-s));
        s =System.currentTimeMillis();
        for(int i=0; i<100; i++){
            long[] id = new long[18];
            for(int in = 0; in<18; in++)
                if(in!=16)id[in]=System.nanoTime();
            id[16]=(long) (Math.random() * 600) + 100;
            get_number_img_nicks(id,6);
            //settings.write_nicks_img(Long.toString(id[16]),id[16],id);
        }
        System.out.println("time "+(System.currentTimeMillis()-s));*/

        //540 437
        /*BufferedImage img  = read_image("nick_l").getSubimage(540,437,82,15);
        save_image(ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no(ocr.get_scale_image(img,4),true),100),"Mtest\\sub_nickl2");
        System.out.println(useTesseract_ltsm.get_ocr(ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no(ocr.get_scale_image(img,4),true),100),"nicks"));*/

        /*System.out.println( hashmap_id_img_pix_nick.get(108831758170400L));
        System.out.println( hashmap_id_img_pix_nick.get(108831771956300L));
        for(Map.Entry<Long,long[]>entry:sortedmap_all_imgs_pix_of_nicks.entrySet())
            if(entry.getValue()[16]==108831758170400L||entry.getValue()[16]==108831771956300L) {
                            long[] data = Arrays.copyOf(entry.getValue(),16);

            show_img_from_arr_long(data);
                System.out.println();
                System.out.println();
            }*/
       /* BufferedImage img_light = read_image("Mtest\\poker chips l");
        BufferedImage img_dark = read_image("Mtest\\poker chips d");
        save_image(getGrayScale(img_light),"Mtest\\pokerchipsl_gs");
        save_image(getGrayScale(img_dark),"Mtest\\pokerchipsd_gs");
        System.out.println(ocr.get_max_brightness(img_light));
        System.out.println(ocr.get_max_brightness(img_dark));
        save_image(ocr.get_white_black_image(img_light,161),"Mtest\\pc_l");
        save_image(ocr.get_white_black_image(img_dark,129),"Mtest\\pc_d");
        System.out.println("l "+get_brightness(img_light));
        System.out.println("d "+get_brightness(img_dark));
        float b =1;
        for(int i=0; i<100; i++){ b+=0.005;
            System.out.println("b "+b+" "+get_brightness(re_bright(img_dark,b)));
        }

        save_image(re_bright(img_dark,1.115f),"Mtest\\pc_d_rebr");*/

       // OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(1);
     //compare_binar_imgs(read_image("test\\poker chips_178715279560800_105"),read_image("test\\poker chips_178703125392000_105"),105);
        /*System.out.println(get_max_brightness(read_image("error_img\\1 28")));
        System.out.println(is_error_image(read_image("error_img\\1 28")));*/
        // p for 0 1 2 3 = 2
        // p for 4 5 = 1
        /*int p =1;
        int x = coords_places_of_nicks[4][0]+p;
        int y = coords_places_of_nicks[4][1];
        int w = 82;
        int h = 15;
        // 0 2 -1 -2
        // for idimg 0 2 -1 -3
        BufferedImage cheked_img = ocr.check_free_of_kursor(x-5,y+1,w+5,h-1,240,0,0,-1,0);
        if(cheked_img==null) System.out.println("null");
        BufferedImage bufferedImage = read_image("Mtest\\winlongnick").
                getSubimage(coord_left_up_of_tables[3][0],coord_left_up_of_tables[3][1],639,468).
                getSubimage(x-5,y+1,w+5,h-1);
        save_image(cheked_img,"Mtest\\nickt3p5_2");
        System.out.println(useTesseract.get_ocr(ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no(ocr.get_scale_image(cheked_img,4),true),105),"nicks"));*/
        //long[] test = ocr.get_img_pix(cheked_img,105);
        //show_img_from_arr_long(test);

        //System.out.println("b "+get_max_brightness(read_image("for_ocr_number\\osr_0t1")));
        //get_number_hand(read_image("for_ocr_number\\osr_0t1"),ocr);
       /* for(int i=130; i<160; i++)
            System.out.println("GREY "+i);
        save_image(ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no(image,true),105),"lastcards2\\bwcards\\"+a.getName());
        String tess = useTesseract.get_ocr(ocr.get_white_black_image(image,100),"stacks").trim();
        System.out.println(tess+"        "+a.getName());
        save_image(ocr.get_white_black_image(image,100),"test2\\_"+tess);*/
        int c = -1;
       /* for(File a: new File("F:\\Moe_Alex_win_10\\JavaProjects\\ForGoodGame\\test5").listFiles()){
            if(a.isFile()){
                BufferedImage image = ImageIO.read(a);
                //for(int i=79; i<90; i++)
                    save_image(ocr.get_white_black_image
                        (ocr.set_grey_and_inverse_or_no(image,true),80),"test5\\shab\\"+a.getName().substring(a.getName().lastIndexOf("_")));


            }
        }*/

      /*  BufferedImage image = read_image("test5\\_0_34");
        for(int i=75; i<100; i++)save_image(ocr.get_white_black_image
                (ocr.set_grey_and_inverse_or_no(image,true),i),"test4\\_"+i);*/

         //save_ObjectInFile();
       /*BufferedImage image = read_image("test3\\0\\_0");
       *//* for(int i=100; i<200; i++){
            System.out.println(useTesseract.get_ocr(ocr.get_white_black_image(image,i),"stacks"));
            save_image(ocr.get_white_black_image(image,i),"test2\\"+i);
        }*//*
        //File test = new File(home_folder+"test3\\test\\");
        image = image.getSubimage(25,3,26,5);
        save_image(image,"test\\t\\sm");*/
       /* BufferedImage img_5er = read_image("test3\\0\\_0_c1_c9s");
        BufferedImage img_5sh = read_image("lastcards2\\bwcards\\card_5c.png");
        System.out.println(ocr.compare_buffred_images(img_5sh,img_5er,10));*/
/*
        Settings.setting_cupture_video();
        for(long[] nick:sortedmap_all_imgs_pix_of_nicks.values()){
            show_img_from_arr_long(nick);
            System.out.println();
            System.out.println();

        }*/
       /* Settings.setting_cupture_video();
        for(long[] num:_long_arr_cards_for_compare)show_img_from_arr_long(num,14,14);*/
        /*BufferedImage img_5sh = read_image("lastcards2\\bwcards\\card_Tc.png");
        show_img_from_img(img_5sh,15,17);


        BufferedImage img_5col = read_image("lastcards2\\card_Tc").getSubimage(2,1,14,14);;
       long[]  num = ocr.get_longarr_HashImage(img_5col,0,0,14,14,3,150);


        show_img_from_arr_long(num,14,14);


        System.out.println("a2 "+Long.toBinaryString(num[2]));*/




        /*System.out.println(useTesseract.get_ocr(ocr.get_white_black_image
                (ocr.set_grey_and_inverse_or_no(ocr.get_scale_image(read_image("test5\\table_3"),2),true),100),"stacks"));
        ocr.get_white_black_image(ocr.get_scale_image(ocr.set_grey_and_inverse_or_no(read_image("test5\\table_3"),true),2),100)*/

        /*System.out.println("***");
        System.out.println(useTesseract.get_ocr(ocr.get_white_black_image(
                ocr.get_scale_image(ocr.set_grey_and_inverse_or_no(read_image("test5\\_stack_39_105"),true),2),125),"stacks"));
        System.out.println("***");*/

        /*save_image(ocr.get_white_black_image(
                ocr.set_grey_and_inverse_or_no(ocr.get_scale_image(read_image("test5\\table_4_stack_"),2),true),125),"Mtest\\errorim4");*/

        //save_image(ocr.get_white_black_image(read_image("Mtest\\errorbig1"),125),"Mtest\\errorbig2");
        //System.out.println(ocr.get_int_MaxBrightnessMiddleImg(read_image("test5\\_stack_39_105"),0,0,72,13));
        /*String zer ="01110" +
                    "10001" +
                    "10001" +
                    "10001" +
                    "10001" +
                    "10001" +
                    "10001" +
                    "10001" +
                    "01110";

   read_ObjectFromFile();
  short[] num = get_shortarr_HashNumberImg(read_image("test5\\shab\\_1_stack_80.5_125"),0,2,72,9,130);

      ;
      short[] eight = get_shortarr_HashShablonNumber(5,num,1);
      short[] zero = get_shortarr_HashShablonNumber(5,num,8);
      short[] dot =  get_shortarr_HashShablonNumber(2,num,15);
        short[] five =  get_shortarr_HashShablonNumber(5,num,19);

        show_shortarr_HashShablonNumber(eight);
        show_shortarr_HashShablonNumber(zero);
        show_shortarr_HashShablonNumber(dot);
        show_shortarr_HashShablonNumber(five);
      _short_arrs_shablons_numbers[10] = dot;
      _short_arrs_shablons_numbers[8] = eight;
      _short_arrs_shablons_numbers[0] = zero;
        _short_arrs_shablons_numbers[5] = five;*/
      //save_ObjectInFile();
        BufferedImage bufferedImageframe = read_image("test4\\8985\\_1_147");
        boolean is_correct_nicks = true;int[] correction_for_place_of_nicks = {1,2,2,2,1,1};
        for(int img_nicks=0; img_nicks<6; img_nicks++ ){
            int x_of_nick = coords_places_of_nicks[img_nicks][0]+correction_for_place_of_nicks[img_nicks]-5;
            int y_of_nick = coords_places_of_nicks[img_nicks][1]+1;
            int width_nick = 87;
            int height_nick = 14;
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++ "+img_nicks);
            if(is_CorrectImageOfNumberHandAndNicks(x_of_nick,y_of_nick,width_nick,height_nick,240,240,bufferedImageframe))continue;
            is_correct_nicks = false;
            //break;
        }


        System.out.println(is_correct_nicks);
    }




}
