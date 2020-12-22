package org.trenkvaz.main;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.IplImage;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.trenkvaz.database_hands.Work_DataBase;
import org.trenkvaz.stats.MainStats;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RescaleOp;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.main.OcrUtils.*;
import static org.trenkvaz.ui.StartAppLauncher.*;


class TestCurrentHand {
    int table;
    long time_hand;
    boolean testStartByNumHand =  false;
    int poker_position_of_hero = -1;
    int position_bu_on_table = 0;
    int testFinished = 0;
    String[] nicks = new String[6], cards_hero = {"",""};
    Float[] startStacks = new Float[6];
    int[] poker_positions_by_pos_table_for_nicks;


    List<List<Float>> preflopActionsStats = new ArrayList<>(6);
    List<List<String>> testGetTurnPlayers = new ArrayList<>(6);

    public TestCurrentHand(){
        for(int i=0; i<6; i++){
            testGetTurnPlayers.add(new ArrayList<>());
            startStacks[i] = 0f;
        }
    }

    public void setStartConditions(CurrentHand currentHand,boolean testStartByNumHand){
        this.table = currentHand.testTable; this.time_hand = currentHand.time_hand; this.testStartByNumHand = testStartByNumHand;
        this.poker_position_of_hero = currentHand.poker_position_of_hero; this.position_bu_on_table = currentHand.position_bu_on_table;
        this.cards_hero[0] = currentHand.cards_hero[0];this.cards_hero[1] = currentHand.cards_hero[1];
        this.nicks[0] = nick_hero;
        this.poker_positions_by_pos_table_for_nicks = currentHand.poker_positions_by_pos_table_for_nicks.clone();
        this.preflopActionsStats = currentHand.preflopActionsStats;
        this.startStacks = currentHand.startStacks;
    }

    public void setNicks(String[] nicks1){ nicks = nicks1.clone(); }

    //public void setStartStacks(Float[] startStacks1){ startStacks = startStacks1; }

    //public void setOldStartStacks(Float[] oldStartStacks1){oldStartStacks = oldStartStacks1;}

    //public void setPreflop_by_positions( ArrayList<ArrayList<Float>> preflop_by_positions1){ preflop_by_positions = preflop_by_positions1;}

    //public void setPreflop_actions_for_stats(List<List<Float>> preflop_actions_for_stats1){preflop_actions_for_stats = preflop_actions_for_stats1;}

   // public void setPreflopActionsStats(List<List<Float>> preflopActionsStats1){preflopActionsStats = preflopActionsStats1;}

    public void setTestGetTurnPlayers(int pokPos,String act){testGetTurnPlayers.get(pokPos).add(act);}

    public void setTestFinished(int testFinished1){testFinished = testFinished1;}


    public void finalCurrendHand(){
        // расстановка ников по покерным позициям
        String[] nicks_by_positions = new String[6];
        for(int i=0; i<6; i++){
            if(nicks[poker_positions_by_pos_table_for_nicks[i]-1]==null)continue;
            nicks_by_positions[i] = nicks[poker_positions_by_pos_table_for_nicks[i]-1];
        }
        nicks = nicks_by_positions;
    }
}


public class Testing {


