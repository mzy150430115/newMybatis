package pool;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {

    //连接池对象设计成单例模式
    //1.私有构造方法
    private ConnectionPool(){ }
    //2.需要在当前类的内部自己创建一个对象  new ConnectionPool();
    // 属性(get!) 构造方法(已经私有了 不行)  块  (块不行，对象的方法拿不出来)
//    private static ConnectionPool connectionPool = new ConnectionPool();//饿汉式
//    //3.设计一个方法 用来返回当前的对象
//    public static ConnectionPool getInstance(){
//        return connectionPool;
//    }
    private static ConnectionPool connectionPool;//懒汉式
     public static  ConnectionPool getInstance() {//这里加锁性能会变慢
         if (connectionPool == null) {
             synchronized (ConnectionPool.class) {
                 if (connectionPool == null) {
                     connectionPool = new ConnectionPool();
                 }
             }
         }
         return connectionPool;
     }
         //-------------------------------------------
         //管理connection 连接池
         private List<MyConnection> poolList = new ArrayList();
        private static int minConnectionCount = Integer.parseInt(ConfigReader.getValue("minConnectionCount"));

         //先给池子里来十个链接
         {
             for (int i = 0; i < minConnectionCount; i++) {
                 poolList.add(new MyConnection());
             }
         }
         //设计一个方法 给用户使用 获取一个链接
         public synchronized MyConnection getMC () {
             MyConnection result = null;
             //从连接池中寻找一个可用链接
             for (int i = 0; i < poolList.size(); i++) {
                 MyConnection mc = (MyConnection)poolList.get(i);
                 if (!mc.isUsed()) {
                     mc.setUsed(true);
                     result = mc;
                     break;
                 }
             }
             return result;
         }
         //设计一个方法 排队等待的机制
         public MyConnection getConnection(){
             int count = 0;
             MyConnection mc = this.getMC();
             while (mc==null && count<Integer.parseInt (ConfigReader.getValue("waittime"))*10){
                 mc = this.getMC();
                 try {
                     Thread.sleep(100);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 count++;
             }
             //跳出循环 2中可能 1找到啦 2时间超过五秒啦
             if(mc==null){
                 //自定义异常 输出
             }return mc;
         }

     }