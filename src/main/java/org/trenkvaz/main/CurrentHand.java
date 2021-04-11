package org.trenkvaz.main;

import javafx.scene.text.Text;
import org.trenkvaz.database_hands.ReadHistoryGetStats;
import org.trenkvaz.database_hands.Work_DataBase;
import org.trenkvaz.ui.StartAppLauncher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.main.OCR.*;
import static org.trenkvaz.newstats.CreateNewHUD.mapTypesPots;
import static org.trenkvaz.ui.StartAppLauncher.*;
//import static org.trenkvaz.ui.StartAppLauncher.creatingHUD;


public class CurrentHand {

    int testTable;
    long time_hand;
    String[] nicks = new String[6], cards_hero = {"",""};
    Float[] startStacks = new Float[6];
    float[][] startStacksAtStreets = {new float[6],new float[6],new float[6],new float[6],new float[6]};


    int pokerPosHero = -1;


    //int[] poker_positions_by_pos_table_for_nicks;
    boolean is_nicks_filled = false, is_stacks_filled = false;


    int position_bu_on_table = 0;


    int streetAllIn = -1;
    int[] firstBetPostflopPokerPos = {-1,-1,-1,-1};
    int[] playersFoldOrAllIn = new int[6];
    boolean[] isFinishedStreets = {false,false,false,false};
    boolean[] isStartStreets =  {false,false,false,false,false};
    int startAmountPlayers = 0;
    boolean isFoldHero = false;

    float[] startInvest = {0,0,0,0,SB,1};

    List<List<Float>> preflopActionsStats = new ArrayList<>(6);
    List<List<Float>> flopActionsStats = new ArrayList<>(6);
    List<List<Float>> turnActionsStats = new ArrayList<>(6);
    List<List<Float>> riverActionsStats = new ArrayList<>(6);
    static String[][] testCards = new String[6][2];

    

    /*List<List<String>> allActionsTest = new ArrayList<>(6);
    float[] currentStacks = new float[6];*/

    public record TempHand(long time_hand, short cards_hero, short position_hero, Float[] stacks, String[] nicks){}
    //CreatingHUD creatingHUD;
    //TEST
    OCR ocr;

    CurrentHand(OCR ocr){
        this.ocr = ocr;
        //this.creatingHUD = ocr.creatingHUD;
        //creatingHUD.clear_MapStats();
        createNewHUD.initNewTableHUD(ocr.table-1);
        testTable = ocr.table;
        for(int i=0; i<6; i++){
            preflopActionsStats.add(new ArrayList<>()); preflopActionsStats.get(i).add(0.0f);
            flopActionsStats.add(new ArrayList<>());turnActionsStats.add(new ArrayList<>());riverActionsStats.add(new ArrayList<>());
            startStacks[i] = 0f;
/*
            allActionsTest.add(new ArrayList<>());
            if(i<4)allActionsTest.get(i).add("0");
            if(i==4)allActionsTest.get(i).add(SB+"");
            if(i==5)allActionsTest.get(i).add("1");*/
        }
        time_hand =  get_HandTime();
        position_bu_on_table = ocr.current_bu;
        nicks[0] = NICK_HERO;
        cards_hero[0] = ocr.current_hero_cards[0];
        cards_hero[1] = ocr.current_hero_cards[1];

    }


