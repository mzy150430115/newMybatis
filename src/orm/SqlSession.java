package orm;

import orm.annotation.Delete;
import orm.annotation.Insert;
import orm.annotation.Select;
import orm.annotation.Update;
import pool.ConnectionPool;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class SqlSession {
    //这个类负责做数据库的读写操作
    //

    //存放一个Handler属性 处理SQL 参数 返回值等等
    private Handler handler = new Handler();

    //设计一个方法 可以处理任意一个表格的增删改
    public void update(String sql, Object obj) {
        try {
            //0.解析sql语句
            SQLAndKey sqlAndKey = handler.parseSQL(sql);
            //1.获取连接
            Connection conn = ConnectionPool.getInstance().getConnection();
            //2.获取状态参数（sql）
            PreparedStatement pstat = conn.prepareStatement(sqlAndKey.toString());
            //3.将sql和问号信息拼接完整
            if (obj != null) {
                handler.handleParameter(pstat, obj, sqlAndKey.getKeyList());
            }
            //4.执行executeUpdate()
            pstat.executeUpdate();
            //5.关闭
            pstat.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert(String sql, Object obj) {
        this.update(sql, obj);
    }

    public void delete(String sql, Object obj) {
        this.update(sql, obj);
    }

    //-------------------------------------------------------
    public void update(String sql) {
        this.update(sql, null);
    }

    public void insert(String sql) {
        this.insert(sql, null);
    }

    public void delete(String sql) {
        this.delete(sql, null);
    }

    //--------------------------------------------------------

    //设计一个方法 可以处理任何一个表格的单挑查询操作
    //需要参数 1.String sql 2.SQL上的信息 3.告知一个返回值
    public  <T>T selectOne(String sql,Object obj,Class resultType) throws Exception{
        return (T)this.selectList(sql, obj, resultType).get(0);
    }
    public  <T>T selectOne(String sql,Class resultType) throws Exception{
        return (T)this.selectList(sql, null, resultType).get(0);
    }

    public  <T> List<T> selectList(String sql, Object obj, Class resultType) throws Exception{
        List<T> result =  new ArrayList();
        //1.解析SQL
        SQLAndKey sqlAndKey = handler.parseSQL(sql);
        //2.创建连接
        Connection conn = ConnectionPool.getInstance().getConnection();
        //3.状态参数
        PreparedStatement pstat = conn.prepareStatement(sqlAndKey.getNewsql());
        //4.将SQL和提供的obj拼接完整
        if(obj!=null){
            handler.handleParameter(pstat,obj,sqlAndKey.getKeyList());
        }
        //5.执行操作       ResultSet =
        ResultSet rs = pstat.executeQuery();
        //6.将结果集的数据拆开来 重新存入一个容器(domain map String int)
        while (rs.next()){
            //存入一个容器
            result.add((T)handler.handleResult(rs,resultType));
        }
        //7.关闭
        rs.close();
        conn.close();
        pstat.close();
        //将新的容器返回
        return result;
    }
    public  <T> List<T> selectList(String sql,Class resultType) throws Exception{
        return this.selectList(sql,null,resultType);
    }

    //-------------------------------------------------------
    //设计一个方法 根据给我的类型 创建一个代理对象
    //参数是一个Class（必须是接口）
    //返回值是对象（Class类型的子类）代理
    public <T>T getMapper(Class clazz){//DAO接口
//        //创建代理对象
//        //1.需要一个类加载器ClassLoader 将clazz加载
//        ClassLoader loader = clazz.getClassLoader();
//        //2.需要一个Class[] 代理的接口是谁 通常数组就一个长度
//        Class[] interfaces =  new Class[]{clazz};
//        //3.需要一个InvocationHandler 需要代理做哪个执行的方法
//        InvocationHandler h = new ProxyHanlderImpl();
//        Object obj = Proxy.newProxyInstance(loader,interfaces,h);
        return(T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler(){
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //这个是匿名内部类
                //调用sqlsession类中的方法

                //1.获取上面的注解
                Annotation an = method.getAnnotations()[0];
                //2.分析注解类型--为了根据注解类型调用对应的方法
                Class type = an.annotationType();
                //3.获取注解中的SQL语句
                Method valueMethod = type.getDeclaredMethod("value");
                //4.执行注解的方法 获取sql
                String sql = (String)valueMethod.invoke(an);
                //5.分析参数（要么没有 要么一个）
                Object param = (args==null) ? null : args[0];
//                if(args==null){
//                    param = null;
//                }else {
//                    param = args[0];
//                }
                //6.根据注解类型判断该执行哪种操作
                if(type==Insert.class){
                    SqlSession.this.insert(sql,param);
                }else if(type== Delete.class){
                    SqlSession.this.delete(sql,param);
                }else if(type== Update.class){
                    SqlSession.this.delete(sql,param);
                }else if(type== Select.class){//里面有返回值了
                    Class methodReturnTypeClass = method.getReturnType();
                    if(methodReturnTypeClass==List.class){//多条查询
                        //多条查询比较麻烦
                        //需要获取List集合中的泛型
                        Type returnType = method.getGenericReturnType();//获取返回值的具体类型（java.util.list）
                        //Type是个父接口 好多类型都实现了它
                        //现在上面那一行的代码是个多态
                        ParameterizedType realReturnType =(ParameterizedType)returnType;
                        //继续反射这个类型中的所有泛型
                        Type[] patternTypes = realReturnType.getActualTypeArguments();//获取所有的泛型
                        Type patternType = patternTypes[0];
                        //将这个泛型类还原回Class
                        Class realPatternType  = (Class)patternType;
                        //执行查询多条操作
                        return SqlSession.this.selectList(sql,param,realPatternType);
                    }else {
                        return SqlSession.this.selectOne(sql,param,methodReturnTypeClass);
                    }
                }
                    return null;
            }
        });
    }

    //一个私有内部类 是InvocationHandler的具体实现类
//    private class ProxyHanlderImpl implements InvocationHandler{
//
//    }
}
