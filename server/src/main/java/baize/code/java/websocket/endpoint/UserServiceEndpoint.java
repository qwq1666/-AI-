package baize.code.java.websocket.endpoint;

import baize.code.java.ai.service.AIService;
import baize.code.java.config.ChatMessageCoder;
import baize.code.java.mapper.SessionLogMapper;
import baize.code.java.service.SessionService;
import baize.code.java.utils.SessionFind;
import baize.code.java.websocket.message.ChatMessage;
import baize.code.java.entity.SessionLog;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/user/chat/{userId}", decoders = ChatMessageCoder.class, encoders = ChatMessageCoder.class)
public class UserServiceEndpoint implements WebSocketEndpoint {
    private static final ConcurrentHashMap<Integer, UserServiceEndpoint> userEndpointPool = new ConcurrentHashMap<>();
    private final SessionService sessionService;
    private final AIService aIService;
    private final SessionFind sessionFind;
    private final SessionLogMapper sessionLogMapper;

    private Session session;
    private Integer userId;

    public UserServiceEndpoint(SessionService sessionService, AIService aIService, SessionFind sessionFind, SessionLogMapper sessionLogMapper) {
        this.sessionService = sessionService;
        this.aIService = aIService;
        this.sessionFind = sessionFind;
        this.sessionLogMapper = sessionLogMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        this.session = session;
        this.userId = userId;
        userEndpointPool.put(userId, this);
    }

    @OnClose
    public void onClose() {
        if (userId != null) {
            userEndpointPool.remove(userId);
        }
    }

    // TODO:005
    @OnMessage//用户发送消息触发
    public void onMessage(ChatMessage message, Session session) throws EncodeException, IOException, IllegalAccessException {
        //开启会话
        baize.code.java.entity.Session chatSession = sessionService.find(message, userId, this);
        switch (chatSession.getConversationStatus()){
            case AI->{
                aIService.turnToManualJudgement(chatSession,message);
                if (chatSession.getConversationStatus()== baize.code.java.entity.Session.ConversationStatus.HUMAN){
                    CommercialTenantEndpoint tenantEndpointById = sessionFind.getTenantEndpointById(message.getSessionId());
                    if (tenantEndpointById!=null){
                        tenantEndpointById.sendMessage(message);
                    }
                } else {
                    aIService.chat(chatSession,message,this);
                }
            }
            case HUMAN -> {
                sessionLogMapper.insert(SessionLog.builder()
                        .type(message.getType())
                        .sessionId(message.getSessionId())
                        .content(message.getMessage())
                        .build());
                CommercialTenantEndpoint tenantEndpoint = sessionFind.getTenantEndpointById(message.getSessionId());
                if (tenantEndpoint!=null){
                    tenantEndpoint.sendMessage(message);
                }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        try {
            error.printStackTrace();
            session.getBasicRemote().sendObject(ChatMessage.builder()
                    .state(ChatMessage.State.ERROR)
                    .message(error.getMessage())
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 封装发送消息的方法
     */
    public void sendMessage(ChatMessage chatMessage) throws EncodeException, IOException {
        this.session.getBasicRemote().sendObject(chatMessage);
    }

    @Override
    public SessionLog.Type getEndpointType() {
        return SessionLog.Type.USER;
    }

    public static UserServiceEndpoint findEndPoint(Integer userId) {
        return userEndpointPool.get(userId);
    }
}
