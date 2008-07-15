package org.magnus.communication;

import org.magnus.R;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SmsGenerator extends Activity {

    private String m_PhoneNumber = null;
    private TextView m_MaxChar;
    private EditText m_Body;
    
    /** The MAXSMSLENGTH currently is the SMS message max length of 160, 
     * support only one TXT message for now (i.e. concatenated SMS is phase 2) */
    private int MAXSMSLENGTH = 160; 
    
    /** The SMSLOG used for LogCat logging. */
    private String SMSLOG = "smsLog";

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        /* set the content view to the TXT message composer view */ 
        setContentView(R.layout.sms_compose);
        
        /* get the bundle from the intent */
        Bundle extras = getIntent().getExtras();
                      
        /* get the phone number to send TXT message to */
        //TODO - error checking for bundle
        m_PhoneNumber = extras.getString(Communication.RECIPIENT_URI);
        Log.i(SMSLOG, "SMS To: " + m_PhoneNumber);
        
        /* get the name and number and format: Nick <7185551212> */
        String toField = extras.getString(Communication.RECIPIENT_NAME)+ 
                  " <" + 
                  m_PhoneNumber +
                  ">";
        
        /* fill the TO address */ 
        final EditText to = (EditText) this.findViewById(R.id.to_field);
        to.setText(toField);
        
        /* get the body handle so we can get the TXT inside it */
        //TODO - if 160/160 change MAX text to red
        //TODO - limit the # of char's to 160
        m_Body = (EditText) this.findViewById(R.id.body_field);
        m_MaxChar = (TextView) this.findViewById(R.id.max_char_txt);
        //m_MaxChar = (TextView) this.findViewById(R.id.msg_counter);
                
        /* display the default max length 
         * DISPLAY: 0/160 Maximum Characters */
        //TODO - fix the text movement when length is > 9
        m_MaxChar.setText("0/" + MAXSMSLENGTH + " " + getText(R.string.sms_max_chars)+ " "); 
        //m_MaxChar.setText("0/" + MAXSMSLENGTH + " ");
        
        /* when the user enters or deletes text, update the om screen counter */
        m_Body.addTextChangedListener( new TextWatcher() {
            
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                //intentionally blank
            }
            
            public void onTextChanged(CharSequence s, int start, int before, int count){
                /* update the character count 
                 * DISPLAY: 3/160 Maximum Characters */
                m_MaxChar.setText(m_Body.getText().length() + "/" + MAXSMSLENGTH + " " + getText(R.string.sms_max_chars)+ " ");
                //m_MaxChar.setText(m_Body.getText().length() + "/" + MAXSMSLENGTH + " ");
            }
        }
        
        );
        
       
        /* map the send button to an event listener and send SMS when clicked */
        final Button sendBtn = (Button) this.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                
                String msgBody = m_Body.getText().toString();
                Log.i(SMSLOG, "SMS Message Body: " + msgBody);
                
                /* Use the SMS manager with the existing SMSC */
                SmsManager smsMgr = SmsManager.getDefault();
                smsMgr.sendTextMessage(m_PhoneNumber, null, msgBody, 
                        //TODO - add error/success intent handling as defined here:
                        // http://code.google.com/android/reference/android/telephony/gsm/SmsManager.html#sendTextMessage(java.lang.String,%20java.lang.String,%20java.lang.String,%20android.content.Intent,%20android.content.Intent,%20android.content.Intent)
                        null, null, null);
                
                        //TODO - exit gracefully
                        finish();
            }
        });
    }

    
    
    

}
