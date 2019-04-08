package it.uniroma1.boubouk;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(primaryKeys = {"user", "book"})
public class BookOwnership {
    @NonNull
    private String user;
    @NonNull
    private String book;

    public BookOwnership(@NonNull String user, @NonNull String book) {
        this.user = user;
        this.book = book;
    }

    @NonNull
    public String getUser() {
        return user;
    }

    public void setUser(@NonNull String user) {
        this.user = user;
    }

    @NonNull
    public String getBook() {
        return book;
    }

    public void setBook(@NonNull String book) {
        this.book = book;
    }
}
