package org.trenkvaz.main;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.*;
import java.util.*;
import java.util.List;

import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.main.OCR.is_noCursorInterferenceImage;

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

    static void save_ObjectInFile(Object ob, String name_file){
        try {FileOutputStream file=new FileOutputStream(home_folder+"\\"+name_file+".file");
            ObjectOutput out = new ObjectOutputStream(file);
            out.writeObject(ob);
            out.close();
            file.close();
        } catch(IOException e) {
            System.out.println(e);
        }
    }



    static <T> T read_ObjectFromFile(String name_file){
        T type = null;
        try {	FileInputStream file=new FileInputStream(home_folder+"\\"+name_file+".file");
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
                int grey = get_intGreyColor(image,i,j);
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
                int grey = get_intGreyColor(img,x,y);
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
            int grey = get_intGreyColor(image,x,y);
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


    static int check_free_of_kursor(int X, int Y, int w, int h, int limit_grey,BufferedImage frame){
        //save_image(frame.getSubimage(X,Y,w,h),"tables_img\\t_");
        int max = 0;
        for(int x=X; x<w+X; x++){
            for(int y=Y; y<h+Y; y+=h-1){
                int grey = get_intGreyColor(frame,x,y);
                //System.out.println("1 grey "+grey);
                if(grey>max)max=grey;
                //if(grey>limit_grey)return null;
            }
        }
        for(int y=Y; y<h+Y; y++)
            for(int x=X; x<w+X; x+=w-1){
                int grey = get_intGreyColor(frame,x,y);
                //System.out.println("2 grey "+grey);
                if(grey>max)max=grey;
                //if(grey>limit_grey)return null;
            }
        //if(max>80)System.out.println("MAX "+max);
        //return frame.getSubimage(X,Y,w,h);
        return max;
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
                int grey1 = get_intGreyColor(img1,x,y);
                if(grey1<limit_grey)grey1=1;else grey1=0;
                int grey2 = get_intGreyColor(img2,x,y);
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
                int grey = get_intGreyColor(image,i,j);
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



    record Cards(BufferedImage[] card,String nominal_card){}

    public static List<BufferedImage[]> list_test_numberhands = new ArrayList<>();


    public static List<Cards> list_test_cards = new ArrayList<>();



    static List<int[]> get_list_intarr_HashNumberImg(BufferedImage image_table, int X, int Y, int W, int H, int limit_grey,
                                                     int indents_left_right, int size_dot_in_pix, int size_symbol, int size_intarr_hashimage){

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
        for(int[] num:coords_line_x_for_one_num){
            // для записи точки
            if(num==null) { result.add(null);
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
        return result;
    }


    static float get_OcrNum(List<int[]> list_hash_nums){
        int first_of_pair_error = 0, second_of_pair_error = 0, limit_error = 10, total_error = 0;
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
                total_error+= get_AmountOneBitInInt(shablons_numbers_0_9_for_stacks[number][ind_num]^list_hash_nums.get(hash_num)[ind_num]);
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
        float result = 0;
        try {
            result =  Float.parseFloat(res);
        } catch (Exception e){
            return -1;
        }

        return result;
    }


    static int get_AmountOneBitInInt(int lng){
        return count_one_in_numbers[(short)(lng>>16)+32768]+count_one_in_numbers[(short)(lng)+32768];
    }


    static short[] get_shortarr_HashShablonNumber(int amount_line_of_num, short[] shortarr_hashnumberimg,int start_line){
        short[] shortarr_shablon = new short[amount_line_of_num];
        for(int i=start_line, ind =0; i<amount_line_of_num+start_line; i++,  ind++) shortarr_shablon[ind] = shortarr_hashnumberimg[i];
        return shortarr_shablon;
    }


    static void show_HashShablonNumber(int[] shortarr_shablon,int W,int H){


        for(int y=0; y<H; y++){
            for(int x=0; x<W; x++){
                //if(y<3&&x==0)continue;
                //System.out.println(y+" "+x);
                //count_pix++;
               int coord_in_arr_long = (y+H*x);
                //System.out.println(coord_in_arr_long);
               int index_bit = coord_in_arr_long%32;
               int index_in_arrlong = coord_in_arr_long/32;
                //index_bit++;
                //System.out.println(coord_in_arr_long+"  "+index_in_arrlong+"  "+index_bit);
                int pix = shortarr_shablon[index_in_arrlong];
                // 1<<число сдвига маска единицы 000001 двигаешь еденицу влево
                // пикс пример число шорт 16 битов(0..01) маска единицы 0000000000000001 в ней сдвигается 1 на определенное число и по этой маске определяется какой бит
                // есть в числе на месте единицы, число закрывается маской в которой 1 это условная дырка
                //результат ноль или число отличное от нуля так как единица на любом месте дает произвольное число
                // операция побитовое И дает единицу бита если в исходном бите также единица в остальных случаях ноль
                //int s =31;
                //if(index_in_arrlong==1)s = 21;
                int pixl = pix&1<<(31-index_bit);

                if(pixl==0)System.out.print("0");else System.out.print("1");
                System.out.print(" ");
                //System.out.println("ind "+index_bit);
                //if(index_bit==63){index_bit=-1; index_in_arrlong++; }

            }
            System.out.println();
        }
        System.out.println();
        /* int xy = -1;
        for(int x=0; x<W; x++){
            for(int y=0; y<H; y++){
                xy++;
                // индекс бита это остаток от деления координаты на количество битов содержащихся в числе
                int index_bit = xy%32;
                // индекс числа с битами целое число от деления координаты на количество битов содержащихся в числе
                int index_in_arrlong = xy/32;
                //index_bit++;
                //System.out.println(coord_in_arr_long+"  "+index_in_arrlong+"  "+index_bit);
                int pix = shortarr_shablon[index_in_arrlong];

                // так как бит берется слева его индекс это сдвиг маски(единички) на (количество битов в используемом числе минус 1 (так как индексация с нуля)),
                // пример инт 31 лонг 63 и т.д
                // НЕ АКТУАЛЬНО В МЕТОДЕ ПОЛУЧЕНИЯ ХЕША СДЕЛАН СДВИГ В ПОСЛЕДНЕМ ЧИСЛЕ ВЛЕВО ЕСЛИ БИТОВ БОЛЬШЕ ЧЕМ ПИКСЕЛЕЙ
                // если битов из числа используется меньше возможного то берется разница между используеммым количеством и вместимостью битов последнего используемого числа массива
                // пример два 32 битных числа, а всего используется 54 бита, 1 число используется полностью индекс бита будет сдвиг маски 1 на (31-индекс бита)
                // 2 число используется не полностью 54-32 только 22 бита минус 1 для сдвига получаем сдвиг маски на (21-индекс бита)
                int s =31;
                //if(index_in_arrlong==1)s = 21;
                int pixl = pix&1<<(s-index_bit);

                int p =0;
                if(pixl!=0)p=1;
                //System.out.println(index_in_arrlong+"   "+index_bit+"  "+p);
                System.out.print(p+" ");
                    *//*System.out.print("0");else System.out.print("1");
                System.out.print(" ");*//*
            }
            System.out.println();
        }*/
    }

    static int get_intGreyColor(BufferedImage img,int x, int y){
        int val = img.getRGB(x, y);
        return  (int) (((val >> 16) & 0xff) * 0.299 + ((val >> 8) & 0xff) * 0.587 + (val & 0xff) * 0.114);
    }

    public static void main(String[] args) throws Exception {

        OCR ocr = new OCR("", 1, new BufferedImage[]{read_image("Mtest\\win3p5").
                getSubimage(coord_left_up_of_tables[2][0], coord_left_up_of_tables[2][1], 639, 468), null});
        UseTesseract useTesseract = new UseTesseract();
        UseTesseract useTesseract_ltsm = new UseTesseract(7);
        CaptureVideo captureVideo = new CaptureVideo("");
        Settings.setting_cupture_video();



        /*save_image(ocr.get_white_black_image(ocr.set_grey_and_inverse_or_no(read_image("Mtest\\4_289"),true),80),"Mtest\\r0.5");

       List<int[]> num = get_list_intarr_HashNumberImg(read_image("Mtest\\4_289"),0,1,54,9,175,0,2,6,2);


       //show_HashShablonNumber(num.get(0),6,9);
        show_HashShablonNumber(shablons_numbers_0_9_for_stacks[5],8,12);
        //int[] shab = shablons_numbers_0_9[0].clone();*/



        ////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
}
