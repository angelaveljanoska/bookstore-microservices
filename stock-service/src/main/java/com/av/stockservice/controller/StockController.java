package com.av.stockservice.controller;

import com.av.stockservice.dto.StockResponseDto;
import com.av.stockservice.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<StockResponseDto> checkStock(@RequestParam List<String> itemCodes, @RequestParam List<Integer> itemQuantities) {
        return stockService.checkStock(itemCodes, itemQuantities);
    }
}
