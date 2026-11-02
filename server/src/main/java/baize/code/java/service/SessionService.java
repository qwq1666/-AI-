package baize.code.java.service;

import com.baomidou.mybatisplus.extension.service.IService;
import baize.code.java.common.Result;
import baize.code.java.entity.Session;

import java.util.List;

public interface SessionService extends IService<Session> {

    Result<List<Session>> userGetLastSessionList(Integer userId);

    Result<List<Session>> ctGetLastSessionList(Integer ctId);
}