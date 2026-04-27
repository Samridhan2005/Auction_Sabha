package com.cts.mfrp.au;

import com.cts.mfrp.au.handler.BroadcastWebSocketHandler;
import com.cts.mfrp.au.service.BidTimerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class AuctionSabhaApplication {
	public static void main(String[] args) {
        ApplicationContext con= SpringApplication.run(AuctionSabhaApplication.class, args);
        BidTimerService bt=con.getBean(BidTimerService.class);
        BroadcastWebSocketHandler bw=con.getBean(BroadcastWebSocketHandler.class);
        bt.setBroadcastWebSocketHandler(bw);
	}
}