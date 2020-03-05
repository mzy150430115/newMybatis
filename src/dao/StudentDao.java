package dao;

import domain.Student;
import orm.annotation.Delete;
import orm.annotation.Insert;
import orm.annotation.Select;
import orm.annotation.Update;
import java.util.List;
import java.util.Map;


@SuppressWarnings("all")
public interface StudentDao {
    //负责数据库的读写

    //属性sqlseesion
    //private SqlSession sqlSession = new SqlSession();

    //DAO
    //添加记录
    @Insert("insert into student values(#{sid},#{sname},#{ssex},#{sage})")
    public void insert(Map student);

    //删除记录
    @Delete("delete from student where sid = #{sid}")
    public void delete(Integer sid);

    //修改
    @Update("update student set sname=#{sname},ssex=#{ssex},ssage=#{sage} where sid=#{sid}")
    public void update(Student student);

    //查询
    @Select("select * from student where sid = #{sid}")
    public Map selectOne(Integer sid) throws Exception;//那返回类型咋办呢
                                                        //不用传啦 看方法本身的返回类型

    //多条查询
    @Select("select * from student")
    public List<Student> selectList() throws Exception;
}

