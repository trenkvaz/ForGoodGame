package org.trenkvaz.newstats;



import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

import static org.trenkvaz.main.CaptureVideo.NICK_HERO;
import static org.trenkvaz.main.OCR.*;

public class FilterStata implements Serializable {

    static final int UTG = 0, MP = 1, CO = 2, BU = 3, SB = 4, BB = 5;
    static final int ACT_PLAYER = 0,  LIMP = 1, LIMPS = 2, RAISER = 3, CALLERS = 4,  _3BET = 5, CALLERS_3BET= 6, _4BET = 7, CALLERS_4BET= 8, _5BET = 9, CALLERS_5BET= 10;
    // new int[]{0,-1,1,-1,2,-1,-1,-1}
    static final int SELECT = 0, CALL = 1, RAISE = 2;
    static final String[] strPositions = {"utg","mp","co","bu","sb","bb"};
    static final int[] postflopPoses = {4,5,0,1,2,3};

    /*public Map<String, DataStata> mapNicksDates = new HashMap<>();
    public transient DataStata[] dataStatsOneHand;*/
    public boolean isCreateStructureDB = false;
    public String mainNameFilter;
    // 0 позиции игрока, 1 позиции оппов;  0 нет позы, 1 есть поза
    int[] posesPlayer;
    int[][] posOpps;
    public String strPosStata;
    private boolean isAllowInGame = false;
    //public boolean isRanges = false;
    public boolean isVsHero = false;
    public int[] raiseSizesForRange;
    public boolean isRangeCall = false;
    //    0 не, 1 да - Действия на Префлоп,Флоп,Терн,Ривер;  0 нет, 1 да - Видел Флоп, Терн, Ривер
    // Условия для выборки
    public int streetOfActs = -1; // Префлоп,Флоп,Терн,Ривер - 0,1,2,3
    private int seenStreet = 0;
    // Результат, действия

    // префлоп рейзы в ББ, на постфлопе проценты от банка
    // пример префлоп 2 3 5 значит до 2, от 2 до 3, от 3 до 5
    // пример постфлоп 30 50 100 это проценнты от банка до 30, от 30 до 50 и т.д
    public int[] vsBetSizes;
    // W$WSF, WTSD, W$SD, AG
    public boolean[] specStats = new boolean[4];
    // 1 раунд акт 0 или сделанное действие для определения пота нужны сделанные действия, если это расчет префлоп стат то в последнем раунде действие не важно и там всегда 0
    // , если важна позиция действия и было действие 2, нет действие -1, было действие поза не важна 1
    // поза может быть важна только для рейзов и первого лимпа
    public List<int[]> conditionsPreflopActions;
    public List<int[]> conditionsFlopActions;
    public List<int[]> conditionsTurnActions;
    public List<int[]> conditionsRiverActions;
    public boolean[] structureParametres = new boolean[9];
    // TEST
    static String[] card;

    public String getFullNameStata(){return mainNameFilter+strPosStata;}


    public void countOnePlayerStata(boolean isInGame,int posPlayer,String nick, float stack, List<List<List<Float>>> sizeActionsStreetsStats,
                                    boolean isWin,boolean isShowDown,String[] cardsPlayer,int rangePlayer,int posHero,DataStata dataStata,List<int[][]> listPokerActionsInRoundsByPositions){
        if(isInGame)if(!isAllowInGame)return;
        if(posesPlayer[posPlayer]==0)return;
        //DataStata dataStata = getNewDataStata(nick);


                                                                                                                          // TEST !!
       /* boolean isTest = false;
        if(getFullNameStata().equals("sraisepot_vs_caller_flop_ip_co_v_all_v_sb_bb_")&&nick.equals("$ю$"+NICK_HERO+"$ю$")){
            isTest = true;
            card = cardsPlayer;
        }
        if(!isTest) return;*/



        if(streetOfActs==-1){ countSpecialNotActionStats(dataStata,sizeActionsStreetsStats,posPlayer,isWin,isShowDown,listPokerActionsInRoundsByPositions.get(0));}
        else if(streetOfActs==0){countPreflop(dataStata,sizeActionsStreetsStats.get(0),posPlayer,nick,stack,rangePlayer,posHero, listPokerActionsInRoundsByPositions.get(0));}
        else countPostFlop(dataStata,sizeActionsStreetsStats.get(streetOfActs),streetOfActs,posPlayer,nick,stack,listPokerActionsInRoundsByPositions);
        //mapNicksDates.put(nick,dataStata);
    }


