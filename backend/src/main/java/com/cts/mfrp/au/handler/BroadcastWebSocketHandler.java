package com.cts.mfrp.au.handler;

import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.model.BidExt;
import com.cts.mfrp.au.repository.AuctionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BroadcastWebSocketHandler extends TextWebSocketHandler {
    private static final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private AuctionRepository auctionRepository;
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("Connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {

        BidExt bid =
                objectMapper.readValue(message.getPayload(), BidExt.class);

        Auction auction = auctionRepository.findById(bid.getAuctionId())
                .orElseThrow();

        if (!"LIVE".equals(auction.getStatus())) {
            session.sendMessage(new TextMessage("""
            {
              "type": "ERROR",
              "message": "Auction is not live"
            }
            """));
            return;
        }

        // process bid safely
        broadcast(bid);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("Disconnected: " + session.getId());
    }

    private void broadcast(BidExt bidExt) throws Exception {
        String json = objectMapper.writeValueAsString(bidExt);
        TextMessage textMessage = new TextMessage(json);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(textMessage);
            }
        }
    }

    public void broadcastSystemEvent(String type, int auctionId) throws Exception {

        String json = """
        {
          "type": "%s",
          "auctionId": %d,
          "timestamp": "%s"
        }
        """.formatted(type, auctionId, Instant.now());

        TextMessage message = new TextMessage(json);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(message);
            }
        }
    }
}
