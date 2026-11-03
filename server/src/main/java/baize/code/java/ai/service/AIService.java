package baize.code.java.ai.service;

import baize.code.java.entity.Session;
import baize.code.java.websocket.message.ChatMessage;

public interface AIService {
    void turnToManualJudgement(Session chatSession, ChatMessage message);
}
