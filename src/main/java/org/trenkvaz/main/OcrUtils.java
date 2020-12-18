package org.trenkvaz.main;

import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.trenkvaz.main.CaptureVideo.*;

public class OcrUtils {


    public static int get_int_CompareLongHashesToShablons(long[] hash_for_compare,long[][] shablons){
        int first_of_pair_error = 0, second_of_pair_error = 0, error = 10, size_shbalons = shablons.length, amount_nums = shablons[0].length;
        int choosed_shablon_text = -1;
        out: for(int ind_shablon=0; ind_shablon<size_shbalons; ind_shablon++) {
            first_of_pair_error = 0; second_of_pair_error = 0;
            for(int i=0; i<amount_nums; i++){
                if(i%2==0)first_of_pair_error = get_AmountOneBitInLong(shablons[ind_shablon][i]^hash_for_compare[i]);
                if(i%2!=0)second_of_pair_error = get_AmountOneBitInLong(shablons[ind_shablon][i]^hash_for_compare[i]);
                //System.out.println(ind_shablon+"  "+(first_of_pair_error+second_of_pair_error));
                if(i>0&&(first_of_pair_error+second_of_pair_error)>error){ continue out;  }
            }
            choosed_shablon_text = ind_shablon;
        }
        return choosed_shablon_text;
    }


    public static float get_OcrNum(List<int[]> list_hash_nums, int max_error, String type_shablon){
        if(list_hash_nums==null||list_hash_nums.isEmpty()||list_hash_nums.get(0)==null)return -1;
        int[][] shablons = shablons_numbers_0_9_for_stacks;
        if(type_shablon.equals("actions")) shablons = shablons_numbers_0_9_for_actions;
        int total_error = 0, number_with_min_error = -1, min_error = max_error; String res = "";
        int size = list_hash_nums.size(), size_of_num = list_hash_nums.get(0).length;
        // числа берутся справа налево
        for(int hash_num=size-1;  hash_num>-1; hash_num--){
            // точка
            if(list_hash_nums.get(hash_num)==null) { res+="."; continue;}
            number_with_min_error = -1;
            min_error = max_error;
            out: for(int number = 0; number<10; number++){
                total_error = 0;
                // boolean is_equal = true;
                for(int ind_num=0; ind_num<size_of_num; ind_num++){
                    total_error+= get_AmountOneBitInInt(shablons[number][ind_num]^list_hash_nums.get(hash_num)[ind_num]);

                    if(total_error>=max_error){ continue out;  }
                }
                // находится индекс в шаблоне числе с минимальным количеством ошибок
                if(total_error<min_error){
                    min_error = total_error;
                    number_with_min_error = number;
                }
            }
            if(number_with_min_error==-1)return -1;
            res+=number_with_min_error;
        }

        float result = -1;
        try{
            result = Float.parseFloat(res);
        } catch (Exception a){
            return -1;
        }
        return result;
    }


    public static boolean compare_CurrentListNumsAndNewListNums(List<int[]> current_list_nums,List<int[]> _new_list_nums, int limit_error){

        if(current_list_nums.size()!=_new_list_nums.size())return false;
        for(int ind_nums = 0; ind_nums<current_list_nums.size(); ind_nums++){
            // проверка на равенство наличия и положения точки в двух списках чисел
            if(current_list_nums.get(ind_nums)==null){ if(_new_list_nums.get(ind_nums)==null)continue; else return false; }
            int total_error_in_num = 0;
            for(int ind_of_num=0; ind_of_num<2; ind_of_num++){
                if(_new_list_nums.get(ind_nums)==null) {
                    // TEST
                    //save_image(frame[0].getSubimage(gxa,gya,gwa,gha),"test5\\_"+(c++));
                    return false;}
                total_error_in_num += get_AmountOneBitInInt(current_list_nums.get(ind_nums)[ind_of_num]^_new_list_nums.get(ind_nums)[ind_of_num]);
                if(total_error_in_num>limit_error)return false;
            }
        }
        return true;
    }


