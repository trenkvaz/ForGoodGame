package org.trenkvaz.newstats;



import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

public class FilterStata implements Serializable {

    static final int UTG = 0, MP = 1, CO = 2, BU = 3, SB = 4, BB = 5;
    static final int ACT_PLAYER = 0,  LIMPS = 1, CALLERS = 2, LIMP = 3, RAISER = 4, _3BET = 5, _4BET = 6, _5BET = 7;
    static final int SELECT = 0, CALL = 1, RAISE = 2;
    static final String[] strPositions = {"utg","mp","co","bu","sb","bb"};

    /*public Map<String, DataStata> mapNicksDates = new HashMap<>();
    public transient DataStata[] dataStatsOneHand;*/

    public String mainNameFilter;
    // 0 позиции игрока, 1 позиции оппов;  0 нет позы, 1 есть поза
    int[][] posStata;
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
    // 1 раунд акт 0, если важна позиция действия и было действие 2, нет действие -1, было действие поза не важна 1
    public List<int[]> conditionsPreflopActions;
    public boolean[] structureParametres = new boolean[9];


    public String getFullNameStata(){return mainNameFilter+strPosStata;}


    public void countOnePlayerStata(boolean isInGame,int posPlayer,String nick, float stack, List<List<List<Float>>> sizeActionsStreetsStats,
                                    boolean isWin,boolean isShowDown,String[] cardsPlayer,int rangePlayer,int posHero,DataStata dataStata,List<int[][]> listPokerActionsInRoundsByPositions){
        if(isInGame)if(!isAllowInGame)return;
        if(posStata[0][posPlayer]==0)return;
        //DataStata dataStata = getNewDataStata(nick);
        if(streetOfActs==-1){ countSpecialNotActionStats(dataStata,sizeActionsStreetsStats,posPlayer,isWin,isShowDown,listPokerActionsInRoundsByPositions.get(0));}
        else if(streetOfActs==0){countPreflop(dataStata,sizeActionsStreetsStats.get(0),posPlayer,nick,stack,rangePlayer,posHero, listPokerActionsInRoundsByPositions.get(0));}
        else countPostFlop(dataStata,sizeActionsStreetsStats.get(streetOfActs),streetOfActs,posPlayer,nick,stack);
        //mapNicksDates.put(nick,dataStata);
    }


    public boolean countPreflop(DataStata dataStata,List<List<Float>> preflopSizeActions, int posPlayer, String nick, float stack, int rangePlayer,int posHero,int[][] pokerActsRoundsByPoses){

        if(pokerActsRoundsByPoses[0][posPlayer]==0)return false; // ситуация когда херо на ББ и все сфолдили
        for(int round =0; round<conditionsPreflopActions.size(); round++){ if(!isEqualsPreflopConditionsToActions(pokerActsRoundsByPoses,round,posPlayer))return false;}
        //return true;
        dataStata.isRecord =true;
        dataStata.mainSelCallRaise[SELECT]++;
        int pokerActPlayer = pokerActsRoundsByPoses[conditionsPreflopActions.size()-1][posPlayer];
        if(pokerActPlayer==-10||pokerActPlayer==10)return false;
        if(pokerActPlayer<0){dataStata.mainSelCallRaise[CALL]++; if(isRangeCall&&rangePlayer!=0){dataStata.rangeCall[rangePlayer]++; } }
        if(pokerActPlayer>0){dataStata.mainSelCallRaise[RAISE]++;
        // если нужно просчитать Рейндж Сайзы и есть карты
        if(raiseSizesForRange!=null&&rangePlayer!=0)countRaiseSizesForRange(dataStata,preflopSizeActions.get(posPlayer),rangePlayer,stack); }
        return true;
    }


    private void countPostFlop(DataStata dataStata,List<List<Float>> postflopActions,int street, int pokPos, String nick, float stack){

    }


