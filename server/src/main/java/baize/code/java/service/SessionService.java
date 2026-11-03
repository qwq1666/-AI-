package baize.code.java.service;

import baize.code.java.websocket.endpoint.UserServiceEndpoint;
import baize.code.java.websocket.message.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import baize.code.java.common.Result;
import baize.code.java.entity.Session;
import jakarta.websocket.EncodeException;

import java.io.IOException;
import java.util.List;

public interface SessionService extends IService<Session> {

    Result<List<Session>> userGetLastSessionList(Integer userId);

    Result<List<Session>> ctGetLastSessionList(Integer ctId);

    Session find(ChatMessage message, Integer userId, UserServiceEndpoint userServiceEndpoint) throws EncodeException, IOException;
}