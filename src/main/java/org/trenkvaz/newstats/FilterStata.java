package org.trenkvaz.newstats;



import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

public class FilterStata implements Serializable {

    static final int UTG = 0, MP = 1, CO = 2, BU = 3, SB = 4, BB = 5;
    static final int ACT_PLAYER = 0, POS_1LIMP = 1, NUMS_LIMPS = 2, POS_RAISER = 3, POS_1CALLER = 4, NUMS_CALLERS = 5, POS_3BETER = 6, POS_1CALLER_3BET = 7,
    NUMS_CALLERS_3BET = 8, POS_4BETER = 9;
    static final int SELECT = 0, CALL = 1, RAISE = 2;
    static final String[] strPositions = {"utg","mp","co","bu","sb","bb"};

    public Map<String, DataStata> mapNicksDates = new HashMap<>();
    public transient DataStata[] dataStatsOneHand;

    public String mainNameFilter;
    // 0 позиции игрока, 1 позиции оппов;  0 нет позы, 1 есть поза
    int[][] posStata;
    public String strPosStata;
    private boolean isAllowInGame = false;
    //public boolean isRanges = false;
    public boolean isVsHero = false;
    public int[] raiseSizesForRange;
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
    public List<int[]> conditionsPreflopActions;


    public String getFullNameStata(){return mainNameFilter+strPosStata;}


    public void countOnePlayerStata(boolean isInGame,int posPlayer,String nick, float stack, List<List<List<Float>>> actionsStreetsStats,boolean isWin,boolean isShowDown){
        if(isInGame)if(!isAllowInGame)return;
        if(posStata[0][posPlayer]==0)return;
        DataStata dataStata = getNewDataStata(nick);
        if(streetOfActs==-1){ countSpecialNotActionStats(dataStata,actionsStreetsStats,posPlayer,isWin,isShowDown);}
        else if(streetOfActs==0){countPreflop(dataStata,actionsStreetsStats.get(0),posPlayer,nick,stack);}
        else countPostFlop(dataStata,actionsStreetsStats.get(streetOfActs),streetOfActs,posPlayer,nick,stack);
        mapNicksDates.put(nick,dataStata);
    }


    private void countPreflop(DataStata dataStata,List<List<Float>> preflopActions, int posPlayer, String nick, float stack){

        int[][] actsRoundsByPoses = getActionsInRoundsByPositions(preflopActions,true);
        if(actsRoundsByPoses[0][posPlayer]==0)return; // ситуация когда херо на ББ и все сфолдили
        for(int round =0; round<conditionsPreflopActions.size(); round++){ if(!isEqualsPreflopConditionsToActions(actsRoundsByPoses,round,posPlayer))return;}

        dataStata.mainSelCallRaise[SELECT]++;
        int actPlayer = actsRoundsByPoses[conditionsPreflopActions.size()-1][posPlayer];
        if(actPlayer==-10||actPlayer==10)return;
        if(actPlayer<0)dataStata.mainSelCallRaise[CALL]++;
        if(actPlayer>0)dataStata.mainSelCallRaise[RAISE]++;

    }


    private void countPostFlop(DataStata dataStata,List<List<Float>> postflopActions,int street, int pokPos, String nick, float stack){

    }


