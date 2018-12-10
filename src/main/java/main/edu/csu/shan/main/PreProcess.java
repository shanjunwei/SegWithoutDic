package main.edu.csu.shan.main;

import main.edu.csu.shan.config.Config;
import main.edu.csu.shan.pojo.Term;
import main.edu.csu.shan.util.FileUtils;
import main.edu.csu.shan.util.HanUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static main.edu.csu.shan.config.Constants.wcMap;

/**
 * ����Ԥ����
 */
public class PreProcess {

    private static final double alpha = 0.15;  //  ���ŶȦ�
    private static final double beta = 0.40;  // ���Ŷ� ��

    private static String novel;   // С˵�ı�
    private static boolean countWordFrequency = true;   // �Ƿ�����Ƶͳ��
    private static boolean debug_text_lenth = false;   // debug ��ʾ��Ϣ���ƣ����ȴ���4�Ĳ���ʾ

    private static LinkedHashSet<String> seg_result = new LinkedHashSet<>();   // �дʽ����
    private static LinkedHashSet<String> seg_final_result = new LinkedHashSet<>();   // �ִ����պ�ѡ���
    private static HashSet stopWordSet = new HashSet();     // ͣ�ôʹ�ϣ��

    StringBuilder debug_Info = new StringBuilder();   // debug ��Ϣ�����ڴ����ļ���

