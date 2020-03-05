package pool;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigReader {
    //读取文件
    private static Properties properties;
    //类加载的时候 一次性读取文件内容存在缓存里
    private static Map<String,String> configMap;

    static {
        //
          try {
              properties = new Properties();
              configMap = new HashMap<String,String>();
              //
              InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("configuration.properties");
              properties.load(is);

              Enumeration en = properties.propertyNames();
              while (en.hasMoreElements()){
                  String key =(String)en.nextElement();
                  String value = properties.getProperty(key);
                  configMap.put(key,value);
              }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //给别人提供一个方法，去缓存里面读取数据
    public static String getValue(String key){
        return configMap.get(key);
    }


}
