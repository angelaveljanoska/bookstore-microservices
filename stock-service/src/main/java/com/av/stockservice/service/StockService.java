package com.av.stockservice.service;

import com.av.stockservice.dto.StockResponseDto;
import com.av.stockservice.model.Stock;
import com.av.stockservice.repository.StockRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<StockResponseDto> checkStock(List<String> codes, List<Integer> quantities) {
        log.info("Checking stock!");
        Thread.sleep(1000);
        log.info("Waiting has ended!");
        if (codes.size() != quantities.size()) {
            throw new IllegalArgumentException("Invalid arrays provided for book codes and/or quantities!");
        }
        List<Stock> stock = stockRepository.findByBookCodeIn(codes);
        if (quantities.size() != stock.size()) {
            throw new RuntimeException("Error during stock validation!");
        }
        List<StockResponseDto> stocksList = new ArrayList<>();
        for (int i = 0; i < quantities.size(); i++) {
            stocksList.add(StockResponseDto.builder().bookCode(codes.get(i)).isInStock(stock.get(i).getQuantity() >= quantities.get(i)).build());
        }
        return stocksList;
    }
}
