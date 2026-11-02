package baize.code.java.service;

import com.baomidou.mybatisplus.extension.service.IService;
import baize.code.java.common.Result;
import baize.code.java.entity.SessionLog;

import java.util.List;

public interface SessionLogService extends IService<SessionLog> {


    Result<?> readCtMessage(Integer sessionId, Integer userId);

    Result<?> readUserMessage(Integer sessionId, Integer ctId);

    Result<List<SessionLog>> getWindowMessage(Integer sessionId);

    Result<Integer> userGetUnreadMessageCount(Integer sessionId);

    Result<Integer> ctGetUnreadMessageCount(Integer sessionId);
}