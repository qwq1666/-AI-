package baize.code.java.websocket.endpoint;

import baize.code.java.config.ChatMessageCoder;
import baize.code.java.entity.SessionLog;
import baize.code.java.websocket.message.ChatMessage;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

// TODO:006
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
@Component
@ServerEndpoint(value = "/commercialTenant/chat/{ctId}",decoders = ChatMessageCoder.class, encoders = ChatMessageCoder.class)
public class CommercialTenantEndpoint implements WebSocketEndpoint {
    private static final ConcurrentHashMap<Integer, CommercialTenantEndpoint> commercialTenantEndpointConcurrentEndpointPool = new ConcurrentHashMap<>();

    private Session session;
    private Integer ctId;

    @OnOpen
    public void onOpen(Session session, @PathParam("ctId") Integer ctId) {
        this.session = session;
        this.ctId = ctId;
        commercialTenantEndpointConcurrentEndpointPool.put(ctId, this);
    }

    @OnClose
    public void onClose() {
        if (ctId != null) {
            commercialTenantEndpointConcurrentEndpointPool.remove(ctId);
        }
    }

    @OnMessage
    public void onMessage(ChatMessage message, Session session) throws EncodeException, IOException {

    }

    @OnError
    public void onError(Session session, Throwable error) {
        try {
            error.printStackTrace();
            session.getBasicRemote().sendObject(ChatMessage.builder()
                    .state(ChatMessage.State.ERROR)
                    .message(error.getMessage())
                    .build()
            );
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
        return SessionLog.Type.COMMERCIAL_TENANT;
    }

    public static CommercialTenantEndpoint findEndPoint(Integer userId) {
        return commercialTenantEndpointConcurrentEndpointPool.get(userId);
    }
}
