package org.trenkvaz.newstats;



import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterStata implements Serializable {

    record DataStata(int[] selCallRaise, int[] selCallRaiseVsHero, int[] rangeCall, List<int[]> rangeRaiseSizes){}

    transient Map<String, DataStata> nicksDataStats = new HashMap<>();
    public transient DataStata[] dataStatsOneHand;

    public String mainNameFilter;
    int[][] posStata;
    public String strPosStata;
    static final int UTG = 0, MP = 1, CO = 2, BU = 3, SB = 4, BB = 5;
    static final String[] strPositions = {"utg","mp","co","bu","sb","bb"};
    public boolean isRanges = false;
    public boolean isVsHero = false;
    // -1 нет, 0 не важно, 1 да
    // Вин,Шоуд,Префлоп,Флоп,Терн,Ривер
    private int[] condActions;


    public String getFullNameStata(){return mainNameFilter+strPosStata;}


    public void countOnePlayerStata(boolean isInGame,int indNick,String nick, float stack, List<List<List<Float>>> actionsStreetsStats,boolean isWin,boolean isShowDown){

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

        public Builder isRange(){stata.isRanges = true; return this;}

        public Builder isVsHero(){stata.isVsHero = true; return this;}

        public Builder setCondActions(int[] condActions1){ stata.condActions = condActions1; return this; }

        public FilterStata build() { return stata; }
    }

    public static void main(String[] args) {
        FilterStata filterStats = new Builder().setPosStata(new int[][]{{0,1,1,1,1,1},{1,1,1,1,1,1}}).build();
        System.out.println(filterStats.strPosStata);
    }
}
