package main.edu.csu.shan.main;

import main.edu.csu.shan.config.Config;
import main.edu.csu.shan.pojo.Term;
import main.edu.csu.shan.util.FileUtils;
import main.edu.csu.shan.util.HanUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static main.edu.csu.shan.config.Constants.wcMap;

/**
 * 数据预处理
 */
public class PreProcess {

    private static final double alpha = 0.15;  //  置信度α
    private static final double beta = 0.40;  // 置信度 β

    private static String novel;   // 小说文本
    private static boolean countWordFrequency = true;   // 是否开启词频统计
    private static boolean debug_text_lenth = false;   // debug 显示信息控制，长度大于4的才显示

    private static LinkedHashSet<String> seg_result = new LinkedHashSet<>();   // 切词结果集
    private static LinkedHashSet<String> seg_final_result = new LinkedHashSet<>();   // 分词最终候选结果
    private static HashSet stopWordSet = new HashSet();     // 停用词哈希表

    StringBuilder debug_Info = new StringBuilder();   // debug 信息，用于存于文件中

    private void doConfidenceCalculation() {
        initData();   // 数据预处理: 以非中文字符切分、以停用词切分、以FMM算法切分
        //  所有词频减半
  /*      for (String key : wcMap.keySet()) {
            wcMap.put(key, wcMap.get(key) / 2);
        }*/

        // 置信度计算
        String[] replaceNonChinese = HanUtils.replaceNonChineseCharacterAsBlank(novel);  // 去掉非中文字符   里边没有逗号
        for (int i = 0; i < replaceNonChinese.length; i++) {
            String textDS = replaceNonChinese[i];   // 这里没有逗号
       /*     System.out.println("原字符串1=>" + textDS);
            debug_Info.append("原字符串1=>" + textDS + "\n");*/
            if (StringUtils.isNotBlank(textDS) && textDS.length() != 1) {
                String[] withoutStopWords = HanUtils.segmentByStopWords(textDS);   // 将以非中文字符分割后的结果再以停用词分割
                for (int j = 0; j < withoutStopWords.length; j++) {
                    String text = withoutStopWords[j];
                    if (StringUtils.isNotBlank(text) && text.length() != 1) {
                        debug_text_lenth = text.length() > 4 ? true : false;
                        // if (debug_text_lenth) debug_Info.append("原字符串2=>" + text + "\n");
                        countWordFrequency = false;
                        LinkedHashSet<String> termList = HanUtils.segment(text, false);    // 置信度比较的是这里面的值
                        //System.out.print("切分字串=>");
                        // if (debug_text_lenth) debug_Info.append("切分字串=>");
                        List<Integer> frequencyList = new ArrayList<>();
                        int max_frequency = 0;
                        for (String str : termList) {
                            //if (debug_text_lenth) debug_Info.append(str + "->" + wcMap.get(str) + " ");
                            // System.out.print(str + "->" + wcMap.get(str) + " ");
                            if (wcMap.get(str) > max_frequency) {
                                max_frequency = wcMap.get(str);
                            }
                            frequencyList.add(wcMap.get(str));
                        }
                        // if (debug_text_lenth) debug_Info.append("\n");
                        //System.out.println();
                        // 词频一致，取最长
                        boolean frequencyEqualTrue = true;
                        if (frequencyList.size() > 1) {
                            int before = frequencyList.get(0);
                            for (int k = 0; k < frequencyList.size(); k++) {
                                before = k - 1 < 0 ? frequencyList.get(0) : frequencyList.get(k - 1);
                                int current = frequencyList.get(k);

                                if (before != current) {
                                    frequencyEqualTrue = false;
                                }
                            }
                        }
                        if (frequencyEqualTrue) {
                            int max_len = 0;
                            for (String str : termList) {
                                max_len = str.length() > max_len ? str.length() : max_len;
                            }
                            for (String str : termList) {
                                if (str.length() == max_len) {
                                    seg_final_result.add(str);
                                    //if (debug_text_lenth) debug_Info.append(" 结果集->" + str);
                                    // System.out.print(" " + str);
                                }
                            }
                        } else {
                            // 取过滤
                            LinkedHashSet result = filterByConfidence(text.length(), termList);
                            seg_final_result.addAll(result);
                            //if (debug_text_lenth) debug_Info.append(" 结果集->" + result);
                            //System.out.println(" 结果集->" + result);
                        }
                        // if (debug_text_lenth) debug_Info.append("\n");
                        // System.out.println();
                    }
                }
            }
        }


        // 最终结果排序输出
        List<String> list = new ArrayList<>(seg_final_result);
        list.sort(Comparator.comparing(HanUtils::firstPinyinCharStr));
        // 对候选集根据词频降序排列
        Map<String, Integer> map = new HashMap<>();
        for (String word : seg_final_result) {
            map.put(word, wcMap.get(word));
        }
        List<Map.Entry<String, Integer>> infoIds = new ArrayList(map.entrySet());
        Collections.sort(infoIds, (o1, o2) -> (o2.getValue() - o1.getValue()));        //排序
        FileUtils.writeStringToFile(Config.DebugPath, String.valueOf(infoIds));
        // System.out.println();
    }


