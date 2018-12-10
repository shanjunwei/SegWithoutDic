package main.edu.csu.shan.util;


import main.edu.csu.shan.pojo.Term;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.edu.csu.shan.config.Config.MAX_STOP_WORD_LEN;
import static main.edu.csu.shan.config.Constants.*;
import static main.edu.csu.shan.config.Config.MAX_WORD_LEN;

/**
 * ���ִ�����ع�����
 */
public class HanUtils {

    // ʶ������ķ���  ����Ӣ�ģ���㣬��ѧ�����
    public static boolean isChineseCharacter(String text) {   // ����ʶ������
        // ������֤����
        String regEx = "[\\u4e00-\\u9fa5]+";
        // ����������ʽ
        Pattern pattern = Pattern.compile(regEx);
        // ���Դ�Сд��д��
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();  // �ַ����Ƿ���������ʽ��ƥ��
    }

    // �жϴ���ӽ����ս������ �ִ��Ƿ���֮ǰ���غ�
    public static boolean hasNonCommonWithAllAddedResultSet(LinkedHashSet AddedResultSet, String key) {
        Iterator<String> iterator = AddedResultSet.iterator();
        while (iterator.hasNext()) {
            if (hasCommonStr(key, iterator.next(), false)) {    // ������߼�����--> Ӧ������֮ǰ���������κ��ַ��������غ�
                return false;
            }
        }
        return true;
    }

    public static boolean hasNonCommonWithAllAddedResultSet(List<Term> AddedResultSet, Term key) {
        Iterator<Term> iterator = AddedResultSet.iterator();
        while (iterator.hasNext()) {
            if (hasCommonStr(iterator.next(),key)) {    // ������߼�����--> Ӧ������֮ǰ���������κ��ַ��������غ�
                return false;
            }
        }
        return true;
    }

    // �ж������ַ����Ƿ��н���
    public static boolean hasCommonStr(String str1, String str2, boolean isFirstTimeScreen) {  // ���Ʋ������Ƿ��ǵ�һ��ɸѡ
        char[] chars = str1.toCharArray();
        // ��ͷ�Ĳ����н���
        if (!isFirstTimeScreen && str1.substring(0, 1).equals(str2.substring(0, 1))) {  // �ڶ���ɸѡ��ͷ�Ĳ����н���
            return false;
        }
        for (int i = 0; i < chars.length; i++) {
            if (str2.contains(chars[i] + "")) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasCommonStr(Term str1, Term str2) {  // ���Ʋ������Ƿ��ǵ�һ��ɸѡ
        int leftBound11 = str1.leftBound;
        int rightBound12 = str1.rightBound;
        int leftBound21 = str2.leftBound;
        int rightBound22 = str2.rightBound;
        if (leftBound21 >= leftBound11 && leftBound21 < rightBound12) {
            return true;
        }
        if (rightBound22 > leftBound11 && rightBound22 < rightBound12) {
            return true;
        }
        return false;
    }

    /**
     * �ж������ַ������ַ��Ƿ����
     */
    public static boolean hasCommonFirstCharacter(String str1, String str2) {
        if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
            return false;
        }
        return str1.substring(0, 1).equals(str2.substring(0, 1));
    }


    /**
     * ȡ���Ĵʻ�ת����ƴ��    �� ������ ->  zhangshanfeng
     */
    public static String firstPinyinCharStr(String chineseWord) {
        StringBuffer pinyinName = new StringBuffer();
        char[] nameChar = chineseWord.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char cha : nameChar) {
            try {
                String[] str_array = PinyinHelper.toHanyuPinyinStringArray(cha, defaultFormat);
                pinyinName.append(str_array[0]);
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                badHanyuPinyinOutputFormatCombination.printStackTrace();
            }
        }
        return pinyinName.toString();
    }


