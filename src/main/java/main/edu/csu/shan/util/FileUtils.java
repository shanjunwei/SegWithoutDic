package main.edu.csu.shan.util;


import main.edu.csu.shan.main.SegmentByWordOccurrenceAndStopWord;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

public class FileUtils {

    // �ļ���д
    public static String readFileToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }


    public static void writeFileToPath(String outPutPath, LinkedHashSet<String> stringSet) {
        try {
            FileOutputStream writer = new FileOutputStream(outPutPath);
            OutputStreamWriter bw = new OutputStreamWriter(writer, "UTF-8");          // ��utf-8д���
            stringSet.forEach(it -> {
                if (StringUtils.isNotBlank(it)) {
                    String result = it + " ";
                    try {
                        bw.write(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            bw.close();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public static void writeFileToPath(String outPutPath, List<String> list, Map<String, Integer> wcMap) {
        try {
            FileOutputStream writer = new FileOutputStream(outPutPath);
            OutputStreamWriter bw = new OutputStreamWriter(writer, "UTF-8");          // ��utf-8д���
            list.forEach(it -> {
                if (StringUtils.isNotBlank(it) && wcMap.get(it) >= 4) {
                    try {
                        bw.write(it + " -> " + wcMap.get(it) + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            bw.close();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /// ��ʼ����ϣ��
    // ���ж�ȡ������
    public static HashSet readFileByLineToHashSet(String inputFilePath) {
        HashSet set = new HashSet();
        try {
            // ��utf-8��ȡ�ļ�
            FileInputStream fis = new FileInputStream(inputFilePath);
            InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            while ((str = br.readLine()) != null) {
                set.add(str);
            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return set;
    }

    /// ��ʼ����ϣ��
    // ���ж�ȡ������
    public static void writeStringToFile(String outPutPath, String text) {
        try {
            FileOutputStream writer = new FileOutputStream(outPutPath);
            OutputStreamWriter bw = new OutputStreamWriter(writer, "UTF-8");          // ��utf-8д���
            bw.write(text);
            bw.close();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * �ִʲ��Ұѽ��һ����д���ļ�
     *
     * @param outPutPath
     * @param list
     */
    public static void SegAndWriteFileByLine(String outPutPath, List<String> list) {
        SegmentByWordOccurrenceAndStopWord segment = new SegmentByWordOccurrenceAndStopWord();
        try {
            FileOutputStream writer = new FileOutputStream(outPutPath);
            OutputStreamWriter bw = new OutputStreamWriter(writer, "UTF-8");          // ��utf-8д���
            list.forEach(it -> {
                if (StringUtils.isNotBlank(it)) {
                    try {
                        String segResult = String.valueOf(segment.segment(it));
                        bw.write(it+"   -->   "+segResult + "\n");
                        System.out.println(it+"   -->   "+segResult + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            bw.close();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*// ���ж�ȡ������
    public static HashSet readFileByLineToHashSet(String inputFilePath) {
        HashSet set = new HashSet();
        try {
            // ��utf-8��ȡ�ļ�
            FileInputStream fis = new FileInputStream(inputFilePath);
            InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            while ((str = br.readLine()) != null) {
                set.add(str);
            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return set;
    }*/
}