    public boolean countPreflop(DataStata dataStata,List<List<Float>> preflopSizeActions, int posPlayer, String nick, float stack, int rangePlayer,int posHero,int[][] pokerActsRoundsByPoses){

        if(pokerActsRoundsByPoses[0][posPlayer]==0)return false; // ситуация когда херо на ББ и все сфолдили
        if(pokerActsRoundsByPoses.length<conditionsPreflopActions.size())return false;
        for(int round =0; round<conditionsPreflopActions.size(); round++){ if(!isEqualsPreflopConditionsToActions(pokerActsRoundsByPoses,round,posPlayer))return false;}
        //return true;
        dataStata.isRecord =true;
        dataStata.mainSelCallRaise[SELECT]++;

        /*System.out.println("name "+getFullNameStata());
        System.out.println(Arrays.toString(pokerActsRoundsByPoses[0])+" "+posPlayer);
        if(pokerActsRoundsByPoses.length>1)System.out.println("2 "+Arrays.toString(pokerActsRoundsByPoses[1]));*/

        int pokerActPlayer = pokerActsRoundsByPoses[conditionsPreflopActions.size()-1][posPlayer];
        if(pokerActPlayer==-10)return false;
        if(pokerActPlayer<0||pokerActPlayer==10){dataStata.mainSelCallRaise[CALL]++; if(isRangeCall&&rangePlayer!=0){dataStata.rangeCall[rangePlayer]++; } }
        else if(pokerActPlayer>0){dataStata.mainSelCallRaise[RAISE]++;
        // если нужно просчитать Рейндж Сайзы и есть карты
        if(raiseSizesForRange!=null&&rangePlayer!=0)countRaiseSizesForRange(dataStata,preflopSizeActions.get(posPlayer),rangePlayer,stack); }
        return true;
    }


    private void countPostFlop(DataStata dataStata,List<List<Float>> postflopActions,int street, int posPlayer, String nick, float stack,List<int[][]> listPokerActionsInRoundsByPositions){

       // System.out.println("1 post");

        if(listPokerActionsInRoundsByPositions.get(0)[0][posPlayer]==0)return; // ситуация когда херо на ББ и все сфолдили
        if(listPokerActionsInRoundsByPositions.get(street).length==0)return; // нет постфлопа

        for(int round =0; round<conditionsPreflopActions.size(); round++){
            if(!isEqualsPreflopConditionsToActions(listPokerActionsInRoundsByPositions.get(0),round,posPlayer))return;}

       // System.out.println("IS !!!!!!!!");

        List<int[]> conditionsPostFlop = conditionsFlopActions;
        if(street==TURN)conditionsPostFlop = conditionsTurnActions;else if(street==RIVER)conditionsPostFlop = conditionsRiverActions;
        for(int round =0; round<conditionsPostFlop.size(); round++){
            if(!isEqualsPostFlopConditions(listPokerActionsInRoundsByPositions.get(street),round,posPlayer,conditionsPostFlop))return;}
        dataStata.isRecord =true;
        dataStata.mainSelCallRaise[SELECT]++;

                                                                                                                           // TEST !!!
       // System.out.println(Arrays.toString(card));

        int pokerActPlayer = listPokerActionsInRoundsByPositions.get(street)[conditionsPostFlop.size()-1][posPlayer];
        if(pokerActPlayer==10||pokerActPlayer==-10)return;
        if(pokerActPlayer>0)dataStata.mainSelCallRaise[RAISE]++;
        else if(pokerActPlayer<0)dataStata.mainSelCallRaise[CALL]++;
    }


    private void countAgainstHero(DataStata dataStata,int posHero,int[][] actsRoundsByPoses,int posPlayer){
        boolean isImpPos = false;
        for(int[] condRaund:conditionsPreflopActions) isImpPos = IntStream.range(1, condRaund.length).anyMatch(i -> condRaund[i] == 2);
        if(!isImpPos)return;
        // здесь может быть несколько раундов с позами оппов нужно выбрать где будет херо
        //if(posesPlayer[posHero]==0)return;
        dataStata.isRecord = true;
        dataStata.selCallRaiseVsHero[SELECT]++;
        // последнее действие на основе последнего индекса условий действий, указывает на последний раунд где есть нужное действие
        int actPlayer = actsRoundsByPoses[conditionsPreflopActions.size()-1][posPlayer];
        if(actPlayer==-10)return;
        if(actPlayer<0||actPlayer==10)dataStata.selCallRaiseVsHero[CALL]++;
        else if(actPlayer>0)dataStata.selCallRaiseVsHero[RAISE]++;
    }


