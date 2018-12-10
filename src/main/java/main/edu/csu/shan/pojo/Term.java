package main.edu.csu.shan.pojo;

/**
 * 切分的字组合，并非真正意义上的词
 *  [leftBound , rightBound)  左闭右开区间
 */
public class Term {
    public String value;   // 切片的结构
    public int count;  // 词频
    public int leftBound;  // 左边界  在原来短句中的起始索引位置
    public int rightBound;  // 右边界

    public Term(String value, int count) {
        this.value = value;
        this.count = count;
    }

    public Term(String value, int leftBound, int rightBound) {
        this.value = value;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getLeftBound() {
        return leftBound;
    }

    public void setLeftBound(int leftBound) {
        this.leftBound = leftBound;
    }

    public int getRightBound() {
        return rightBound;
    }

    public void setRightBound(int rightBound) {
        this.rightBound = rightBound;
    }

    @Override
    public String toString() {
        return "Term{" +
                "value='" + value + '\'' +
                ", count=" + count +
                ", leftBound=" + leftBound +
                ", rightBound=" + rightBound +
                '}';
    }
}
