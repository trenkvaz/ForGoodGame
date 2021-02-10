package org.trenkvaz.newstats;



import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

public class FilterStata implements Serializable {

    static final int UTG = 0, MP = 1, CO = 2, BU = 3, SB = 4, BB = 5;
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
    public List<int[]> raundsConditionsActions;


    public String getFullNameStata(){return mainNameFilter+strPosStata;}


    public void countOnePlayerStata(boolean isInGame,int pokPos,String nick, float stack, List<List<List<Float>>> actionsStreetsStats,boolean isWin,boolean isShowDown){
        if(isInGame)if(!isAllowInGame)return;
        if(posStata[0][pokPos]==0)return;
        DataStata dataStata = getNewDataStata(nick);
        if(streetOfActs==-1){ countSpecialNotActionStats(dataStata,actionsStreetsStats,pokPos,isWin,isShowDown);}
        else if(streetOfActs==0){countPreflop(dataStata,actionsStreetsStats.get(0),pokPos,nick,stack);}
        else countPostFlop(dataStata,actionsStreetsStats.get(streetOfActs),streetOfActs,pokPos,nick,stack);
        mapNicksDates.put(nick,dataStata);
    }


    private void countPreflop(DataStata dataStata,List<List<Float>> preflopActions, int pokPos, String nick, float stack){ }


    private void countPostFlop(DataStata dataStata,List<List<Float>> postflopActions,int street, int pokPos, String nick, float stack){

    }

    private void countSpecialNotActionStats(DataStata dataStata,List<List<List<Float>>> actionsStreetsStats,int pokPos,boolean isWin,boolean isShowDown){
        if(specStats[0]) countW$WSF(dataStata,actionsStreetsStats,pokPos,isWin,isShowDown);
        if(specStats[1]) countWTSD(dataStata,isShowDown);
        if(specStats[2]) countW$SD(dataStata,isWin,isShowDown);
    }


    private void countW$WSF(DataStata dataStata, List<List<List<Float>>> actionsStreetsStats, int pokPos, boolean isWin, boolean isShowDown){
         // проверка что игрок видел флоп шоудаун это флоп или действие на флопе
         if(!isShowDown){ if(actionsStreetsStats.get(1).get(pokPos).isEmpty())return; }
         dataStata.W$WSF[0]++; // выборка плюс
         if(isWin)dataStata.W$WSF[1]++;
    }


    private void countWTSD(DataStata dataStata, boolean isShowDown){
        dataStata.WTSD[0]++;if(isShowDown)dataStata.WTSD[1]++;
    }


    private void countW$SD(DataStata dataStata,boolean isWin, boolean isShowDown){
        if(isShowDown){dataStata.W$SD[0]++;if(isWin)dataStata.W$SD[1]++; }
    }



    private DataStata getNewDataStata(String nick){
        DataStata dataStata = mapNicksDates.get(nick);
        if(dataStata==null) dataStata = new DataStata();
        return dataStata;
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

        public Builder isVsHero(){stata.isVsHero = true; return this;}

        public Builder isAllowInGame(){stata.isAllowInGame = true; return this;}

        public Builder setStreetOfActs(int streetOfActs){ stata.streetOfActs = streetOfActs; return this; }

        public Builder setSeenStreet(int seenStreet1){ stata.seenStreet = seenStreet1; return this; }

        public Builder setVsBetSizes(int[] vsBetSizes1){ stata.vsBetSizes = vsBetSizes1; return this; }

        public Builder setRaiseSizesForRange(int[] raiseSizesForRange){ stata.raiseSizesForRange = raiseSizesForRange; return this; }

        public Builder setRaundsConditionsActions(List<int[]> raundsConditionsActions){stata.raundsConditionsActions = raundsConditionsActions; return this;}



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
