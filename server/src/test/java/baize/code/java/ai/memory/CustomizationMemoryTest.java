package baize.code.java.ai.memory;

import baize.code.java.entity.SessionLog;
import baize.code.java.service.SessionLogService;
import baize.code.java.utils.KeyUtils;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomizationMemoryTest {
    private CustomizationMemory memory;
    private SessionLogService logs;
    private ListOperations<String, String> list;
    private String key;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        memory = new CustomizationMemory();
        logs = mock(SessionLogService.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        list = mock(ListOperations.class);
        when(redis.opsForList()).thenReturn(list);
        ReflectionTestUtils.setField(memory, "stringRedisTemplate", redis);
        ReflectionTestUtils.setField(memory, "sessionLogService", logs);
        ReflectionTestUtils.setField(memory, "memoryKey", "memory");
        ReflectionTestUtils.setField(memory, "memoryLength", 10);
        key = KeyUtils.redisKeyUtils("memory", "1");
    }

    @Test
    void cacheHitReturnsMessagesInCacheOrderWithoutQueryingDatabase() {
        when(list.range(key, -10, -1)).thenReturn(List.of(
                json(SessionLog.Type.USER, "你好"),
                json(SessionLog.Type.ASSISTANT, "您好")));
        List<Message> messages = memory.get("1");
        assertEquals(List.of("你好", "您好"), messages.stream().map(Message::getText).toList());
        assertEquals(MessageType.USER, messages.get(0).getMessageType());
        assertEquals(MessageType.ASSISTANT, messages.get(1).getMessageType());
        verifyNoInteractions(logs);
    }

    @Test
    void newConversationReturnsEmptyList() {
        when(list.range(key, -10, -1)).thenReturn(List.of());
        when(logs.tryGetSessionLog("1")).thenReturn(List.of());
        assertNotNull(memory.get("1"));
        assertTrue(memory.get("1").isEmpty());
    }

    @Test
    void unsupportedToolHistoryDoesNotLeaveNullMessages() {
        when(list.range(key, -10, -1)).thenReturn(List.of(
                json(SessionLog.Type.TOOL, "工具结果"),
                json(SessionLog.Type.COMMERCIAL_TENANT, "人工回复")));
        List<Message> messages = memory.get("1");
        assertEquals(1, messages.size());
        assertEquals(MessageType.ASSISTANT, messages.get(0).getMessageType());
        assertEquals("人工回复", messages.get(0).getText());
    }

    private String json(SessionLog.Type type, String content) {
        return JSONUtil.toJsonStr(SessionLog.builder().type(type).content(content).build());
    }
}
