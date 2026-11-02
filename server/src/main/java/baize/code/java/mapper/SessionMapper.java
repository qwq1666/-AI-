package baize.code.java.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import baize.code.java.entity.Session;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SessionMapper extends BaseMapper<Session> {
}