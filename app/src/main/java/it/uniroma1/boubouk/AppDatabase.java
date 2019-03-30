package it.uniroma1.boubouk;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Book.class}, version = 1, exportSchema = false)
public abstract class BouboukDatabase extends RoomDatabase {
    public abstract DaoAccess daoAccess() ;
}
