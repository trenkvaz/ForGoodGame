package org.trenkvaz.converter;

import org.apache.commons.lang3.ArrayUtils;
import org.trenkvaz.database_hands.GetNicksForHands;
import org.trenkvaz.database_hands.Work_DataBase;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.ui.StartAppLauncher.RED;
import static org.trenkvaz.ui.StartAppLauncher.RESET;

public class PartyToPokerStars {

    static final DateFormat formatter= new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss", Locale.US);
    static final DateFormat starsFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
    static final String folderPokerStars = "F:\\Moe_Alex_win_10\\Poker\\PartyPokerHands\\PokerStars\\";


    static void start_ReadFilesInFolder(String folder){

        for(File a: Objects.requireNonNull(new File(folder).listFiles())){
            if(a.isFile()&&a.getName().endsWith(".txt")){
                read_File(a.getPath(),a.getName().replaceFirst("[.][^.]+$", ""));
            }
        }
    }

    private static void read_File(String files,String namefile){
        final String start_line_of_hand = "***** Hand History";
        List<String> hand = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(files));
            String line;
            while ((line = br.readLine()) != null) {
                if(line.length()==0)continue;
                if(line.contains(start_line_of_hand)){
                    if(hand!=null) read_HandHistory(hand,namefile);
                    hand = new ArrayList<>();
                    hand.add(line);
                } else if(hand!=null)hand.add(line);
            }
            if(hand!=null) read_HandHistory(hand,namefile);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void read_HandHistory(List<String> partyHand,String namefile){
        if(partyHand.stream().anyMatch(c->c.startsWith("Hand was run twice")))return;
        String[] strBlinds = partyHand.get(1).substring(0,partyHand.get(1).indexOf(" ")).split("/");
        List<String> starsHand = getStartPokerStarsHand(partyHand,strBlinds);
        List<String> nicks = getNicks(partyHand);
        String[] resultHand = new String[nicks.size()];
        String board = changeSymbols(partyHand,starsHand,nicks,strBlinds,resultHand);

        float[] betLose = new float[nicks.size()];
        float[] collected = new float[nicks.size()];
        String[] showdowns = new String[nicks.size()];
        getResultPartyHand(betLose,collected,showdowns,nicks,partyHand);
        float[] betLoseSort = betLose.clone();
        Arrays.sort(betLoseSort);
        float uncalledBet = BigDecimal.valueOf(betLoseSort[betLoseSort.length-1]-betLoseSort[betLoseSort.length-2]).setScale(2, RoundingMode.HALF_UP).floatValue();
        if(uncalledBet!=0) {
            int indUncalledNick = ArrayUtils.indexOf(betLose,betLoseSort[betLoseSort.length-1]);
            starsHand.add("Uncalled bet ($"+uncalledBet+") returned to "+nicks.get(indUncalledNick));
            collected[indUncalledNick]=BigDecimal.valueOf(collected[indUncalledNick]-uncalledBet).setScale(2, RoundingMode.HALF_UP).floatValue();
           /* if(collected[indUncalledNick]<=0) System.out.println(RED+" +++++++++"+starsHand.get(0));
            System.out.println(RESET);*/
        }


        if(Arrays.stream(showdowns).anyMatch(Objects::nonNull)){
            starsHand.add("*** SHOW DOWN ***");
            for(int i=0; i<nicks.size(); i++)if(showdowns[i]!=null)starsHand.add(nicks.get(i)+": shows "+showdowns[i]);
        }

        for(int i=0; i<nicks.size(); i++) if(collected[i]>0)starsHand.add(nicks.get(i)+" collected $"+collected[i]+" from pot");

        starsHand.add("*** SUMMARY ***");
        float totalPot = 0;
        for(int i=0; i<nicks.size(); i++)totalPot+=collected[i];
        totalPot = BigDecimal.valueOf(totalPot).setScale(2, RoundingMode.HALF_UP).floatValue();
        float rake = 0;
        for(int i=8+nicks.size(); i<partyHand.size(); i++)
            if(partyHand.get(i).startsWith("Main Pot: ")){
               String strRake = partyHand.get(i).substring(partyHand.get(i).indexOf(" Rake: ")+8);
               totalPot = BigDecimal.valueOf(totalPot+Float.parseFloat(strRake)).setScale(2, RoundingMode.HALF_UP).floatValue();
               starsHand.add("Total pot $"+totalPot+" | Rake $"+strRake);
            }

        if(!board.equals(""))starsHand.add("Board"+board);
        String pos = "";
        for(int i=0; i<nicks.size(); i++){
            if(i==0)pos = " (button)"; if(i==1) pos = " (small blind)"; if(i==2) pos = " (big blind)";if(i>2)pos = "";
            if(!resultHand[i].equals("r")){ starsHand.add("Seat "+(i+1)+": "+nicks.get(i)+pos+resultHand[i]);
            } else {
              if(showdowns[i]==null)starsHand.add("Seat "+(i+1)+": "+nicks.get(i)+pos+" collected ($"+collected[i]+")");
              else {
                  if(collected[i]>0)starsHand.add("Seat "+(i+1)+": "+nicks.get(i)+pos+"  showed "+showdowns[i]+" and won ($"+collected[i]+")");
                  else starsHand.add("Seat "+(i+1)+": "+nicks.get(i)+pos+"  showed "+showdowns[i]+" and lost");
              }
            }

        }

        /*for (String line:starsHand) System.out.println("*"+line+"*");
        System.out.println("===============================");*/
        /*List<String> nicks = getNicks(partyHand);
        for(String nick:nicks) System.out.println("*"+nick+"*");
        System.out.println("===============================");*/
        write_NewHistoryHandsWithNicks(namefile,starsHand);
    }