    private boolean isEqualsPreflopConditionsToActions(int[][] actsRoundsByPoses, int round, int posPlayer){
        // если позиция действия не важна или количество игроков то в условиях пишется 6, отсутствие действия -1
        int[] resultActions = new int[10];
        Arrays.fill(resultActions,-1);
        for (int pokPos=0; pokPos<6; pokPos++){
            int act = actsRoundsByPoses[round][pokPos];
            if(posPlayer==pokPos){ resultActions[ACT_PLAYER] = act; continue;}
            switch (act) {
                case -1 -> { if(resultActions[POS_1LIMP]==-1){ resultActions[POS_1LIMP] = pokPos; resultActions[NUMS_LIMPS] =1; } else resultActions[NUMS_LIMPS]++;}
                case -2 -> { if(resultActions[POS_1CALLER]==-1){ resultActions[POS_1CALLER] = pokPos; resultActions[NUMS_CALLERS] =1; } else resultActions[NUMS_CALLERS]++;}
                case -3 -> { if(resultActions[POS_1CALLER_3BET]==-1){resultActions[POS_1CALLER_3BET] = pokPos;resultActions[NUMS_CALLERS_3BET] =1; }else resultActions[NUMS_CALLERS_3BET]++;}
                case 2 -> resultActions[POS_RAISER] = pokPos;
                case 3 -> resultActions[POS_3BETER] = pokPos;
                case 4 -> resultActions[POS_4BETER] = pokPos;
            }
        }
        // если раунд не последний сравниваются действия игрока и с его действия в условии
        if(round<conditionsPreflopActions.size()-1){ if(resultActions[ACT_PLAYER]!=conditionsPreflopActions.get(round)[ACT_PLAYER])return false;}

        for(int act=1; act<10; act++){
            if(conditionsPreflopActions.get(round)[act]==resultActions[act])continue;
            if(conditionsPreflopActions.get(round)[act]==-1||resultActions[act]==-1)return false;
            if(conditionsPreflopActions.get(round)[act]!=6)return false;
        }
        return true;
    }



    public int[][] getActionsInRoundsByPositions(List<List<Float>> actions,boolean isPreflop){
        int maxSizeListActions = actions.stream().mapToInt(List::size).max().getAsInt();
        int cor = 0; if(isPreflop)cor=1;
        int[][] roundsPosAct = new int[maxSizeListActions-cor][6];
        int raise = cor;
        for(int act=cor; act<maxSizeListActions; act++)
            for(int pokPos=0; pokPos<6; pokPos++){
                if(actions.get(pokPos).size()-1<act)continue;
                float action = actions.get(pokPos).get(act);
                if(action==Float.NEGATIVE_INFINITY)roundsPosAct[act-cor][pokPos]=-10;
                else if(action==Float.POSITIVE_INFINITY)roundsPosAct[act-cor][pokPos]= 10;
                else if(action!=Float.NEGATIVE_INFINITY&&action<0)roundsPosAct[act-cor][pokPos]= -(raise);
                else if(action!=Float.POSITIVE_INFINITY&&action>0){ if(raise==5)roundsPosAct[act-cor][pokPos] = raise;else roundsPosAct[act-cor][pokPos] = ++raise;}
            }
        return roundsPosAct;
    }






    private void countSpecialNotActionStats(DataStata dataStata,List<List<List<Float>>> actionsStreetsStats,int posPlayer,boolean isWin,boolean isShowDown){
        if(specStats[0]) countW$WSF(dataStata,actionsStreetsStats,posPlayer,isWin,isShowDown);
        if(specStats[1]) countWTSD(dataStata,isShowDown);
        if(specStats[2]) countW$SD(dataStata,isWin,isShowDown);
    }


    private void countW$WSF(DataStata dataStata, List<List<List<Float>>> actionsStreetsStats, int posPlayer, boolean isWin, boolean isShowDown){
         // проверка что игрок видел флоп шоудаун это флоп или действие на флопе
         if(!isShowDown){ if(actionsStreetsStats.get(1).get(posPlayer).isEmpty())return; } dataStata.W$WSF[0]++; if(isWin)dataStata.W$WSF[1]++;
    }


    private void countWTSD(DataStata dataStata, boolean isShowDown){ dataStata.WTSD[0]++;if(isShowDown)dataStata.WTSD[1]++; }


    private void countW$SD(DataStata dataStata,boolean isWin, boolean isShowDown){ if(isShowDown){dataStata.W$SD[0]++;if(isWin)dataStata.W$SD[1]++; } }


    private DataStata getNewDataStata(String nick){ DataStata dataStata = mapNicksDates.get(nick);if(dataStata==null) dataStata = new DataStata();return dataStata; }