    public void setTypePotForPostFlop(){

        //System.out.println(RED+"TYPES POT"+RESET);
        int maxSizeListActions = preflopActionsStats.stream().mapToInt(List::size).max().getAsInt();
        int[] pot = new int[7]; // limp raise call 3bet call3bet 4bet call4bet    лимпы и коллы если один 1 если больше то 2
        int[] lastActions = new int[6];
        int raise = 1; int actionType = 0;
        for(int act=1; act<maxSizeListActions; act++)
            for(int pokPos=0; pokPos<6; pokPos++){
                if(preflopActionsStats.get(pokPos).size()-1<act)continue;
                float actionSize = preflopActionsStats.get(pokPos).get(act);
                //System.out.println("act "+actionSize);
                if(actionSize==Float.NEGATIVE_INFINITY)actionType=-10;
                else if(actionSize==Float.POSITIVE_INFINITY)actionType= 10;
                else if(actionSize!=Float.NEGATIVE_INFINITY&&actionSize<0)actionType= -(raise);
                else if(actionSize!=Float.POSITIVE_INFINITY&&actionSize>0){ if(raise==5)actionType = raise;
                else actionType = ++raise;}
                lastActions[pokPos]=actionType;
                if(actionType==-10||actionType==10)continue;
                switch (actionType){
                    case -1 -> { if(pot[0]==0)pot[0]=1;else pot[0]=2;}
                    case -2 -> { if(pot[2]==0)pot[2]=1;else pot[2]=2;}
                    case -3 -> { if(pot[4]==0)pot[4]=1;else pot[4]=2;}
                    case -4 -> { if(pot[6]==0)pot[6]=1;else pot[6]=2;}
                    case 2-> pot[1] = 1;
                    case 3-> pot[3] = 1;
                    case 4-> pot[5] = 1;
                }
            }
        String namePot = null;
        //System.out.println("pot "+Arrays.toString(pot));
        for(Map.Entry<String,int[]> entry: mapTypesPots.entrySet()) {
            //System.out.println(entry.getKey()+" "+Arrays.toString(entry.getValue()));
            if(Arrays.equals(entry.getValue(),pot)){ namePot = entry.getKey(); break; }
        }

        String[] typesPots = {"PRE","PRE","PRE","PRE","PRE","PRE"};
        if(namePot!=null)
        for(int pokPos= 0; pokPos<6; pokPos++){
            if(lastActions[pokPos]==0||lastActions[pokPos]==-10||lastActions[pokPos]==10)continue;
            if(lastActions[pokPos]>0)typesPots[ocr.pokerPosIndWithNumOnTable[pokPos]-1] = namePot+"_R";
            else typesPots[ocr.pokerPosIndWithNumOnTable[pokPos]-1] = namePot+"_C";
        }


        if(namePot!=null)ocr.testCurrentHand.setResultPotShowHUD(time_hand,pot,lastActions,true);                 //TEST

        /*System.out.println("TABEL "+testTable);
        System.out.println("pot "+Arrays.toString(pot));
        System.out.println("lastact "+Arrays.toString(lastActions));
        System.out.println("strpot "+Arrays.toString(typesPots));*/

      setDataToCreateNewHUD(typesPots);
    }





    public void setDataToCreateNewHUD(String[] typesPots){
        /*int street = 0;
        if(streetAllIn!=-1){
            street = streetAllIn;
        } else {
            if(isStartStreets[RIVER]||isStartStreets[ENDRIVER])street = RIVER;
            else if(isStartStreets[TURN])street = TURN;
            else if(isStartStreets[FLOP])street = FLOP;
        }
        List<List<Float>> streetActionsStats = preflopActionsStats;
        if(street==1) streetActionsStats = flopActionsStats;
        if(street==2) streetActionsStats = turnActionsStats;
        if(street==3) streetActionsStats = riverActionsStats;*/
        //creatingHUD.addNewHUDToOldHUD();

        Boolean[] isPot = {false};                                                                                       //TEST

        createNewHUD.createHUDoneTable(isPot,nicks,ocr.table-1,typesPots,ocr.pokerPosIndWithNumOnTable,pokerPosHero,0,null);

        if(ocr.testCurrentHand.isPot)ocr.testCurrentHand.resultPotShowHUD+=" "+isPot[0]+"\r\n";                          // TEST
    }



    void setIs_nicks_filled(){

        String[] typesPots = {"PRE","PRE","PRE","PRE","PRE","PRE"};
        setDataToCreateNewHUD(typesPots);

        // проверка что все ники распознаны, чтобы не обращатся к методу распознавания ников
        for(int i=1; i<6; i++){
            if(ocr.frameTable.whoPlayOrNo()[i]==0)continue;
            if(nicks[i]==null){is_nicks_filled = false; return;}
        }
        is_nicks_filled = true;
    }


