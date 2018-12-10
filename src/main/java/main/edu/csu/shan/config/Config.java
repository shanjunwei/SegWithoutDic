package main.edu.csu.shan.config;

/**
 * Created by bruce_shan on 2018/12/4 16:51.
 * Corporation CSU Software
 */
public class Config {
    public static final StringBuilder  debugInfo  = new StringBuilder();
    public final static double MIN_PROBABILITY = 1e-10;   // 10 �ĸ�10 �η�
    public static final int MAX_STOP_WORD_LEN = 4;  // ͣ�ô���󳤶�Ϊ4
    public static final int MAX_WORD_LEN = 4 + 1;  // �ִ���󳤶�Ϊ4
    public static final int MAX_WORD_COUNT = 1;  // �ִ���С��Ƶ
    public static final double beta = 0.51;  // ���Ŷ� ��

    public  final static double MIN_LEFT_ENTROPY = 0.01;   // ��С����,���������ع���
    public  final static double MIN_RIGHT_ENTROPY = 0.01;   // ��С����,���������ع���


    //public static String NovelPath = "D:\\HanLP\\novel\\ƽ��������.txt"; // ƽ��������
    public static String NovelPath = "D:\\data\\test-text.txt"; // �������
    public static String DebugPath = "E:\\shan\\china_daily\\666.txt";   //  debug ��Ϣ���
    /**
     * �ַ����Ͷ�Ӧ��
     */
    public static String CharTypePath = "data/dictionary/other/CharType.bin";
    /**
     * ͣ�ôʴʵ�
     */
    public static String StopWordsPath = "D:\\HanLP\\stopwords.txt";   // ͣ�ô�·��

    /**
     *  �ֵ������л�·��
     */
    public static String SerialPath = "D:\\BigData\\HanLP\\trie_serial.txt";
}

/*
    private static final int MAX_STOP_WORD_LEN = 4;  // ͣ�ô���󳤶�Ϊ4
    private static final int MAX_WORD_LEN = 4 + 1;  // �ִ���󳤶�Ϊ4
    private static final int MAX_WORD_COUNT = 1;  // �ִ���С��Ƶ*/