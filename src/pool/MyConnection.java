package pool;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class MyConnection extends AbstractConnection{


    private Connection conn;
    private boolean used = false;

    private static String driver = ConfigReader.getValue("driver");
    private static String url = ConfigReader.getValue("url");
    private static String username = ConfigReader.getValue("username");
    private static String password = ConfigReader.getValue("password");


    //静态类只加载一次
    static {
        try {
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //每次调用当前Connection构造方法的时候执行一次
    {
        try {
            conn = DriverManager.getConnection(url,username,password);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Connection getConn() {
        return conn;
    }

    public boolean isUsed() {
        //boolean类型的属性get方法命名为is
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    //以下都是实现接口得来的
    public Statement createStatement() throws SQLException {
        return this.conn.createStatement();
    }
    public PreparedStatement prepareStatement(String sql) throws SQLException {
            return this.conn.prepareStatement(sql);
    }
    public void close() throws SQLException {
        this.used=false;
    }


}