    public void initData() {   // 数据预处理
        initStopWords();  // 初始化停用词集合
        // 读取小说文本
        novel = FileUtils.readFileToString(Config.NovelPath);
        String[] replaceNonChinese = HanUtils.replaceNonChineseCharacterAsBlank(novel);  // 去掉非中文字符   里边没有逗号
        // 再拆分停用词
        for (int i = 0; i < replaceNonChinese.length; i++) {
            String textDS = replaceNonChinese[i];   // 这里没有逗号
            if (StringUtils.isNotBlank(textDS) && textDS.length() != 1) {
                String[] withoutStopWords = HanUtils.segmentByStopWords(textDS);   // 将以非中文字符分割后的结果再以停用词分割
                for (int j = 0; j < withoutStopWords.length; j++) {
                    String text = withoutStopWords[j];
                    if (StringUtils.isNotBlank(text) && text.length() != 1) {
                        LinkedHashSet<String> termList = HanUtils.segment(text, true);    // 置信度比较的是这里面的值
                        if (termList != null) {
                            seg_result.addAll(termList);
                        }
                    }
                }
            }
        }
        //System.out.println("切分字串的个数" + seg_result.size());
        //System.out.println();
    }

    //  初始化停用词哈希表
    private void initStopWords() {
        stopWordSet = FileUtils.readFileByLineToHashSet("D:\\HanLP\\stopwords.txt");
    }


    /**
     * @param s_len    原字符串长度  s指挥警察四处奔忙  取候选集前根号len(s)个  ,s为待FMM 切分串
     * @param termList 候选集  指挥->117 指挥警->2 指挥警察->2 挥警->2 挥警察->2 挥警察四->2 警察->51 警察四->2 警察四处->2 察四->2 察四处->2 察四处奔->2 四处->35 四处奔->6 四处奔忙->2 处奔->10 处奔忙->2 奔忙->4
     * @return 置信度过滤，过滤的结果无交集
     */
    public LinkedHashSet<String> filterByConfidence(int s_len, LinkedHashSet<String> termList) {
        // 将切分结果集分为以首字符区分的若干组
        HashSet<String> firstCharacters = new HashSet<>();   // 首字符 集合
        for (String word : termList) {
            if (!firstCharacters.contains(word.substring(0, 1))) {
                firstCharacters.add(word.substring(0, 1));
            }
        }
        // 将原来的集合分为若干组
        List<List<String>> teams = new ArrayList<>();  //  分组集合
        List<String> seg_list = new ArrayList<>(termList);
        int p = 0;
        String history = seg_list.get(0).substring(0, 1);
        String seg = seg_list.get(0);
        String firstChar = seg.substring(0, 1);  // 首字符
        while (p < seg_list.size()) {
            List<String> seg_team = new ArrayList<>();
            while (firstChar.equals(history)) {
                seg_team.add(seg_list.get(p));
                p++;

                if (p >= seg_list.size()) break;

                firstChar = seg_list.get(p).substring(0, 1);
            }
            teams.add(seg_team);
            if (p >= seg_list.size()) break;
            history = seg_list.get(p).substring(0, 1);
        }

        // 每个组挑选一个候选对象  方法是每组倒序排列
        LinkedHashSet<String> result_set = new LinkedHashSet();
        teams.forEach(list -> {
            String topCandidateFromSet = getTopCandidateFromSet(list);
            result_set.add(topCandidateFromSet);
        });
        // 根据每个组挑选出来的候选对象 再进行倒排序
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String word : result_set) {
            map.put(word, wcMap.get(word));
        }
        List<Map.Entry<String, Integer>> infoIds = new ArrayList(map.entrySet());
        //排序
        Collections.sort(infoIds, (o1, o2) -> (orderByConfidence(o1, o2, false)));    // 排序的稳定性

