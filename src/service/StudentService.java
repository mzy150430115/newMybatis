package service;

import dao.StudentDao;
import domain.Student;
import orm.SqlSession;

import java.util.List;
import java.util.Map;

public class StudentService {

    //private StudentDao dao = new StudentDao();

    private StudentDao dao = new SqlSession().getMapper(StudentDao.class);

    //设计一个方法 新增一个学生（注册）
    public void regist(Map student){
        //写入数据库
        dao.insert(student);
    }

    //设计一个方法 删除一个学生（注销）
    public void delete(Integer sid){
        //删除 写数据库 dao支持
        dao.delete(sid);
    }

    //设计一个方法 修改学生的信息
    public void update(Student student){
        dao.update(student);
    }

    //设计一个方法 查询单个学生信息
    public Map selectOne(Integer sid) throws Exception {
        return dao.selectOne(sid);
    }

    //设计一个方法 多条查询
    public List<Student> selectList() throws Exception {
        return dao.selectList();
    }
}
