package main.edu.csu.shan.main;

import main.edu.csu.shan.config.Config;
import main.edu.csu.shan.pojo.Term;
import main.edu.csu.shan.util.FileUtils;
import main.edu.csu.shan.util.HanUtils;

import java.util.*;

import static main.edu.csu.shan.config.Config.debugInfo;
import static main.edu.csu.shan.config.Constants.wcMap;

/**
 * 通过 去停用词和词共现统计量分词
 */
public class SegmentByWordOccurrenceAndStopWord {
    public static PreProcess preProcess = new PreProcess();

    static {
        preProcess.initData();   //  数据预处理
    }

    /**
     * 暴露给外部调用的分词接口
     * 传进来的参数 没有非中文字符，是一个句子
     */
    public List<String> segment(String text) {
        String segResult = segmentToString(text);
        debugInfo.append("\n____________________________>>句子分词结果____>>" + Arrays.asList(segResult.split(" ")));
        debugInfo.append("\n");
        return Arrays.asList(segResult.split(" "));
    }

    /**
     * 暴露给外部调用的分词接口
     * 传进来的参数 没有非中文字符，一个句子
     */
    public String segmentToString(String text) {

        if(text.equals("就已经消失得无踪无影了")){
            System.out.println("=====");
        }

        debugInfo.append("元句子-->" + text);
        List<List<String>> stopWordsAndSegs = HanUtils.segmentByStopWordsDes(text);
        debugInfo.append("   ||----停用词列表 " + stopWordsAndSegs.get(0) + " ||----非停用词" + stopWordsAndSegs.get(1)+"\n");
        //System.out.println("停用词列表" +stopWordsAndSegs.get(0)  +"  非停用词"+ stopWordsAndSegs.get(1));
        List<String> exactWords = new ArrayList<>();
        // 去停用词后只有一个单独汉字的情况
        stopWordsAndSegs.get(1).forEach(it -> {
            if (it.length() == 1) {
                exactWords.add(it);
            } else {
                List<Term> termList = HanUtils.segmentToTerm(it, false);    // 置信度比较的是这里面的值
                // 词提取
                List<String> result = extractWordsFromNGram(termList);
                exactWords.addAll(result);
            }
        });
        // 除了提取的词以及 停用词 剩下的都是词了
        for (String stopWord : stopWordsAndSegs.get(0)) {
            text = text.replaceAll(stopWord, " " + stopWord + " ");
        }
        for (String seg : exactWords) {
            text = text.replaceAll(seg, " " + seg + " ");
        }
        text = text.trim();   // 这里一定要赋值不然没用
        text = text.replaceAll("\\s{1,}", " ");
        return text;
    }


    /**
     * 原字符串长度  s指挥警察四处奔忙  取候选集前根号len(s)个  ,s为待FMM 切分串
     *
     * @param termList 候选集  指挥->117 指挥警->2 指挥警察->2 挥警->2 挥警察->2 挥警察四->2 警察->51 警察四->2 警察四处->2 察四->2 察四处->2 察四处奔->2 四处->35 四处奔->6 四处奔忙->2 处奔->10 处奔忙->2 奔忙->4
     * @return 置信度过滤，过滤的结果无交集
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
        // 将切分结果集分为以首字符区分的若干组
        List<List<Term>> teams = new ArrayList<>();  //  分组集合
        List<Term> seg_list = new ArrayList<>(termList);
        int p = 0;
        String history = seg_list.get(0).getValue().substring(0, 1);
        String seg = seg_list.get(0).getValue();
        String firstChar = seg.substring(0, 1);  // 首字符
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
        // 每个组挑选一个候选对象  方法是每组倒序排列
        List<Term> result_list = new ArrayList<>();    //  将候选结果集里的词在原来句子中的位置标注一下
        teams.forEach(list -> {
            // 打印 debug 信息
            debugInfo.append("第一轮筛选前___|");
            list.forEach(it -> {
                debugInfo.append(" " + it.getValue() + ":" + wcMap.get(it.getValue()));
            });

            Term topCandidateFromSet = preProcess.getTopCandidateFromSetNew(list);   // 第一轮决策
            result_list.add(topCandidateFromSet);

            debugInfo.append("第一轮筛选后___|");
            list.forEach(it -> {
                debugInfo.append(" " + it.getValue() + ":" + wcMap.get(it.getValue()));
            });
            debugInfo.append("\n");
        });

        // 排序
        debugInfo.append("第二轮筛选前___|");
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
        debugInfo.append("第二轮筛选后___/");
        debugInfo.append(" " + result);
        debugInfo.append("\n");
        return result;
    }

    public static void main(String[] args) {
        /*SegmentByWordOccurrenceAndStopWord segment = new SegmentByWordOccurrenceAndStopWord();
        segment.segment("只有在半山腰县立高中的大院坝里").forEach(it -> {
            System.out.println("-->" + it);
        });
        List<String> list = segment.segment("只有在半山腰县立高中的大院坝里");
        System.out.println(list);*/
        String novel = FileUtils.readFileToString(Config.NovelPath);
        List<String> replaceNonChinese = Arrays.asList(HanUtils.replaceNonChineseCharacterAsBlank(novel));  // 去掉非中文字符   里边没有逗号
        SegmentByWordOccurrenceAndStopWord segment = new SegmentByWordOccurrenceAndStopWord();
       replaceNonChinese.forEach(it->{
           segment.segment(it);
       });

       FileUtils.writeStringToFile("D:\\HanLP\\平凡的世界\\debugInfo.txt",debugInfo.toString());
        //FileUtils.SegAndWriteFileByLine("D:\\HanLP\\平凡的世界\\segResult3.txt", replaceNonChinese);
    }
}
