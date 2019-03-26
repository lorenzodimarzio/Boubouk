package it.uniroma1.boubouk.booksapi;

import java.util.List;

import it.uniroma1.boubouk.Book;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiInterface {
    @GET
    Call<List<Book>> getBooks(@Url String url);

}