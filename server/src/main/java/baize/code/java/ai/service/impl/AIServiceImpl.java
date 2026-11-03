package baize.code.java.ai.service.impl;

import baize.code.java.ai.service.AIService;
import baize.code.java.entity.Session;
import baize.code.java.mapper.SessionMapper;
import baize.code.java.utils.KeyUtils;
import baize.code.java.websocket.message.ChatMessage;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private SessionMapper sessionMapper;
    @Value("${session.key}")
    private String sessionKey;
    @Value("${session.expiration-duration}")
    private long sessionExpireTime;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("classpath:template/convert-to-manual-judgment-prompts.st")
    private Resource contextResource;//转人工的提示词资源
    @Override
    public void turnToManualJudgement(Session chatSession, ChatMessage message) {
        //添加转人工的判断逻辑
        String result = chatClient.prompt()
                .system(contextResource)
                .user(message.getMessage())
                .call()
                .content();
        //需要ai返回true或者false
        //将AI的结果转为Boolean类型
        boolean needTransfer = false;
        if(result!=null){
            needTransfer = Boolean.parseBoolean(result);
        }
        //如果需要转人工 改变会话状态中对话状态为HUMAN
        if(needTransfer){
            chatSession.setConversationStatus(Session.ConversationStatus.HUMAN);
            //将转人工的对话状态跟新在数据库中
            sessionMapper.updateById(chatSession);
            //将状态更新到redis中
            stringRedisTemplate.opsForValue().set(
                    KeyUtils.redisKeyUtils(sessionKey,chatSession.getId()),
                    JSONUtil.toJsonStr(chatSession),
                    sessionExpireTime,
                    TimeUnit.MINUTES
            );
        }
    }
}
