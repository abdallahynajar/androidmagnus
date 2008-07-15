package org.magnus.communication;

import org.magnus.R;

import android.app.Activity;

import android.content.ComponentName;

import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.provider.Im;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gtalkservice.IChatSession;
import com.google.android.gtalkservice.IGTalkService;
import com.google.android.gtalkservice.IGTalkSession;
import com.google.android.gtalkservice.Presence;

public class GTalkClient extends Activity implements View.OnClickListener {
    private static final String LOG_TAG = "GTalkClient";

    IChatSession mXmppSession = null;
    EditText mSendText;
    ListView mListMessages;
    EditText mRecipient;
    Button mSend;
    Button mSetup;
    
    /* Get info from intent on who to contact via gtalk */
    private String m_ContactUri;
    private String m_ContactName;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.gtalk_main);
        
        /* get the bundle from the intent */
        Bundle extras = getIntent().getExtras();
                      
        /* get the URI to start gtalk message to */
        //TODO - error checking for bundle
        m_ContactUri = extras.getString(Communication.RECIPIENT_URI);
        Log.i(LOG_TAG, "jabber id: " + m_ContactUri);
        
        /* get the name of the contact */
        String m_ContactName = extras.getString(Communication.RECIPIENT_NAME); 
        Log.i(LOG_TAG, "contact name: " + m_ContactName);

        // gather the troops
        mSendText = (EditText) findViewById(R.id.sendText);
        mListMessages = (ListView) findViewById(R.id.listMessages);
        mRecipient = (EditText) findViewById(R.id.recipient);
        mRecipient.setText(m_ContactUri);
        mSend = (Button) findViewById(R.id.send);
        mSetup = (Button) findViewById(R.id.setup);

        // set up handler for on click
        mSetup.setOnClickListener(this);
        mSend.setOnClickListener(this);      

        bindService((new Intent()).setComponent(
                com.google.android.gtalkservice.GTalkServiceConstants.GTALK_SERVICE_COMPONENT),
               mConnection, 0);
        
        /* simulate the Setup keypress */
        runGtalkSetup(m_ContactUri);
    }

    /**
     * Let the user know there was an issue
     *
     * @param msg
     */
    private void logMessage(CharSequence msg) {
      

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Setup the XMPP Session using a service connection
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the XmppService has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            IGTalkService xmppService = IGTalkService.Stub.asInterface(service);

            try {
                IGTalkSession gtalkSession = xmppService.getDefaultSession();
                mXmppSession = gtalkSession.createChatSession(m_ContactName);
                if (mXmppSession == null) {
                    // this should not happen.
                    logMessage(getText(R.string.xmpp_session_not_found));
                    return;
                }
                gtalkSession.setPresence(new Presence(Im.PresenceColumns.AVAILABLE, "Am here now!"));
            } catch (DeadObjectException ex) {
                Log.e(LOG_TAG, "caught " + ex);
                logMessage(getText(R.string.found_stale_xmpp_service));
            }

            mSendText.setEnabled(true);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mXmppSession = null;
            mSendText.setEnabled(false);
        }
    };

    private void runGtalkSetup(String jabberUri)
    {
        Log.i(LOG_TAG, "Setup");
        // Run a query against CONTENT_URI = "content://im/messages"
        Cursor cursor = managedQuery(Im.Messages.CONTENT_URI, null,
                "contact=\'" + jabberUri + "\'", null, null);

        // Display the cursor results in a simple list
        // Note that the adapter is dyamic (picks up new entries automatically)
        ListAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                cursor, // Give the cursor to the list adatper
                new String[]{Im.MessagesColumns.BODY},
                new int[]{android.R.id.text1});

        this.mListMessages.setAdapter(adapter);     
    }
    
    /**
     * Handle clicks on the 2 buttions
     *
     * @param view
     */
    public void onClick(View view) {
        if (view == mSetup) {
            Log.i(LOG_TAG, "onClick - Setup");
            runGtalkSetup(mRecipient.getText().toString());
        } else if (view == mSend) {
            // use XmppService to send data message to someone
            if (!isValidUsername(m_ContactUri)) {
                logMessage(getText(R.string.gtalk_invalid_username));
                return;
            }

            if (mXmppSession == null) {
                logMessage(getText(R.string.xmpp_service_not_connected));
                return;
            }

            try {
                mXmppSession.sendTextMessage(mSendText.getText().toString());
            } catch (DeadObjectException ex) {
                Log.e(LOG_TAG, "caught " + ex);
                logMessage(getText(R.string.found_stale_xmpp_service));
                mXmppSession = null;
            }
        }
    }

    private boolean isValidUsername(String username) {
        return !TextUtils.isEmpty(username) && username.indexOf('@') != -1;
    }
}
