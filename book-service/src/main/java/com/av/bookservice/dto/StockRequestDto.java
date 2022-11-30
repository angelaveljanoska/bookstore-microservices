package com.av.bookservice.dto;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
public class StockRequestDto {
    private String bookCode;
    private BigDecimal price;
    private int quantity;
}