    static synchronized void show_test_total_hand(TestCurrentHand testCurrentHand,boolean isError){

        testCurrentHand.finalCurrendHand();
        //if(!testCurrentHand.currentHand.creat_PreflopActionsInHandForCountStats())return;
        //TEST
        //if(!testCurrentHand.currentHand.is_start_turn)return;

        Date d = new Date();
        DateFormat formatter= new SimpleDateFormat("HH.mm.ss");
        String Z = formatter.format(d);

        //if(testCurrentHand.currentHand.cards_hero[0].equals("7c")&&testCurrentHand.currentHand.cards_hero[1].equals("7h")) System.err.println("===================================================");
       /* if(testCurrentHand.currentHand.is_start_turn) System.out.print(RED);
        else System.out.print(RESET);*/
        String logtest = "";
        if(isError){System.out.print(RED+"ERROR HAND !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  ");  logtest+="ERROR HAND \r\n";
            Settings.ErrorLog("ERROR  hand "+testCurrentHand.time_hand+" t "+testCurrentHand.table);
        }
        else System.out.print(RESET);

        System.out.println(Z+"  "+testCurrentHand.time_hand+"     ****** cards "+testCurrentHand.cards_hero[0]+testCurrentHand.cards_hero[1]+
                " bu "+testCurrentHand.position_bu_on_table +" table "+testCurrentHand.table);

        logtest += Z+"  "+testCurrentHand.time_hand+"     ****** cards "+testCurrentHand.cards_hero[0]+testCurrentHand.cards_hero[1]+"  bu "+testCurrentHand.position_bu_on_table +" table "+testCurrentHand.table+" \r\n";

        boolean testbreak = false;
        boolean is_save_test_list = false;
        System.out.print(RESET);
        /*int maxsizeoldpreflop = testCurrentHand.preflop_by_positions.stream().mapToInt(List::size).max().getAsInt();
        int maxsizeoldpreflopstat = testCurrentHand.preflop_actions_for_stats.stream().mapToInt(List::size).max().getAsInt();*/
        int maxsizpreflop = testCurrentHand.preflopActionsStats.stream().mapToInt(List::size).max().getAsInt();


       String testGetTurnPlayer = "Hand "+testCurrentHand.time_hand+"\r\n";


        for(int i=0; i<6; i++) {
            logtest += rightpad(testCurrentHand.nicks[i],16)+"    "+rightpad(testCurrentHand.startStacks[i].toString(),6)+"  ";
            if(testCurrentHand.poker_position_of_hero==i) System.out.print(BLUE+rightpad(testCurrentHand.nicks[i],16)+"    "
                    +rightpad(testCurrentHand.startStacks[i].toString(),6)+"  ");
            else System.out.print(rightpad(testCurrentHand.nicks[i],16)+"    "+rightpad(testCurrentHand.startStacks[i].toString(),6)+"  ");

            if(testCurrentHand.nicks[i]==null) {       Settings.ErrorLog(" NO NICK  hand "+testCurrentHand.time_hand+" t "+testCurrentHand.table+" p "+i);
            /*for(BufferedImage image:testRecPlayers[i].imges_nick)
            Testing.save_image(image,     "test5\\"+hand+"\\nick_"+i);*/
            }
            if(testCurrentHand.cards_hero[0].equals(""))Settings.ErrorLog("NO CARDS hand "+testCurrentHand.time_hand+" t "+testCurrentHand.table+" p "+i);

            if(testCurrentHand.startStacks[i]<=0){ Settings.ErrorLog(" NO STACK  hand "+testCurrentHand.time_hand+" t "+testCurrentHand.table+" p "+i+" stack "+testCurrentHand.startStacks[i]+
                    " cards "+testCurrentHand.cards_hero[0]+testCurrentHand.cards_hero[1]);
               /* for(BufferedImage image:testRecPlayers[i].imges_stack)
                    Testing.save_image(image,     "test5\\"+hand+"\\stack_"+i);*/

            }
           if(testbreak){ System.out.println(RESET);
               logtest+="\r\n";                    continue;}

            /*for (int a=0; a<maxsizeoldpreflop; a++) {
                String action = "";
                if(a<testCurrentHand.preflop_by_positions.get(i).size())action = testCurrentHand.preflop_by_positions.get(i).get(a).toString();
                logtest+=rightpad(action,9)+" ";
                if(testCurrentHand.poker_position_of_hero==i) System.out.print(BLUE+rightpad(action,9)+" ");
                else System.out.print(rightpad(action,9)+" ");
            }
            logtest+="    _______________         ";

            if(testCurrentHand.poker_position_of_hero==i)System.out.print(BLUE+" _______________ ");
            else System.out.print(" _______________ ");

            for(int a=1; a<maxsizeoldpreflopstat; a++) {
                String action = "";
                if(a<testCurrentHand.preflop_actions_for_stats.get(i).size())action = testCurrentHand.preflop_actions_for_stats.get(i).get(a).toString();
                logtest+=rightpad(action,9)+" ";
                if(testCurrentHand.poker_position_of_hero==i) System.out.print(BLUE+rightpad(action,9)+" ");
                else System.out.print(rightpad(action,9)+" ");
            }*/




           /* logtest += "    "+rightpad(testCurrentHand.startStacks[i].toString(),6)+"  ";
            if(testCurrentHand.poker_position_of_hero==i) System.out.print(BLUE+"    "+rightpad(testCurrentHand.startStacks[i].toString(),6)+"  ");
            else System.out.print("    "+rightpad(testCurrentHand.startStacks[i].toString(),6)+"  ");*/


            if(testCurrentHand.poker_position_of_hero==i)System.out.print(BLUE+" _______________ ");
            else System.out.print(" _______________ ");

            for(int a=0; a<maxsizpreflop; a++) {
                String action = "";
                if(a<testCurrentHand.preflopActionsStats.get(i).size())action = testCurrentHand.preflopActionsStats.get(i).get(a).toString();
                logtest+=rightpad(action,9)+" ";
                if(testCurrentHand.poker_position_of_hero==i) System.out.print(BLUE+rightpad(action,9)+" ");
                else System.out.print(rightpad(action,9)+" ");
            }


            /*if(testCurrentHand.currentHand.poker_position_of_hero==i)System.out.println(BLUE);
            else */
           /* if(testCurrentHand.currentHand.is_start_flop&&testCurrentHand.currentHand.arr_continue_players_flop[i]==1) System.out.print(GREEN+"    "+testCurrentHand.currentHand.stacks_flop[i]);

            if(testCurrentHand.currentHand.is_start_turn&&testCurrentHand.currentHand.arr_continue_players_turn[i]==1) System.out.print(GREEN+"    "+testCurrentHand.currentHand.stacks_turn[i]);*/

            testGetTurnPlayer+=rightpad(testCurrentHand.nicks[i],16)+" ";
            String curact = ""; String resact = ""; int countact = 0;
            for(String act:testCurrentHand.testGetTurnPlayers.get(i)){
                if(!act.equals(curact)){ if(countact>0)resact = countact+"_"+curact;  testGetTurnPlayer+=resact; curact = act; countact=1;}
                else countact++;
                }
            resact = countact+"_"+curact;
            testGetTurnPlayer+=resact;
            testGetTurnPlayer+="\r\n";

            System.out.println(RESET);
            logtest+="\r\n";
        }

       /* if(testbreak)return;
        if(testCurrentHand.currentHand.is_allin) {

            if(testCurrentHand.testRIT){
                System.out.println(RED+"RIT ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
                Settings.ErrorLog("RIT "+testCurrentHand.currentHand.time_hand+" t "+testCurrentHand.table+" p ");
                logtest+="RIT |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\r\n";
            } else {
                System.out.println(RED+"ALLIN PREFLOP ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
                Settings.ErrorLog("ALLIN PREFLOP "+testCurrentHand.currentHand.time_hand+" t "+testCurrentHand.table+" p ");
                logtest+="ALLIN PREFLOP |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\r\n";
            }
        }*/

       /* if(testCurrentHand.currentHand.is_start_flop){
            System.out.println(GREEN+"FLOP"); logtest+="FLOP\r\n";
            for(int i=0; i<6; i++){
                if(testCurrentHand.currentHand.arr_continue_players_flop[i]==-1)continue;
                System.out.print(testCurrentHand.currentHand.nicks[i]+"  ");
                logtest+=testCurrentHand.currentHand.nicks[i]+"  ";
                if(testCurrentHand.currentHand.arr_alliner_players_flop[i]==1){
                    System.out.println(" ALLIN PREFLOP");
                    logtest+=" ALLIN PREFLOP\r\n";
                } else {
                    if(!testCurrentHand.currentHand.flop_by_positions.get(i).isEmpty()){ String firstBet = "";
                        for (int a=0; a<testCurrentHand.currentHand.flop_by_positions.get(i).size(); a++) {
                            if(testCurrentHand.currentHand.firstBetPostflopPokerPos[1]==i&&a==0)firstBet = "first bet   ";
                            logtest+=firstBet+testCurrentHand.currentHand.flop_by_positions.get(i).get(a)+"  ";
                            System.out.print(firstBet+testCurrentHand.currentHand.flop_by_positions.get(i).get(a)+"  ");
                            firstBet = "";
                        }
                    }
                    System.out.println();
                    logtest+="\r\n";
                }
            }
        }*/

        if(testCurrentHand.testStartByNumHand){
            System.out.println(RED+"START BY NUMBERHAND ////////////////////////////////////////////");
            Settings.ErrorLog("START BY NUMBERHAND "+testCurrentHand.time_hand+" t "+testCurrentHand.table+" p ");
            logtest+="START BY NUMBERHAND ////////////////////////////////////////////////\r\n";


        }

        logtest+="testFinished "+testCurrentHand.testFinished+" \r\n";
        System.out.println(RESET+"******************************************");

        logtest+="****************************************** \r\n";
        Testing.write_LogTest(logtest,"logtest");
        testGetTurnPlayer+="****************************************** \r\n";
        Testing.write_LogTest(testGetTurnPlayer,"testturnplayers");

    }

