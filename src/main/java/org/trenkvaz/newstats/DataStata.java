package org.trenkvaz.newstats;

import java.util.ArrayList;
import java.util.List;

public class DataStata {

    public int[] mainSelCallRaise;// основная стата выборка, колл, рейз
    public int[][] vsBetSizeSelCallRaise; // статы против определенных рейзов для каждого сайза рейза оппа ответ - выборка, колл, рейз
    public int[] selCallRaiseVsHero; // основная стата против Херо ответ - выборка, колл, рейз
    public int[] rangeCall; // 170 1-169 показывает карты с которыми был колл прибавляется единица за каждую карту
    public List<int[]> rangeRaiseSizes; // показывает карты с которыми был рейз, рейзы разные на каждый рейз массив как в колле
    public int[]W$WSF;
    public int[]WTSD;
    public int[]W$SD;
    public int[]VPIP_PFR;

    public DataStata(FilterStata filterStata){
        if(filterStata.streetOfActs!=-1)mainSelCallRaise = new int[3];
        if(filterStata.vsBetSizes!=null)vsBetSizeSelCallRaise = new int[filterStata.vsBetSizes.length][3];
        if(filterStata.isVsHero)selCallRaiseVsHero = new int[3];
        if(filterStata.isRangeCall)rangeCall = new int[170];
        if(filterStata.raiseSizesForRange!=null){  rangeRaiseSizes = new ArrayList<>(filterStata.raiseSizesForRange.length);
            for(int i=0; i<filterStata.raiseSizesForRange.length; i++){ int[] rangeRaiseSize = new int[170]; rangeRaiseSizes.add(rangeRaiseSize); }    }
        if(filterStata.specStats[0]) W$WSF = new int[2];
        if(filterStata.specStats[1]) WTSD = new int[2];
        if(filterStata.specStats[2]) W$SD = new int[2];
        if(filterStata.specStats[3]) VPIP_PFR = new int[3];
    }


}