    // ���������ַ�  �Լ�����ͣ�ô�  �Կո����
    public static String[] replaceNonChineseCharacterAsBlank(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (HanUtils.isChineseCharacter(String.valueOf(chars[i]))) {
                stringBuilder.append(chars[i]);
            } else {
                stringBuilder.append(",");
            }
        }
        String temp = stringBuilder.toString().replaceAll("[,]+", ",");  // �Զ���Ƿִ��ַ����кϲ�����
        String[] seg_nonChinese_result = temp.split(",");
        return seg_nonChinese_result;
    }


    // ȥ��ͣ�ôʣ�ȥ��ͣ�ôʴӵ͵���
    private String[] segmentByStopWordsAes(String text) {
        if (text.length() == 1) {
            return " ".split(" ");    // ���ؽ��Ϊ {}
        }
        int temp_max_len = Math.min(text.length() + 1, MAX_STOP_WORD_LEN + 1);
        int p = 0;
        while (p < text.length()) {
            int q = 0;
            while (q < temp_max_len) {  // ����ȡ�ʵĳ���
                // ȡ�ʴ�  p --> p+q
                if (p + q > text.length()) {
                    break;
                }
                String strChar = text.substring(p, p + q);
                if (stopWordSet.contains(strChar)) {
                    text = text.replaceAll(strChar, ",");
                    p++;
                    continue;  // ͣ�ô��Թ�
                }
                q++;
            }
            p++;
        }

        String temp = text.replaceAll("[,]+", ",");  // �Զ���Ƿִ��ַ����кϲ�����
        String[] seg_stop_result = temp.split(",");
        return seg_stop_result;
    }

    /**
     * ȥ��ͣ�ô�,�����ִʴ���ͣ�ôʷָ�  �Ӵ�Сƥ��ͣ�ô�
     * ����Щ�޸�,����ͣ�ôʺ��зּ���
     */
    public static List<List<String>> segmentByStopWordsDes(String text) {
        List<List<String>> result = new ArrayList<>();
        List<String> stopWords = new ArrayList<>();
        List<String> segList = new ArrayList<>();
        if (text.length() == 1) {
            List<String> list = Arrays.asList((" ".split(" ")));
            stopWords.addAll(list);
            result.add(stopWords);
            result.add(segList);
            return result;    // ���ؽ��Ϊ {}
        }
        int temp_max_len = Math.min(text.length() + 1, MAX_STOP_WORD_LEN);
        int p = 0;
        while (p < text.length()) {
            int q = temp_max_len;
            while (q > 0) {  // ����ȡ�ʵĳ���
                // ȡ�ʴ�  p --> p+q
                if (p + q > text.length()) {
                    q--;    // ������ȥ����ȫ
                    continue;
                }
                String strChar = text.substring(p, p + q);
                if (stopWordSet.contains(strChar)) {
                    stopWords.add(strChar);
                    text = text.replaceAll(strChar, ",");
                    p++;
                    q = temp_max_len;
                    continue;  // ͣ�ô��Թ�
                }
                q--;
            }
            p++;
        }

        String temp = text.replaceAll("[,]+", " ");  // �Զ���Ƿִ��ַ����кϲ�����
        List<String> seg_result = Arrays.asList(temp.trim().split(" "));
        result.add(stopWords);
        result.add(seg_result);
        return result;
    }


    /**
     * ȥ��ͣ�ô�,�����ִʴ���ͣ�ôʷָ� �Ӹߵ���ȡ��
     */
    public static String[] segmentByStopWords(String text) {
        if (text.length() == 1) {
            return " ".split(" ");    // ���ؽ��Ϊ {}
        }
        int temp_max_len = Math.min(text.length() + 1, MAX_STOP_WORD_LEN);
        int p = 0;
        while (p < text.length()) {
            int q = temp_max_len;
            while (q > 0) {  // ����ȡ�ʵĳ���
                // ȡ�ʴ�  p --> p+q
                if (p + q > text.length()) {
                    q--;    // ������ȥ����ȫ
                    continue;
                }
                String strChar = text.substring(p, p + q);
                if (stopWordSet.contains(strChar)) {  //|| strChar.contains(",")
                    // System.out.println("==>" + strChar);
                    text = text.replaceAll(strChar, ",");
                    p++;
                    q = temp_max_len;
                    continue;  // ͣ�ô��Թ�
                }
                q--;
            }
            p++;
        }

        String temp = text.replaceAll("[,]+", ",");  // �Զ���Ƿִ��ַ����кϲ�����
        String[] seg_stop_result = temp.split(",");
        return seg_stop_result;
    }


    // �зִ�  FMM �㷨
    public static List<Term> segmentToTerm(String text, boolean countWordFrequency) {
        //  �ͽ�����������ͣ�ô��з�
        List<Term> termList = new ArrayList<>();
        if (text.length() == 1) {
            return null;
        }
        int temp_max_len = Math.min(text.length() + 1, MAX_WORD_LEN);
        int p = 0;
        while (p < text.length()) {
            int q = 1;
            while (q < temp_max_len) {  // ����ȡ�ʵĳ���
                if (q == 1) {
                    q++;
                    continue;  // ����Ϊ1�Թ�,�������ֲ����зִ�����
                }
                // ȡ�ʴ�  p --> p+q
                if (p + q > text.length()) {
                    break;
                }
                String strChar = text.substring(p, p + q);
                Term term = new Term(strChar, p, p + q);
                termList.add(term);
                // ͳ�ƴʴ��Ĵ�Ƶ
                if (countWordFrequency) {
                    if (wcMap.containsKey(strChar)) {
                        wcMap.put(strChar, wcMap.get(strChar) + 1);
                    } else {
                        wcMap.put(strChar, 1);
                    }
                }
                q++;
            }
            p++;
        }
        return termList;
    }

    // �зִ�  FMM �㷨
    public static LinkedHashSet<String> segment(String text, boolean countWordFrequency) {
        //System.out.println("ԭ��������"+text);
        //wordCountSingleWord(text);    // ����ͳ�Ƶ����ֵĴ�Ƶ
        //  �ͽ�����������ͣ�ô��з�
        LinkedHashSet<String> termList = new LinkedHashSet<>();
        if (text.length() == 1) {
            return null;
        }
        int temp_max_len = Math.min(text.length() + 1, MAX_WORD_LEN);
        int p = 0;
        while (p < text.length()) {
            int q = 1;
            while (q < temp_max_len) {  // ����ȡ�ʵĳ���
                if (q == 1) {
                    q++;
                    continue;  // ����Ϊ1�Թ�,�������ֲ����зִ�����
                }
                // ȡ�ʴ�  p --> p+q
                if (p + q > text.length()) {
                    break;
                }
                String strChar = text.substring(p, p + q);
                //  System.out.println("�ʺ�ԭ����λ��: "+strChar+"  "+p+"->"+q);
                // System.out.println();
                termList.add(strChar);
                // ͳ�ƴʴ��Ĵ�Ƶ
                if (countWordFrequency) {
                    if (wcMap.containsKey(strChar)) {
                        wcMap.put(strChar, wcMap.get(strChar) + 1);
                    } else {
                        wcMap.put(strChar, 1);
                    }
                }
                q++;
            }
            p++;
        }
        return termList;
    }


