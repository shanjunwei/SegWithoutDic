package main.edu.csu.shan.pojo;

import java.util.*;

public class EntropyMap {

    public final static Map<String, List<Term>> leftEntropyMap = new HashMap<>();

    public final static Map<String, List<Term>> rightEntropyMap = new HashMap<>();


    EntropyMap(LinkedHashSet<String> result_set, Map<String, Integer> wcMap) {

        List<String> seg_list = new ArrayList<>(result_set);


        // 初始化右邻字 哈希列表  key: w  value 以w开头的右邻字列表
        int p = 0;
        String history = seg_list.get(0);           // 第一个字符
        String seg = seg_list.get(0);        // 切分
        while (p < seg_list.size()) {
            List<String> seg_team = new ArrayList<>();
            while (seg.startsWith(history) && !seg.equals(history)) {
                seg_team.add(seg_list.get(p));
                p++;

                if (p >= seg_list.size()) break;

                seg = seg_list.get(0);        // 切分
            }

            //  构建右邻字 哈希链表   key: w  value 以w开头的右邻字列表
            InsertEntropyMap(history, new Term(seg, wcMap.get(seg)), rightEntropyMap);
            if (p >= seg_list.size()) break;
            history = seg_list.get(p).substring(0, 1);
        }


        // 初始化右邻字 哈希列表  key: w  value 以w开头的右邻字列表


    }

    private void initEntropyMap(List<String> seg_list, Map<String, Integer> wcMap, Map<String, List<Term>> entropyMap, boolean right) {

        int p = 0;
        String history = seg_list.get(0);           // 第一个字符
        String seg = seg_list.get(0);        // 切分
        while (p < seg_list.size()) {
            List<String> seg_team = new ArrayList<>();
            boolean flag = right ? seg.startsWith(history) && !seg.equals(history) : seg.endsWith(history) && !seg.equals(history);
            while (flag) {
                seg_team.add(seg_list.get(p));
                p++;

                if (p >= seg_list.size()) break;

                seg = seg_list.get(0);        // 切分
            }

            //  构建右邻字 哈希链表   key: w  value 以w开头的右邻字列表
            InsertEntropyMap(history, new Term(seg, wcMap.get(seg)), entropyMap);
            if (p >= seg_list.size()) break;
            history = seg_list.get(p).substring(0, 1);
        }

    }


    private void InsertEntropyMap(String key, Term current, Map<String, List<Term>> entropyMap) {
        List<Term> list;
        if (!entropyMap.containsKey(key)) {
            list = new ArrayList<>();
            list.add(current);

        } else {
            list = entropyMap.get(key);
            list.add(current);
            entropyMap.put(key, list);
        }
        entropyMap.put(key, list);
    }
}
