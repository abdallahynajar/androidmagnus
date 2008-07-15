package org.magnus.communication.email;

import org.magnus.R;
import org.magnus.communication.Communication;

import android.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SendMail extends Activity {
    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Bundle extras = getIntent().getExtras();
        setContentView(R.layout.email);
        final Button send = (Button) this.findViewById(R.id.send);
        //TODO: make user-name and password persistent
        final EditText userid = (EditText) this.findViewById(R.id.userid);
        final EditText password = (EditText) this.findViewById(R.id.password);
        final EditText from = (EditText) this.findViewById(R.id.from);
        final EditText to = (EditText) this.findViewById(R.id.to);
        to.setText(extras.getString(Communication.RECIPIENT_URI));
        final EditText subject = (EditText) this.findViewById(R.id.subject);
        final EditText body = (EditText) this.findViewById(R.id.body);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                GMailSender sender = new GMailSender(userid.getText().toString(), password.getText().toString());
                try {
                    sender.sendMail(subject.getText().toString(),
                            body.getText().toString(),
                            from.getText().toString(),
                            to.getText().toString());
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
                Toast.makeText(SendMail.this, getText(R.string.email_sent).toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
