package org.trenkvaz.converter;

import org.apache.commons.lang3.ArrayUtils;
import org.trenkvaz.database_hands.GetNicksForHands;
import org.trenkvaz.database_hands.Work_DataBase;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;
import java.util.stream.IntStream;

import static org.trenkvaz.database_hands.Work_DataBase.*;
import static org.trenkvaz.main.CaptureVideo.NICK_HERO;
import static org.trenkvaz.ui.StartAppLauncher.RED;
import static org.trenkvaz.ui.StartAppLauncher.RESET;

public class PartyToPokerStars {

    static final DateFormat formatter= new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss", Locale.US);
    static final DateFormat starsFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
    static DecimalFormat numberFormat;

    static final String folderPokerStars = "F:\\Moe_Alex_win_10\\Poker\\PartyPokerHands\\PokerStars\\out_test_hand\\";
    //static final String folderPokerStars = "F:\\Moe_Alex_win_10\\Poker\\PartyPokerHands\\PokerStars\\forHM\\";
    static boolean isCorrectHeroNick = false;



    static void start_ReadFilesInFolder(String folder){
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator('.');
        numberFormat = new DecimalFormat("#.##",decimalSymbols);

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

   static void correctHeroNick(List<String> partyHand,String namefile){
       List<String> nicks = getNicks(partyHand);
       List<String[]> nicksHeroes = new ArrayList<>();
       boolean isNickHero = false;
       //for(String nick:nicks) if(nick.length()>4&&nick.contains("Hero")){isNickHero = true; nicksHeroes.add(new String[]{nick,nick.replace("Hero","")});         }
       for(String nick:nicks) if(nick.equals("Hero")){isNickHero = true; break;  }
       if(isNickHero){
           int p =0;
           for(int i=4; i<10; i++){ p++;
               if(partyHand.get(i).startsWith("Seat "+p+": Hero "))partyHand.set(i,partyHand.get(i).replace("Hero",NICK_HERO));}

           for(int i=4+nicks.size(); i<nicks.size()+10; i++)
               if(partyHand.get(i).startsWith("Dealt to Hero "))partyHand.set(i,partyHand.get(i).replace("Hero",NICK_HERO));


           for(int i=4+nicks.size(); i<partyHand.size(); i++){
               if(partyHand.get(i).startsWith("Hero "))partyHand.set(i,partyHand.get(i).replace("Hero",NICK_HERO));
           }

       }

       write_NewHistoryHandsWithNicks(namefile,partyHand);
   }




    private static void read_HandHistory(List<String> partyHand,String namefile){
        if(isCorrectHeroNick){correctHeroNick(partyHand,namefile);return;}

        String[] strBlinds = partyHand.get(1).substring(0,partyHand.get(1).indexOf(" ")).split("/");
        List<String> starsHand = getStartPokerStarsHand(partyHand,strBlinds);
        System.out.println(namefile+"   "+starsHand.get(0));
        List<String> nicks = getNicks(partyHand);

        //nicks.forEach(System.out::println);

        String[] resultHand = new String[nicks.size()];
        List<String> board = changeSymbols(partyHand,starsHand,nicks,resultHand);

        float[] betLose = new float[nicks.size()];
        float[] collected = new float[nicks.size()];
        String[] showdowns = new String[nicks.size()];

        getResultPartyHand(betLose,collected,showdowns,nicks,partyHand);

        //for(float g:collected) System.out.println(" "+g);

        float[] betLoseSort = betLose.clone();
        Arrays.sort(betLoseSort);

        float uncalledBet = BigDecimal.valueOf(betLoseSort[betLoseSort.length-1]-betLoseSort[betLoseSort.length-2]).setScale(2, RoundingMode.HALF_UP).floatValue();
        int indUncalledNick = -1;
        if(uncalledBet!=0) {
            indUncalledNick = ArrayUtils.indexOf(betLose,betLoseSort[betLoseSort.length-1]);
            starsHand.add("Uncalled bet ($"+numberFormat.format(uncalledBet)+") returned to "+nicks.get(indUncalledNick));
            collected[indUncalledNick]=BigDecimal.valueOf(collected[indUncalledNick]-uncalledBet).setScale(2, RoundingMode.HALF_UP).floatValue();
           /* if(collected[indUncalledNick]<=0) System.out.println(RED+" +++++++++"+starsHand.get(0));
            System.out.println(RESET);*/
        }
        boolean isRIT = false;
        if(partyHand.stream().anyMatch(c->c.startsWith("Hand was run twice")))isRIT = true;
        String[] boardRIT = null;
        float[][] resultRIT = null;
        if(isRIT){
          boardRIT = changeRIT(partyHand,starsHand,nicks,board);

          /*  for (String line:starsHand) System.out.println("*"+line+"*");

            for(float a:betLoseSort) System.out.println(a);
            System.out.println("===============================");*/



        }


        if(!isRIT){
        boolean isShowdown = false;
        if(Arrays.stream(showdowns).anyMatch(Objects::nonNull)){ isShowdown = true;
            starsHand.add("*** SHOW DOWN ***");
            for(int i=0; i<nicks.size(); i++)if(showdowns[i]!=null)starsHand.add(nicks.get(i)+": shows "+showdowns[i]);
        }



        for(int i=0; i<nicks.size(); i++) if(collected[i]>0)starsHand.add(nicks.get(i)+" collected $"+numberFormat.format(collected[i])+" from pot");

        if(!isShowdown)for(int i=0; i<nicks.size(); i++) if(collected[i]>0)starsHand.add(nicks.get(i)+": doesn't show hand ");

        } else {
            //for (String line:partyHand) System.out.println("*"+line+"*");

            resultRIT = getCollectedRIT(uncalledBet,indUncalledNick,partyHand,nicks,showdowns);

            //System.out.println("===============================");

            starsHand.add("*** FIRST SHOW DOWN ***");
            for(int i=0; i<nicks.size(); i++)if(showdowns[i]!=null)starsHand.add(nicks.get(i)+": shows "+showdowns[i]);
            for(int i=0; i<nicks.size(); i++) if(resultRIT[0][i]>0)starsHand.add(nicks.get(i)+" collected $"+numberFormat.format(resultRIT[0][i])+" from pot");

            starsHand.add("*** SECOND SHOW DOWN ***");
            for(int i=0; i<nicks.size(); i++)if(showdowns[i]!=null)starsHand.add(nicks.get(i)+": shows "+showdowns[i]);
            for(int i=0; i<nicks.size(); i++) if(resultRIT[1][i]>0)starsHand.add(nicks.get(i)+" collected $"+numberFormat.format(resultRIT[1][i])+" from pot");

        }



        starsHand.add("*** SUMMARY ***");
        float totalPot = 0;
        for(int i=0; i<nicks.size(); i++)totalPot+=collected[i];
        totalPot = BigDecimal.valueOf(totalPot).setScale(2, RoundingMode.HALF_UP).floatValue();
        float rake = 0;
        for(int i=8+nicks.size(); i<partyHand.size(); i++)
            if(partyHand.get(i).startsWith("Main Pot: ")){
               String strRake = partyHand.get(i).substring(partyHand.get(i).indexOf(" Rake: ")+8);
               rake = Float.parseFloat(strRake);
               totalPot = BigDecimal.valueOf(totalPot+rake).setScale(2, RoundingMode.HALF_UP).floatValue();
               starsHand.add("Total pot $"+numberFormat.format(totalPot)+" | Rake $"+numberFormat.format(rake)+" ");
            }


        if(!isRIT){if(!board.get(0).equals(""))starsHand.add("Board"+board.get(0));}
        else {
            starsHand.add("Hand was run twice");
            starsHand.add("FIRST Board"+boardRIT[0]);
            starsHand.add("SECOND Board"+boardRIT[1]);
        }



        String pos = "";
        for(int i=0; i<nicks.size(); i++){
            if(i==0)pos = " (button)"; if(i==1) pos = " (small blind)"; if(i==2) pos = " (big blind)";if(i>2)pos = "";
            if(!resultHand[i].equals("r")){ starsHand.add("Seat "+(i+1)+": "+nicks.get(i)+pos+resultHand[i]);
            } else {
              if(showdowns[i]==null)starsHand.add("Seat "+(i+1)+": "+nicks.get(i)+pos+" collected ($"+numberFormat.format(collected[i])+")");
              else {
                  //if(!isRIT){
                  if(collected[i]>0)starsHand.add("Seat "+(i+1)+": "+nicks.get(i)+pos+" showed "+showdowns[i]+" and won ($"+numberFormat.format(collected[i])+")");
                  else starsHand.add("Seat "+(i+1)+": "+nicks.get(i)+pos+" showed "+showdowns[i]+" and lost");
                 /* } else {
                      String rit = "Seat "+(i+1)+": "+nicks.get(i)+pos+" showed "+showdowns[i];
                      if(resultRIT[0][i]>0)rit+=" and won ($"+resultRIT[0][i]+"),";
                      else rit+=" and lost,";
                      if(resultRIT[1][i]>0)rit+=" and won ($"+resultRIT[1][i]+")";
                      else rit+=" and lost";
                      starsHand.add(rit);
                  }*/
              }
            }

        }

        /*for (String line:starsHand) System.out.println("*"+line+"*");
        System.out.println("===============================");*/
        /*List<String> nicks = getNicks(partyHand);
        for(String nick:nicks) System.out.println("*"+nick+"*");
        System.out.println("===============================");*/
        //if(isRIT)
        write_NewHistoryHandsWithNicks(namefile,starsHand);
    }


    static void getResultPartyHand(float[] betLose, float[] collected, String[] showdowns, List<String> nicks, List<String> partyHand){

        for (int i=partyHand.size()-nicks.size()-5; i<partyHand.size(); i++){
            for(int i_nick = 0; i_nick<nicks.size(); i_nick++){

                if(partyHand.get(i).startsWith(nicks.get(i_nick)+" balance ")){
                    if(partyHand.get(i).contains(" didn't bet (folded)"))break;
                    int bet = partyHand.get(i).indexOf(" bet ",nicks.get(i_nick).length()+10);
                    int card = -1;
                    if(bet!=-1){
                        betLose[i_nick] = Float.parseFloat(partyHand.get(i).substring(bet+5,partyHand.get(i).indexOf(",",bet))
                                .replaceAll("[^0-9]\\.?[^0-9]","").replaceAll("[^0-9.]",""));
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
                            if(card!=-1){
                               /* System.out.println("c *"+partyHand.get(i).substring(lost,card)+"*"+partyHand.get(i).substring(lost,card)
                                        .replaceAll("[^0-9]\\.?[^0-9]","").replaceAll("[^0-9.]",""));*/

                                betLose[i_nick] = Float.parseFloat(partyHand.get(i).substring(partyHand.get(i).indexOf(" ",lost+3),card)
                                    .replaceAll("[^0-9]\\.?[^0-9]","").replaceAll("[^0-9.]","")); }
                            else {
                                /*System.out.println("l *"+partyHand.get(i).substring(lost)+"*"+partyHand.get(i).substring(lost)
                                        .replaceAll("[^0-9]\\.?[^0-9]","").replaceAll("[^0-9.]",""));*/

                                betLose[i_nick] = Float.parseFloat(partyHand.get(i).substring(partyHand.get(i).indexOf(" ",lost+3))
                                    .replaceAll("[^0-9]\\.?[^0-9]","").replaceAll("[^0-9.]","")); }

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

    static List<String> changeSymbols(List<String> partyHand,List<String> starsHand,List<String> nicks, String[] resultHand){
        List<String> boardFlopTurn = new ArrayList<>();
        int p =0;
        for(int i=4; i<10; i++){ p++;
            if(partyHand.get(i).startsWith("Seat "+p+": ")) {
                //System.out.println(partyHand.get(i).substring(partyHand.get(i).lastIndexOf("$")+1,partyHand.get(i).lastIndexOf(")")));
                float stack = Float.parseFloat(partyHand.get(i).substring(partyHand.get(i).lastIndexOf("$")+1,partyHand.get(i).lastIndexOf(")")));
                //System.out.println(numberFormat.format(stack));
               // System.out.println(partyHand.get(i).substring(0,partyHand.get(i).lastIndexOf("$")+1)+numberFormat.format(stack)+" in chips) ");
                starsHand.add(partyHand.get(i).substring(0,partyHand.get(i).lastIndexOf("$")+1)+numberFormat.format(stack)+" in chips) "); }
        }

        String postblinds = "";
        for(int i=4+nicks.size(); i<4+nicks.size()+2; i++){
            if(partyHand.get(i).contains(" posts small blind "))postblinds = ": posts small blind $";
            else if(partyHand.get(i).contains(" posts big blind "))postblinds = ": posts big blind $";
            for(int n=0; n<nicks.size(); n++){
                if(partyHand.get(i).startsWith(nicks.get(n))){
                    starsHand.add(nicks.get(n)+postblinds+partyHand.get(i).substring(partyHand.get(i).lastIndexOf("(")+1).replace(")",""));
                }
            }
        }
        starsHand.add("*** HOLE CARDS ***");
        int indexOpt = IntStream.range(nicks.size()+6, partyHand.size()).filter(i -> partyHand.get(i).contains("** Dealing down cards **")).findFirst().getAsInt()+1;
        String cards = partyHand.get(indexOpt).substring(partyHand.get(indexOpt).lastIndexOf(" [ "),
                partyHand.get(indexOpt).lastIndexOf("]")+1).replace(",","").replace("[ ","[").replace(" ]","]");
        String dealt = partyHand.get(indexOpt).substring(0,partyHand.get(indexOpt).lastIndexOf("[")-1)+cards;
        starsHand.add(dealt);
        String board = ""; String cardStreet = ""; int street = 0; resultHand[1] = "r"; resultHand[2] = "r";
        String flop = "", turn = "";
        for(int i=8+nicks.size(); i<partyHand.size(); i++){
            if(partyHand.get(i).startsWith("** Summary **"))break;
            if(partyHand.get(i).startsWith("** Dealing Flop **")){
                board = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace(",","").replace("[ ","[").replace(" ]","]");
                starsHand.add("*** FLOP ***"+board);
                flop = board;
                street++;
                continue;
            }
            if(partyHand.get(i).startsWith("** Dealing Turn **")){
                cardStreet = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace("[ ","[").replace(" ]","]");
                starsHand.add("*** TURN ***"+board+cardStreet);
                board = board.replace("]","")+cardStreet.replace("[","");
                turn = board;
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
                if(partyHand.get(i).startsWith(nicks.get(n)+" ")){
                    if(partyHand.get(i).contains(" is all-In."))break;

                    if(partyHand.get(i).contains(" folds ")){
                        if(resultHand[n]==null)resultHand[n] = " folded before Flop (didn't bet)";
                        else {
                            if(street==0)resultHand[n] = " folded before Flop";
                            if(street==1)resultHand[n] = " folded on the Flop";
                            if(street==2)resultHand[n] = " folded on the Turn";
                            if(street==3)resultHand[n] = " folded on the River";
                        }
                        //System.out.println("N "+n);
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
        boardFlopTurn.add(board); boardFlopTurn.add(flop); boardFlopTurn.add(turn);
          return boardFlopTurn;
    }


    static String[] changeRIT(List<String> partyHand,List<String> starsHand,List<String> nicks,List<String> board){
        String[] boards = new String[2]; String cardStreet = "";

        for(int i=8+nicks.size(); i<partyHand.size(); i++){
            if(partyHand.get(i).startsWith("** Summary **"))break;
            if(partyHand.get(i).startsWith("** Dealing FIRST Flop **")){
                boards[0] = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace(",","").replace("[ ","[").replace(" ]","]");
                starsHand.add("*** FIRST FLOP ***"+boards[0]);
                continue;
            }
            if(partyHand.get(i).startsWith("** Dealing FIRST Turn **")){
                cardStreet = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace("[ ","[").replace(" ]","]");
                if(boards[0]==null)boards[0]=board.get(1);
                starsHand.add("*** FIRST TURN ***"+boards[0]+cardStreet);
                boards[0] = boards[0].replace("]","")+cardStreet.replace("[","");
                continue;
            }
            if(partyHand.get(i).startsWith("** Dealing FIRST River **")){
                cardStreet = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace("[ ","[").replace(" ]","]");
                if(boards[0]==null)boards[0]=board.get(2);
                starsHand.add("*** FIRST RIVER ***"+boards[0]+cardStreet);
                boards[0] = boards[0].replace("]","")+cardStreet.replace("[","");
                continue;
            }

            if(partyHand.get(i).startsWith("** Dealing SECOND Flop **")){
                boards[1] = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace(",","").replace("[ ","[").replace(" ]","]");
                starsHand.add("*** SECOND FLOP ***"+boards[1]);
                continue;
            }
            if(partyHand.get(i).startsWith("** Dealing SECOND Turn **")){
                cardStreet = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace("[ ","[").replace(" ]","]");
                if(boards[1]==null)boards[1]=board.get(1);
                starsHand.add("*** SECOND TURN ***"+boards[1]+cardStreet);
                boards[1] = boards[1].replace("]","")+cardStreet.replace("[","");
                continue;
            }
            if(partyHand.get(i).startsWith("** Dealing SECOND River **")){
                cardStreet = partyHand.get(i).substring(partyHand.get(i).lastIndexOf(" [ "),
                        partyHand.get(i).lastIndexOf("]")+1).replace("[ ","[").replace(" ]","]");
                if(boards[1]==null)boards[1]=board.get(2);
                starsHand.add("*** SECOND RIVER ***"+boards[1]+cardStreet);
                boards[1] = boards[1].replace("]","")+cardStreet.replace("[","");

            }

        }
        return boards;
    }

    static float[][] getCollectedRIT(float uncalled,int indUncalledNick,List<String> partyHand,List<String> nicks,String[] showdowns){
          float[][] result = new float[2][nicks.size()]; int p = -1;
          int indexOpt = IntStream.range(nicks.size()+8, partyHand.size()).filter(i -> partyHand.get(i).contains("Hand was run twice")).findFirst().getAsInt();
          for(int i=indexOpt; i<partyHand.size(); i++){
              if(partyHand.get(i).startsWith("FIRST Board:")){p=0;continue;}
              if(partyHand.get(i).startsWith("SECOND Board:")){p=1;continue;}
              if(partyHand.get(i).contains(" balance "))break;
              if(partyHand.get(i).contains(" collected "))
                  for(int n=0; n<nicks.size(); n++){
                      if(partyHand.get(i).startsWith(nicks.get(n)+" collected ")){
                          //String line = partyHand.get(i).substring(nicks.get(n).length()+12,partyHand.get(i).indexOf(" ",nicks.get(n).length()+12));
                          float collect = Float.parseFloat(partyHand.get(i).substring(nicks.get(n).length()+12,partyHand.get(i).indexOf(" ",nicks.get(n).length()+12)));
                          if(p==0){
                                  if(n==indUncalledNick)result[0][n]= BigDecimal.valueOf(collect-uncalled).setScale(2, RoundingMode.HALF_UP).floatValue();
                                  else result[0][n]= BigDecimal.valueOf(collect).setScale(2, RoundingMode.HALF_UP).floatValue();

                              int card = partyHand.get(i).indexOf("[ ",nicks.get(n).length()+10);
                              if(card!=-1){
                                  showdowns[n] = partyHand.get(i).substring(card,partyHand.get(i).indexOf("]",card)+1)
                                          .replace(",","").replace("[ ","[").replace(" ]","]");
                              }

                          }
                          if(p==1)result[1][n]= BigDecimal.valueOf(collect).setScale(2, RoundingMode.HALF_UP).floatValue();
                      }
                  }
          }
          return result;
    }

    static List<String> getStartPokerStarsHand(List<String> partyHand, String[] strBlinds){
       List<String> starsHand = new ArrayList<>();

       String numHand = partyHand.get(0).substring(28,partyHand.get(0).lastIndexOf(" ")).replaceAll("[^0-9]","").substring(0,13);
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
        //start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\JavaProjects\\ForGoodGame\\test_party\\output");
        start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\Poker\\PartyPokerHands\\PokerStars\\in_test_hand\\");
        //start_ReadFilesInFolder("F:\\Moe_Alex_win_10\\Poker\\PartyPokerHands\\PokerStars\\party_right\\");
    }

}
