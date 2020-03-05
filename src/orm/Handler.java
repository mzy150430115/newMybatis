package orm;

import javax.sound.midi.MetaEventListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Handler {
    //处理SQL
    //处理SQL上的问号信息
    //处理不同类型的值 map domain

    //设计一个方法 用来解析SQL语句
    //现在改成默认修饰符（同包可以掉）
    SQLAndKey parseSQL(String sql) {
        //sql--->
        //1.将所有的#｛xxx｝存起来 作为key--以后和domain或者map中的key对应
        //2.将现在的sql替换回带？的形式
        StringBuilder newsql = new StringBuilder();//目的是为了存放之后新的sql语句
        List<String> keyList = new ArrayList<>();//目的是为了解析所有存放所有解析出来的key
        //解析
        while (true) {
            //找到"#｛｝"位置
            int left = sql.indexOf("#{");
            int right = sql.indexOf("}");

            if (left != -1 && right != -1 && left < right) {//判断成一组  获取key
                newsql.append(sql.substring(0, left));//原来sql左半部分存在新sql中
                newsql.append("?");//拼接问号
                String key = sql.substring(left + 2, right);
                keyList.add(key);
            } else {//没有成一组
                newsql.append(sql);
                break;
            }
            sql = sql.substring(right + 1);//原来sql的右半部分做再次拼接
        }
        //最终将解析完毕的两个结果包装成一个对象
        return new SQLAndKey(newsql, keyList);
    }
    //-----------------------------------------------------------------------
    //设计一个方法 负责给下面这个方法擦屁股的（map）
    private void setMap(PreparedStatement pstat, Object obj, List keyList) throws SQLException {
        Map map = (Map)obj;
        //将keyList解析出来的key遍历，去map寻找value value赋值后到sql上进行拼接
        for(int i=0;i<keyList.size();i++){
            String key = (String)keyList.get(i);//分析原来sql得到的#｛key｝
            Object value = map.get(key);//传递的那些值（为了sql）
            pstat.setObject(i+1,value);//拼接赋值的过程

        }
    }
    //设计一个方法 负责给下面这个方法擦屁股的（domain）
    private void setObject(PreparedStatement pstat,Object obj,List keyList) throws SQLException{
        //将keyList中解析的key遍历 反射去domain对象中寻找属性 获取value 赋值到list上
        //先获取obj对应的class
        try {
        Class clazz = obj.getClass();
        //遍历
        for(int i=0;i<keyList.size();i++) {
            //获取key
            String key = (String) keyList.get(i);
            //通过key反射找到domain中对应的属性
            Field field = clazz.getDeclaredField(key);
            String fieldName = field.getName();
            //获取属性对应的get方法
            String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            //反射找到属性对应的get方法
            Method getMethod = null;
            getMethod = clazz.getMethod(getMethodName);
            //执行get方法 获取属性值
            Object value = null;
            value = getMethod.invoke(obj);
            //让pstat把数据拼接完整
            pstat.setObject(i + 1, value);
        }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    //设计一个方法 负责分析obj对象模型，将sql和Object中的值拼接完整
    //首先需要参数 1.pstat 2.真正的值Object 3.分析后得到的keyList
    // obj是为了传递值的 可以传递一个基础类型的 也可以一个domain 可以一个map
     void handleParameter(PreparedStatement  pstat,Object obj,List keyList) throws SQLException {
        //先反射看一看obj是什么类型
        Class clazz = obj.getClass();
        //判断类型
        if(clazz==int.class || clazz==Integer.class){
            pstat.setInt(1,(Integer)obj);
        }else if(clazz==float.class || clazz==Float.class){
            pstat.setFloat(1,(Float)obj);
        }
        else if(clazz==double.class || clazz==Double.class){
            pstat.setDouble(1,(Double) obj);
        }
        else if(clazz==String.class){
            pstat.setString(1,(String) obj);
        }
        else if(clazz.isArray()){//数组
            //能力范围之外，按顺序执行吧
        }else {
            //不是基础类型的（基本类型，包装类，String）
            if(obj instanceof Map){//如果传递的obj属于map类型
                //将keyList解析出来的key遍历，去map寻找value
                this.setMap(pstat,obj,keyList);
            }else {//那就是domain啦，如果不是我也无能为力了
                //将keyList中解析的key遍历 反射去domain对象中寻找属性 获取value 赋值到list上
                this.setObject(pstat,obj,keyList);
            }
        }
    }




    //----------------------------------------------------------------------
    //设计一个小方法 负责给下面这个方法赋值 map
    private void getMap(ResultSet rs,Object result) throws SQLException {
        //创建一个map对象
        //获取结果集的全部信息存入map里
        ResultSetMetaData metaData = rs.getMetaData();//获取结果集中的全部信息（列名和值）
        //遍历 从1开始
        for(int i=1;i<= metaData.getColumnCount();i++){
            String columnName =  metaData.getColumnName(i);
            //结果集取值
            Object value = rs.getObject(columnName);
            //获取结果集
            ((Map)result).put(columnName,value);

        }
    }
    //设计一个小方法 负责给下面这个方法赋值 domain
    private void  getObject(ResultSet rs,Object result) throws SQLException {

            try {
                Class clazz =result.getClass();//对象对应的类
                //获取结果集所有信息
                ResultSetMetaData metaData = rs.getMetaData();
                //将结果的信息遍历
                for(int i=1;i<=metaData.getColumnCount();i++){
                    //获取每一个列名字
                    String colunmName = metaData.getColumnName(i);
                    //获取结果集的值
                    Object value = rs.getObject(colunmName);
                    //存放在domain对象对应的属性里
                    //通过列名找属性
                  Field field = clazz.getDeclaredField(colunmName);
                  //获取属性名字
                    String fieldName = field.getName();
                    //根据属性名字拼接出对应的set方法名
                    String setMethodName = "set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);
                    //找到set方法
                    Method setMethod = clazz.getMethod(setMethodName,field.getType());
                    //执行set方法 给属性赋值
                    setMethod.invoke(result,value);
            }
        } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

    }
    //设计一个方法 负责分析给定Class类型 确定返回值是什么类型 进行组装
    //将结果集的值拆分出来 存入新的容器中
    //参数：Class
    <T>T handleResult(ResultSet rs,Class resultType) throws SQLException{
        Object result = null;
        if(resultType==int.class || resultType==Integer.class){
            result = rs.getInt(1);
        }else if(resultType==float.class || resultType==Float.class){
            result = rs.getFloat(1);
        }else if(resultType==double.class || resultType==Double.class){
            result = rs.getDouble(1);
        }else if(resultType==String.class){
            result = rs.getString(1);
        }else{
            try {
                result = resultType.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if(resultType == HashMap.class){//map
                //将结果集的信息一个个存入Map
               this.getMap(rs,result);
            }else {//domain
                //存入domain
                this.getObject(rs,result);
            }
        }
        return (T)result;
    }

}