    static void getResultPartyHand(float[] betLose, float[] collected, String[] showdowns, List<String> nicks, List<String> partyHand){

        for (int i=partyHand.size()-nicks.size(); i<partyHand.size(); i++){
            for(int i_nick = 0; i_nick<nicks.size(); i_nick++){
                if(partyHand.get(i).startsWith(nicks.get(i_nick)+" balance ")){
                    if(partyHand.get(i).contains(" didn't bet (folded)"))break;
                    int bet = partyHand.get(i).indexOf(" bet ",nicks.get(i_nick).length()+10);
                    int card = -1;
                    if(bet!=-1){
                        betLose[i_nick] = Float.parseFloat(partyHand.get(i).substring(bet+6,partyHand.get(i).indexOf(",",bet)));
                        int collect = partyHand.get(i).indexOf(" collected ",nicks.get(i_nick).length()+10);
                        if(collect!=-1){
                              /*System.out.println(partyHand.get(i));
                              String strBet = partyHand.get(i).substring(collect+12,partyHand.get(i).indexOf(",",collect));
                              System.out.println("*"+strBet+"*");*/
                              collected[i_nick] = Float.parseFloat(partyHand.get(i).substring(collect+12,partyHand.get(i).indexOf(",",collect)));
                        }
                        card = partyHand.get(i).indexOf("[ ",nicks.get(i_nick).length()+10);

                    } else {
                        int lost = partyHand.get(i).indexOf(" lost ",nicks.get(i_nick).length()+10);
                        if(lost!=-1){
                            card = partyHand.get(i).indexOf("[ ",nicks.get(i_nick).length()+10);
                            if(card!=-1){ betLose[i_nick] = Float.parseFloat(partyHand.get(i).substring(lost+7,card)); }
                            else { betLose[i_nick] = Float.parseFloat(partyHand.get(i).substring(lost+7,partyHand.get(i).indexOf(" ",lost+7))); }

                        }
                    }
                    if(card!=-1){
                        showdowns[i_nick] = partyHand.get(i).substring(card,partyHand.get(i).indexOf("]",card)+1)
                                .replace(",","").replace("[ ","[").replace(" ]","]");
                    }
                }
            }
        }

    }

