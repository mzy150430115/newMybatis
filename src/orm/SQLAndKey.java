package orm;

import java.util.List;

public class SQLAndKey {
    //这个类只是为了存放解析sql以后得到的两个结果
    private StringBuilder newsql;
    private List<String> keyList;

    public SQLAndKey(StringBuilder newsql, List<String> keyList) {
        this.newsql = newsql;
        this.keyList = keyList;
    }

    public String getNewsql() {
        return newsql.toString();
    }

    public List<String> getKeyList() {
        return keyList;
    }
}