    private void countRaiseSizesForRange(DataStata dataStata, List<Float> preflopActions, int rangePlayer, float stack){
        int first = 0, second =0; float raise = 0;
        for(int i=0; i<raiseSizesForRange.length; i++){
            if(i>0)first = raiseSizesForRange[i-1];
            second = raiseSizesForRange[i];
            raise = preflopActions.get(conditionsPreflopActions.size());
            if(raise==stack)raise = 100000; // эквивалент оллина
            // первое число в rangeRaiseSizes это количество рейзов данным сайзом, следующие 169 это количество рейз конкретной комбой
            if(raise>first&&raise<=second){ dataStata.rangeRaiseSizes.get(i)[0]++; dataStata.rangeRaiseSizes.get(i)[rangePlayer]++;break;}
            // лист с массивами на два элимента
        }
    }



    private boolean isEqualsPreflopConditionsToActions(int[][] actsRoundsByPoses, int round, int posPlayer){
        // проверка необходимое действие предидущих раундов игрока совпадало с реальным действием в первом раунде
        // или если это условие типа пота то все действия должны совпадать
        if(conditionsPreflopActions.get(round)[0]!=0)if(conditionsPreflopActions.get(round)[0]!=actsRoundsByPoses[round][posPlayer])return false;



        int[] resultActions = new int[11];
        Arrays.fill(resultActions,-1);
        if(round==0) { for (int pokPos=0; pokPos<posPlayer; pokPos++) setResultActions(resultActions,pokPos,actsRoundsByPoses[round][pokPos]); }
        else if(round==1){



            /*System.out.println("action player 1 raund "+conditionsPreflopActions.get(0)[0]+" stataction "+actsRoundsByPoses[0][posPlayer]);
            for(int i=0; i<actsRoundsByPoses.length;i++){
                System.out.print("act "+(i+1)+"  ");
                for(int a=0; a<6; a++) System.out.print(actsRoundsByPoses[i][a]+" ");
                System.out.println();
            }*/
            for(int pokPos = posPlayer+1; pokPos<6; pokPos++) setResultActions(resultActions,pokPos,actsRoundsByPoses[0][pokPos]);
            //if(actsRoundsByPoses.length<2)return false;
            if(actsRoundsByPoses.length==2)for(int pokPos = 0; pokPos<posPlayer; pokPos++) setResultActions(resultActions,pokPos,actsRoundsByPoses[1][pokPos]);
        }
        // не совпадают условия и действия липеров коллеров всех банков
        /*if(conditionsPreflopActions.get(round)[LIMPS]!=resultActions[LIMPS])return false;
        if(conditionsPreflopActions.get(round)[CALLERS]!=resultActions[CALLERS])return false;*/
        //for(int a:resultActions) System.out.print(" "+a);
        /*if(round==1){
        System.out.println("res "+Arrays.toString(resultActions));
        System.out.println("cond "+Arrays.toString(conditionsPreflopActions.get(round)));
            System.out.println("acts 0 "+Arrays.toString(actsRoundsByPoses[0]));
            if(actsRoundsByPoses.length==2)System.out.println("acts 1 "+Arrays.toString(actsRoundsByPoses[1]));
            System.out.println();
        }*/



        //for(int c=2; c<11; c+=2)if(conditionsPreflopActions.get(round)[c]!=resultActions[c])return false;

        //if(round==1)System.out.println(round+" is act");

        // для лимперов так
        if(conditionsPreflopActions.get(round)[2]!=resultActions[2])return false;

        for(int act=1; act<11; act++){
            if(act==2)continue;
            if(conditionsPreflopActions.get(round)[act]==-1&&resultActions[act]==-1)continue; // не должно быть действия и нет действия
            if(conditionsPreflopActions.get(round)[act]!=-1&&resultActions[act]==-1)return false; // должно быть действие и нет действия
            if(conditionsPreflopActions.get(round)[act]==-1&&resultActions[act]!=-1)return false;// не должно быть действия и есть действие

            if(conditionsPreflopActions.get(round)[act]==2){
                // если коллеров больше чем 1, а это число 6 значит позиция не определена и считается что действие не подошло условию
                if(resultActions[act]==6)return false;
                // поза действия важна проверка что в индексах поз оппов нет нуля, значит нет соответсвия необходимых позиций оппов и его позиций действия
                if(posOpps[round][resultActions[act]]==0)return false;
            }
        }



        return true;
    }