    static String changeSymbols(List<String> partyHand,List<String> starsHand,List<String> nicks,String[] strBlinds, String[] resultHand){

        int p =0;
        for(int i=4; i<10; i++){ p++;
            if(partyHand.get(i).startsWith("Seat "+p+": ")) { starsHand.add(partyHand.get(i).substring(0,partyHand.get(i).lastIndexOf(")"))+" in chips) "); }}
        p=0;
        String postblinds = " posts small blind $"+strBlinds[0];
        for(int i=4+nicks.size(); i<4+nicks.size()+2; i++){ p++;
            if(p==2)postblinds = " posts big blind $"+strBlinds[1];
            for(int n=0; n<nicks.size(); n++){
                if(partyHand.get(i).startsWith(nicks.get(n))){
                    starsHand.add(nicks.get(n)+":"+postblinds);
                }
            }
        }
        starsHand.add("*** HOLE CARDS ***");
        String cards = partyHand.get(7+nicks.size()).substring(partyHand.get(7+nicks.size()).lastIndexOf(" [ "),
                partyHand.get(7+nicks.size()).lastIndexOf("]")+1).replace(",","").replace("[ ","[").replace(" ]","]");
        String dealt = partyHand.get(7+nicks.size()).substring(0,partyHand.get(7+nicks.size()).lastIndexOf("[")-1)+cards;
        starsHand.add(dealt);
        String board = ""; String cardStreet = ""; int street = 0; resultHand[1] = "r"; resultHand[2] = "r";
        for(int i=8+nicks.size(); i<partyHand.size(); i++){
            if(partyHand.get(i).startsWith("** Summary **"))break;
            if(partyHand.get(i).startsWith("** Dealing Flop **")){
                board = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace(",","").replace("[ ","[").replace(" ]","]");
                starsHand.add("*** FLOP ***"+board);
                street++;
                continue;
            }
            if(partyHand.get(i).startsWith("** Dealing Turn **")){
                cardStreet = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace("[ ","[").replace(" ]","]");
                starsHand.add("*** TURN ***"+board+cardStreet);
                board = board.replace("]","")+cardStreet.replace("[","");
                street++;
                continue;
            }
            if(partyHand.get(i).startsWith("** Dealing River **")){
                cardStreet = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace("[ ","[").replace(" ]","]");
                starsHand.add("*** RIVER ***"+board+cardStreet);
                board = board.replace("]","")+cardStreet.replace("[","");
                street++;
                continue;
            }
            for(int n=0; n<nicks.size(); n++){
                if(partyHand.get(i).startsWith(nicks.get(n))){
                    if(partyHand.get(i).contains(" is all-In."))break;

                    if(partyHand.get(i).contains(" folds ")){
                        if(resultHand[n]==null)resultHand[n] = " folded before Flop (didn't bet)";
                        else {
                            if(street==0)resultHand[n] = " folded before Flop";
                            if(street==1)resultHand[n] = " folded on the Flop";
                            if(street==2)resultHand[n] = " folded on the Turn";
                            if(street==3)resultHand[n] = " folded on the River";
                        }
                        starsHand.add(nicks.get(n)+": folds "); break;
                    }
                    if(partyHand.get(i).contains(" calls ")){ resultHand[n] = "r";
                        starsHand.add(nicks.get(n)+": calls $"+partyHand.get(i).substring(nicks.get(n).length()+8).replaceAll("[^0-9?!.]","")); break;}
                    if(partyHand.get(i).contains(" bets ")){ resultHand[n] = "r";
                        starsHand.add(nicks.get(n)+": bets $"+partyHand.get(i).substring(nicks.get(n).length()+7).replaceAll("[^0-9?!.]","")); break;}
                    if(partyHand.get(i).contains(" checks ")){starsHand.add(nicks.get(n)+": checks "); break;}
                    if(partyHand.get(i).contains(" raises ")){
                        resultHand[n] = "r";
                        /*String firstR = partyHand.get(i).substring(nicks.get(n).length()+8,partyHand.get(i).indexOf(" to "));
                        String secondR = partyHand.get(i).substring(partyHand.get(i).indexOf(" to ")+4);
                        System.out.println("*"+firstR+"*"+secondR+"*");*/
                        starsHand.add(nicks.get(n)+": raises $"+partyHand.get(i).substring(nicks.get(n).length()+8,partyHand.get(i).indexOf(" to "))+" to $"+
                                partyHand.get(i).substring(partyHand.get(i).indexOf(" to ")+4));

                        break;}
                }
            }

        }
          return board;
    }

    static List<String> getStartPokerStarsHand(List<String> partyHand, String[] strBlinds){
       List<String> starsHand = new ArrayList<>();

       String numHand = partyHand.get(0).substring(28,partyHand.get(0).lastIndexOf(" "));
       String strTime = getTime(partyHand.get(1));
       String topStarsHand = "PokerStars Zoom Hand #"+numHand+":  Hold'em No Limit ($"+strBlinds[0]+"/$"+strBlinds[1]+") - "+strTime;
       starsHand.add(topStarsHand);
       starsHand.add("Table 'Haley' 6-max Seat #1 is the button");
       return starsHand;
    }




    static String getTime(String lineParty){
        String[] date = lineParty.substring(lineParty.lastIndexOf('-')+2).split(" ");
        try {
            int chasov = 9;
            long timeHand = formatter.parse(date[5]+"/"+date[1]+"/"+date[2]+" "+date[3]).getTime();
            String timeET = starsFormat.format(timeHand);
            String timeLocal = starsFormat.format(timeHand+3600000*chasov);
            return timeLocal+" GMT+05:00 ["+timeET+" ET]";
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    static List<String> getNicks(List<String> partyHand){
        List<String> nicks = new ArrayList<>();
        int p =0;
        for(int i=4; i<10; i++){ p++;
            if(partyHand.get(i).startsWith("Seat "+p+": "))nicks.add(partyHand.get(i).substring(8,partyHand.get(i).lastIndexOf(" ")));}
        return nicks;
    }



    private static void write_NewHistoryHandsWithNicks(String name_file,List<String> starsHand){
        String hand = "";
        for(String line:starsHand)hand+=line+"\r\n";

        hand+="\r\n\r\n";

        try (OutputStream os = new FileOutputStream(folderPokerStars+name_file+"_nicks.txt",true)) {
            os.write(hand.getBytes(StandardCharsets.UTF_8));


        } catch (FileNotFoundException e) {
        } catch (IOException s) {
        }
    }


    public static void main(String[] args) {
        start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\JavaProjects\\ForGoodGame\\test_party\\output");
    }

}
