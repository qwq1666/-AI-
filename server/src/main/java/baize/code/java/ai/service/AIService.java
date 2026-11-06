package baize.code.java.ai.service;

import baize.code.java.entity.Session;
import baize.code.java.websocket.endpoint.UserServiceEndpoint;
import baize.code.java.websocket.message.ChatMessage;
import jakarta.websocket.EncodeException;

import java.io.IOException;

public interface AIService {
    void turnToManualJudgement(Session chatSession, ChatMessage message);

    void chat(Session chatSession, ChatMessage message, UserServiceEndpoint userServiceEndpoint) throws IllegalAccessException, EncodeException, IOException;
}
