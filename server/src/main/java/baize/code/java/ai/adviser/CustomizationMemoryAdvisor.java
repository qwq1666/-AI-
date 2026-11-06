package baize.code.java.ai.adviser;

import baize.code.java.ai.memory.CustomizationMemory;
import baize.code.java.service.SessionService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CustomizationMemoryAdvisor implements BaseChatMemoryAdvisor {
    @Resource
    private CustomizationMemory customizationMemory;
    @Resource
    private SessionService sessionService;
    private final PromptTemplate systemPromptTemplate = new PromptTemplate("{instructions}\n\nUse the conversation memory" +
            " from the MEMORY section to provide accurate answers.\n\n---------------------\nMEMORY:\n{memory}\n---------------------\n\n");

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        Map<String, Object> context = chatClientRequest.context();
        String conversationId = getConversationId(chatClientRequest.context(), "default");
        if ("default".equals(conversationId)){
            throw new RuntimeException("会话ID不能为空");
        }
        //获取记忆
        List<Message> memoryMessage = customizationMemory.get(conversationId);
        //将记忆添加到提示词中
        String memory = memoryMessage.stream().filter((m) -> m.getMessageType()== MessageType.USER || m.getMessageType() ==
                MessageType.ASSISTANT).map((m)->{
            String identity = String.valueOf(m.getMessageType());
            return identity + ":" +m.getText();
        }).collect(Collectors.joining(System.lineSeparator()));
        SystemMessage systemMessage = chatClientRequest.prompt().getSystemMessage();
        String newSystemText = this.systemPromptTemplate.render(Map.of("instructions", systemMessage, "memory", memory));
        //复制一个聊天请求
        ChatClientRequest processChatClientRequest = chatClientRequest.mutate().prompt(chatClientRequest.prompt().augmentSystemMessage(newSystemText)).build();
        UserMessage userMessage = chatClientRequest.prompt().getUserMessage();
        this.customizationMemory.add(conversationId,userMessage);
        return processChatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        String conversationId = getConversationId(chatClientResponse.context(), "default");
        if ("default".equals(conversationId)){
            throw new RuntimeException("会话ID不能为空");
        }
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if (chatResponse==null){
            throw new RuntimeException("AI返回的消息为空");
        }
        StringBuilder content = new StringBuilder();
        for (Generation result : chatResponse.getResults()) {
            content.append(result.getOutput().getText());
        }
        AssistantMessage assistantMessage = new AssistantMessage(content.toString());
        this.customizationMemory.add(conversationId,assistantMessage);
        return chatClientResponse;
    }
    //整合执行流程
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        //先执行before方法
        ChatClientRequest precessedRequest = this.before(chatClientRequest, streamAdvisorChain);
        //继续下游的流式调用
        Flux<ChatClientResponse> chatClientResponseFlux = streamAdvisorChain.nextStream(precessedRequest);
        return new ChatClientMessageAggregator().aggregateChatClientResponse(
                chatClientResponseFlux,
                aggreatedResponse->{
                    this.after(aggreatedResponse,streamAdvisorChain);
                }
        );
    }
    @Override
    public int getOrder() {
        return 0;
    }
}
