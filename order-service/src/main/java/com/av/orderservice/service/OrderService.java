package com.av.orderservice.service;

import com.av.orderservice.dto.ItemDto;
import com.av.orderservice.dto.OrderRequestDto;
import com.av.orderservice.dto.OrderResponseDto;
import com.av.orderservice.dto.StockResponseDto;
import com.av.orderservice.event.OrderCreatedEvent;
import com.av.orderservice.model.Item;
import com.av.orderservice.model.Order;
import com.av.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public String createOrder(OrderRequestDto orderRequestDto) {
        Order order = new Order();
        List<Item> items = orderRequestDto.getItems().stream().map(this::toItem).toList();
        order.setItems(items);
        List<String> itemCodes = items.stream().map(Item::getBookCode).toList();
        List<Integer> itemQuantities = items.stream().map(Item::getQuantity).toList();
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
            boolean validOrder = stockStatus.stream().allMatch(StockResponseDto::isInStock);
            if (validOrder) {
                orderRepository.save(order);
                kafkaTemplate.send("notificationTopic", new OrderCreatedEvent(order.getId()));
                return "Success!";
            } else {
                String unavailableBooks = stockStatus.stream().filter(stock -> !stock.isInStock()).reduce("", (curr, acc) -> acc + " " + curr, (a, b) -> (a + b));
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
        item.setPrice(itemDto.getPrice());
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
