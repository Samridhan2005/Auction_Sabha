package com.cts.mfrp.au;

import com.cts.mfrp.au.handler.BroadcastWebSocketHandler;
import com.cts.mfrp.au.service.AuctionSchedulerService;
import com.cts.mfrp.au.service.BidTimerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuctionSabhaApplication {
	public static void main(String[] args) {
        ApplicationContext con = SpringApplication.run(AuctionSabhaApplication.class, args);
        BroadcastWebSocketHandler bw = con.getBean(BroadcastWebSocketHandler.class);
        con.getBean(BidTimerService.class).setBroadcastWebSocketHandler(bw);
        con.getBean(AuctionSchedulerService.class).setBroadcastWebSocketHandler(bw);
	}
}