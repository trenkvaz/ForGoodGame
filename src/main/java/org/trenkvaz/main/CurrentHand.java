package org.trenkvaz.main;

import org.trenkvaz.database_hands.ReadHistoryGetStats;
import org.trenkvaz.database_hands.Work_DataBase;
import org.trenkvaz.ui.StartAppLauncher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.trenkvaz.main.CaptureVideo.*;
import static org.trenkvaz.main.OCR.PREFLOP;
import static org.trenkvaz.ui.StartAppLauncher.SCALE;
import static org.trenkvaz.ui.StartAppLauncher.totalResultHero;
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

    float[] resultsAllin = new float[6];
    float[] winLosePlayers = new float[6];
    float[][] totalInvestsByStreet = new float[4][6];

    List<List<Float>> preflopActionsStats = new ArrayList<>(6);
    List<List<Float>> flopActionsStats = new ArrayList<>(6);
    List<List<Float>> turnActionsStats = new ArrayList<>(6);
    List<List<Float>> riverActionsStats = new ArrayList<>(6);

    public record TempHand(long time_hand, short cards_hero, short position_hero, Float[] stacks, String[] nicks){}
    CreatingHUD creatingHUD;
    //TEST
    OCR ocr;

    CurrentHand(OCR ocr){
        this.ocr = ocr;
        this.creatingHUD = ocr.creatingHUD;
        creatingHUD.clear_MapStats();
        testTable = ocr.table;
        for(int i=0; i<6; i++){
            preflopActionsStats.add(new ArrayList<>()); preflopActionsStats.get(i).add(0.0f);
            flopActionsStats.add(new ArrayList<>());turnActionsStats.add(new ArrayList<>());riverActionsStats.add(new ArrayList<>());
            startStacks[i] = 0f;
        }
        time_hand =  get_HandTime();
        position_bu_on_table = ocr.current_bu;
        nicks[0] = NICK_HERO;
        cards_hero[0] = ocr.current_hero_cards[0];
        cards_hero[1] = ocr.current_hero_cards[1];
        totalInvestsByStreet[PREFLOP][4]= StartAppLauncher.SB;
        totalInvestsByStreet[PREFLOP][5]= 1;
    }



    void setIs_nicks_filled(){
        creatingHUD.send_current_hand_to_creating_hud(nicks,ocr.pokerPosIndWithNumOnTable, pokerPosHero);
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
        countResultHand();
        roundingAllNums();
        if(let_SaveTempHandsAndCountStatsCurrentGame){
            float[] stacks = new float[6];
            for(int i=0; i<6; i++){ stacks[i]=this.startStacks[i]; }
            ReadHistoryGetStats.count_StatsCurrentGame(current_map_stats, work_main_stats,nicks,stacks,preflopActionsStats,startAmountPlayers);
            Work_DataBase.record_rec_to_TableTempHands(new TempHand(time_hand,get_short_CardsHero(cards_hero),(short) pokerPosHero, startStacks,nicks));
        }
    }



    static short get_short_CardsHero(String[] cards_hero){ return (short)
            ((byte) Arrays.asList(DECK).indexOf(cards_hero[0])*1000+(byte) Arrays.asList(DECK).indexOf(cards_hero[1])); }


    private void countResultHand(){
            for(int pokerPos =0; pokerPos<6; pokerPos++){
                if(ocr.pokerPosIndWithNumOnTable[pokerPos]==0)continue;
                float invest = 0; for(int i=0; i<4; i++) invest += totalInvestsByStreet[i][pokerPos];
                winLosePlayers[pokerPos] =  BigDecimal.valueOf(resultsAllin[pokerPos]-invest).setScale(SCALE, RoundingMode.HALF_UP).floatValue();
            }
        totalResultHero+=winLosePlayers[pokerPosHero];
    }


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
