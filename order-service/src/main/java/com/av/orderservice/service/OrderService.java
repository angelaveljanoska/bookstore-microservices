package com.av.orderservice.service;

import com.av.orderservice.dto.*;
import com.av.orderservice.event.OrderCreatedEvent;
import com.av.orderservice.model.Item;
import com.av.orderservice.model.Order;
import com.av.orderservice.repository.OrderRepository;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public String createOrder(OrderRequestDto orderRequestDto) {
        Order order = new Order();
        List<Item> items = orderRequestDto.getItems().stream().map(this::toItem).toList();
        List<String> itemCodes = items.stream().map(Item::getBookCode).toList();
        List<Integer> itemQuantities = items.stream().map(Item::getQuantity).toList();

        List<BookPricesResponseDto> bookPrices = webClientBuilder.build().get().uri("http://book-service/api/book/prices",
                        uriBuilder -> uriBuilder
                                .queryParam("bookCodes", itemCodes)
                                .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToFlux(BookPricesResponseDto.class).collectList().block();
        if (bookPrices == null) {
            throw new RuntimeException("Cannot get book prices!");
        }
        BigDecimal totalPrice = items.stream().map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(bookPrices.stream().filter(it -> it.getBookCode().equals(item.getBookCode())).findFirst().map(BookPricesResponseDto::getPrice).orElseThrow(() -> new RuntimeException("Error during price calculation!")))).reduce(BigDecimal.valueOf(0), BigDecimal::add);
        order.setTotalPrice(totalPrice);
        order.setItems(items);
        Span stockServiceLookup = tracer.nextSpan().name("stockServiceLookup");
        try (Tracer.SpanInScope spanInScope = tracer.withSpan(stockServiceLookup.start())) {
            List<StockResponseDto> stockStatus = webClientBuilder.build().get().uri("http://stock-service/api/stock",
                            uriBuilder -> uriBuilder
                                    .queryParam("itemCodes", itemCodes)
                                    .queryParam("itemQuantities", itemQuantities)
                                    .build())
                    .retrieve().bodyToFlux(StockResponseDto.class).collectList().block();
            if (stockStatus == null) {
                throw new RuntimeException("Error fetching stock for requested books!");
            }
            stockStatus.forEach(item -> log.info("Stock: {}", item.isInStock()));
            boolean validOrder = stockStatus.stream().allMatch(StockResponseDto::isInStock);
            if (validOrder) {
                orderRepository.save(order);
                kafkaTemplate.send("notificationTopic", new OrderCreatedEvent(order.getId()));
                // updateStock with http
                int resultCode = Objects.requireNonNull(webClientBuilder.build().put().uri("http://stock-service/api/stock",
                                        uriBuilder -> uriBuilder
                                                .queryParam("stockIds", stockStatus.stream().map(StockResponseDto::getStockId).toList())
                                                .queryParam("counts", itemQuantities)
                                                .queryParam("increase", false)
                                                .build())
                                .exchange()
                                .block())
                        .rawStatusCode();
                if (resultCode >= 200 && resultCode < 300) {
                    return "Sucessfully created order, total price is " + totalPrice;
                } else {
                    orderRepository.delete(order);
                    return "Error during order creation, please try again later.";
                }
            } else {
                String unavailableBooks = stockStatus.stream().filter(stock -> !stock.isInStock()).map(StockResponseDto::getBookCode).reduce("", (curr, acc) -> acc + " " + curr, (a, b) -> (a + b));
                throw new IllegalArgumentException("Book(s) " + unavailableBooks + " not available at the moment!");
            }
        } finally {
            stockServiceLookup.end();
        }
    }

    public List<OrderResponseDto> getOrders() {
        List<OrderResponseDto> orders = orderRepository.findAll().stream().map(this::fromOrder).toList();
        return orders;
    }

    private Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setBookCode(itemDto.getBookCode());
        item.setQuantity(itemDto.getQuantity());

        return item;
    }

    private OrderResponseDto fromOrder(Order order) {
        OrderResponseDto orderResponseDto = new OrderResponseDto();
        orderResponseDto.setId(order.getId());
        orderResponseDto.setItems(order.getItems());
        return orderResponseDto;
    }
}