        //根据最终要求的数据大小取最终结果集
        LinkedHashSet final_result = new LinkedHashSet();
        int n = (int) Math.round(Math.sqrt(s_len));
        for (int i = 0; i < result_set.size(); i++) {
            if (final_result.isEmpty()) {
                final_result.add(infoIds.get(0).getKey());
            }
            if (HanUtils.hasNonCommonWithAllAddedResultSet(final_result, infoIds.get(i).getKey())) {
                final_result.add(infoIds.get(i).getKey());
            }
        }
        return final_result;
    }

    //  倒序取一个
    public String getTopCandidateFromSet(List<String> termList) {
        // 对候选集根据词频降序排列
        Map<String, Integer> map = new HashMap<>();
        for (String word : termList) {
            map.put(word, wcMap.get(word));
        }
        List<Map.Entry<String, Integer>> infoIds = new ArrayList(map.entrySet());
        //排序
        Collections.sort(infoIds, (o1, o2) -> (orderByConfidence(o1, o2, true)));  // 第一轮筛选

        return infoIds.get(0).getKey();
    }


    //  倒序取一个
    public Term getTopCandidateFromSetNew(List<Term> termList) {
        //System.out.println("第一轮决策前"+termList);
        //排序
        Collections.sort(termList, (o1, o2) -> (orderByConfidence(o1, o2)));  // 第一轮筛选       // 对候选集根据词频降序排列
        //System.out.println("第一轮排序后"+termList);
        return termList.get(0);
    }

    public int orderByConfidence(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2, boolean isFirstTimeScreen) {
        int min = Math.min(o1.getValue(), o2.getValue());
        int max = Math.max(o1.getValue(), o2.getValue());
        double conf = (double) min / max;
        // 适用于第二轮筛选
        if (min != 1 && (HanUtils.hasCommonStr(o1.getKey(), o2.getKey(), isFirstTimeScreen) && conf > beta)) {   //  有交集的字符串处理，适合第二轮筛选
            // 两个都取  偏向字符串长度较长的
            return o2.getKey().length() - o1.getKey().length();
        } else {
            return o2.getValue() - o1.getValue();
        }
    }

    public int orderByConfidence(Term o1, Term o2) {
        int min = Math.min(wcMap.get(o1.getValue()), wcMap.get(o2.getValue()));
        int max = Math.max(wcMap.get(o1.getValue()), wcMap.get(o2.getValue()));
        double conf = (double) min / max;
        // 适用于第二轮筛选
        if (min != 1 && (HanUtils.hasCommonStr(o1, o2) && conf > beta)) {   //  有交集的字符串处理，适合第二轮筛选
            // 两个都取  偏向字符串长度较长的
            return o2.getValue().length() - o1.getValue().length();
        } else {
            return  wcMap.get(o2.getValue())  - wcMap.get(o1.getValue());
        }
    }

  /*  // 判断两个字符串是否有交集
    public boolean hasCommonStr(String str1, String str2, boolean isFirstTimeScreen) {  // 控制参数，是否是第一轮筛选
        char[] chars = str1.toCharArray();
        // 共头的不算有交集
        if (!isFirstTimeScreen && str1.substring(0, 1).equals(str2.substring(0, 1))) {  // 第二轮筛选共头的不算有交集
            return false;
        }
        for (int i = 0; i < chars.length; i++) {
            if (str2.contains(chars[i] + "")) {
                return true;
            }
        }
        return false;
    }*/

    public static void main(String[] args) {
        PreProcess preProcess = new PreProcess();
        preProcess.initData();
        //preProcess.doConfidenceCalculation();
      /*  String test_text = "牙湾煤矿采五区孙少平请速";
        countWordFrequency = false;
        LinkedHashSet<String> termList = preProcess.segment(test_text);    // 置信度比较的是这里面的值

        termList.forEach(it -> {
            System.out.print(it + "->" + wcMap.get(it) + " ");
        });
        System.out.println();
        System.out.println();
        preProcess.fiterByConfidence(test_text.length(), termList);*/
    }
}


/*
    // 切分词
    private LinkedHashSet<String> segment(String text) {
        //  送进来的切先以停用词切分
        LinkedHashSet<String> termList = new LinkedHashSet<>();
        if (text.length() == 1) {
            return null;
        }
        int temp_max_len = Math.min(text.length() + 1, MAX_WORD_LEN);
        int p = 0;
        while (p < text.length()) {
            int q = 1;
            while (q < temp_max_len) {  // 控制取词的长度
                if (q == 1) {
                    q++;
                    continue;  // 长度为1略过,单个汉字不具有分词意义
                }
                // 取词串  p --> p+q
                if (p + q > text.length()) {
                    break;
                }
                String strChar = text.substring(p, p + q);
                termList.add(strChar);
                // 统计词串的词频
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
    }*/


/*    // 将非中文字符  以及中文停用词  以空格替代
    private String[] replaceNonChineseCharacterAsBlank(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (HanUtils.isChineseCharacter(String.valueOf(chars[i]))) {
                stringBuilder.append(chars[i]);
            } else {
                stringBuilder.append(",");
            }
        }
        String temp = stringBuilder.toString().replaceAll("[,]+", ",");  // 对多个非分词字符进行合并处理
        String[] seg_nonChinese_result = temp.split(",");
        return seg_nonChinese_result;
    }*/