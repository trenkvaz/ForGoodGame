package org.trenkvaz.database_hands;

import org.trenkvaz.main.CurrentHand;

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
import java.util.stream.Collectors;

import static org.trenkvaz.database_hands.Work_DataBase.close_DataBase;
import static org.trenkvaz.database_hands.Work_DataBase.get_list_TempHandsMinMaxTime;
import static org.trenkvaz.main.CaptureVideo.Deck;

public class GetNicksForHands {

    record HandFromHistory(long time_hand, short cards_hero, short position_hero, float[] stacks, List<String> handfromhistory,int[] idplayers){}

    static final DateFormat formatter= new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss", Locale.US);
    static List<HandFromHistory> list_handsfromhistory = new ArrayList<>();

    static void start_ReadFilesInFolder(String folder){
        for(File a: Objects.requireNonNull(new File(folder).listFiles())){
            if(a.isFile()&&a.getName().endsWith("party07.txt")){ read_File(a.getPath()); }
        }


      select_TempHandsForHandFromHistory();

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
                get_PositionHero(sublist_players),get_Stacks(sublist_players,get_BB(hand.get(1))),hand,new int[6]));
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

    public static String get_str_Date(long time_hand){
        return formatter.format(time_hand);
    }


    static void select_TempHandsForHandFromHistory(){

        List<CurrentHand.TempHand> list_temphands_for_select = get_list_TempHandsMinMaxTime(list_handsfromhistory.get(0).time_hand,
                list_handsfromhistory.get(list_handsfromhistory.size()-1).time_hand+30000);
        /*for (CurrentHand.TempHand tempHand:list_temphands_for_select){
            System.out.println("time "+get_str_Date(tempHand.time_hand())+" cards "+get_str_Cards(tempHand.cards_hero())
                    // +" pos_hero "+tempHand.position_hero()
            );
        }*/

        List<CurrentHand.TempHand> list_selected_temphands = new ArrayList<>();

        for(HandFromHistory handfromhistory:list_handsfromhistory){
           // фильтр временных рук по времени относительно рук из истории если рука больше чем на 10 секунд отличается от истории, то она не отбирается
           for(CurrentHand.TempHand temphand:list_temphands_for_select){
               if(temphand.cards_hero()==handfromhistory.cards_hero&&temphand.position_hero()==handfromhistory.position_hero){
                   if(Math.abs(handfromhistory.time_hand-temphand.time_hand())<10_000) list_selected_temphands.add(temphand);
               }
           }
           if(list_selected_temphands.isEmpty())continue;
           CurrentHand.TempHand selected_temphand = null;
           if(list_selected_temphands.size()==1) { //System.out.println(get_str_Date(handfromhistory.time_hand)+"   "+get_str_Date(list_selected_temphands.get(0).time_hand()));
               selected_temphand = list_selected_temphands.get(0);
           }
           else {
               /*System.out.println(get_str_Date(handfromhistory.time_hand)+" *****************************************  ");
               list_selected_temphands.forEach(System.out::println);
               System.out.println("**********************");*/
               // если выбрано больше чем одна временная рука, то отбирается рука у которой меньше всего ошибок при сравнении стеков по позициям
               int min_error = 6, total_error =0, index_hand_with_min_error = -1;
               for(int ind_temphand = 0; ind_temphand<list_selected_temphands.size(); ind_temphand++){
                   total_error =0;
                   for(int ind_stack =0; ind_stack<6; ind_stack++){
                       if(handfromhistory.stacks[ind_stack]-list_selected_temphands.get(ind_temphand).stacks()[ind_stack]!=0)total_error++;
                   }
                   if(total_error<min_error){
                       min_error = total_error;
                       index_hand_with_min_error = ind_temphand;
                   }
               }
               if(index_hand_with_min_error!=-1)selected_temphand = list_selected_temphands.get(index_hand_with_min_error);
           }

           for(int i=0; i<6; i++) handfromhistory.idplayers[i] = selected_temphand.idplayers()[i];

           list_selected_temphands.clear();
        }

    }

    public static void main(String[] args) {
        new Work_DataBase();
        start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\JavaProjects\\ForGoodGame\\test_party");
       /* System.out.println("count hands "+c);
        for (HandFromHistory hand:list_handsfromhistory)
            System.out.println(hand.time_hand+"  "+get_str_Cards(hand.cards_hero));*/

        close_DataBase();
    }

}