    private void countAgainstHero(DataStata dataStata,int posHero,int[][] actsRoundsByPoses,int posPlayer){
        boolean isImpPos = false;
        for(int[] condRaund:conditionsPreflopActions) isImpPos = IntStream.range(1, condRaund.length).anyMatch(i -> condRaund[i] == 2);
        if(!isImpPos)return;
        if(posStata[1][posHero]==0)return;
        dataStata.isRecord = true;
        dataStata.selCallRaiseVsHero[SELECT]++;
        // последнее действие на основе последнего индекса условий действий, указывает на последний раунд где есть нужное действие
        int actPlayer = actsRoundsByPoses[conditionsPreflopActions.size()-1][posPlayer];
        if(actPlayer==-10||actPlayer==10)return;
        if(actPlayer<0)dataStata.selCallRaiseVsHero[CALL]++;
        if(actPlayer>0)dataStata.selCallRaiseVsHero[RAISE]++;
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
        }
    }



    private boolean isEqualsPreflopConditionsToActions(int[][] actsRoundsByPoses, int round, int posPlayer){
        int[] resultActions = new int[8];
        Arrays.fill(resultActions,-1);
        if(round==0) { for (int pokPos=0; pokPos<posPlayer; pokPos++) setResultActions(resultActions,pokPos,actsRoundsByPoses[round][pokPos]); }
        else if(round==1){
            // проверка необходимое действие первого раунда игрока совпадало с рейальным действием в первом раунде
            if(conditionsPreflopActions.get(0)[0]!=actsRoundsByPoses[0][posPlayer])return false;
            for(int pokPos = posPlayer+1; pokPos<6; pokPos++) setResultActions(resultActions,pokPos,actsRoundsByPoses[0][pokPos]);
            for(int pokPos = 0; pokPos<posPlayer; pokPos++) setResultActions(resultActions,pokPos,actsRoundsByPoses[1][pokPos]);
        }
        // не совпадают условия и действия
        if(conditionsPreflopActions.get(round)[LIMPS]!=resultActions[LIMPS])return false;
        if(conditionsPreflopActions.get(round)[CALLERS]!=resultActions[CALLERS])return false;
        for(int act=3; act<8; act++){
            if(conditionsPreflopActions.get(round)[act]==-1&&resultActions[act]==-1)continue; // не должно быть действия и нет действия
            if(conditionsPreflopActions.get(round)[act]!=-1&&resultActions[act]==-1)return false; // должно быть действие и нет действия
            if(conditionsPreflopActions.get(round)[act]==-1&&resultActions[act]!=-1)return false;// не должно быть действия и есть действие
            // поза действия важна проверка что в индексах поз оппов нет нуля, значит нет соответсвия необходимых позиций оппов и его позиций действия
            if(conditionsPreflopActions.get(round)[act]==2){ if(posStata[1][resultActions[act]]==0)return false;}
        }
        return true;
    }

    private void setResultActions(int[] resultActions,int pokPos, int act){
        switch (act) {
            case -1 -> {if(resultActions[LIMP]==-1) resultActions[LIMP] = pokPos; else resultActions[LIMPS]=1;}
            case -2 ->  resultActions[CALLERS] = 1;
            case 2 -> resultActions[RAISER] = pokPos;
            case 3 -> resultActions[_3BET] = pokPos;
            case 4 -> resultActions[_4BET] = pokPos;
            case 5 -> resultActions[_5BET] = pokPos;
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
        if(actPlayer<0)dataStata.VPIP_PFR[CALL]++;
        if(actPlayer>0)dataStata.VPIP_PFR[RAISE]++;
    }


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

        public Builder isVsHero(){stata.isVsHero = true; stata.structureParametres[2] = true; return this;}

        public Builder isAllowInGame(){stata.isAllowInGame = true; return this;}

        public Builder isRangeCall(){stata.isRangeCall = true; stata.structureParametres[3] = true;return this;}

        public Builder setStreetOfActs(int streetOfActs){ stata.streetOfActs = streetOfActs; stata.structureParametres[0] = true; return this; }

        public Builder setSeenStreet(int seenStreet1){ stata.seenStreet = seenStreet1; return this; }

        public Builder setVsBetSizes(int[] vsBetSizes1){ stata.vsBetSizes = vsBetSizes1; stata.structureParametres[1] = true; return this; }

        public Builder setRaiseSizesForRange(int[] raiseSizesForRange){ stata.raiseSizesForRange = raiseSizesForRange; stata.structureParametres[4] = true;return this; }

        public Builder setConditionsPreflopActions(List<int[]> conditionsPreflopActions){stata.conditionsPreflopActions = conditionsPreflopActions; return this;}

        public Builder setSpecStats(int stata1){ stata.specStats[stata1] = true;
            stata.structureParametres[stata1+5] = true;
        return this;}



        public FilterStata build(){return stata;}
    }


    public static void main(String[] args) {
        FilterStata filterStats = new Builder().setPosStata(new int[][]{{0,1,1,1,1,1},{1,1,1,1,1,1}}).build();


        System.out.println(filterStats.strPosStata);
        int[] streetsActs = {0,0,0,1};
        System.out.println(IntStream.range(0, streetsActs.length).filter(i -> streetsActs[i] == 1).findFirst().getAsInt());
    }
}