    public static class Builder {
        private final FilterStata stata;

        public Builder() {
            stata = new FilterStata();
        }

        public Builder setPosStata(int[][] posStata1){
            stata.posStata = posStata1;
            long countPosHero = Arrays.stream(posStata1[0]).filter(c->c>0).count();
            long countPosOpps = Arrays.stream(posStata1[1]).filter(c->c>0).count();
            stata.strPosStata = "";
            if(countPosHero==6){stata.strPosStata+="all_v";}
            else {for(int i=0; i<6; i++){ if(posStata1[0][i]==0)continue;stata.strPosStata+=strPositions[i]+"_"; } stata.strPosStata+="v";}
            if(countPosOpps==6)stata.strPosStata+="_all";
            else for(int i=0; i<6; i++){ if(posStata1[1][i]==0)continue;stata.strPosStata+=strPositions[i]+"_"; }
            return this;
        }

        public Builder setMainNameFilter(String mainNameFilter1 ){ stata.mainNameFilter = mainNameFilter1; return this;}

        //public Builder isRange(){stata.isRanges = true; return this;}

        public Builder isVsHero(){stata.isVsHero = true; return this;}

        public Builder isAllowInGame(){stata.isAllowInGame = true; return this;}

        public Builder setStreetOfActs(int streetOfActs){ stata.streetOfActs = streetOfActs; return this; }

        public Builder setSeenStreet(int seenStreet1){ stata.seenStreet = seenStreet1; return this; }

        public Builder setVsBetSizes(int[] vsBetSizes1){ stata.vsBetSizes = vsBetSizes1; return this; }

        public Builder setRaiseSizesForRange(int[] raiseSizesForRange){ stata.raiseSizesForRange = raiseSizesForRange; return this; }

        public Builder setConditionsPreflopActions(List<int[]> conditionsPreflopActions){stata.conditionsPreflopActions = conditionsPreflopActions; return this;}



        public FilterStata build(){return stata;}
    }

    public class DataStata {
        public int[] mainSelCallRaise;// основная стата выборка, колл, рейз
        public int[][] vsBetSizeSelCallRaise; // статы против определенных рейзов для каждого сайза рейза оппа ответ - выборка, колл, рейз
        public int[] selCallRaiseVsHero; // основная стата против Херо ответ - выборка, колл, рейз
        public int[] rangeCall; // 170 1-169 показывает карты с которыми был колл прибавляется единица за каждую карту
        public List<int[]> rangeRaiseSizes; // показывает карты с которыми был рейз, рейзы разные на каждый рейз массив как в колле
        public int[]W$WSF;
        public int[]WTSD;
        public int[]W$SD;
        public int[]Ag;

        public DataStata(){
            if(streetOfActs!=-1)mainSelCallRaise = new int[3];
            if(vsBetSizes!=null)vsBetSizeSelCallRaise = new int[vsBetSizes.length][3];
            if(isVsHero)selCallRaiseVsHero = new int[3];
            if(raiseSizesForRange!=null){ rangeCall = new int[170]; rangeRaiseSizes = new ArrayList<>(raiseSizesForRange.length);
                for(int i=0; i<raiseSizesForRange.length; i++){ int[] rangeRaiseSize = new int[170]; rangeRaiseSizes.add(rangeRaiseSize); }    }
            if(specStats[0]) W$WSF = new int[2];
            if(specStats[1]) WTSD = new int[2];
            if(specStats[2]) W$SD = new int[2];
        }

    }

    public static void main(String[] args) {
        FilterStata filterStats = new Builder().setPosStata(new int[][]{{0,1,1,1,1,1},{1,1,1,1,1,1}}).build();


        System.out.println(filterStats.strPosStata);
        int[] streetsActs = {0,0,0,1};
        System.out.println(IntStream.range(0, streetsActs.length).filter(i -> streetsActs[i] == 1).findFirst().getAsInt());
    }
}
