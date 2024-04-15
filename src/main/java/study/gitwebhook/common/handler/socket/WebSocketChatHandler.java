package study.gitwebhook.common.handler.socket;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import study.gitwebhook.entity.websocket.ChatMessage;

/*
* WebSocket Handler 작성
* 소켓 통신은 서버와 클라이언트가 1:n으로 관계를 맺는다. 따라서 한 서버에 여러 클라이언트 접속 가능
* 서버에는 여러 클라이언트가 발송한 메시지를 받아 처리해줄 핸들러가 필요
* TextWebSocketHandler를 상속받아 핸들러 작성
* 클라이언트로 받은 메시지를 log로 출력하고 클라이언트로 환영 메시지를 보내줌
* */

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketChatHandler extends TextWebSocketHandler{
    
    private final ObjectMapper mapper;

    // 현재 연결된 세션들
    private final Set<WebSocketSession> sessions = new HashSet<>();

    // ChatRoomId: {session1, session2}
    private final Map<Long, Set<WebSocketSession>> chatRoomSessionMap = new HashMap<>();

    // 소켓 연결 확인
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("{} 연결됨", session.getId());
        sessions.add(session);
    }

    // 소켓 통신 시 메시지의 전송을 다루는 부분
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("payload {}", payload);

        // 페이로드 -> chatMessage로 변환
        ChatMessage chatMessage = mapper.readValue(payload, ChatMessage.class);
        log.info("session {}", chatMessage.toString());

        Long chatRoomId = chatMessage.getChatRoomId();

        // 메모리 상에 채팅방에 대한 세션이 없다면 만듦
        if(!chatRoomSessionMap.containsKey(chatRoomId)) 
            chatRoomSessionMap.put(chatRoomId, new HashSet<>());

        Set<WebSocketSession> chatRoomSession = chatRoomSessionMap.get(chatRoomId);

        // message에 담긴 타입을 확인.
        // 이 때 message에서 getType으로 가져온 내용이
        // ChatMessage의 열거형인 MessageType 안의 ENTER와 동일한 값이라면
        if(!chatMessage.getMessageType().equals(ChatMessage.MessageType.ENTER)) {
            chatRoomSession.add(session);
        }

        if(chatRoomSession.size() >= 3) {
            removeClosedSession(session);
        }

        sendMessageToChatRoom(chatMessage,chatRoomSession);
    }

    // 소켓 종료 확인
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("{} 연결 끊김", session.getId());
        sessions.remove(session);
    }

    private void removeClosedSession(Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.removeIf(session -> !sessions.contains(session));
    }

    private void sendMessageToChatRoom(ChatMessage chatMessage, Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.parallelStream().forEach(session -> sendMessage(session, chatMessage));
    }

    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
