package com.cts.mfrp.au.handler;

import com.cts.mfrp.au.exception.AuctionIllegalStateException;
import com.cts.mfrp.au.exception.AuctionNotFoundException;
import com.cts.mfrp.au.exception.LowBidException;
import com.cts.mfrp.au.exception.UserNotFoundException;
import com.cts.mfrp.au.model.Auction;
import com.cts.mfrp.au.model.Bid;
import com.cts.mfrp.au.model.BidExt;
import com.cts.mfrp.au.model.User;
import com.cts.mfrp.au.service.*;
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
    @Autowired
    private WalletService walletService;
    @Setter
    @Getter
    private float curBid;
    private int prevUserId;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("Connected: " + session.getId());
    }


    //    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message)throws Exception {
//        BidExt bidExt = objectMapper.readValue(message.getPayload(), BidExt.class);
//        System.out.println("Received employee: " + bidExt.getBidderId());
//        broadcast(bidExt);
//    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try{
            BidExt bid = objectMapper.readValue(message.getPayload(), BidExt.class);
            Auction auction = auctionService.findById(bid.getAuctionId());
            User u = userService.findById(bid.getBidderId());
            if (u == null) throw new UserNotFoundException("Not a valid user.");
            if(auction==null) throw new AuctionNotFoundException("Auction not found.");
            if (!"LIVE".equals(auction.getStatus())) throw new AuctionIllegalStateException("Auction is not live.");
            if (walletService.getBalance(bid.getBidderId()).getAvailableBalance() < bid.getAmount()) throw new LowBidException("Bid Amount Should not be greater than your wallet balance.");
            if (curBid >= bid.getAmount()) throw new LowBidException("Bid Amount Should be greater than current Bid.");

            if (prevUserId != 0) {
                walletService.unfreezeBal(prevUserId, curBid);
                walletService.freezeBal(bid.getBidderId(), bid.getAmount());
            }
            auctionService.setHighestBidder(auction.getAuctionId(), u);
            curBid = bid.getAmount();
            Bid x = new Bid();
            x.setBidAmount(curBid);
            x.setBidder(userService.findById(bid.getBidderId()));
            x.setAuction(auction);
            bidService.insertBid(x);

            bidTimerService.resetTimer(bid.getAuctionId());

            String json = objectMapper.writeValueAsString(bid);
            TextMessage textMessage = new TextMessage(json);
            broadcast(textMessage);
            prevUserId = bid.getBidderId();
        }
        catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
        catch (Exception ex){
            sendError(session, ex.getMessage());
        }
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


    private void sendError(WebSocketSession session, String message) throws Exception {

        String json = """
                {
                  "type": "ERROR",
                  "message": "%s",
                  "timestamp": "%s"
                }
                """.formatted(message, Instant.now());
        TextMessage msg = new TextMessage(json);
        session.sendMessage(msg);

    }
}
