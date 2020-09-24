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

    public static void save_image(BufferedImage image,String name_file){
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


    static void test_work_compare_nicks_img(){
        new Settings();
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






    static void show_img_from_arr_long(long[] arr_long){
        int count_pix = -1;
        for(int y=0; y<13; y++){
            for(int x=0; x<79; x++){
                if(y<3&&x==0)continue;
                //System.out.println(y+" "+x);
                //count_pix++;
                int coord_in_arr_long = (y+13*x)-3;
                int index_bit = coord_in_arr_long%64;
                int index_in_arrlong = coord_in_arr_long/64;
                //index_bit++;
                //System.out.println(coord_in_arr_long+"  "+index_in_arrlong+"  "+index_bit);
                long pix = arr_long[index_in_arrlong];
                // 1<<число сдвига маска единицы 000001 двигаешь еденицу влево
                pix = pix&(long)1<<(63-index_bit);
                if(pix==0)System.out.print("0");else System.out.print("1");
                //System.out.println("ind "+index_bit);
                //if(index_bit==63){index_bit=-1; index_in_arrlong++; }
            }
            System.out.println();
        }
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
    public static void main(String[] args) throws Exception {
        /*static int[][] coords_places_of_nicks = {{297,320},{15,253},{15,120},{264,67},{543,120},{543,253}};

        static int[][] coord_left_up_of_tables = {{0,0},{640,0},{1280,0},{0,469},{640,469},{1280,469}};

        static int[][] coords_buttons = {{382,287},{144,231},{156,133},{242,104},{459,133},{473,231}};

        static int[][] coords_cards_hero = {{287,286},{331,286}};*/
        OCR ocr = new OCR("",1,new BufferedImage[]{read_image("Mtest\\winlongnick").getSubimage(coord_left_up_of_tables[3][0],coord_left_up_of_tables[3][1],639,468),null});
        UseTesseract useTesseract =new UseTesseract();
        UseTesseract useTesseract_ltsm = new UseTesseract(7);
        CaptureVideo captureVideo = new CaptureVideo("");
        Settings settings = new Settings();
        BufferedImage win = read_image("Mtest\\win_long_nick");
        save_image(win.getSubimage(640+12,469+120,87,15),"Mtest\\long_nick");
        System.out.println(useTesseract.get_ocr(ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no(
                ocr.get_scale_image(win.getSubimage(640+12,469+120,87,15),4),true),100),"nicks").trim());
        BufferedImage long_nick = win.getSubimage(640+12,469+120,87,15);
        save_image(Thumbnails.of(ocr.set_grey_and_inverse_or_no
                (long_nick,true)).size(long_nick.getWidth(),long_nick.getWidth()).asBufferedImage(),"Mtest\\img8x8");

        //System.out.println("win "+captureVideo.is_check_free_of_kursor(640+264,468+67,82,15,100,win));
        //System.out.println(ocr.check_free_of_kursor());
       /* BufferedImage nick = read_image("SittingD_new");
        save_image(get_white_black_image(nick,100),"old_wb");
        save_image(get_white_black(nick),"new_wb");

        System.out.println(useTesseract.get_ocr(nick,"nicks"));

        System.out.println(useTesseract.get_ocr(get_white_black(nick),"nicks"));

        BufferedImage window = read_image("allwindow2");
        BufferedImage white_nick = window.getSubimage(15,253,82,15);
        BufferedImage black_nick = window.getSubimage(640+15,469+253,82,15);
        save_image(ocr.get_scale_image(get_white_black_image(ocr.set_grey_and_inverse_or_no(white_nick,true),100),2),"white_wbx2");
        save_image(ocr.get_scale_image(get_white_black_image(ocr.set_grey_and_inverse_or_no(black_nick,true),150),2),"black_wbx2");
        save_image(black_nick,"black_nick");

        System.out.println(useTesseract.get_ocr(ocr.get_scale_image(get_white_black_image(ocr.set_grey_and_inverse_or_no(black_nick,true),150),2),"nicks"));
        System.out.println(useTesseract.get_ocr(ocr.get_scale_image(get_white_black_image(ocr.set_grey_and_inverse_or_no(white_nick,true),100),2),"nicks"));
        save_image(get_white_black(ocr.set_grey_and_inverse_or_no(ocr.get_scale_image(white_nick,2),true)),"white_wbx2_n");
        save_image(get_white_black(ocr.set_grey_and_inverse_or_no(ocr.get_scale_image(black_nick,2),true)),"black_wbx2_n");
        save_image(get_white_black(nick),"wb_nick");*/
        /*float brightenFactor = 1.2f;
        RescaleOp op = new RescaleOp(brightenFactor, 0, null);
        op.filter(black_nick, black_nick);*/
        //save_image(black_nick,"black_nick_br");

       /* BufferedImage sw = get_white_black(ocr.get_scale_image(get_white_black_image(ocr.set_grey_and_inverse_or_no(white_nick,true),100),2));
        BufferedImage sb = get_white_black(ocr.get_scale_image(get_white_black_image(ocr.set_grey_and_inverse_or_no(black_nick,true),150),2));
        save_image(sw,"sw");
        save_image(sb,"sb");*/
        /*save_image(get_white_black_image(ocr.set_grey_and_inverse_or_no(white_nick,true),100),"white_wbx2_o");
        save_image(get_white_black_image(ocr.set_grey_and_inverse_or_no(black_nick,true),150),"black_wbx2_o");
        long s =System.currentTimeMillis();
        System.out.println(useTesseract.get_ocr(get_white_black_image(nick,100),"nicks"));
        System.out.println((System.currentTimeMillis()-s));
        System.out.println(useTesseract.get_ocr(get_scaled(nick),"nicks"));

       save_image(get_scaled(nick),"scaled_nick");*/

        /*save_image(ocr.get_scale_image(black_nick,2),"black_nickX2");
        save_image(get_white_black_image(ocr.set_grey_and_inverse_or_no(ocr.get_scale_image(white_nick,2),true),100),"WHx2wbaver");
        save_image(get_white_black_image(ocr.set_grey_and_inverse_or_no(ocr.get_scale_image(black_nick,2),true),150),"BLx2wbaver");
        save_image(get_sharp(ocr.get_scale_image(read_image("original\\YukKiVH"),4.0)),"YukKiVH_sharp");
        save_image(ocr.get_scale_image(read_image("original\\YukKiVH"),10),"YukKiVH_sharp2");
        save_image(ocr.get_scale_image(read_image("original\\YukKiVH"),20.0),"YukKiVH_sharp2_2");*/
       // System.out.println(is_error_image(read_image("error_img\\1 13")));
       //System.out.println(is_error_image(read_image("error_img\\6 209")));
        /*System.out.println(is_error_image(read_image("Mtest\\tiaczek")));
        System.out.println(is_error_image(read_image("original\\3")));*/
       /* save_image(ocr.get_white_black_image(read_image("Mtest\\tiaczek"),100),"Mtest\\tiaczek_100");
        save_image(ocr.get_white_black_image(read_image("Mtest\\tiaczek"),150),"Mtest\\tiaczek_150");*/
        //save_image(ocr.get_white_black_image(ocr.get_scale_image(read_image("for_ocr_number\\2040037899"),2),200),"Mtest\\n_200");
        //System.out.println((((double)15/2)));

        //System.out.println("win "+captureVideo.is_check_free_of_kursor(0,0,82,15,100,read_image("Mtest\\tiaczek")));

/*
        BufferedImage black_white_img = ocr.set_grey_and_inverse_or_no(read_image("original\\BryanKearney 138").getSubimage(2,1,77,13),true);
        save_image(ocr.get_scale_image(black_white_img,0.5),"Mtest\\BryanKearney 138_cut_smalled");*/
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
        int p =1;
        int x = coords_places_of_nicks[4][0]+p;
        int y = coords_places_of_nicks[4][1];
        int w = 82;
        int h = 15;
        // 0 2 -1 -2
        // for idimg 0 2 -1 -3
        BufferedImage cheked_img = ocr.check_free_of_kursor(x-5,y+1,w+5,h-1,240,0,0,0,0);
        if(cheked_img==null) System.out.println("null");
        BufferedImage bufferedImage = read_image("Mtest\\winlongnick").
                getSubimage(coord_left_up_of_tables[3][0],coord_left_up_of_tables[3][1],639,468).
                getSubimage(x-5,y+1,w+5,h-1);
        save_image(cheked_img,"Mtest\\longnick2foridimg");
        System.out.println(useTesseract.get_ocr(ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no(ocr.get_scale_image(cheked_img,4),true),105),"nicks"));
        long[] test = ocr.get_img_pix(cheked_img,105);
    }




}
