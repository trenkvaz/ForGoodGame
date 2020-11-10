package org.trenkvaz.database_hands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.trenkvaz.main.CaptureVideo.Deck;

public class GetNicksForHands {

    record HandFromHistory(long time_hand, short cards_hero, short position_hero, float[] stacks){}

    static final DateFormat formatter= new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss", Locale.US);
    static List<HandFromHistory> list_handsfromhistory = new ArrayList<>();

    static void start_ReadFilesInFolder(String folder){
        for(File a: Objects.requireNonNull(new File(folder).listFiles())){
            if(a.isFile()&&a.getName().endsWith("party07.txt")){ read_File(a.getPath()); }
        }
    }


    private static void read_File(String files){
        final String start_line_of_hand = "***** Hand History";
        List<String> hand = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(files));
            String line;
            while ((line = br.readLine()) != null) {
                if(line.length()==0)continue;
                if(line.contains(start_line_of_hand)){
                    if(hand!=null)read_Hand(hand);
                    hand = new ArrayList<>();
                    hand.add(line);
                } else if(hand!=null)hand.add(line);
            }
            if(hand!=null)read_Hand(hand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int c = 0;

    private static void read_Hand(List<String> hand){
         c++;
         List<String> sublist_players = hand.subList(4,10);
         list_handsfromhistory.add(new HandFromHistory(get_TimeHand(hand.get(1)),get_CardsHero(hand.get(13)),
                get_PositionHero(sublist_players),get_Stacks(sublist_players,get_BB(hand.get(1)))));
    }


    private static long get_TimeHand(String line){
        String[] date = line.substring(line.lastIndexOf('-')+2).split(" ");
        try {
            int chasov = 10;
            return formatter.parse(date[5]+"/"+date[1]+"/"+date[2]+" "+date[3]).getTime()+3600000*chasov;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }


    private static short get_PositionHero(List<String> sublist_players){
        for (int i=0; i<6; i++)
            if(sublist_players.get(i).contains("Hero")){
            if(i<3)return (short) (i+3);
            else return (short) (i-3);
        }
        return -1;
    }


    private static float get_BB(String line){ return Float.parseFloat(line.substring(line.indexOf("/")+1,line.indexOf(" "))); }


    private static float[] get_Stacks(List<String> sublist_players,float bb){
        float[] result = new float[6]; int position = -1;
        for (int i=0; i<6; i++) {
            if(i<3)position = i+3;
            else position = i-3;
            String line = sublist_players.get(i);
            result[position]  = new BigDecimal(Float.parseFloat(line.substring(line.indexOf("$")+1,line.indexOf(")")))/bb).setScale(1, RoundingMode.HALF_UP).floatValue();
        }
        return result;
    }


    private static short get_CardsHero(String line){ return (short)((byte)Arrays.asList(Deck).indexOf(line.subSequence(16,18))*1000+(byte)Arrays.asList(Deck).indexOf(line.subSequence(20,22)));}

   public static String get_str_Cards(short cards){
        int c = (cards < 0) ? cards+65536 : cards;
        return Deck[c/1000]+" "+Deck[c%1000];
    }

    public static void main(String[] args) {
        start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\JavaProjects\\ForGoodGame\\test_party");
        System.out.println("count hands "+c);
        for (HandFromHistory hand:list_handsfromhistory)
            System.out.println(hand.time_hand+"  "+get_str_Cards(hand.cards_hero));
    }

}
