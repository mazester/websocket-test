package com.study.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.web.bind.annotation.RequestParam;

@ServerEndpoint("/websocket/{room}/{uid}/{name}")
public class websocketTest {

	private static final Set<websocketTest> connections = new CopyOnWriteArraySet<websocketTest>();
//	private Long room;
//	private Long uid;
//	private String name;
	private Session session;
	
	@OnOpen
	public void onOpen(Session session, 
			@PathParam(value = "room") Long room,
			@PathParam(value = "uid") Long uid,
			@PathParam(value = "name") String name) {
		this.session = session;
//		this.room = room;
//		this.uid = uid;
//		this.name = name;
		
		session.getUserProperties().put("name",name);
		session.getUserProperties().put("uid",uid);
		session.getUserProperties().put("room",room);
		
		connections.add(this);
		System.out.println("connect!!");
	}

	/**
     * 연결 끊기
     */
    @OnClose
    public void onClose() {
        connections.remove(this);
        String name = (String) session.getUserProperties().get("name");
        String message = String.format("System> %s, %s", name,
                " has disconnection.");
        websocketTest.broadCast(session, message);
    }

    /**
     * 수신 메시지
     * 
     * @param message
     * @param nickName
     */
    @OnMessage
    public void onMessage(String message) {
    	System.out.println("receive : " + message);
    	
    	
    	websocketTest.broadCast(session,  message);
    	try {
    		//session.getBasicRemote().sendText("asdfasfsaf");
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    /**
     * 오류 메시지 응답
     * 
     * @param throwable
     */
    @OnError
    public void onError(Throwable throwable) {
        System.out.println(throwable.getMessage());
    }
    
    /*
    private void broadCast(String message) {
		
		System.out.println("boardcast:" + message );
		
        for (websocketTest chat : connections) {
            try {
                synchronized (chat) {
                	System.out.println(">> "+room+ "," + chat.room);
                	if(chat.room==room)
                		chat.session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
            	System.out.println(">> " + e.toString());
            	e.printStackTrace();
                connections.remove(chat);
                try {
                    chat.session.close();
                } catch (IOException e1) {
                }
                broadCast(String.format("System> %s %s", chat.name,
                        " has bean disconnection."));
            }
        }
    }
    */
	
  
	private static void broadCast(Session session, String message) {
		
		System.out.println("boardcast:" + message );
		
		Long sendRoom = (Long)session.getUserProperties().get("room");
		String sendName = (String)session.getUserProperties().get("name");
		Long sendUid = (Long)session.getUserProperties().get("uid");
		
        for (websocketTest chat : connections) {
            try {
                synchronized (chat) {
                	Long recvRoom = (Long)chat.session.getUserProperties().get("room");
                	
                	if(recvRoom.intValue()==sendRoom.intValue()) {
                		chat.session.getBasicRemote().sendText(message);
                	}
                }
            } catch (IOException e) {
            	System.out.println(">> " + e.toString());
            	e.printStackTrace();
                connections.remove(chat);
                try {
                    chat.session.close();
                } catch (IOException e1) {
                }
                websocketTest.broadCast(session, String.format("System> %s %s", sendName,
                        " has bean disconnection."));
            }
        }
    }
	
	
  
}