    private void doConfidenceCalculation() {
        initData();   // ����Ԥ����: �Է������ַ��з֡���ͣ�ô��з֡���FMM�㷨�з�
        //  ���д�Ƶ����
  /*      for (String key : wcMap.keySet()) {
            wcMap.put(key, wcMap.get(key) / 2);
        }*/

        // ���Ŷȼ���
        String[] replaceNonChinese = HanUtils.replaceNonChineseCharacterAsBlank(novel);  // ȥ���������ַ�   ���û�ж���
        for (int i = 0; i < replaceNonChinese.length; i++) {
            String textDS = replaceNonChinese[i];   // ����û�ж���
       /*     System.out.println("ԭ�ַ���1=>" + textDS);
            debug_Info.append("ԭ�ַ���1=>" + textDS + "\n");*/
            if (StringUtils.isNotBlank(textDS) && textDS.length() != 1) {
                String[] withoutStopWords = HanUtils.segmentByStopWords(textDS);   // ���Է������ַ��ָ��Ľ������ͣ�ôʷָ�
                for (int j = 0; j < withoutStopWords.length; j++) {
                    String text = withoutStopWords[j];
                    if (StringUtils.isNotBlank(text) && text.length() != 1) {
                        debug_text_lenth = text.length() > 4 ? true : false;
                        // if (debug_text_lenth) debug_Info.append("ԭ�ַ���2=>" + text + "\n");
                        countWordFrequency = false;
                        LinkedHashSet<String> termList = HanUtils.segment(text, false);    // ���ŶȱȽϵ����������ֵ
                        //System.out.print("�з��ִ�=>");
                        // if (debug_text_lenth) debug_Info.append("�з��ִ�=>");
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
                        // ��Ƶһ�£�ȡ�
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
                                    //if (debug_text_lenth) debug_Info.append(" �����->" + str);
                                    // System.out.print(" " + str);
                                }
                            }
                        } else {
                            // ȡ����
                            LinkedHashSet result = filterByConfidence(text.length(), termList);
                            seg_final_result.addAll(result);
                            //if (debug_text_lenth) debug_Info.append(" �����->" + result);
                            //System.out.println(" �����->" + result);
                        }
                        // if (debug_text_lenth) debug_Info.append("\n");
                        // System.out.println();
                    }
                }
            }
        }


        // ���ս���������
        List<String> list = new ArrayList<>(seg_final_result);
        list.sort(Comparator.comparing(HanUtils::firstPinyinCharStr));
        // �Ժ�ѡ�����ݴ�Ƶ��������
        Map<String, Integer> map = new HashMap<>();
        for (String word : seg_final_result) {
            map.put(word, wcMap.get(word));
        }
        List<Map.Entry<String, Integer>> infoIds = new ArrayList(map.entrySet());
        Collections.sort(infoIds, (o1, o2) -> (o2.getValue() - o1.getValue()));        //����
        FileUtils.writeStringToFile(Config.DebugPath, String.valueOf(infoIds));
        // System.out.println();
    }


    public void initData() {   // ����Ԥ����
        initStopWords();  // ��ʼ��ͣ�ôʼ���
        // ��ȡС˵�ı�
        novel = FileUtils.readFileToString(Config.NovelPath);
        String[] replaceNonChinese = HanUtils.replaceNonChineseCharacterAsBlank(novel);  // ȥ���������ַ�   ���û�ж���
        // �ٲ��ͣ�ô�
        for (int i = 0; i < replaceNonChinese.length; i++) {
            String textDS = replaceNonChinese[i];   // ����û�ж���
            if (StringUtils.isNotBlank(textDS) && textDS.length() != 1) {
                String[] withoutStopWords = HanUtils.segmentByStopWords(textDS);   // ���Է������ַ��ָ��Ľ������ͣ�ôʷָ�
                for (int j = 0; j < withoutStopWords.length; j++) {
                    String text = withoutStopWords[j];
                    if (StringUtils.isNotBlank(text) && text.length() != 1) {
                        LinkedHashSet<String> termList = HanUtils.segment(text, true);    // ���ŶȱȽϵ����������ֵ
                        if (termList != null) {
                            seg_result.addAll(termList);
                        }
                    }
                }
            }
        }
        //System.out.println("�з��ִ��ĸ���" + seg_result.size());
        //System.out.println();
    }

    //  ��ʼ��ͣ�ôʹ�ϣ��
    private void initStopWords() {
        stopWordSet = FileUtils.readFileByLineToHashSet("D:\\HanLP\\stopwords.txt");
    }


    /**
     * @param s_len    ԭ�ַ�������  sָ�Ӿ����Ĵ���æ  ȡ��ѡ��ǰ����len(s)��  ,sΪ��FMM �зִ�
     * @param termList ��ѡ��  ָ��->117 ָ�Ӿ�->2 ָ�Ӿ���->2 �Ӿ�->2 �Ӿ���->2 �Ӿ�����->2 ����->51 ������->2 �����Ĵ�->2 ����->2 ���Ĵ�->2 ���Ĵ���->2 �Ĵ�->35 �Ĵ���->6 �Ĵ���æ->2 ����->10 ����æ->2 ��æ->4
     * @return ���Ŷȹ��ˣ����˵Ľ���޽���
     */
    public LinkedHashSet<String> filterByConfidence(int s_len, LinkedHashSet<String> termList) {
        // ���зֽ������Ϊ�����ַ����ֵ�������
        HashSet<String> firstCharacters = new HashSet<>();   // ���ַ� ����
        for (String word : termList) {
            if (!firstCharacters.contains(word.substring(0, 1))) {
                firstCharacters.add(word.substring(0, 1));
            }
        }
        // ��ԭ���ļ��Ϸ�Ϊ������
        List<List<String>> teams = new ArrayList<>();  //  ���鼯��
        List<String> seg_list = new ArrayList<>(termList);
        int p = 0;
        String history = seg_list.get(0).substring(0, 1);
        String seg = seg_list.get(0);
        String firstChar = seg.substring(0, 1);  // ���ַ�
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

        // ÿ������ѡһ����ѡ����  ������ÿ�鵹������
        LinkedHashSet<String> result_set = new LinkedHashSet();
        teams.forEach(list -> {
            String topCandidateFromSet = getTopCandidateFromSet(list);
            result_set.add(topCandidateFromSet);
        });
        // ����ÿ������ѡ�����ĺ�ѡ���� �ٽ��е�����
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String word : result_set) {
            map.put(word, wcMap.get(word));
        }
        List<Map.Entry<String, Integer>> infoIds = new ArrayList(map.entrySet());
        //����
        Collections.sort(infoIds, (o1, o2) -> (orderByConfidence(o1, o2, false)));    // ������ȶ���

        //��������Ҫ������ݴ�Сȡ���ս����
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

    //  ����ȡһ��
    public String getTopCandidateFromSet(List<String> termList) {
        // �Ժ�ѡ�����ݴ�Ƶ��������
        Map<String, Integer> map = new HashMap<>();
        for (String word : termList) {
            map.put(word, wcMap.get(word));
        }
        List<Map.Entry<String, Integer>> infoIds = new ArrayList(map.entrySet());
        //����
        Collections.sort(infoIds, (o1, o2) -> (orderByConfidence(o1, o2, true)));  // ��һ��ɸѡ

        return infoIds.get(0).getKey();
    }


    //  ����ȡһ��
    public Term getTopCandidateFromSetNew(List<Term> termList) {
        //System.out.println("��һ�־���ǰ"+termList);
        //����
        Collections.sort(termList, (o1, o2) -> (orderByConfidence(o1, o2)));  // ��һ��ɸѡ       // �Ժ�ѡ�����ݴ�Ƶ��������
        //System.out.println("��һ�������"+termList);
        return termList.get(0);
    }

    public int orderByConfidence(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2, boolean isFirstTimeScreen) {
        int min = Math.min(o1.getValue(), o2.getValue());
        int max = Math.max(o1.getValue(), o2.getValue());
        double conf = (double) min / max;
        // �����ڵڶ���ɸѡ
        if (min != 1 && (HanUtils.hasCommonStr(o1.getKey(), o2.getKey(), isFirstTimeScreen) && conf > beta)) {   //  �н������ַ��������ʺϵڶ���ɸѡ
            // ������ȡ  ƫ���ַ������Ƚϳ���
            return o2.getKey().length() - o1.getKey().length();
        } else {
            return o2.getValue() - o1.getValue();
        }
    }

    public int orderByConfidence(Term o1, Term o2) {
        int min = Math.min(wcMap.get(o1.getValue()), wcMap.get(o2.getValue()));
        int max = Math.max(wcMap.get(o1.getValue()), wcMap.get(o2.getValue()));
        double conf = (double) min / max;
        // �����ڵڶ���ɸѡ
        if (min != 1 && (HanUtils.hasCommonStr(o1, o2) && conf > beta)) {   //  �н������ַ��������ʺϵڶ���ɸѡ
            // ������ȡ  ƫ���ַ������Ƚϳ���
            return o2.getValue().length() - o1.getValue().length();
        } else {
            return  wcMap.get(o2.getValue())  - wcMap.get(o1.getValue());
        }
    }

  /*  // �ж������ַ����Ƿ��н���
    public boolean hasCommonStr(String str1, String str2, boolean isFirstTimeScreen) {  // ���Ʋ������Ƿ��ǵ�һ��ɸѡ
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
    }*/

    public static void main(String[] args) {
        PreProcess preProcess = new PreProcess();
        preProcess.initData();
        //preProcess.doConfidenceCalculation();
      /*  String test_text = "����ú�����������ƽ����";
        countWordFrequency = false;
        LinkedHashSet<String> termList = preProcess.segment(test_text);    // ���ŶȱȽϵ����������ֵ

        termList.forEach(it -> {
            System.out.print(it + "->" + wcMap.get(it) + " ");
        });
        System.out.println();
        System.out.println();
        preProcess.fiterByConfidence(test_text.length(), termList);*/
    }
}


/*
    // �зִ�
    private LinkedHashSet<String> segment(String text) {
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
    }*/


/*    // ���������ַ�  �Լ�����ͣ�ô�  �Կո����
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
        String temp = stringBuilder.toString().replaceAll("[,]+", ",");  // �Զ���Ƿִ��ַ����кϲ�����
        String[] seg_nonChinese_result = temp.split(",");
        return seg_nonChinese_result;
    }*/