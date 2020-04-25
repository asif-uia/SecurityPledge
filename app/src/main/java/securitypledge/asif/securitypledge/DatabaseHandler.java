package securitypledge.asif.securitypledge;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Asif on 1/21/2018
 * Last edited by Asif 2/11/2018, 2339Hours
 */

public class DatabaseHandler {
    final Context c;
    SQLiteDatabase db;

    static final String DBNAME = "BFD_database";
    static final int DBVERSION = '1';

    static final String ID = "_id";
    static final String ID2 = "id2";

    static final String Number = "number";
    static final String Text = "text";

    static final String TBNAME = "ContactBFD";
    static final String TBNAME1 = "MessageBFD";

    static final String CREATE_TB = "CREATE TABLE ContactBFD(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "number TEXT NOT NULL);";
    static final String CREATE_TB2 = "CREATE TABLE MessageBFD(id2 INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "text TEXT NOT NULL);";

    DatabaseHelper helper;

    public DatabaseHandler(Context ctx) {
        this.c = ctx;
        helper = new DatabaseHelper(c);

    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DBNAME, null, DBVERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TB);
                db.execSQL(CREATE_TB2);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("DatabaseHandler", "Upgrading DB");
            db.execSQL("DROP TABLE IF EXISTS ContactBFD");

            db.execSQL("DROP TABLE IF EXISTS MessageBFD");

            onCreate(db);
        }
    }

    //Open the Database
    public DatabaseHandler openDB() {
        try {
            db = helper.getWritableDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void close() {
        helper.close();
    }

    //Insertion into "ContactBFD" Table
    public long add(String number) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(Number, number);

            return db.insert(TBNAME, ID, cv);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Insertion into "MessageBFD" table
    public long push(String msg) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(Text, msg);

            return db.insert(TBNAME1, ID, cv);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Getting Contact Numbers
    public Cursor getAllNumbers() {
        String[] columns = {ID, Number};
        return db.query(TBNAME, columns, null, null, null, null, null);
    }

    //Getting Custom Texts
    public Cursor getAllTexts() {
        String[] columns = {ID2, Text};
        return db.query(TBNAME1, columns, null, null, null, null, null);
    }





    public int delete(long id) {
        return db.delete(TBNAME, ID + "=?", new String[]{String.valueOf(id)});
    }

    public void clearDB() {
        db.execSQL("DELETE FROM ContactBFD");
    }

    //Clear default text messages
    public void clearDefault() {
        db.execSQL("DELETE FROM MessageBFD");
    }
}
