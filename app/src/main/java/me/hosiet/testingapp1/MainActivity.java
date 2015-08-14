package me.hosiet.testingapp1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "me.hosiet.testingapp1.MESSAGE";
    private Button [] mButton = new Button[9]; /* only declaration, not defination; not using 0 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* the OnClickListener() used for giving click info */
        OnClickListener play_listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.edit_message);
                editText.setText(v.getTag().toString());
                /* Call sendMessageToPi() when ready */
                sendMessageToPi(v);
            }
        };

        /* initialzations of my 8 buttons */
        for (int i = 1; i <= 8; i++) {
            /* use special way instead of R.id.* */
            mButton[i] = (Button)findViewById(getResources().getIdentifier("button_0" + Integer.toString(i), "id", this.getPackageName()));
            mButton[i].setOnClickListener(play_listener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Called when have the button clicked */
    public void sendMessage(View view) {
        // Let's try.
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /** Send to Raspberry Pi */
    public void sendMessageToPi(View view) {
        if (view == null) {
            /* view must not be null */
            Log.e("ERROR1", "View is NULL!");
            return;
        }

        final View myview = view; /* set to final to avoid problems? */

        /* now begin the socket connection */

        /* Don't do it in main thread.
           or, there will be android.os.NetworkOnMainThreadException */
        // in Android 4.0 or later, you may not perform network operation in main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                try {
                    InetAddress serverAddr = InetAddress.getByName("do1.hosiet.me");
                    socket = new Socket(serverAddr, 9999);
                    Log.i("NetworkingThread", "Socket connected");
                    String toServer = myview.getTag().toString() + "\n";
                    Log.i("NetworkingThread", "Target string is "+toServer);
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);

                    out.println(toServer);
                    out.flush();
                    Log.i("NetworkingThread", "String Sent");
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (socket == null || socket.isClosed()) {
                            Log.e("NetworkingThread", "Unexpected null socket object");
                        } else {
                            socket.close();
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //pass
    }
}