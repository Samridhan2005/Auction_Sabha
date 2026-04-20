package com.cts.mfrp.au.handler;

import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.model.Bid;
import com.cts.mfrp.au.model.BidExt;
import com.cts.mfrp.au.service.AuctionService;
import com.cts.mfrp.au.service.BidService;
import com.cts.mfrp.au.service.BidTimerService;
import com.cts.mfrp.au.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
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
    private UserService userService;
    @Autowired
    private BidTimerService bidTimerService;
    @Autowired
    private AuctionService auctionService;
    @Autowired
    private BidService bidService;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("Connected: " + session.getId());
    }

    @Setter
    @Getter
    private double curBid;

    //    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message)throws Exception {
//        BidExt bidExt = objectMapper.readValue(message.getPayload(), BidExt.class);
//        System.out.println("Received employee: " + bidExt.getBidderId());
//        broadcast(bidExt);
//    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        BidExt bid = objectMapper.readValue(message.getPayload(), BidExt.class);
        Auction auction = auctionService.findById(bid.getAuctionId());

        if (!"LIVE".equals(auction.getStatus())) {
            session.sendMessage(new TextMessage("""
            {
              "type": "ERROR",
              "message": "Auction is not live"
            }
            """));
            return;
        }

        if(curBid>=bid.getAmount()){
            session.sendMessage(new TextMessage("""
            {
              "type": "ERROR",
              "message": "Bid Amount Should be greater than current Bid."
            }
            """));
            return;
        }
        curBid=bid.getAmount();
//        Bid x=new Bid();
//        x.setBidAmount((float)curBid);
//        x.setBidder(userService.findById(bid.getBidderId()));
//        x.setAuction(auction);
//        bidService.insertBid(x);


        bidTimerService.resetTimer(bid.getAuctionId());

        String json = objectMapper.writeValueAsString(bid);
        TextMessage textMessage = new TextMessage(json);
        broadcast(textMessage);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("Disconnected: " + session.getId());
    }

    private void broadcast(TextMessage textMessage) throws Exception {

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

        broadcast(message);
    }
}
