package com.av.stockservice;

import com.av.stockservice.model.Stock;
import com.av.stockservice.repository.StockRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
public class StockServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockServiceApplication.class, args);
    }

    /*@Bean
    public CommandLineRunner loadData(StockRepository stockRepository) {
        return args -> {
            Stock stock1 = new Stock();
            stock1.setBookCode("bookcode123");
            stock1.setQuantity(123);

            Stock stock2 = new Stock();
            stock2.setBookCode("bookcode0");
            stock2.setQuantity(0);

            stockRepository.save(stock1);
            stockRepository.save(stock2);
        };
    }*/
}
