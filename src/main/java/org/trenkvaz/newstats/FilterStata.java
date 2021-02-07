package org.trenkvaz.newstats;



import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class FilterStata implements Serializable {

    static final int UTG = 0, MP = 1, CO = 2, BU = 3, SB = 4, BB = 5;
    static final String[] strPositions = {"utg","mp","co","bu","sb","bb"};

    //record DataStata(int[] mainSelCallRaise, int[][] vsBetSizeSelCallRaise, int[] selCallRaiseVsHero, int[] rangeCall, List<int[]> rangeRaiseSizes){}

    //transient Map<String, DataStata> nicksDataStats = new HashMap<>();
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
    // -1 нет, 0 не важно, 1 да - Вин,Шоуд;   0 не, 1 да - Действия на Префлоп,Флоп,Терн,Ривер;  0 нет, 1 да - Видел Флоп, Терн, Ривер
    private int[] winShow;
    private int streetOfActs = -1; // Префлоп,Флоп,Терн,Ривер - 0,1,2,3
    private int seenStreet = 0;
    // префлоп рейзы в ББ, на постфлопе проценты от банка
    // пример префлоп 2 3 5 значит до 2, от 2 до 3, от 3 до 5
    // пример постфлоп 30 50 100 это проценнты от банка до 30, от 30 до 50 и т.д
    public int[] vsBetSizes;


    public String getFullNameStata(){return mainNameFilter+strPosStata;}


    public void countOnePlayerStata(boolean isInGame,int pokPos,String nick, float stack, List<List<List<Float>>> actionsStreetsStats,boolean isWin,boolean isShowDown){
        if(isInGame)if(!isAllowInGame)return;
        if(posStata[0][pokPos]==0)return;
        if(streetOfActs==-1){ countSpecialNotActionStats(pokPos,nick,isWin,isShowDown); return;}
        if(streetOfActs==0){countPreflop(actionsStreetsStats.get(0),pokPos,nick,stack); return;}
        countPostFlop(actionsStreetsStats.get(streetOfActs),streetOfActs,pokPos,nick,stack);
    }


    private void countPreflop(List<List<Float>> preflopActions, int pokPos, String nick, float stack){ }


    private void countPostFlop(List<List<Float>> postflopActions,int street, int pokPos, String nick, float stack){

    }

    private void countSpecialNotActionStats(int pokPos, String nick,boolean isWin,boolean isShowDown){



    }


    private DataStata getNewDataStata(String nick){
        DataStata dataStata = mapNicksDates.get(nick);
        if(dataStata==null){
        dataStata = new DataStata();
        if(vsBetSizes!=null)  dataStata.vsBetSizeSelCallRaise = new int[vsBetSizes.length][3];
        if(isVsHero) dataStata.selCallRaiseVsHero = new int[3];
        }
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

        public FilterStata build() { return stata; }
    }

    public class DataStata {
        public int[] mainSelCallRaise = new int[3];
        public int[][] vsBetSizeSelCallRaise; // для каждого сайза рейза оппа ответ - выборка, колл, рейз
        public int[] selCallRaiseVsHero; // ответ - выборка, колл, рейз
        public int[] rangeCall; // 170 1-169 рендж прибавляется единица за каждую карту
        public List<int[]> rangeRaiseSizes; // каждый массив это 170 0-

    }

    public static void main(String[] args) {
        FilterStata filterStats = new Builder().setPosStata(new int[][]{{0,1,1,1,1,1},{1,1,1,1,1,1}}).build();


        System.out.println(filterStats.strPosStata);
        int[] streetsActs = {0,0,0,1};
        System.out.println(IntStream.range(0, streetsActs.length).filter(i -> streetsActs[i] == 1).findFirst().getAsInt());
    }
}
