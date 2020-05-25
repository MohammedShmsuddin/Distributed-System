package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko & Mohammed Samsuddin
 *
 */
public class GroupMessengerProvider extends ContentProvider {



    // Create SQLiteDatabase for CRUD and DataBaseHelper to Create and Upgrade Database
    // Resource: https://developer.android.com/training/data-storage/sqlite
    private SQLiteDatabase sqLiteDatabase;
    private DatabaseHelper dataBaseHelper;

    static final String DATABASE_NAME = "MyKeyValueDB";
    static final String TABLE_NAME = "KeyValueTable";

    // Column name
    static final String KEY_COLUMN = "key";
    static final String VALUE_COLUMN = "value";

    static final int DATABASE_VERSION = 3;

    static final String CREATE_DB_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + KEY_COLUMN + " TEXT, " +  VALUE_COLUMN + " TEXT NOT NULL);";



    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.


        // The DatabaseHelper class is used to create a helper object which creates the database if it does not already exist.
        // This method would return false if the provider won’t be loaded because the database is not accessible, otherwise would return true.
        dataBaseHelper = new DatabaseHelper(getContext());

        // Create and/or open a database that will be used for reading and writing.
        sqLiteDatabase = dataBaseHelper.getWritableDatabase();


        return (sqLiteDatabase == null) ? false : true;
    }


    /**
     *
     * @param uri   URI where the records are going to be inserted
     * @param initialValues values that are going to make up the record
     * @return  returns the URI of the new record
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         *
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */


        long rowID = 0;
        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        // values<key, value> pair
        rowID = sqLiteDatabase.insert(TABLE_NAME, null, values);


        // if the rowID is greater than 0 (which means that the record is added at the table)
        // and the new URI is created by using the withAppendedId() method.
        if (rowID > 0) {

            // new URI is created by using the withAppendedId() method
            // Source: https://developer.android.com/reference/android/content/ContentUris
            Uri newURI = ContentUris.withAppendedId(uri, rowID);


            // Notifying listeners of dataset changes
            // Clients often want to be notified about changes in the underlying datastore of your content provider.
            // So inserting data, as well as deleting or updating data should trigger this notification.
            getContext().getContentResolver().notifyChange(newURI, null);

            return newURI;

        } else {

            Log.e("Insertion Failed: ", uri.toString());
            return null;
        }

    }


    /**
     * Used to retrieve the data stored in the database and return a Cursor instance
     *
     * @param uri The URI to query
     * @param projection The list of columns to put into the cursor. If null, all columns are included.
     * @param selection A selection criteria to apply when filtering rows. If null, then all rows are included.
     * @param selectionArgs Any additional arguments that need to be passed to the SQL query operation to perform the selection.
     * @param sortOrder How the rows in the cursor should be sorted.
     * @return A Cursor containing the results of the query.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */


        // SQLiteQueryBuilder is a helper class that creates the proper SQL syntax for us.
        // Source: https://developer.android.com/reference/android/content/ContentProvider#query(android.net.Uri,%20java.lang.String[],%20java.lang.String,%20java.lang.String[],%20java.lang.String)
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        // Sets the database table to be used for this query
        sqLiteQueryBuilder.setTables(TABLE_NAME);


        // This is the query that does the work, searches the database and returns the result.
        // It is passed a number of parameters (from the content resolver):
        Cursor cursor = sqLiteQueryBuilder.query(sqLiteDatabase, null, "key = " + "'" + selection + "'", selectionArgs, null, null, sortOrder);


        // Pass the query Uri here. This will ensure that Observers will be notified that the database has been modified. Open cursors will be refreshed
        cursor.setNotificationUri(getContext().getContentResolver(), uri);




        Log.v("query", selection);
        return cursor;
    }







    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }




    /**
     * A helper class to manage our database creation and version management.
     * Source: https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper
     *
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {


        /**
         * Constructor to initialize the Database helper object
         * @param context
         */
        DatabaseHelper(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }


        /**
         * Called when the database is created for the FIRST time.
         * If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
         * @param sqLiteDatabase
         */
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            sqLiteDatabase.execSQL(CREATE_DB_TABLE);
        }




        /**
         * Called when the database needs to be upgraded.
         * This method will only be called if a database already exists on disk with the same DATABASE_NAME,
         * but the DATABASE_VERSION is different than the version of the database that exists on disk.
         * @param sqLiteDatabase
         * @param oldVersion
         * @param newVersion
         */
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

            // Simplest implementation is to drop all old tables and recreate them
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }




}
