package com.av.bookservice.repository;

import com.av.bookservice.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findBookByBookCodeIn(List<String> bookCodes);
}
