package com.av.bookservice.controller;

import com.av.bookservice.dto.BookPricesResponseDto;
import com.av.bookservice.dto.BookRequestDto;
import com.av.bookservice.dto.BookResponseDto;
import com.av.bookservice.model.Book;
import com.av.bookservice.service.BookService;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/book")

public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addBook(@RequestBody BookRequestDto bookRequestDto) {
        bookService.addBook(bookRequestDto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BookResponseDto> getBooks() {
        return bookService.getBooks();
    }

    @GetMapping("/prices")
    @ResponseStatus(HttpStatus.OK)
    public List<BookPricesResponseDto> getBookPriceByBookCode(@RequestParam List<String> bookCodes) {
        return bookService.getBookPricesByBookCodes(bookCodes);
    }
}
