package main.edu.csu.shan.config;

import main.edu.csu.shan.util.FileUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  常量
 */
public class Constants {
    public static  String NOVEL;
    public static HashSet stopWordSet = new HashSet();     // 停用词哈希表
    public static Map<String, Integer> wcMap = new ConcurrentHashMap<>();   // 用于统计词频

    static {
        initStopWords();
    }


    //  初始化停用词哈希表
    private static void initStopWords() {
        stopWordSet = FileUtils.readFileByLineToHashSet(Config.StopWordsPath);
    }

}
