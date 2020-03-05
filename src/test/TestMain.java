package test;


import pool.ConnectionPool;

import java.sql.*;

public class TestMain {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        //  如果当前有11个线程同时执行上面获取连接的方法 会如何？？？
//        TestThread tt1 = new TestThread();
//        tt1.start();
//        TestThread tt2 = new TestThread();
//        tt2.start();
//        TestThread tt3 = new TestThread();
//        tt3.start();
//        TestThread tt4 = new TestThread();
//        tt4.start();
//        TestThread tt5 = new TestThread();
//        tt5.start();
//        TestThread tt6 = new TestThread();
//        tt6.start();


//        //1.原来的方式也是多态
//        Class.forName("com.mysql.jdbc.Driver");
//        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testmybatis?useSSL=false","root","123456");
//        //父类Connection的引用  指向一个子类对象JDBC4Connection
//        //  原来的子类是谁？    反射
//        Class clazz = conn.getClass();
//        System.out.println(clazz.getName());
//        //原来的真实子类
//        //com.mysql.jdbc.JDBC4Connection


        //经过了一顿猛如虎的修改
        //0.配置文件configuration.properties(目的 driver url username password)
        //1.创建一个连接池对象
        //2.获取连接
        Connection conn = ConnectionPool.getInstance().getConnection();
        //3.状态参数
        PreparedStatement pstat = conn.prepareStatement("");
        //4.执行操作
        ResultSet rs = pstat.executeQuery();
        //5.关闭
        conn.close();//MyConnection 子类的close
        //MyConnection子类的独有方法
        //多态的效果  没办法调用到子类独有的方法      造型--->MyConnection
        //conn.setUsed(false);//用连接池的方式 释放









        //1.导包--需要
        //2.加载驱动？？？ 不用
        //3.获取连接
//        long t1 = System.currentTimeMillis();
//        ConnectionPool connectionPool = new ConnectionPool();
//        MyConnection myConnection = connectionPool.getMC();
//        Connection conn = myConnection.getConn();
//        long t2 = System.currentTimeMillis();
//        //4.状态参数
//        PreparedStatement pstat = conn.prepareStatement("select * from emp");
//        //5.执行操作
//        ResultSet rs = pstat.executeQuery();
//        //6.关闭
//        rs.close();
//        pstat.close();
//        myConnection.setUsed(false);//释放
//        System.out.println(t2-t1);
//
//        System.out.println("==================================");
//        long t3 = System.currentTimeMillis();
//        Connection conn2 = connectionPool.getMC().getConn();
//        long t4 = System.currentTimeMillis();
//        System.out.println(t4-t3);








//        try {
//            //JDBC六部曲
//            //1.导包
//            //2.加载驱动
//            String driver = "com.mysql.jdbc.Driver";
//            String url = "jdbc:mysql://localhost:3306/testmybatis?useSSL=false";
//            String user = "root";
//            String password = "123456";
//            String sql = "select  * from emp";
//            Class.forName(driver);
//            //3.获取连接
//            long t1 = System.currentTimeMillis();
//            Connection conn = DriverManager.getConnection(url,user,password);
//            long t2 = System.currentTimeMillis();
//            //4.状态参数(流)
//            //Statement stat = conn.createStatement();  拼接字符串 SQL注入
//            PreparedStatement pstat = conn.prepareStatement(sql);
//            //5.执行操作
//            //      写操作(增删改)    int = executeUpdate();
//            //      读操作(查询)      ResultSet = executeQuery();
//            ResultSet rs = pstat.executeQuery();
//            while(rs.next()){
//                System.out.println(rs.getInt("empno")+"--"+rs.getString("ename")+"--"+rs.getString("job")+"--"+rs.getFloat("sal"));
//            }
//            long t3 = System.currentTimeMillis();
//            System.out.println(t2-t1);
//            System.out.println(t3-t2);
//            //6.关闭
//            rs.close();
//            pstat.close();
//            conn.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
}