    public static List<int[]> get_list_intarr_HashNumberImg(BufferedImage image_table, int X, int Y, int W, int H, int limit_grey,
                                                     int indents_left_right, int size_dot_in_pix, int size_symbol, int size_intarr_hashimage){
        // создается списко с координатами начала Х линии символа и конца Х линии, точки обозначаются НУЛЛ
        List<int[]> coords_line_x_for_one_num = new ArrayList<>();
        int[] start_end_num = null;
        boolean is_x_black = false; int count_black_x_line = 0, count_size_num = 0;
        for (int x = X+W-indents_left_right-1; x > X+indents_left_right+1; x--) {
            // определяется есть ли черный пиксель в текущей линии если есть то счетчик увеличивается
            is_x_black = false;
            for (int y = Y; y < Y+H; y++) { if(get_intGreyColor(image_table,x,y)>limit_grey){ is_x_black = true; break; } }
            if(is_x_black) { count_black_x_line++; }
            // если линия белая, то проверяется сколько черных линий было до этого, если 3, а это точка, то все обнуляется в лист заносится нулл
            else {
                if(count_black_x_line==size_dot_in_pix){
                    coords_line_x_for_one_num.add(null);count_black_x_line = 0;
                    continue;
                }
                // если счетчик черных линий равен нулю, при определении белой линиий значит, что символы не начинались и идет пробел это возврат цикла
                // если счетчик черных линий больше нуля, значит начался символ, но некоторые символы короче заявленных поэтому белая линия игнорируется и цикл идет вниз,
                // к примеру еденица меньше стандартных для символа количества черных линий
                // сделано чтобы белая линия не сбивала подсчет линий числа
                if(count_black_x_line==0) continue;
            }
            // проверяется условие есть ли начало числа
            if(count_black_x_line==1){
                start_end_num = new int[2]; start_end_num[0] = x; count_size_num = 1;   // начинается счетчик линий числа
                continue;
            }
            count_size_num++;
            // есть счетчик линий дошел до размеров символов то обнуляются все счетчики и завершается получение кординат числа
            if(count_size_num==size_symbol){
                assert start_end_num != null;start_end_num[1] = x;coords_line_x_for_one_num.add(start_end_num);count_size_num = 0;count_black_x_line = 0;
            }
        }
        List<int[]> result = new ArrayList<>();
        boolean is_first_dot = false;
        int amountDigitPreDot = 0;
        for(int[] num:coords_line_x_for_one_num){
            // для записи точки, отмечается только первая точка, идет подсчет цифр до точки если их меньше 3, то точка считается если больше то точка не учитывается
            // это чтобы исключить попадание точек длинных чисел больше 1000
            if(num==null) { if(!is_first_dot&&amountDigitPreDot<3){result.add(null); is_first_dot=true;}
                continue;} else amountDigitPreDot++;
            int start = num[1], end = num[0];
            int _32_pixels =0;
            int[] intarr_hashimage = new int[size_intarr_hashimage]; int index_intarr_hashimage = -1, count_32_pix = 0,
                    amount_pix = (end-start+1)*H, count_all_pix = 0;
            //System.out.println(start+" end "+end);
            out: for (int x = start; x < end+1; x++){
                for (int y = Y; y < Y+H; y++) {
                    count_all_pix++;
                    _32_pixels<<=1;
                    count_32_pix++;
                    if(get_intGreyColor(image_table,x,y)>limit_grey)_32_pixels+=1;
                    // если последнее число имеет больше битов, чем нужно для оставшихся в цикле пикселей, то проверяется условие на общее количество пройденных пикселей,
                    // если оно равно общему количество пикселей в изображении то число с битами обрабатывается досрочно
                    if(count_32_pix==32||count_all_pix==amount_pix){
                        // сдвиг влево на недостающее количество раз если битов в числе больше чем оставшихся пикселей
                        // если так не сделать слева числа будут нули, и потом в получении изображения из битов нужно будет в послденем числе изменять смещение для битов
                        if(count_32_pix<32)_32_pixels<<=(32-count_32_pix);
                        index_intarr_hashimage++;
                        intarr_hashimage[index_intarr_hashimage] = _32_pixels;
                        _32_pixels = 0;
                        count_32_pix = 0;
                    }
                    // на случай если изображение больше чем битов в числе
                    if(index_intarr_hashimage==size_intarr_hashimage-1)break out;
                }

            }
            result.add(intarr_hashimage);
        }
        if(result.isEmpty()||result.get(0)==null) return null;
        return result;
    }

    public static long[] get_longarr_HashImage(BufferedImage image,int X, int Y, int W, int H, int amount_64nums, int limit_grey){
        //System.out.println("new x "+X+" y "+Y+" w "+W+" H "+H);
        long _64_pixels =0, count_black_pix = 0, amount_pix = W*H;

        long[] longarr_hashimage = new long[amount_64nums+1]; int index_longarr_hashimage = -1, count_64_pix = 0;
        int count_all_pix = 0;
        out:for (int x = X; x < X+W; x++){
            for (int y = Y; y < Y+H; y++) {
                count_all_pix++;
                count_64_pix++;
                _64_pixels<<=1;
                if(get_intGreyColor(image,x,y)>limit_grey){ _64_pixels+=1; count_black_pix++; }
                if(count_64_pix==64||count_all_pix==amount_pix){
                    //System.out.println(count_64_pix+" "+(amount_pix%count_64_pix));
                    if(count_64_pix<64)_64_pixels<<=(64-count_64_pix);
                    index_longarr_hashimage++;
                    longarr_hashimage[index_longarr_hashimage] = _64_pixels;
                    count_64_pix = 0;
                    _64_pixels = 0;
                }
                if(index_longarr_hashimage==amount_64nums-1)break out;
            }
        }
        longarr_hashimage[amount_64nums] = count_black_pix;
        return longarr_hashimage;
    }


