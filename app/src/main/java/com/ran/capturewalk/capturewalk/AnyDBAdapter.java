package com.ran.capturewalk.capturewalk;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.vision.barcode.Barcode;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AnyDBAdapter {
    private static final String DATABASE_NAME = "LocationDB.sqlite";
    private static final int DATABASE_VERSION = 2;
    private static String DB_PATH = null;
    private static final String TAG = "AnyDBAdapter";
    private static SQLiteDatabase mDb;
    private final Context adapterContext;
    private DatabaseHelper mDbHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        Context helperContext;

        DatabaseHelper(Context context) {
            super(context, AnyDBAdapter.DATABASE_NAME, null, AnyDBAdapter.DATABASE_VERSION);
            this.helperContext = context;
        }

        public void onCreate(SQLiteDatabase db) {
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(AnyDBAdapter.TAG, "Upgrading database!!!!!");
        }

        public void createDataBase() throws IOException {
            if (!checkDataBase()) {
                getReadableDatabase();
                try {
                    copyDataBase();
                } catch (IOException e) {
                    throw new Error("Error copying database");
                }
            }
        }

        public SQLiteDatabase getDatabase() {
            return SQLiteDatabase.openDatabase(AnyDBAdapter.DB_PATH + AnyDBAdapter.DATABASE_NAME, null, 1);
        }

        private boolean checkDataBase() {
            SQLiteDatabase checkDB = null;
            try {
                checkDB = SQLiteDatabase.openDatabase(AnyDBAdapter.DB_PATH + AnyDBAdapter.DATABASE_NAME, null, 1);
            } catch (SQLiteException e) {
            }
            if (checkDB != null) {
                checkDB.close();
            }
            if (checkDB != null) {
                return true;
            }
            return false;
        }

        private void copyDataBase() throws IOException {
            InputStream myInput = this.helperContext.getAssets().open(AnyDBAdapter.DATABASE_NAME);
            OutputStream myOutput = new FileOutputStream(AnyDBAdapter.DB_PATH + AnyDBAdapter.DATABASE_NAME);
            byte[] buffer = new byte[Barcode.UPC_E];
            while (true) {
                int length = myInput.read(buffer);
                if (length > 0) {
                    myOutput.write(buffer, 0, length);
                } else {
                    myOutput.flush();
                    myOutput.close();
                    myInput.close();
                    return;
                }
            }
        }

        public void openDataBase() throws SQLException {
            AnyDBAdapter.mDb = SQLiteDatabase.openDatabase(AnyDBAdapter.DB_PATH + AnyDBAdapter.DATABASE_NAME, null, 0);
        }

        public synchronized void close() {
            if (AnyDBAdapter.mDb != null) {
                AnyDBAdapter.mDb.close();
            }
            super.close();
        }
    }

    static {
        DB_PATH = "//data/data/com.ran.capturewalk.capturewalk/databases/";
    }

    public AnyDBAdapter(Context context) {
        this.adapterContext = context;
    }

    public AnyDBAdapter open() throws SQLException {
        this.mDbHelper = new DatabaseHelper(this.adapterContext);
        try {
            this.mDbHelper.createDataBase();
            try {
                this.mDbHelper.openDataBase();
                return this;
            } catch (SQLException sqle) {
                throw sqle;
            }
        } catch (IOException e) {
            throw new Error("Unable to create database");
        }
    }

    public Cursor SelectEntries() {
        String query = "SELECT distance,date,time FROM distance_data";
        System.out.println(query);
        return mDb.rawQuery(query, null);
    }

    public void deleteCommand() {
        String command = "DELETE FROM distance_data";
        System.out.println(command);
        mDb.execSQL(command);
    }

    public void InsertEntry(double distance, String date, String time) {
        String command = "INSERT INTO distance_data(distance,date,time) VALUES('" + distance + "','" + date + "','" + time + "')";
        System.out.println(command);
        mDb.execSQL(command);
        Toast.makeText(this.adapterContext, "Inserted in database", 0).show();
    }

    public void close() {
        this.mDbHelper.close();
    }
}
