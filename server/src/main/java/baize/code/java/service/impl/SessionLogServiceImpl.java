package baize.code.java.service.impl;

import baize.code.java.service.SessionLogService;
import baize.code.java.utils.KeyUtils;
import baize.code.java.utils.TypeConversion;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import baize.code.java.code.ResultCode;
import baize.code.java.common.Result;
import baize.code.java.entity.Session;
import baize.code.java.entity.SessionLog;
import baize.code.java.mapper.SessionLogMapper;
import baize.code.java.mapper.SessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SessionLogServiceImpl extends ServiceImpl<SessionLogMapper, SessionLog> implements SessionLogService {
    private final StringRedisTemplate stringRedisTemplate;
    private final SessionMapper sessionMapper;
    @Value("${memory.redis-length}")
    private Integer memoryLength;
    @Value("${memory.expiration-duration}")
    private Integer timeout;
    @Value("${memory.key}")
    private String memoryKey;


    @Override
    public Result<?> readCtMessage(Integer sessionId, Integer userId) {
        // 检查是否有该会话
        Session session = sessionMapper.selectOne(new LambdaQueryWrapper<>(Session.class)
                .eq(Session::getId, sessionId)
                .eq(Session::getUserId, userId)
        );
        if (session == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        // 修改这个session的关于商户或者AI发送的消息
        lambdaUpdate().set(SessionLog::getReadStatus, SessionLog.ReadStatus.READ)
                .eq(SessionLog::getSessionId, sessionId)
                .eq(SessionLog::getType, SessionLog.Type.ASSISTANT)
                .or()
                .eq(SessionLog::getType, SessionLog.Type.COMMERCIAL_TENANT)
                .update();
        return Result.success(ResultCode.UPDATE_SUCCESS);
    }

    @Override
    public Result<?> readUserMessage(Integer sessionId, Integer ctId) {
        // 检查是否有该会话
        Session session = sessionMapper.selectOne(new LambdaQueryWrapper<>(Session.class)
                .eq(Session::getId, sessionId)
                .eq(Session::getCtId, ctId)
        );
        if (session == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        // 修改这个session的关于商户或者AI发送的消息
        lambdaUpdate().set(SessionLog::getReadStatus, SessionLog.ReadStatus.READ)
                .eq(SessionLog::getSessionId, sessionId)
                .eq(SessionLog::getType, SessionLog.Type.USER)
                .update();
        return Result.success(ResultCode.UPDATE_SUCCESS);
    }

    @Override
    public Result<List<SessionLog>> getWindowMessage(Integer sessionId) {
        return Result.success(
                ResultCode.GET_SUCCESS,
                lambdaQuery().eq(SessionLog::getSessionId, sessionId)
                        .orderByAsc(SessionLog::getTimestamp)
                        .list()
        );
    }

    @Override
    public Result<Integer> userGetUnreadMessageCount(Integer sessionId) {
        return Result.success(
                ResultCode.GET_SUCCESS,
                lambdaQuery().eq(SessionLog::getSessionId, sessionId)
                        .eq(SessionLog::getType, SessionLog.Type.COMMERCIAL_TENANT)
                        .or()
                        .eq(SessionLog::getType, SessionLog.Type.ASSISTANT)
                        .count().intValue()
        );
    }

    @Override
    public Result<Integer> ctGetUnreadMessageCount(Integer sessionId) {
        return Result.success(
                ResultCode.GET_SUCCESS,
                lambdaQuery().eq(SessionLog::getSessionId, sessionId)
                        .eq(SessionLog::getType, SessionLog.Type.USER)
                        .count().intValue()
        );
    }

    @Override
    public List<SessionLog> tryGetSessionLog(String conversationId) {
        //  从数据库中获取最近的10条记录 【分页查询】
        return lambdaQuery().eq(SessionLog::getSessionId, conversationId)
                .orderByDesc(SessionLog::getTimestamp)
                .page(new Page<>(1, memoryLength))
                .getRecords();
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        //转换为SessionLog
        List<SessionLog> sessionLogList = messages.stream().map(message -> {
            SessionLog.Type type = TypeConversion.messageToSessionType(message.getMessageType());
            return SessionLog.builder()
                    .sessionId(Integer.valueOf(conversationId))
                    .content(message.getText())
                    .type(type)
                    .build();
        }).toList();
        saveBatch(sessionLogList);
        addToRedis(conversationId,sessionLogList);
    }

    @Override
    public void addToRedis(String conversationId, List<SessionLog> sessionLogs) {
        if (sessionLogs==null || sessionLogs.isEmpty()){
            return;
        }
        stringRedisTemplate.opsForList().rightPushAll(
                KeyUtils.redisKeyUtils(memoryKey,conversationId),
                sessionLogs.stream().map(JSONUtil::toJsonStr).toList()
        );
        stringRedisTemplate.expire(KeyUtils.redisKeyUtils(memoryKey,conversationId),timeout, TimeUnit.MINUTES);
        //检查redis中list的长度 如果超过长度则将旧数据删除
        if(Optional.ofNullable(stringRedisTemplate.opsForList().size(KeyUtils.redisKeyUtils(memoryKey,conversationId)))
                .orElse(0L) > memoryLength){
            stringRedisTemplate.opsForList().trim(KeyUtils.redisKeyUtils(memoryKey,conversationId),-memoryLength,-1);
        }
    }
}