    private boolean isEqualsPostFlopConditions(int[][] actsRoundsByPoses, int round, int posPlayer, List<int[]> conditionsPostFlop){
       /* List<int[]> conditionsPostFlop = conditionsFlopActions;
        if(street==2)conditionsPostFlop = conditionsTurnActions;else if(street==3)conditionsPostFlop = conditionsRiverActions;*/


        if(conditionsPostFlop.get(round)[0]!=0)if(conditionsPostFlop.get(round)[0]!=actsRoundsByPoses[round][posPlayer])return false;
        if(conditionsPostFlop.get(0).length==2){ // игра 1 на 1
            int actOpps = 0;
           //if(actsRoundsByPoses.length>round) System.out.println("act "+round+" "+Arrays.toString(actsRoundsByPoses[round]));

            for(int pokPos = 0; pokPos<6; pokPos++){
                if(postflopPoses[pokPos]==posPlayer)break;
                if(actsRoundsByPoses[round][postflopPoses[pokPos]]==0)continue;
                actOpps = actsRoundsByPoses[round][postflopPoses[pokPos]];
            }
                                                                                                                         // TEST
            //System.out.println("cond "+conditionsPostFlop.get(round)[1]+" act "+ actOpps);

            return conditionsPostFlop.get(round)[1] == actOpps;
        }
        return true;
    }


    private void setResultActions(int[] resultActions,int pokPos, int act){
        switch (act) {
            // если первый лимпер то указывается его поза, если есть еще лимперы то указывается что есть лимперы
            case -1 -> {if(resultActions[LIMP]==-1) resultActions[LIMP] = pokPos; else resultActions[LIMPS]=1;}
            // для коллеров пока так если один то указывается поза если много то 6
            case -2 ->  { if(resultActions[CALLERS]==-1)resultActions[CALLERS] = pokPos; else resultActions[CALLERS] = 6;                   }
            case 2 -> resultActions[RAISER] = pokPos;
            case 3 -> resultActions[_3BET] = pokPos;
            case -3 -> { if(resultActions[CALLERS_3BET]==-1)resultActions[CALLERS_3BET] = pokPos; else resultActions[CALLERS_3BET] = 6;                   }
            case 4 -> resultActions[_4BET] = pokPos;
            case -4 -> { if(resultActions[CALLERS_4BET]==-1)resultActions[CALLERS_4BET] = pokPos; else resultActions[CALLERS_4BET] = 6;                   }
            case 5 -> resultActions[_5BET] = pokPos;
            case -5 -> { if(resultActions[CALLERS_5BET]==-1)resultActions[CALLERS_5BET] = pokPos; else resultActions[CALLERS_5BET] = 6;                   }
        }
    }


    private void countSpecialNotActionStats(DataStata dataStata,List<List<List<Float>>> actionsStreetsStats,int posPlayer,
                                            boolean isWin,boolean isShowDown,int[][] actsRoundsByPoses){
        if(specStats[0]) countW$WSF(dataStata,actionsStreetsStats,posPlayer,isWin,isShowDown);
        if(specStats[1]) countWTSDafFLOP(dataStata,actionsStreetsStats.get(1),posPlayer,isShowDown);
        if(specStats[2]) countW$SD(dataStata,isWin,isShowDown);
        if(specStats[3]) countVPIPandPFR(dataStata,actsRoundsByPoses,posPlayer);
    }


    private void countW$WSF(DataStata dataStata, List<List<List<Float>>> actionsStreetsStats, int posPlayer, boolean isWin, boolean isShowDown){
         // проверка что игрок видел флоп шоудаун это флоп или действие на флопе
         if(!isShowDown){ if(actionsStreetsStats.get(1).get(posPlayer).isEmpty())return; }
         dataStata.isRecord =true;
         dataStata.W$WSF[0]++; if(isWin)dataStata.W$WSF[1]++;
    }


