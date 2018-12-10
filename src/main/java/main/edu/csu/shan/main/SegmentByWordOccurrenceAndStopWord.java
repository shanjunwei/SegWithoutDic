package main.edu.csu.shan.main;

import main.edu.csu.shan.config.Config;
import main.edu.csu.shan.pojo.Term;
import main.edu.csu.shan.util.FileUtils;
import main.edu.csu.shan.util.HanUtils;

import java.util.*;

import static main.edu.csu.shan.config.Config.debugInfo;
import static main.edu.csu.shan.config.Constants.wcMap;

/**
 * ͨ�� ȥͣ�ôʺʹʹ���ͳ�����ִ�
 */
public class SegmentByWordOccurrenceAndStopWord {
    public static PreProcess preProcess = new PreProcess();

    static {
        preProcess.initData();   //  ����Ԥ����
    }

    /**
     * ��¶���ⲿ���õķִʽӿ�
     * �������Ĳ��� û�з������ַ�����һ������
     */
    public List<String> segment(String text) {
        String segResult = segmentToString(text);
        debugInfo.append("\n____________________________>>���ӷִʽ��____>>" + Arrays.asList(segResult.split(" ")));
        debugInfo.append("\n");
        return Arrays.asList(segResult.split(" "));
    }

    /**
     * ��¶���ⲿ���õķִʽӿ�
     * �������Ĳ��� û�з������ַ���һ������
     */
    public String segmentToString(String text) {

        if(text.equals("���Ѿ���ʧ��������Ӱ��")){
            System.out.println("=====");
        }

        debugInfo.append("Ԫ����-->" + text);
        List<List<String>> stopWordsAndSegs = HanUtils.segmentByStopWordsDes(text);
        debugInfo.append("   ||----ͣ�ô��б� " + stopWordsAndSegs.get(0) + " ||----��ͣ�ô�" + stopWordsAndSegs.get(1)+"\n");
        //System.out.println("ͣ�ô��б�" +stopWordsAndSegs.get(0)  +"  ��ͣ�ô�"+ stopWordsAndSegs.get(1));
        List<String> exactWords = new ArrayList<>();
        // ȥͣ�ôʺ�ֻ��һ���������ֵ����
        stopWordsAndSegs.get(1).forEach(it -> {
            if (it.length() == 1) {
                exactWords.add(it);
            } else {
                List<Term> termList = HanUtils.segmentToTerm(it, false);    // ���ŶȱȽϵ����������ֵ
                // ����ȡ
                List<String> result = extractWordsFromNGram(termList);
                exactWords.addAll(result);
            }
        });
        // ������ȡ�Ĵ��Լ� ͣ�ô� ʣ�µĶ��Ǵ���
        for (String stopWord : stopWordsAndSegs.get(0)) {
            text = text.replaceAll(stopWord, " " + stopWord + " ");
        }
        for (String seg : exactWords) {
            text = text.replaceAll(seg, " " + seg + " ");
        }
        text = text.trim();   // ����һ��Ҫ��ֵ��Ȼû��
        text = text.replaceAll("\\s{1,}", " ");
        return text;
    }


