package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko & Mohammed Samsuddin
 *
 */
public class GroupMessengerActivity extends Activity {



    static final String TAG = GroupMessengerActivity.class.getSimpleName();

    // Clients ports that we are going to connect
    static final String REMOTE_PORTS [] = {"11108", "11112", "11116", "11120", "11124"};

    // All our app open one server socket that listens on port:10000.
    static final int SERVER_PORT = 10000;


    // Each messages our App receives assigned to a unique id to store in content provider as <key>
    // msgID increment by 1 for each message
    private static int msgID = 0;

    private Uri mUri;
    private ContentResolver mContentResolver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);


        // provider URI
        mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");
        mContentResolver = this.getContentResolver();


        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));







        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(new OnPTestClickListener(tv, getContentResolver()));



        try {

            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);      // SERVER_PORT = 10000
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }



        // EditText for writing the message
        final EditText editText = (EditText) findViewById(R.id.editText1);

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                 * On click on <Send> button. Get the message from EditText then append the message in TextView
                 */
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView myTextView = (TextView) findViewById(R.id.textView1);
                myTextView.append("\t" + msg); // This is one way to display a string.

                /*
                 * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                 * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                 * the difference, please take a look at
                 * http://developer.android.com/reference/android/os/AsyncTask.html
                 */
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }



    /**
     * buildUri() demonstrates how to build a URI for a ContentProvider.
     *
     * @param scheme
     * @param authority
     * @return the URI
     */
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
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



}
