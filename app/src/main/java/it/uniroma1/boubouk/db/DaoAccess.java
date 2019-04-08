package it.uniroma1.boubouk.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import it.uniroma1.boubouk.classes.User;
import it.uniroma1.boubouk.classes.Book;
import it.uniroma1.boubouk.classes.BookOwnership;

@Dao
public interface DaoAccess {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBook(Book book);

    @Delete
    void removeBook(Book book);

    @Query("SELECT id, title, author, publisher, isbn, pages, year, imageUrl, buyLink, plot" +
            " FROM Book JOIN BookOwnership ON Book.id = BookOwnership.book WHERE user = :user")
    Book[] retrieveAllBooks(String user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOwnership(BookOwnership ownership);

    @Delete
    void removeOwnership(BookOwnership ownerships);

    @Query("SELECT COUNT(*) FROM Book JOIN BookOwnership ON Book.id = BookOwnership.book " +
            "WHERE book = :book")
    int countOwnerships(String book);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUser(User user);

    @Query("SELECT * FROM BookOwnership WHERE user = :user AND book = :book")
    BookOwnership checkAlreadyOwned(String user, String book);
}

