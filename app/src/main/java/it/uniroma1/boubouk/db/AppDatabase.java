package it.uniroma1.boubouk.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;

import it.uniroma1.boubouk.R;
import it.uniroma1.boubouk.SearchOnlineActivity;
import it.uniroma1.boubouk.Util;
import it.uniroma1.boubouk.classes.Book;
import it.uniroma1.boubouk.classes.BookOwnership;
import it.uniroma1.boubouk.classes.User;

@Database(entities = {Book.class, BookOwnership.class, User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;
    private User currentUser;

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "user-database")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    /*public boolean syncDatabase(Context context, String[] tables, Request.Method method, String url) {
        if (Util.checkConnection(context)) {
            JsonObjectRequest request = new JsonObjectRequest(
                    method,
                    FIXED_URL + (searchByKeyword ? text : "isbn:" + text),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            );
            queue.add(request);
        } else {
        }
    }*/

    /* public static void destroyInstance() {
        INSTANCE = null;
    } */

    public User getUser() {
        return currentUser;
    }
    public void setUser(User user) {
        currentUser = user;
    }

    public abstract DaoAccess daoAccess();
}
