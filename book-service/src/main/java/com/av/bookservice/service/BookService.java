package com.av.bookservice.service;

import com.av.bookservice.dto.BookRequestDto;
import com.av.bookservice.dto.BookResponseDto;
import com.av.bookservice.dto.StockRequestDto;
import com.av.bookservice.model.Book;
import com.av.bookservice.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Slf4j
public class BookService {

    private BookRepository bookRepository;
    private final WebClient.Builder webClientBuilder;

    public BookService(BookRepository bookRepository, WebClient.Builder webClientBuilder) {
        this.bookRepository = bookRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public void addBook(BookRequestDto bookRequestDto) {
        Book book = Book.builder().bookCode(bookRequestDto.getBookCode()).name(bookRequestDto.getName()).author(bookRequestDto.getAuthor()).genre(bookRequestDto.getGenre()).description(bookRequestDto.getDescription()).price(bookRequestDto.getPrice()).build();
        Book savedBook = bookRepository.save(book);
        StockRequestDto stockRequest = new StockRequestDto(savedBook.getBookCode(), savedBook.getPrice(), bookRequestDto.getInitialQuantity());
        webClientBuilder.build().post().uri("http://stock-service/api/stock")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(stockRequest))
                .exchange()
                .block();
        log.info("The book {} is added to the database.", book.getId());
    }

    public List<BookResponseDto> getBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream().map(this::toBookResponse).toList();
    }


    public BookResponseDto toBookResponse(Book book) {
        return BookResponseDto.builder().id(book.getId()).bookCode(book.getBookCode()).name(book.getName()).author(book.getAuthor()).genre(book.getGenre()).description(book.getDescription()).price(book.getPrice()).build();
    }
}