    private void countWTSDafFLOP(DataStata dataStata, List<List<Float>> actionsStreetStats, int posPlayer, boolean isShowDown){
        if(actionsStreetStats.get(posPlayer).isEmpty()&&!isShowDown)return;
        dataStata.isRecord =true;
        dataStata.WTSD[0]++;if(isShowDown)dataStata.WTSD[1]++;
    }


    private void countW$SD(DataStata dataStata,boolean isWin, boolean isShowDown){
        if(isShowDown){ dataStata.isRecord=true; dataStata.W$SD[0]++;if(isWin)dataStata.W$SD[1]++; }
    }


    private void countVPIPandPFR(DataStata dataStata,int[][] actsRoundsByPoses,int posPlayer){
        if(actsRoundsByPoses[0][posPlayer]==0)return;
        dataStata.isRecord=true;
        dataStata.VPIP_PFR[SELECT]++;
        int actPlayer = actsRoundsByPoses[0][posPlayer];
        if(actPlayer==-10||actPlayer==10)return;
        dataStata.VPIP_PFR[CALL]++;
        if(actPlayer>0)dataStata.VPIP_PFR[RAISE]++;
    }


    public static class Builder {
        private final FilterStata stata;

        public Builder() {
            stata = new FilterStata();
        }

        public Builder setPosStata(int[] posPlayer1,int[][] posOpps){
            stata.posesPlayer = posPlayer1;
            stata.posOpps = posOpps;

            stata.strPosStata = "";
            if(Arrays.stream(posPlayer1).filter(c->c>0).count()==6){stata.strPosStata+="all_v_";}
            else {for(int i=0; i<6; i++){ if(posPlayer1[i]==0)continue;stata.strPosStata+=strPositions[i]+"_"; } stata.strPosStata+="v_";}

            for(int r=0; r<posOpps.length; r++){
            if(Arrays.stream(posOpps[r]).filter(c->c>0).count()==6)stata.strPosStata+="all";
            else for(int i=0; i<6; i++){ if(posOpps[r][i]==0)continue;stata.strPosStata+=strPositions[i]+"_"; }
            if(posOpps.length>1&&r<posOpps.length-1)stata.strPosStata+="_v_";
            }

            return this;
        }

        public Builder setMainNameFilter(String mainNameFilter1 ){ stata.mainNameFilter = mainNameFilter1; return this;}

        //public Builder isRange(){stata.isRanges = true; return this;}

        public Builder isVsHero(){stata.isVsHero = true; stata.structureParametres[2] = true; return this;}

        public Builder isAllowInGame(){stata.isAllowInGame = true; return this;}

        public Builder isRangeCall(){stata.isRangeCall = true; stata.structureParametres[3] = true;return this;}

        public Builder setStreetOfActs(int streetOfActs){ stata.streetOfActs = streetOfActs; stata.structureParametres[0] = true; return this; }

        public Builder setSeenStreet(int seenStreet1){ stata.seenStreet = seenStreet1; return this; }

        public Builder setVsBetSizes(int[] vsBetSizes1){ stata.vsBetSizes = vsBetSizes1; stata.structureParametres[1] = true; return this; }

        public Builder setRaiseSizesForRange(int[] raiseSizesForRange){ stata.raiseSizesForRange = raiseSizesForRange; stata.structureParametres[4] = true;return this; }

        public Builder setConditionsPreflopActions(List<int[]> conditionsPreflopActions){stata.conditionsPreflopActions = conditionsPreflopActions; return this;}

        public Builder setConditionsPostFlopActions(List<int[]> conditionsPostflopActions,int street){
           switch (street){
               case FLOP->stata.conditionsFlopActions = conditionsPostflopActions;
               case TURN->stata.conditionsTurnActions = conditionsPostflopActions;
               case RIVER->stata.conditionsRiverActions = conditionsPostflopActions;
           }
        return this;}

        public Builder setSpecStats(int stata1){ stata.specStats[stata1] = true;
            stata.structureParametres[stata1+5] = true;
        return this;}



        public FilterStata build(){return stata;}
    }


    public static void main(String[] args) {
        FilterStata filterStats = new Builder().setPosStata(new int[]{1,1,1,1,1,1},new int[][]{{1,1,1,1,1,1},{1,1,1,1,1,1}}).build();


        System.out.println(filterStats.strPosStata);
    /*    int[] streetsActs = {0,0,0,1};
        System.out.println(IntStream.range(0, streetsActs.length).filter(i -> streetsActs[i] == 1).findFirst().getAsInt());*/
    }
}