    public void finalCurrendHand(){
        // расстановка ников по покерным позициям
        String[] nicks_by_positions = new String[6];
        for(int i=0; i<6; i++){
            if(ocr.pokerPosIndWithNumOnTable[i]==0)continue;
            if(nicks[ocr.pokerPosIndWithNumOnTable[i]-1]==null)continue;
            nicks_by_positions[i] = nicks[ocr.pokerPosIndWithNumOnTable[i]-1];
        }
        nicks = nicks_by_positions;
        roundingAllNums();
        if(let_SaveTempHandsAndCountStatsCurrentGame){
            float[] stacks = new float[6];
            for(int i=0; i<6; i++){ stacks[i]=this.startStacks[i]; }
            Work_DataBase.record_rec_to_TableTempHands(new TempHand(time_hand,get_short_CardsHero(cards_hero),(short) pokerPosHero, startStacks,nicks));
            workStats.countOneHand(testCards,nicks,stacks,null,new ArrayList<>(Arrays.asList(preflopActionsStats,flopActionsStats,turnActionsStats,riverActionsStats)),null,pokerPosHero);
        }

      /*  if(isTestDBandStats){
            float[] stacks = new float[6];
            for(int i=0; i<6; i++){ stacks[i]=this.startStacks[i]; }
            workStats.countOneHand(testCards,nicks,stacks,null,new ArrayList<>(Arrays.asList(preflopActionsStats,flopActionsStats,turnActionsStats,riverActionsStats)),null,pokerPosHero);
        }*/
    }



    static short get_short_CardsHero(String[] cards_hero){ return (short)
            ((byte) Arrays.asList(DECK).indexOf(cards_hero[0])*1000+(byte) Arrays.asList(DECK).indexOf(cards_hero[1])); }




    private void roundingAllNums(){
        for(int pokerPos = 0; pokerPos<6; pokerPos++){
            if(ocr.pokerPosIndWithNumOnTable[pokerPos]==0)continue;
            startStacks[pokerPos] = BigDecimal.valueOf(startStacks[pokerPos]).setScale(SCALE, RoundingMode.HALF_UP).floatValue();

            for(int indAct=1; indAct<preflopActionsStats.get(pokerPos).size(); indAct++){
                float act = preflopActionsStats.get(pokerPos).get(indAct);
                if(act==0||act==Float.NEGATIVE_INFINITY||act==Float.POSITIVE_INFINITY)continue;
                preflopActionsStats.get(pokerPos).set(indAct,BigDecimal.valueOf(act).setScale(SCALE, RoundingMode.HALF_UP).floatValue());
            }

            for(int indAct=0; indAct<flopActionsStats.get(pokerPos).size(); indAct++){
                float act = flopActionsStats.get(pokerPos).get(indAct);
                if(act==0||act==Float.NEGATIVE_INFINITY||act==Float.POSITIVE_INFINITY)continue;
                flopActionsStats.get(pokerPos).set(indAct,BigDecimal.valueOf(act).setScale(SCALE, RoundingMode.HALF_UP).floatValue());
            }

            for(int indAct=0; indAct<turnActionsStats.get(pokerPos).size(); indAct++){
                float act = turnActionsStats.get(pokerPos).get(indAct);
                if(act==0||act==Float.NEGATIVE_INFINITY||act==Float.POSITIVE_INFINITY)continue;
                turnActionsStats.get(pokerPos).set(indAct,BigDecimal.valueOf(act).setScale(SCALE, RoundingMode.HALF_UP).floatValue());
            }

            for(int indAct=0; indAct<riverActionsStats.get(pokerPos).size(); indAct++){
                float act = riverActionsStats.get(pokerPos).get(indAct);
                if(act==0||act==Float.NEGATIVE_INFINITY||act==Float.POSITIVE_INFINITY)continue;
                riverActionsStats.get(pokerPos).set(indAct,BigDecimal.valueOf(act).setScale(SCALE, RoundingMode.HALF_UP).floatValue());
            }
        }
    }

}
