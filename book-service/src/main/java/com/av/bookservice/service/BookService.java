package com.av.bookservice.service;

import com.av.bookservice.dto.BookRequestDto;
import com.av.bookservice.dto.BookResponseDto;
import com.av.bookservice.model.Book;
import com.av.bookservice.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BookService {

    private BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void addBook(BookRequestDto bookRequestDto) {
        Book book = Book.builder().bookCode(bookRequestDto.getBookCode()).name(bookRequestDto.getName()).author(bookRequestDto.getAuthor()).genre(bookRequestDto.getGenre()).description(bookRequestDto.getDescription()).price(bookRequestDto.getPrice()).build();
        bookRepository.save(book);
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
