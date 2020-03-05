package test;

import pool.ConnectionPool;
import pool.MyConnection;

import java.sql.Connection;

public class TestThread extends Thread{

    public void run(){
        //1.获取一个连接池对象
        ConnectionPool connectionPool = ConnectionPool.getInstance();
        //2.获取连接
        Connection mc = connectionPool.getConnection();
        System.out.println(mc);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("用完啦");
//        mc.setUsed(false);
    }
}