    public static boolean compare_LongHashes(long[] current_list_nums,long[] _new_list_nums, int limit_error){
        // нужно знать длину массива, так как сюда также приходит сравнение номера руки и в хеше ника в котором сравнивается последнее число
        int length_array = current_list_nums.length;
        if(length_array==16){if(Math.abs(current_list_nums[15]-_new_list_nums[15])>15)return false;}
        else if(Math.abs(current_list_nums[3]-_new_list_nums[3])>6)return false;
        length_array--;
        int first_of_pair_error = 0, second_of_pair_error = 0;
        for(int ind_nums = 0; ind_nums<length_array; ind_nums++){
            if(ind_nums%2==0)first_of_pair_error = get_AmountOneBitInLong(current_list_nums[ind_nums]^_new_list_nums[ind_nums]);
            if(ind_nums%2!=0)second_of_pair_error = get_AmountOneBitInLong(current_list_nums[ind_nums]^_new_list_nums[ind_nums]);
            //System.out.println((first_of_pair_error+second_of_pair_error));
            if(ind_nums>0&&(first_of_pair_error+second_of_pair_error)>limit_error){ return false;  }
        }
        //System.out.println("true "+error);
        return true;
    }


    public static int get_AmountOneBitInInt(int lng){ return count_one_in_numbers[(short)(lng>>16)+32768]+count_one_in_numbers[(short)(lng)+32768]; }


    public static int get_AmountOneBitInLong(long lng){ return (count_one_in_numbers[(short)(lng>>48)+32768]+count_one_in_numbers[(short)(lng>>32)+32768]
                +count_one_in_numbers[(short)(lng>>16)+32768]+count_one_in_numbers[(short)(lng)+32768]);
    }


    public static int get_int_MaxBrightnessMiddleImg(BufferedImage image,int X,int Y,int W,int H){
        int max = 0, y = Y+H/2;
        for(int x=X; x<X+W; x++){
            int grey = get_intGreyColor(image,x,y);
            if(grey>max)max=grey;
        }
        return max;
    }


    public static BufferedImage get_scale_image(BufferedImage img, int size){ return Scalr.resize(img, Scalr.Method.ULTRA_QUALITY,
            img.getWidth()*size,img.getHeight()*size); }

    public static BufferedImage set_grey_and_inverse_or_no(BufferedImage  source, boolean isnverse){
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        //System.out.println("tip "+source.getType());
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                int grey = get_intGreyColor(source,x,y);
                if(isnverse) grey = 255-grey;
                result.setRGB(x, y, new Color(grey, grey, grey).getRGB());
            }
        }
        return result;
    }


    public static int get_intGreyColor(BufferedImage img,int x, int y){
        int val = img.getRGB(x, y);
        return  (int) (((val >> 16) & 0xff) * 0.299 + ((val >> 8) & 0xff) * 0.587 + (val & 0xff) * 0.114);
    }


    public static boolean is_noCursorInterferenceImage(BufferedImage image,int X, int Y, int W, int H, int limit_grey){
        for(int x=X; x<W+X; x++) for(int y=Y; y<H+Y; y+=H-1) if(get_intGreyColor(image,x,y)>limit_grey)return false;
        for(int y=Y; y<H+Y; y++) for(int x=X; x<W+X; x+=W-1) if(get_intGreyColor(image,x,y)>limit_grey)return false;
        return true;
    }


    public static boolean is_GoodImageForOcrStack(BufferedImage image,int X, int Y, int W, int H, int limit_grey){
        int count_permit_error =0;
        for(int x=X; x<W+X; x++) for(int y=Y; y<H+Y; y+=H-1) {
            if(get_intGreyColor(image,x,y)>limit_grey)count_permit_error++;
            if(count_permit_error>2)return false;
        }
        for(int y=Y; y<H+Y; y++) for(int x=X; x<W+X; x+=W-1) if(get_intGreyColor(image,x,y)>limit_grey)return false;
        return true;
    }


    public static BufferedImage get_white_black_image(BufferedImage  source,int limit_grey){
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < source.getWidth(); x++)
            for (int y = 0; y < source.getHeight(); y++) {
                int grey = get_intGreyColor(source,x,y);
                if(grey<limit_grey){ grey = 0;}
                else grey = 255;
                result.setRGB(x, y, new Color(grey, grey, grey).getRGB());
            }
        //System.out.println("error "+error);
        return result;
    }


    public static String get_suit_of_card(BufferedImage image_card,int X,int Y){
        Color color = new Color(image_card.getRGB(X, Y));
        int blue = color.getBlue();
        int red = color.getRed();
        int green = color.getGreen();
        if(blue>100&&blue>red&&blue>green)return "d";
        if(red>100&&red>blue&&red>green)return "h";
        if(green>100&&green>blue&&green>red)return "c";
        return "s";
    }


}
