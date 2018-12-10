package main.edu.csu.shan.config;

import main.edu.csu.shan.util.FileUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  ����
 */
public class Constants {
    public static  String NOVEL;
    public static HashSet stopWordSet = new HashSet();     // ͣ�ôʹ�ϣ��
    public static Map<String, Integer> wcMap = new ConcurrentHashMap<>();   // ����ͳ�ƴ�Ƶ

    static {
        initStopWords();
    }


    //  ��ʼ��ͣ�ôʹ�ϣ��
    private static void initStopWords() {
        stopWordSet = FileUtils.readFileByLineToHashSet(Config.StopWordsPath);
    }

}
