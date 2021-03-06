package org.trenkvaz.database_hands;

import org.trenkvaz.main.CurrentHand;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.main.CaptureVideo.DECK;
import static org.trenkvaz.main.CaptureVideo.NICK_HERO;
import static org.trenkvaz.ui.StartAppLauncher.RED;

public class GetNicksForHands {

    static final DateFormat formatter= new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss", Locale.US);
    static List<HistoryHand> list_handsfromhistory = new ArrayList<>();
    //static Map<Integer,String> reverse_map_idplayers_nicks;

    record HistoryHand(long time_hand, short cards_hero, short position_hero, float[] stacks, String[] strStacks, List<String> handfromhistory, String[] nicks, Long[] timeTampHandAndAmountPlayer){
        public String get_str_Cards(){
            int c = (cards_hero < 0) ? cards_hero+65536 : cards_hero;
            return DECK[c/1000]+" "+ DECK[c%1000];
        }

        public String get_str_Date(){
            return formatter.format(time_hand);
        }
    }



    static void start_ReadFilesInFolder(String folder){
        for(File a: Objects.requireNonNull(new File(folder).listFiles())){
            if(a.isFile()&&a.getName().endsWith(".txt")){
                System.out.println(a.getName());
                read_File(a.getPath());

                select_TempHandsForHistoryHand();
                write_NewHistoryHandsWithNicks(folder,a.getName().replaceFirst("[.][^.]+$", ""));
            }

            list_handsfromhistory.clear();
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
                    if(hand!=null) read_HandHistoryToList(hand);
                    hand = new ArrayList<>();
                    hand.add(line);
                } else if(hand!=null)hand.add(line);
            }
            if(hand!=null) read_HandHistoryToList(hand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int c = 0;

    private static void read_HandHistoryToList(List<String> hand){

        if(hand.get(1).contains(" Tourney ")){c++;return;}
        List<String> sublist_players =new ArrayList<>(); //hand.subList(4,10);
        int p = 0;
        for(int i=4; i<10; i++){ p++;
            if(hand.get(i).startsWith("Seat "+p+": "))sublist_players.add(hand.get(i));}
        /*System.out.println("++++++++++++++++++++++++++++++++++++++");
        sublist_players.forEach(System.out::println);
        System.out.println("pos hero "+read_PositionHeroForHistoryHand(sublist_players));*/
        int cor = 6-sublist_players.size();
        Long[]timeTampHandAndAmountPlayer = {null,(long)sublist_players.size()};
        String[] strStacks = new String[6];
        float[] stacks =  read_StacksForHistoryHand(sublist_players, read_BBforHistoryHand(hand.get(1)),strStacks);
        list_handsfromhistory.add(new HistoryHand(read_TimeHandForHistoryHand(hand.get(1)), read_CardsHeroForHistoryHand(hand),
                read_PositionHeroForHistoryHand(sublist_players), stacks,strStacks,hand,new String[6],timeTampHandAndAmountPlayer));
    }


    private static long read_TimeHandForHistoryHand(String line){
        String[] date = line.substring(line.lastIndexOf('-')+2).split(" ");
        try {
            int chasov = 9;
            return formatter.parse(date[5]+"/"+date[1]+"/"+date[2]+" "+date[3]).getTime()+3600000*chasov;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }


    private static short read_PositionHeroForHistoryHand(List<String> sublist_players){
        int cor = 6-sublist_players.size();
        for (int i=0; i<sublist_players.size(); i++)
            if(sublist_players.get(i).contains("Hero")){
                if(i<3)return (short) (i+3);
                else return (short) (i-3+cor);
            }
        return -1;
    }

    public String get_str_Cards(int cards_hero){
        int c = (cards_hero < 0) ? cards_hero+65536 : cards_hero;
        return DECK[c/1000]+" "+ DECK[c%1000];
    }

    private static float read_BBforHistoryHand(String line){ return Float.parseFloat(line.substring(line.indexOf("/")+1,line.indexOf(" "))); }


    private static float[] read_StacksForHistoryHand(List<String> sublist_players, float bb,String[] strStacks){
        float[] result = new float[6]; int position = -1;
        int cor = 6-sublist_players.size();
        for (int i=0; i<sublist_players.size(); i++) {
            if(i<3)position = i+3;
            else position = i-3+cor;
            String line = sublist_players.get(i);
            //System.out.println(line.substring(line.indexOf("("),line.indexOf(")")));
            strStacks[position] = line.substring(line.indexOf("("),line.indexOf(")")).replaceAll("[^0-9]\\.?[^0-9]","").replaceAll("[^0-9.]","");//.replaceAll("[^\d*\.?\d*]+","")\d+(\.\d+)
            //System.out.println("*"+strStacks[position]+"*");
            result[position]  = new BigDecimal(Float.parseFloat(strStacks[position])/bb).setScale(1, RoundingMode.HALF_UP).floatValue();
        }
        return result;
    }




    private static short read_CardsHeroForHistoryHand(List<String> hand){
        int i_deal = -1;
        for(int i=7; i<hand.size(); i++ )if(hand.get(i).startsWith("** Dealing down cards **")){i_deal = i+1;break;}
        return (short)((byte)Arrays.asList(DECK).indexOf(hand.get(i_deal).subSequence(16,18))*1000+(byte)Arrays.asList(DECK).indexOf(hand.get(i_deal).subSequence(20,22)));
    }

    public static String get_str_Cards(short cards){
        int c = (cards < 0) ? cards+65536 : cards;
        return DECK[c/1000]+" "+ DECK[c%1000];
    }


    static void select_TempHandsForHistoryHand(){

        List<CurrentHand.TempHand> list_temphands_for_select = get_list_TempHandsMinMaxTime(list_handsfromhistory.get(0).time_hand,
                list_handsfromhistory.get(list_handsfromhistory.size()-1).time_hand+30000);
        //TEST
       /* for (CurrentHand.TempHand tempHand:list_temphands_for_select){
            System.out.println("time   cards "+get_str_Cards(tempHand.cards_hero())
                    // +" pos_hero "+tempHand.position_hero()
            );
        }*/

        List<CurrentHand.TempHand> list_selected_temphands = new ArrayList<>();

        for(HistoryHand handfromhistory:list_handsfromhistory){
           // фильтр временных рук по времени относительно рук из истории если рука больше чем на 30 секунд отличается от истории, то она не отбирается
           for(CurrentHand.TempHand temphand:list_temphands_for_select){
               /*if(temphand.time_hand()==1615562900693L) {
                   System.out.println("HAND ! "+temphand.cards_hero()+" "+temphand.position_hero()+" " +
                           ""+handfromhistory.cards_hero+" "+handfromhistory.position_hero+" "+get_str_Cards(temphand.cards_hero())+"  "+get_str_Cards(handfromhistory.cards_hero));
               }*/

               if(temphand.cards_hero()==handfromhistory.cards_hero&&temphand.position_hero()==handfromhistory.position_hero){
                   //System.out.println("EST !!!!!!!!!!!!");
                   if(Math.abs(handfromhistory.time_hand-temphand.time_hand())<30_000) list_selected_temphands.add(temphand);
               }
           }
            //System.out.println("isemty "+list_selected_temphands.isEmpty());
           if(list_selected_temphands.isEmpty())continue;
           CurrentHand.TempHand selected_temphand = null;
           if(list_selected_temphands.size()==1) { //System.out.print(get_str_Date(handfromhistory.time_hand)+"   "+get_str_Date(list_selected_temphands.get(0).time_hand()));
               //select_TempHandWithMinErrorStacksOfHandHistory(list_selected_temphands,handfromhistory);
               selected_temphand = list_selected_temphands.get(0);
           }
           else {
              /* System.out.println(get_str_Date(handfromhistory.time_hand)+" *****************************************  ");
               for(CurrentHand.TempHand test_temp:list_selected_temphands){
                   System.out.println(get_str_Date(test_temp.time_hand())+"   "+get_str_Cards(test_temp.cards_hero()));
                   for(int i=0; i<6; i++) System.out.println(test_temp.idplayers()[i]+"    "+test_temp.stacks()[i]);
                   System.out.println("------------------------------------------------------------------");
               }

               System.out.println("**********************");*/
               // если выбрано больше чем одна временная рука, то отбирается рука у которой меньше всего ошибок при сравнении стеков по позициям

               selected_temphand = select_TempHandWithMinErrorStacksOfHandHistory(list_selected_temphands,handfromhistory);
           }

            /*System.out.println(get_str_Date(handfromhistory.time_hand)+" "+get_str_Cards(handfromhistory.cards_hero)+"   "+
                    get_str_Date(selected_temphand.time_hand())+"   "+get_str_Cards(selected_temphand.cards_hero()));*/
           if(selected_temphand!=null){
           for(int i=0; i<6; i++) {handfromhistory.nicks[i] = selected_temphand.nicks()[i];
               //System.out.println(selected_temphand.nicks()[i]);
           }
             handfromhistory.timeTampHandAndAmountPlayer[0] = selected_temphand.time_hand();
           }

           list_selected_temphands.clear();
        }

    }


   static CurrentHand.TempHand select_TempHandWithMinErrorStacksOfHandHistory(List<CurrentHand.TempHand> list_selected_temphands, HistoryHand handfromhistory){
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

        /*if(list_selected_temphands.size()==1&&total_error>0){
            System.out.println(handfromhistory.get_str_Cards()+"   "+handfromhistory.get_str_Date()+"====================================================");
            for(int i=0; i<6; i++) System.out.println(handfromhistory.stacks[i]+"   "+list_selected_temphands.get(0).stacks()[i]);
        }*/

        return (index_hand_with_min_error!=-1) ? list_selected_temphands.get(index_hand_with_min_error):null;
    }


    private static void write_NewHistoryHandsWithNicks(String folder, String name_file){

        try (OutputStream os = new FileOutputStream(folder+"\\output\\"+name_file+"_nicks.txt",true)) {
            for(HistoryHand historyhand:list_handsfromhistory){

             os.write((get_NewHistoryHandWithNicks(historyhand.handfromhistory,historyhand.nicks,historyhand.timeTampHandAndAmountPlayer,historyhand.strStacks)+"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                //System.out.println(get_NewHistoryHandWithNicks(historyhand.handfromhistory,historyhand.idplayers)+"\r\n\r\n");
            }
        } catch (FileNotFoundException e) {
        } catch (IOException s) {
        }
    }


   static String get_NewHistoryHandWithNicks(List<String> historyhand,String[] nicks,Long[] timeAndAmountPlayer,String[] strStacks){

       int position = -1;
       List<String> sublist_players = historyhand.subList(4,10);
       //for(int i=4; i<10; i++)if(historyhand.get(i).startsWith("Seat "))sublist_players.add(historyhand.get(i));

       if(timeAndAmountPlayer[0]!=null)historyhand.set(0,historyhand.get(0).substring(0,28)+timeAndAmountPlayer[0]+" *****");
       int amountPlayerTempNahd = (int) Arrays.stream(nicks).filter(Objects::isNull).count();
       // количество игроков в руке базы данных может иметь равное или меньшее число чем в руке истории раздач, так как некоторые игроки могут не определится
       // но вот если игроков в руке базы больше, чем в истории раздач, то это явная ошибка с определением количества играющих
       boolean isError =false;
       if(amountPlayerTempNahd>timeAndAmountPlayer[1]) {
           System.out.println(RED+"error amountplayer timehand "+timeAndAmountPlayer[0]);
        /*   System.out.println("error");
       for(String n:nicks) System.out.println(n);
           System.out.println("+++++++++++++++++++++++");
       for(String l:sublist_players) System.out.println(l);*/
       }
       //for(String line:strStacks) System.out.println(line);

       int cor =(int) (6-timeAndAmountPlayer[1]);
       String[][] seat_nick = new String[6][2];
       // меняются плеер1 и т.д на ники в начале раздачи где они на своих местах
       for (int i=0; i<timeAndAmountPlayer[1]; i++) {
           if(i<3)position = i+3;
           else position = i-3+cor;
           //int id = idplayers[position];
           if(nicks[position]==null)continue;
           String line = sublist_players.get(i);
           //if(!line.startsWith("Seat "+(i+1)+": "))break;
           seat_nick[i][0] = line.substring(8,line.indexOf("(")-1);
           if(seat_nick[i][0].equals("")) return "";
           seat_nick[i][1] = nicks[position];
           line = line.replace(seat_nick[i][0],seat_nick[i][1]);
           //System.out.println(line);
           line = line.substring(0,line.indexOf("("))+"($"+(Float.parseFloat(strStacks[position]))+")";
          // System.out.println(line);
           sublist_players.set(i,line);
       }




       // меняются плеер1 и т.д на ники по ходу раздачи
       for(int i_line=10-cor; i_line<historyhand.size(); i_line++){
           if(historyhand.get(i_line).startsWith("Dealt to Hero ")){
               historyhand.set(i_line,historyhand.get(i_line).replace("Hero",NICK_HERO)); continue;
           }
           for(int i_seat=0; i_seat<timeAndAmountPlayer[1]; i_seat++){
               if(seat_nick[i_seat][0]==null)continue;
               if(historyhand.get(i_line).startsWith(seat_nick[i_seat][0]+" ")){
                   historyhand.set(i_line,historyhand.get(i_line).replace(seat_nick[i_seat][0],seat_nick[i_seat][1]));
               }
           }
       }
       StringBuilder result = new StringBuilder();
       for(String line:historyhand){
          // System.out.println(line);
           result.append(line).append("\r\n");}

       return result.toString();
    }


   /* static Map<Integer,String> reverse_MapIdplayersNicks(Map<String,Integer> map_idplayers_nicks){
        Map<Integer,String> reverse_map_idplayers_nicks = new HashMap<>();
        for(Map.Entry<String,Integer> entry:map_idplayers_nicks.entrySet()){
            reverse_map_idplayers_nicks.put(entry.getValue(),entry.getKey());
        }
        return reverse_map_idplayers_nicks;
    }*/


    public static void main(String[] args) {
        new Work_DataBase();
        //reverse_map_idplayers_nicks = reverse_MapIdplayersNicks(new Work_DataBase().get_map_IdPlayersNicks());
        start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\JavaProjects\\ForGoodGame\\test_party\\");
       /* System.out.println("count hands "+c);
        for (HandFromHistory hand:list_handsfromhistory)
            System.out.println(hand.time_hand+"  "+get_str_Cards(hand.cards_hero));*/
        System.out.println(c);
        close_DataBase();
    }

}
