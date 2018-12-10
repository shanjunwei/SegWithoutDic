package main.edu.csu.shan.other;

import main.edu.csu.shan.util.HanUtils;

import java.io.*;

public class SSparser {


    public static void main(String[] args) {
        // 处理切分汉字串集合
        // 读取停用词字典
        getChineseStopWords("D:\\HanLP\\data\\dictionary\\stopwords.txt");
    }

    // 只要中文的停用词典
    public static void getChineseStopWords(String stopWordFilePath) {
        try {
            // 以utf-8读取文件
            FileInputStream fis = new FileInputStream(stopWordFilePath);
            InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(reader);

            String str = null;
            // 以utf-8写结果
            FileOutputStream writer = new FileOutputStream("D:\\HanLP\\stopwords.txt");
            OutputStreamWriter bw = new OutputStreamWriter(writer, "UTF-8");
            while ((str = br.readLine()) != null) {
                if (HanUtils.isChineseCharacter(str)) {
                    String result = str + "\r\n";
                    bw.write(result);
                }
            }
            br.close();
            reader.close();
            bw.close();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
