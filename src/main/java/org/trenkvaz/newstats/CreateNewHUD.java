package org.trenkvaz.newstats;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.trenkvaz.database_hands.Work_DataBase.strStatsValues;
import static org.trenkvaz.ui.StartAppLauncher.home_folder;
import static org.trenkvaz.ui.StartAppLauncher.workStats;

public class CreateNewHUD {

    static DecimalFormat notZeroFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);
    private static List<List<List<DisplayStata>>> displayStataList;
    static final int SIZE_FONT_STATA = 14;
    static final int SIZE_FONT_SELECT = 10;
    static final int[] COORDS_LINES = {12,25,38,51,64};
    public static final int[] COORDS_STATS = {1,20,39,58,77,96,115};
    private static final DisplayStata[][][][] tablesPlayerMatrixDisplayStata = new DisplayStata[6][6][5][7];
    private static final Text[][][][][] tablesPlayerMatrixText = new Text[6][6][5][7][2];
    static final int SPEC_VALUE = 1, VPIP = 1, PFR =2, CALL =1, RAISE = 2, FOLD =3;

    public CreateNewHUD(){
        readDisplayStataList();
    }

    public CreateNewHUD(int a){}

    public void initNewTableHUD(int table){
        for(int player=0; player<6; player++ )
            for(int line=0; line<5; line++)
                for(int stata=0; stata<7; stata++){
                    tablesPlayerMatrixDisplayStata[table][player][line][stata]=null;
                    tablesPlayerMatrixText[table][player][line][stata][0]=null;
                    tablesPlayerMatrixText[table][player][line][stata][1]=null;
                }
    }



    public Text[][][][] createHUDoneTable(String[] nicks,int table, String[] typesPots, int[] pokerPosIndWithNumOnTable,int posHero){
          // типыПоты игроки должны быть по своим местам за столом
        //List<List<Text>> resultList = new ArrayList<>();

        for(int player = 0; player<6; player++){if(nicks[player]==null)continue;
            for(int line=0; line<5; line++) for(int stata=0; stata<7; stata++){
            if(tablesPlayerMatrixDisplayStata[table][player][line][stata]!=null){
                // пока так стата может вообще не завистеть от типа пота тогда отображается всегда на любой улице это -1
                // если же она зависит от пота, то должна соотвествовать приходящему типу пота, 0 это тип пота префлоп
                // если игрок выбыл до отображается его последний тип пота ПОКА НЕ РЕШИЛ С ПРИВЯЗКОЙ ЕЩЕ К УЛИЦЕ
                if(tablesPlayerMatrixDisplayStata[table][player][line][stata].typePot.equals("ALL"))continue;
                if(tablesPlayerMatrixDisplayStata[table][player][line][stata].typePot.equals(typesPots[player]))continue;
                tablesPlayerMatrixDisplayStata[table][player][line][stata] = null;
                tablesPlayerMatrixText[table][player][line][stata][0]=null;
                tablesPlayerMatrixText[table][player][line][stata][1]=null;
            }

            for(DisplayStata displayStata:displayStataList.get(line).get(stata)){
               if(!displayStata.typePot.equals("ALL")&&(!displayStata.typePot.equals(typesPots[player])))continue;

               if(displayStata.posHero[posHero]==0)continue;
               if(displayStata.posPlayer[ArrayUtils.indexOf(pokerPosIndWithNumOnTable,player+1)]==0)continue;

                //if(nicks[player].equals(NICK_HERO)) System.out.println("dot 1");
               // добавление текста с расчетом возможного смещения по линиям если линия занята
               for(int add=line; add<5; add++) if(tablesPlayerMatrixDisplayStata[table][player][add][stata]==null){
                   tablesPlayerMatrixText[table][player][add][stata][0] = new Text(COORDS_STATS[stata],COORDS_LINES[line],"");
                   tablesPlayerMatrixText[table][player][add][stata][0].setFont(new Font(SIZE_FONT_STATA));
                   addStataToText(tablesPlayerMatrixText[table][player][add][stata],displayStata,nicks[player]);
                   tablesPlayerMatrixDisplayStata[table][player][add][stata] = displayStata;
                   break;
               }
            }
        }
        }
       /* for(int i=0; i<6; i++){resultList.add(new ArrayList<>());
            if(nicks[i]==null)continue;
           for(int l=0; l<5; l++)
               for(int s=0; s<7; s++){
                   if(tablesPlayerMatrixText[table][i][l][s][0]==null)continue;
                   resultList.get(i).add(tablesPlayerMatrixText[table][i][l][s][0]);
                   if(tablesPlayerMatrixText[table][i][l][s][1]==null)continue;
                   resultList.get(i).add(tablesPlayerMatrixText[table][i][l][s][1]);
               }
           //if(nicks[i].equals(NICK_HERO)) System.out.println("text "+resultList.get(i).get(0).getText()+" "+resultList.get(i).get(0).getFill().toString());
        }*/
        return tablesPlayerMatrixText[table];
    }

    private void addStataToText(Text[] text,DisplayStata displayStata,String nick){
        if(displayStata.descriptPot !=null){text[0].setText(displayStata.descriptPot); text[0].setFill(Color.WHITE); return;}

        int[] stata = workStats.getValueOneStata(nick,displayStata.mainStata,displayStata.numStata);

        if(stata==null||stata[0]==0){text[0].setText("--"); text[0].setFill(Color.WHITE); return;}

        int select = stata[0];
        int[] rangeStata = null; int val = -1;
        if(displayStata.value==3)val = select-stata[1]+stata[2]; else  val = stata[displayStata.value];
        float result = 0;
        if(val>0){
        if(displayStata.nameOfRange!=null) {rangeStata = workStats.getValueOneStata(nick,displayStata.nameOfRange,displayStata.numStataRange);
        result = BigDecimal.valueOf(getProcents(rangeStata[1],rangeStata[0])/100*getProcents(val,select)).setScale(1, RoundingMode.HALF_UP).floatValue();
        } else result = BigDecimal.valueOf(getProcents(val,select)).setScale(1, RoundingMode.HALF_UP).floatValue();
        }
        if(result>=10)text[0].setText((result>=99)? "99":Integer.toString(Math.round(result)));
        else  text[0].setText((result==0)? "0":notZeroFormat.format(result));
        if(result==0)text[0].setFill(Color.WHITE);else text[0].setFill(displayStata.get_ColorByRangeOfStata(result));

        if(select<displayStata.condSelect){
            // если выборка меньше порога то справа от статы отображается маленькое число выборка для этого нужна длина текста статы
            int text_length = text[0].textProperty().length().get();
            text[1] = new Text(text[0].getX()+text_length*7, text[0].getY()+3,"" );
            text[1].setFont(new Font(SIZE_FONT_SELECT));
            text[1].setFill(Color.GRAY);
            text[1].setText(Integer.toString(select));
        }
    }

    private static float getProcents (int stata, int select){
        if(select==0)return 0;
        return ((float)stata/(float)select)*100;
    }

    public void creatNewDisplayStata(String mainStata,String nameOfRange,int condSelect,int value,String nameValueDataStata,
                                     int line,int vertical,int[] posHero,int[] posPlayer,String typePot,int[] rangesForColor,Color[] colorsForRange,int numStataRange,
                                             int valueRange){
        DisplayStata displayStata = new DisplayStata();
        displayStata.mainStata = mainStata;displayStata.nameOfRange = nameOfRange;displayStata.condSelect = condSelect;
        displayStata.numStata = Arrays.asList(strStatsValues).indexOf(nameValueDataStata); displayStata.value = value;  //displayStata.streetShow = streetShow;//displayStata.line = line;displayStata.vertical = vertical;

        displayStata.posHero = posHero;displayStata.posPlayer = posPlayer;displayStata.rangesForColor = rangesForColor;
        displayStata.typePot = typePot; displayStata.numStataRange = numStataRange; displayStata.valueRange = valueRange;
        displayStata.rgbForPaints = new double[rangesForColor.length-1][3];
        for(int i=0; i<displayStata.rgbForPaints.length; i++){
            displayStata.rgbForPaints[i][0] = colorsForRange[i].getRed();
            displayStata.rgbForPaints[i][1] = colorsForRange[i].getGreen();
            displayStata.rgbForPaints[i][2] = colorsForRange[i].getBlue(); }
        saveDisplayStata(displayStata,line,vertical);
    }


    public void creatNewDisplayStataDescription(String descriptPot,int line, int vertical){
        DisplayStata displayStata = new DisplayStata();
        displayStata.descriptPot = descriptPot;
        saveDisplayStata(displayStata,line,vertical);
    }

    public void readDisplayStataList(){
        try {	FileInputStream file=new FileInputStream(home_folder+"\\all_settings\\capture_video\\displayStataList.file");
            ObjectInput out = new ObjectInputStream(file);
            displayStataList = (List<List<List<DisplayStata>>>) out.readObject();
            out.close();
            file.close();
        } catch(IOException e) {
            System.out.println(e);
            initListDisplayStata();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    // СДЕЛАТЬ ВОЗМОЖНОСТЬ ПЕРЕЗАПИСИ СТАТЫ
    public void saveDisplayStata(DisplayStata displayStata,int line, int stata){
        if(displayStataList==null)initListDisplayStata();
        displayStataList.get(line).get(stata).add(displayStata);
        try {
            FileOutputStream file=new FileOutputStream(home_folder+"\\all_settings\\capture_video\\displayStataList.file");
            ObjectOutput out = new ObjectOutputStream(file);
            out.writeObject(displayStataList);
            out.close();
            file.close();
        } catch(IOException e) {
            System.out.println(e);
        }
    }

   private void initListDisplayStata(){
       displayStataList = new ArrayList<>();
       for(int line=0; line<5; line++){ displayStataList.add(new ArrayList<>());
           for(int stata=0; stata<7; stata++){
               displayStataList.get(line).add(new ArrayList<>());
           }
       }
   }



    public static void main(String[] args) {
        CreateNewHUD createNewHUD = new CreateNewHUD(0);
        createNewHUD.creatNewDisplayStata("main_wwsf_all_v_all",null,10,SPEC_VALUE,"_wwsf",
                0,4,new int[]{1,1,1,1,1,1},new int[]{1,1,1,1,1,1},"ALL",new int[]{0,48,101},new Color[]{Color.GREEN,Color.PURPLE},0,0);
        createNewHUD.creatNewDisplayStata("main_wtsd_all_v_all",null,10,SPEC_VALUE,"_wtsd",
                1,5,new int[]{1,1,1,1,1,1},new int[]{1,1,1,1,1,1},"ALL",new int[]{0,24,101},new Color[]{Color.GREEN,Color.PURPLE},0,0);
        createNewHUD.creatNewDisplayStata("main_wsd_all_v_all",null,10,SPEC_VALUE,"_wsd",
                1,6,new int[]{1,1,1,1,1,1},new int[]{1,1,1,1,1,1},"ALL",new int[]{0,42,101},new Color[]{Color.GREEN,Color.PURPLE},0,0);

    }
}

class DisplayStata implements Serializable{

    String mainStata; // полное имя статы
    String nameOfRange; // если здесь не нулл то значит нужен рейндж статы здесь указывается полное имя той статы от которой будет считаться рейндж
    String descriptPot;// описание статы это короткое слово перед статой
    int condSelect; // условие показа статы если меньше порога то показ выборки выше без выборки
    int numStata;    // номер статы в ДатаСтата
    int value; // значение статы в спец статах это 1 или 2 , в других 1-колл, 2-рейз, 3- фолд как разница между коллом и рейзом
    int numStataRange; // номер статы в ДатаСтата ренджа
    int valueRange; // значение статы в рендже 1 колл 2 рейз фолда не будет
    //int[] streetShow; // улица на которой показывается стата 4 элемента 0 - нет, 1 да
    /*int line = 0; // номер линии в хаде 1..5
    int vertical = 0; // номер элемента на линии*/
    String typePot; // PRE префлоп, ALL всегда, постфлоп свои имена
    int[] posHero; // условие отображения в зависимости от позы Херо
    int[] posPlayer; // условие отображения в зависимости от позы Игрока на которого стата
    int[] rangesForColor; // диапазоны процентов от которых зависит цвет отображения статы
    double[][] rgbForPaints;
    Paint[] paintsForRange;

    public Paint get_ColorByRangeOfStata(float stata){
        int range= -1;
        for (int i : rangesForColor) { if (i < stata) { range++;continue; }break; }
        return getPaint(range);
    }

    private Paint getPaint(int range){
        if(paintsForRange==null){
            paintsForRange = new Color[rgbForPaints.length];
            for(int i = 0; i< rgbForPaints.length; i++){
                paintsForRange[i] = new Color(rgbForPaints[i][0], rgbForPaints[i][1], rgbForPaints[i][2],1);
            }
        }
        return paintsForRange[range];
    }
}