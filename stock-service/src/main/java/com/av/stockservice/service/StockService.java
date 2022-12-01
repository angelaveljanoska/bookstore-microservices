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
import java.util.Optional;


@Service
@Slf4j
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Stock createStock(Stock stock) {
        return stockRepository.save(stock);
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
            stocksList.add(StockResponseDto.builder().stockId(stock.get(i).getId()).bookCode(codes.get(i)).isInStock(stock.get(i).getQuantity() >= quantities.get(i)).build());
        }
        return stocksList;
    }

    public void updateStock(List<Long> stockIds, List<Integer> counts, Boolean increase) {
        if (stockIds.size() != counts.size()) {
            log.info("Invalid stock params!");
            throw new RuntimeException("Stock parameters are invalid!");
        }
        for (int i = 0; i < stockIds.size(); i++) {
            Long stockId = stockIds.get(i);
            Integer count = counts.get(i);
            Stock stock = stockRepository.findById(stockId).orElseThrow(() -> new RuntimeException("Cannot find stock with ID " + stockId + "!"));
            log.info("Stock count is {}", stock.getQuantity());
            if (!increase && stock.getQuantity() < count) {
                throw new RuntimeException("End quantity cannot be negative!");
            }
            stock.setQuantity(increase ? stock.getQuantity() + count : stock.getQuantity() - count);
            log.info("Updated stock count, new quantity is {}", stock.getQuantity());
            stockRepository.save(stock);
        }
    }
}
