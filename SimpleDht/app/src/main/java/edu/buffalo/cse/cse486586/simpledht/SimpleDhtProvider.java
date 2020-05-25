package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;


/**
 * @author :Mohammed Samsuddin
 * @version :
 */

public class SimpleDhtProvider extends ContentProvider {



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
        public int delete(Uri uri, String selection, String[] selectionArgs) {
                // TODO Auto-generated method stub
                return 0;
        }

        @Override
        public String getType(Uri uri) {
                // TODO Auto-generated method stub
                return null;
        }

        @Override
        public Uri insert(Uri uri, ContentValues initialValues) {
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

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {



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
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
                // TODO Auto-generated method stub
                return 0;
        }

        private String genHash(String input) throws NoSuchAlgorithmException {
                MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                byte[] sha1Hash = sha1.digest(input.getBytes());
                Formatter formatter = new Formatter();
                for (byte b : sha1Hash) {
                        formatter.format("%02x", b);
                }
                return formatter.toString();
        }





        /***
         * ServerTask is an AsyncTask that should handle incoming messages. It is created by
         * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
         *
         * Please make sure you understand how AsyncTask works by reading
         * http://developer.android.com/reference/android/os/AsyncTask.html
         *
         * @author stevko
         *
         */
        private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

                @Override
                protected Void doInBackground(ServerSocket... sockets) {


                        // ServerSocket takes request from many client
                        ServerSocket serverSocket = sockets[0];


                        /*
                         * TODO: Fill in your server code that receives messages and passes them
                         * to onProgressUpdate().
                         */


                        // Wait for connection. Block until a connection is made.

                        try {



                                while (true) { // run until you terminate the program

                                        // every client require a new socket
                                        // if ServerSocket accepting any request, it will create a new socket object to communicate with that particular client
                                        Socket socket = serverSocket.accept();

                                        // Fetch the data we recieved
                                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                                        // save the message in a string
                                        String message = bufferedReader.readLine();

                                        // if message is not null then publish update to UI Thread
                                        if (!message.matches("")) {

                                                this.publishProgress(message);
                                        }


                                        socket.close();
                                }


                        } catch (IOException e) {

                                e.printStackTrace();
                        }


                        return null;
                }



                protected void onProgressUpdate(String...strings) {
                        /*
                         * The following code displays what is received in doInBackground().
                         */
                        String strReceived = strings[0].trim();
                        TextView myTextView = (TextView) findViewById(R.id.textView1);
                        myTextView.append(strReceived + "\t\n");
                        myTextView.append("\n");

                        /*
                         * The following code store our messages in Content provider using <key, value> pair
                         * <key> is sequence of id increment by 1 and unique for each message
                         * <value> is actual message
                         */

                        String msg = strReceived + "\n";
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("key", Integer.toString(msgID));
                        contentValues.put("value", msg);
                        mContentResolver.insert(mUri, contentValues);
                        msgID++;

                        return;
                }
        }






        /***
         * ClientTask is an AsyncTask that should send a string over the network.
         * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
         * an enter key press event.
         *
         * @author stevko
         *
         */
        private class ClientTask extends AsyncTask<String, Void, Void> {

                @Override
                protected Void doInBackground(String... msgs) {
                        try {


                                // Create sockets to connect with the Server
                                // Takes the IP addresses and port as parameter
                                for (String REMOTE_PORT : REMOTE_PORTS) {
                                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT));


                                        // message we want to send to the server
                                        String msgToSend = msgs[0];


                                        /*
                                         * TODO: Fill in your client code that sends out a message.
                                         */

                                        try {


                                                // OutputStreamWriter Convert data into stream format
                                                // Then send the stream to server using socket.getOutputStream()
                                                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                                                // Responsible for sending the data
                                                PrintWriter printWriter = new PrintWriter(outputStreamWriter, true);

                                                // send the data to server and server will process it
                                                printWriter.write(msgToSend);

                                                // if the data is too big for buffer then forcefully send the data to server
                                                printWriter.flush();


                                                socket.close();

                                        } catch (Exception e) {
                                                e.printStackTrace();
                                        }

                                }

                        } catch (UnknownHostException e) {
                                Log.e(TAG, "ClientTask UnknownHostException");
                        } catch (IOException e) {
                                Log.e(TAG, "ClientTask socket IOException");
                        }

                        return null;
                }
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
