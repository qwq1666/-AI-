package baize.code.java.websocket.endpoint;

import baize.code.java.ai.service.AIService;
import baize.code.java.entity.Session;
import baize.code.java.entity.SessionLog;
import baize.code.java.mapper.SessionLogMapper;
import baize.code.java.service.SessionService;
import baize.code.java.utils.SessionFind;
import baize.code.java.websocket.message.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WebSocketEndpointCreationTest {
    @Test
    void tomcatCreatedEndpointsCanUseSpringDependencies() throws Exception {
        SessionService sessions = mock(SessionService.class);
        AIService ai = mock(AIService.class);
        SessionFind finder = mock(SessionFind.class);
        SessionLogMapper logs = mock(SessionLogMapper.class);

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(SessionService.class, () -> sessions);
            context.registerBean(AIService.class, () -> ai);
            context.registerBean(SessionFind.class, () -> finder);
            context.registerBean(SessionLogMapper.class, () -> logs);
            context.register(UserServiceEndpoint.class, CommercialTenantEndpoint.class);
            context.refresh();

            // Reproduce Tomcat's public no-argument construction, outside Spring.
            UserServiceEndpoint user = UserServiceEndpoint.class.getConstructor().newInstance();
            CommercialTenantEndpoint tenant = CommercialTenantEndpoint.class.getConstructor().newInstance();
            assertNotSame(context.getBean(UserServiceEndpoint.class), user);
            assertNotSame(context.getBean(CommercialTenantEndpoint.class), tenant);

            when(sessions.find(any(), any(), same(user))).thenReturn(Session.builder()
                    .id(1).conversationStatus(Session.ConversationStatus.HUMAN).build());
            user.onMessage(ChatMessage.builder().sessionId(1).message("用户问题")
                    .type(SessionLog.Type.ASSISTANT).build(), null);
            verify(logs).insert(argThat((SessionLog log) ->
                    log.getType() == SessionLog.Type.USER && "用户问题".equals(log.getContent())));
            verify(finder).getTenantEndpointById(1);

            tenant.onMessage(ChatMessage.builder().sessionId(1).message("客服回复").build(), null);
            verify(logs).insert(argThat((SessionLog log) ->
                    log.getType() == SessionLog.Type.COMMERCIAL_TENANT && "客服回复".equals(log.getContent())));
            verify(finder).getUserEndpointById(1);
        }
    }
}
