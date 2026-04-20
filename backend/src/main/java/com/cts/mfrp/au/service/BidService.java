package com.cts.mfrp.au.service;

import com.cts.mfrp.au.model.Bid;
import com.cts.mfrp.au.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BidService {

    @Autowired
    private BidRepository bidRepository;

    public void insertBid(Bid b){
        bidRepository.save(b);
    }
}
