package baize.code.java.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import baize.code.java.entity.SessionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SessionLogMapper extends BaseMapper<SessionLog> {
}