    /**
     * ԭ�ַ�������  sָ�Ӿ����Ĵ���æ  ȡ��ѡ��ǰ����len(s)��  ,sΪ��FMM �зִ�
     *
     * @param termList ��ѡ��  ָ��->117 ָ�Ӿ�->2 ָ�Ӿ���->2 �Ӿ�->2 �Ӿ���->2 �Ӿ�����->2 ����->51 ������->2 �����Ĵ�->2 ����->2 ���Ĵ�->2 ���Ĵ���->2 �Ĵ�->35 �Ĵ���->6 �Ĵ���æ->2 ����->10 ����æ->2 ��æ->4
     * @return ���Ŷȹ��ˣ����˵Ľ���޽���
     */
    public List<String> extractWordsFromNGram(List<Term> termList) {
        if (termList.size() == 0) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        if (termList.size() == 1) {
            result.add(termList.get(0).getValue());
            return result;
        }
        // ���зֽ������Ϊ�����ַ����ֵ�������
        List<List<Term>> teams = new ArrayList<>();  //  ���鼯��
        List<Term> seg_list = new ArrayList<>(termList);
        int p = 0;
        String history = seg_list.get(0).getValue().substring(0, 1);
        String seg = seg_list.get(0).getValue();
        String firstChar = seg.substring(0, 1);  // ���ַ�
        while (p < seg_list.size()) {
            List<Term> seg_team = new ArrayList<>();
            while (firstChar.equals(history)) {
                seg_team.add(seg_list.get(p));
                p++;

                if (p >= seg_list.size()) break;

                firstChar = seg_list.get(p).getValue().substring(0, 1);
            }
            teams.add(seg_team);
            if (p >= seg_list.size()) break;
            history = seg_list.get(p).getValue().substring(0, 1);
        }
        // ÿ������ѡһ����ѡ����  ������ÿ�鵹������
        List<Term> result_list = new ArrayList<>();    //  ����ѡ�������Ĵ���ԭ�������е�λ�ñ�עһ��
        teams.forEach(list -> {
            // ��ӡ debug ��Ϣ
            debugInfo.append("��һ��ɸѡǰ___|");
            list.forEach(it -> {
                debugInfo.append(" " + it.getValue() + ":" + wcMap.get(it.getValue()));
            });

            Term topCandidateFromSet = preProcess.getTopCandidateFromSetNew(list);   // ��һ�־���
            result_list.add(topCandidateFromSet);

            debugInfo.append("��һ��ɸѡ��___|");
            list.forEach(it -> {
                debugInfo.append(" " + it.getValue() + ":" + wcMap.get(it.getValue()));
            });
            debugInfo.append("\n");
        });

        // ����
        debugInfo.append("�ڶ���ɸѡǰ___|");
        result_list.forEach(it -> {
            debugInfo.append(" " + it.getValue() + ":" + wcMap.get(it.getValue()));
        });
        result_list.sort((o1, o2) -> wcMap.get(o2.getValue()) - wcMap.get(o1.getValue()));
        List<Term> final_result = new ArrayList<>();
        for (int i = 0; i < result_list.size(); i++) {
            if (final_result.isEmpty()) {
                final_result.add(result_list.get(0));
            }
            if (HanUtils.hasNonCommonWithAllAddedResultSet(final_result, result_list.get(i))) {
                final_result.add(result_list.get(i));
            }
        }
        for (Term word : final_result) {
            result.add(word.getValue());
        }
        debugInfo.append("�ڶ���ɸѡ��___/");
        debugInfo.append(" " + result);
        debugInfo.append("\n");
        return result;
    }

    public static void main(String[] args) {
        /*SegmentByWordOccurrenceAndStopWord segment = new SegmentByWordOccurrenceAndStopWord();
        segment.segment("ֻ���ڰ�ɽ���������еĴ�Ժ����").forEach(it -> {
            System.out.println("-->" + it);
        });
        List<String> list = segment.segment("ֻ���ڰ�ɽ���������еĴ�Ժ����");
        System.out.println(list);*/
        String novel = FileUtils.readFileToString(Config.NovelPath);
        List<String> replaceNonChinese = Arrays.asList(HanUtils.replaceNonChineseCharacterAsBlank(novel));  // ȥ���������ַ�   ���û�ж���
        SegmentByWordOccurrenceAndStopWord segment = new SegmentByWordOccurrenceAndStopWord();
       replaceNonChinese.forEach(it->{
           segment.segment(it);
       });

       FileUtils.writeStringToFile("D:\\HanLP\\ƽ��������\\debugInfo.txt",debugInfo.toString());
        //FileUtils.SegAndWriteFileByLine("D:\\HanLP\\ƽ��������\\segResult3.txt", replaceNonChinese);
    }
}