/*    public static void wordCountSingleWord(String text) {
        char[] chars = text.toCharArray();
        for (char singleWord : chars) {
            if (!singWordCountMap.containsKey(String.valueOf(singleWord))) {
                singWordCountMap.put(String.valueOf(singleWord), 1);
            } else {
                singWordCountMap.put(String.valueOf(singleWord), singWordCountMap.get(String.valueOf(singleWord)) + 1);
            }
        }
    }*/

    /**
     * ��ת�ַ���
     */
    public static String reverseString(String str) {
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * ����ĳ����Ϊ4֮�µ��ҷֶε����п������  ���ӰԺ-> {��Ӱ,Ժ}��{��,ӰԺ}
     */
    public static List<List<String>> getPossibleCombination(String str) {
        if (StringUtils.isBlank(str) || str.length() == 1) {
            return null;
        }
        List<List<String>> result = new ArrayList<>();

        switch (str.length()) {
            case 2:
                List<String> list21 = new ArrayList<>();
                list21.add(str.substring(0, 1));
                list21.add(str.substring(1));
                result.add(list21);
                break;
            case 3:
                List<String> list31 = new ArrayList<>();
                list31.add(str.substring(0, 1));
                list31.add(str.substring(1));
                result.add(list31);
                List<String> list32 = new ArrayList<>();
                list32.add(str.substring(0, 2));
                list32.add(str.substring(2));
                result.add(list32);
                break;
            case 4:
                List<String> list41 = new ArrayList<>();
                list41.add(str.substring(0, 1));
                list41.add(str.substring(1));
                result.add(list41);   //  1 3
                List<String> list42 = new ArrayList<>();
                list42.add(str.substring(0, 3));
                list42.add(str.substring(3));   // 3 1
                result.add(list42);
                List<String> list43 = new ArrayList<>();
                list43.add(str.substring(0, 2));
                list43.add(str.substring(2));   // 2 2
                result.add(list43);
                break;
        }
        return result;
    }
}


/**
 * ȥ��ͣ�ô�,�����ִʴ���ͣ�ôʷָ� �Ӹߵ���ȡ��
 */
   /* public static String[] segmentByStopWordsDes(String text) {
        if (text.length() == 1) {
            return " ".split(" ");    // ���ؽ��Ϊ {}
        }
        int temp_max_len = Math.min(text.length() + 1, MAX_STOP_WORD_LEN);
        int p = 0;
        while (p < text.length()) {
            int q = temp_max_len;
            while (q > 0) {  // ����ȡ�ʵĳ���
                // ȡ�ʴ�  p --> p+q
                if (p + q > text.length()) {
                    q--;    // ������ȥ����ȫ
                    continue;
                }
                String strChar = text.substring(p, p + q);
                if (stopWordSet.contains(strChar)) {  //|| strChar.contains(",")
                    // System.out.println("==>" + strChar);
                    text = text.replaceAll(strChar, ",");
                    p++;
                    q = temp_max_len;
                    continue;  // ͣ�ô��Թ�
                }
                q--;
            }
            p++;
        }

        String temp = text.replaceAll("[,]+", ",");  // �Զ���Ƿִ��ַ����кϲ�����
        String[] seg_stop_result = temp.split(",");
        return seg_stop_result;
    }
*/