package baize.code.java.utils;

import baize.code.java.entity.Session;
import baize.code.java.mapper.SessionMapper;
import baize.code.java.websocket.endpoint.CommercialTenantEndpoint;
import baize.code.java.websocket.endpoint.UserServiceEndpoint;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.apache.tomcat.Jar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SessionFind {
    @Value("${session.key}")
    private String key;
    @Value("${session.expiration-duration}")
    private Integer timeout;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SessionMapper sessionMapper;

    public Session getSessionById(Integer sessionId){
        //通过redis查找
        String json = stringRedisTemplate.opsForValue().getAndExpire(KeyUtils.redisKeyUtils(key, sessionId.toString()), timeout, TimeUnit.MILLISECONDS);
        Session session = null;
        if(json == null){
            //没有则数据库中查找
             session = sessionMapper.selectById(sessionId);
            if(session==null){
                throw new RuntimeException("未查询到指定的对话");
            }
            stringRedisTemplate.opsForValue().set(KeyUtils.redisKeyUtils(key,sessionId.toString()), JSONUtil.toJsonStr(session), timeout, TimeUnit.MILLISECONDS);
        }else{
            session = JSONUtil.toBean(json, Session.class);
        }
        return session;
    }

    public UserServiceEndpoint getUserEndpointById(Integer sessionId){
        Session session = getSessionById(sessionId);
        return UserServiceEndpoint.findEndPoint(session.getUserId());
    }

    public CommercialTenantEndpoint getTenantEndpointById(Integer sessionId){
        Session session = getSessionById(sessionId);
        return CommercialTenantEndpoint.findEndPoint(session.getCtId());
    }
}
