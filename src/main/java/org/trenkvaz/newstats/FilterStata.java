package org.trenkvaz.newstats;



import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterStata implements Serializable {

    static final int UTG = 0, MP = 1, CO = 2, BU = 3, SB = 4, BB = 5;
    static final String[] strPositions = {"utg","mp","co","bu","sb","bb"};

    record DataStata(int[] mainSelCallRaise, int[][] vsBetSizeSelCallRaise, int[] selCallRaiseVsHero, int[] rangeCall, List<int[]> rangeRaiseSizes){}

    transient Map<String, DataStata> nicksDataStats = new HashMap<>();
    public transient DataStata[] dataStatsOneHand;

    public String mainNameFilter;
    // 0 позиции игрока, 1 позиции оппов;  0 нет позы, 1 есть поза
    int[][] posStata;
    public String strPosStata;
    private boolean isAllowInGame = false;
    public boolean isRanges = false;
    public boolean isVsHero = false;
    // -1 нет, 0 не важно, 1 да - Вин,Шоуд;   0 не, 1 да - Действия на Префлоп,Флоп,Терн,Ривер;  0 нет, 1 да - Видел Флоп, Терн, Ривер
    private int[] winShow;
    private int[] condActions;
    private int[] seenStreet;
    // префлоп рейзы в ББ, на постфлопе проценты от банка
    // пример префлоп 2 3 5 значит до 2, от 2 до 3, от 3 до 5
    // пример постфлоп 30 50 100 это проценнты от банка до 30, от 30 до 50 и т.д
    public int[] vsBetSizes;


    public String getFullNameStata(){return mainNameFilter+strPosStata;}


    public void countOnePlayerStata(boolean isInGame,int pokPos,String nick, float stack, List<List<List<Float>>> actionsStreetsStats,boolean isWin,boolean isShowDown){
        if(isInGame)if(!isAllowInGame)return;
        if(posStata[0][pokPos]==0)return;

       for (int i=2; i<9; i++){
           if(i<=5){  if(condActions[2]==1){ countPreflop(actionsStreetsStats.get(0),pokPos,nick);  break; }
                      if(condActions[i]==1){ countPostFlop(actionsStreetsStats.get(i),i,pokPos,nick);              }
           }
       }


    }


    private void countPreflop(List<List<Float>> preflopActions, int pokPos, String nick){ }


    private void countPostFlop(List<List<Float>> postflopActions,int street, int pokPos, String nick){

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

        public Builder isAllowInGame(){stata.isAllowInGame = true; return this;}

        public Builder setCondActions(int[] condActions1){ stata.condActions = condActions1; return this; }

        public Builder setVsBetSizes(int[] vsBetSizes1){ stata.vsBetSizes = vsBetSizes1; return this; }

        public FilterStata build() { return stata; }
    }

    public static void main(String[] args) {
        FilterStata filterStats = new Builder().setPosStata(new int[][]{{0,1,1,1,1,1},{1,1,1,1,1,1}}).build();
        System.out.println(filterStats.strPosStata);
    }
}