    static synchronized void testSaveImgFrameTimeHand(List<OCR.TestRecFrameTimeHand> images_framestimehands){
        int c = 0;
        for(OCR.TestRecFrameTimeHand testRecFrameTimeHand:images_framestimehands){ c++;
            saveImageToFile(testRecFrameTimeHand.imges_frame(),"test5\\_"+c+"_"+testRecFrameTimeHand.timehand());
        }

    }


    static String leftpad(String text, int length) { return String.format("%" + length + "." + length + "s", text); }

    static String rightpad(String text, int length) { return String.format("%-" + length + "." + length + "s", text); }



    public static synchronized void saveImageToFile(BufferedImage image, String name_file){
        try {
            ImageIO.write(image ,"png",new File(home_folder+"\\"+name_file+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }








    public synchronized static void saveImageToFolder(BufferedImage image, String name_file){
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

   public static void save_ObjectInFile(Object ob, String name_file){
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


    static void write_LogTest(String test,String namefile){


        try {
            OutputStream  os = new FileOutputStream(new File(home_folder+"\\test\\"+namefile+".txt"), true);
            os.write(test.getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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


    static int[] arr_AmountAmountOneBitInLongByShort(long img_min_error, long img_nick_for_compare){

       return new int[]{count_one_in_numbers[((short)(img_min_error>>48)^(short)(img_nick_for_compare>>48))+32768],
               count_one_in_numbers[((short)(img_min_error>>32)^(short)(img_nick_for_compare>>32))+32768],
               count_one_in_numbers[((short)(img_min_error>>16)^(short)(img_nick_for_compare>>16))+32768],
               count_one_in_numbers[((short)(img_min_error)^(short)(img_nick_for_compare))+32768]};

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
        int p =0, move1 = 0, move2 = 0;
        for(int y=Y; y<h+Y; y+=h-1){ p++;
            if(p==2) { move1 = 3; move2 = 2; }
            for(int x=X+move1; x<w+X-move2; x++){


                int grey = get_intGreyColor(frame,x,y);
                System.out.println("1 grey "+grey);
                if(grey>max)max=grey;
                //if(grey>limit_grey)return null;
            }
        }
        for(int x=X; x<w+X; x+=w-1)
            for(int y=Y; y<h+Y-10; y++){
                int grey = get_intGreyColor(frame,x,y);
                System.out.println("2 grey "+grey);
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


    static List<int[]> test_get_list_intarr_HashNumberImg(BufferedImage image_table, int X, int Y, int W, int H, int limit_grey,
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


    static void show_HashShablonNumber(long[] shortarr_shablon,int W,int H){

        for(int y=0; y<H; y++){
            for(int x=0; x<W; x++){
                int coord_in_arr_long = (y+H*x);
                int index_bit = coord_in_arr_long%64;
                int index_in_arrlong = coord_in_arr_long/64;
                long pix = shortarr_shablon[index_in_arrlong];
                long pixl = pix&(long) 1<<(63-index_bit);
                if(pixl==0)System.out.print("0");else System.out.print("1");
                System.out.print(" ");
            }
            System.out.println();
        }
        System.out.println();

    }


    static void show_intListHashes(List<int[]> list,int W,int H){
       for(int[] hash:list){
           if(hash==null) {System.out.println("|||||||||||||||||");continue;}
           show_HashShablonNumber(hash,W,H);
           System.out.println("===========================================================");
       }

    }





    static void get_card(long[] card_hash_from_table){
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        int total_error =0, limit_error =10;
        String[] result = new String[2];
        out: for(int nominal_ind_list = 0; nominal_ind_list<52; nominal_ind_list++){
            // сравнение количества черных пикселей между хешем_имдж из массива номиналы_карт с хешем_имдж со стола
            //System.out.println("i "+i+" nom "+nominals_cards[nominal_ind_list/4]+"  err "+abs(_long_arr_cards_for_compare[nominal_ind_list][3]-card_hash_from_table[3]));
            if(Math.abs(_long_arr_cards_for_compare[nominal_ind_list][3]-card_hash_from_table[3])>limit_error)continue;
            System.out.println("blackpix list "+_long_arr_cards_for_compare[nominal_ind_list][3]+"    "+card_hash_from_table[3]);
            total_error = 0;

            for(int ind_num=0; ind_num<3; ind_num++){
                total_error+= get_AmountOneBitInLong(_long_arr_cards_for_compare[nominal_ind_list][ind_num]^card_hash_from_table[ind_num]);
                System.out.println(nominal_ind_list+"  "+total_error);
                //if(total_error>limit_error){ continue out;  }
            }
            if(total_error>limit_error){ continue out;  }
            //System.err.println("TOTAL ERROR "+total_error);
            // если нашлось совпадение, то берется номинал карты деление на 4 для получения индекса где 13 эелементов вместо 52
            result[1]=nominals_cards[nominal_ind_list/4];

            break;
        }
        System.out.println(result[1]);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }


    String nashatimezone(long ldate){
        int chasov = 10;
        long popravka = 3600000*chasov;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String novdata = "";
        try {
            novdata = sdf.format((ldate+(popravka)));
        } catch (Exception e) {

        }
        return novdata;
    }


   static long get_long_TimeHandFromPartyHistory(String time){
       long time_hh = 0;

       DateFormat formatter= new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss",Locale.US);
        try {
            time_hh = formatter.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
       int chasov = 10;
       long popravka = 3600000*chasov;

       return time_hh+popravka;
    }



    public static int get_ArrayIndex(int[] arr,int value) {
        for(int i=0;i<arr.length;i++)
            if(arr[i]==value) return i;
        return -1;
    }


    static final String[] positions_for_query = {null,"UTG","MP","CO","BU","SB","BB"};


    private static float procents(int stata, int select){
        if(select==0)return 0;
        return ((float)stata/(float)select)*100;
    }

    private static void get_stata_one_player(String name, String stata){

        try {
            if(stata.equals("vpip_pfr_3bet")){ Object[][] stats =(Object[][]) get_stats_of_one_player(name,stata).getArray();
                System.out.println("name "+name+" stata "+stata);
                for (int i=0; i<6; i++)
                    System.out.println(positions_for_query[i+1]+" vpip "+procents((int)stats[i][1],(int)stats[i][0])+" pfr "+procents((int)stats[i][2],(int)stats[i][0])+
                            " 3_bet "+procents((int)stats[i][4],(int)stats[i][3])+" count pfr "+stats[i][2]+" count  select 3bet "+stats[i][3]+" count 3bet "+stats[i][4]+" count vpip "+stats[i][1]);
                System.out.println("Total vpip "+procents((int)stats[6][1],(int)stats[6][0])+" pfr "+procents((int)stats[6][2],(int)stats[6][0])+
                        " 3_bet "+procents((int)stats[6][4],(int)stats[6][3])+" count pfr "+stats[6][2]+" count  select 3bet "+stats[6][3]+" count 3bet "+stats[6][4]+" count vpip "+stats[6][1]);
            }

            if(stata.equals("rfi")){ Object[][] stats =(Object[][]) get_stats_of_one_player(name,stata).getArray();
                System.out.println("name "+name+" stata "+stata);
                for (int i=0; i<5; i++)
                    System.out.println(positions_for_query[i+1]+" select "+stats[i][0]+" rfi "+procents((int)stats[i][1],(int)stats[i][0])+" count rfi "+stats[i][1]);
            }
            if(stata.equals("alliners")){ Object[][] stats =(Object[][]) get_stats_of_one_player(name,stata).getArray();
                System.out.println("name "+name+" stata "+stata);
                for(int v=0; v<3; v++){
                    for (int i=0; i<4; i++){
                        System.out.print(" stack: "+v+" select rfi- "+stats[v][0]+" all rfi- "+procents((int)stats[v][1],(int)stats[v][0])+" count rfi-"+stats[v][1]);
                        System.out.println(" select 3bet- "+stats[v][2]+" all 3bet-"+procents((int)stats[v][3],(int)stats[v][2])+" count 3bet- "+stats[v][3]);}
                    System.out.println();}
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static Work_DataBase  work_dataBase;

    static ConcurrentHashMap[] get_StatsFromDataBase(){


        MainStats[] main_stats = work_dataBase.fill_MainArrayOfStatsFromDateBase("main_nicks_stata");
        ConcurrentHashMap[] result = new ConcurrentHashMap[main_stats.length];
        for(int i=0; i<main_stats.length; i++)
            result[i] = new ConcurrentHashMap<> (main_stats[i].getMap_of_Idplayer_stats());
        return result;
    }

    static HashMap<Long,String> hashmap_id_img_pix_nick_old = new HashMap<>();
    static SortedMap<Long,long[]> sortedmap_all_imgs_pix_of_nicks_old = new TreeMap<>();

    public static void read_file_with_nicks_and_img_pixs(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(home_folder+"\\arhive_nicks\\nicks_img.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                if(!(line.startsWith("*")&&line.endsWith("*")))break;
                String[] arr_line = line.substring(1,line.length()-1).split("%");
                //System.out.println("line "+arr_line.length);
                hashmap_id_img_pix_nick_old.put(Long.parseLong(arr_line[1]),arr_line[0]);
                long[] img_pix = new long[16];
                for(int i=2; i<17; i++){
                    img_pix[i-2] = Long.parseLong(arr_line[i]);
                }
                img_pix[15] = Long.parseLong(arr_line[1]);
                sortedmap_all_imgs_pix_of_nicks_old.put(img_pix[15],img_pix);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static org.opencv.core.Mat bufferedImageToMat(BufferedImage bi) {
        org.opencv.core.Mat mat = new org.opencv.core.Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

   /* static {
        System.loadLibrary(home_folder+"opencv_lib\\");
    }*/

    static {
        // $PROJECT_ROOT -- абсолютный путь до библиотеки
        System.load(home_folder+"\\opencv_lib\\opencv_java430.dll");
    }
    public static Mat BufferedImageToMat(BufferedImage img) {
        if (img == null) return new Mat();
        int type = 0;
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            type = CvType.CV_8UC1;
        }
        else if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            type = CvType.CV_8UC3;
        }

        else if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
            type = CvType.CV_8UC4;
        }
        else return new Mat();
        Mat m = new Mat(img.getHeight(), img.getWidth(), type);
        byte[] data =
                ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        if (type == CvType.CV_8UC1 || type == CvType.CV_8UC3) {
            m.put(0, 0, data);
            return m;
        }
        byte[] buf = Arrays.copyOf(data, data.length);
        byte tmp = 0;
        for (int i = 0; i < buf.length; i += 4) { // ABGR => BGRA
            tmp = buf[i];
            buf[i] = buf[i + 1];
            buf[i + 1] = buf[i + 2];
            buf[i + 2] = buf[i + 3];
            buf[i + 3] = tmp;
        }
        m.put(0, 0, buf);
        return m;
    }

    public static BufferedImage MatToBufferedImage(Mat m) {
        if (m == null || m.empty()) return null;
        if (m.depth() == CvType.CV_8U) {}
        else if (m.depth() == CvType.CV_16U) { // CV_16U => CV_8U
            Mat m_16 = new Mat();
            m.convertTo(m_16, CvType.CV_8U, 255.0 / 65535);
            m = m_16;
        }
        else if (m.depth() == CvType.CV_32F) { // CV_32F => CV_8U
            Mat m_32 = new Mat();
            m.convertTo(m_32, CvType.CV_8U, 255);
            m = m_32;
        }
        else
        return null;
        int type = 0;
        if (m.channels() == 1)
            type = BufferedImage.TYPE_BYTE_GRAY;
        else if (m.channels() == 3)
            type = BufferedImage.TYPE_3BYTE_BGR;
        else if (m.channels() == 4)
            type = BufferedImage.TYPE_4BYTE_ABGR;
        else
            return null;
        byte[] buf = new byte[m.channels() * m.cols() * m.rows()];
        m.get(0, 0, buf);
        byte tmp = 0;
        if (m.channels() == 4) { // BGRA => ABGR
            for (int i = 0; i < buf.length; i += 4) {
                tmp = buf[i + 3];
                buf[i + 3] = buf[i + 2];
                buf[i + 2] = buf[i + 1];
                buf[i + 1] = buf[i];
                buf[i] = tmp;
            }
        }
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        byte[] data =
                ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buf, 0, data, 0, buf.length);
        return image;
    }

    public static void main(String[] args) throws Exception {

        OCR ocr = new OCR();
        UseTesseract useTesseract = new UseTesseract();
        UseTesseract useTesseract_ltsm = new UseTesseract(7);
        CaptureVideo captureVideo = new CaptureVideo("");
        Settings.setting_capture_video();
        BufferedImage image = set_grey_and_inverse_or_no(read_image("Mtest\\colnick"),true);
        saveImageToFile(image,"Mtest\\greynick");
        Mat nick = BufferedImageToMat(read_image("Mtest\\bwnick"));
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat img3 = new Mat();
        //Imgproc.erode(nick, img3, kernel);
        Imgproc.medianBlur(nick, img3, 5);
        //saveImageToFile(get_white_black_image(image,105),"Mtest\\bwnick");
        saveImageToFile(MatToBufferedImage(img3),"Mtest\\newnick");
    }
}
