package baize.code.java.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import baize.code.java.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}