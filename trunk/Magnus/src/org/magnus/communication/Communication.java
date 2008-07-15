package org.magnus.communication;

import org.magnus.R;
import org.magnus.communication.email.SendMail;
import org.magnus.contentprovider.Contacts;

import android.app.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * The Class Communication.
 * 
 * @author Nick Chiarulli
 * 
 * This class will invoke the communication application intended by the users selection within
 * the contact application.
 * 
 * Class is in progress ...
 * 
 * References:
 * 
 * startSubActivity(Intent,int):
 * http://code.google.com/android/reference/android/app/Activity.html#startSubActivity(android.content.Intent,%20int)
 * 
 * Intent:
 * http://code.google.com/android/intro/tutorial-ex2.html
 */

public class Communication extends Activity {

     /* 
     * define the types of activities 
     * */

    /** The Constant ACTIVITY_COMMUNICATE will start a communication app. */
    public static final int ACTIVITY_COMMUNICATE = 0;

    /* 
     * define the types of keys for in the extras bundle 
     * */
    
     public static final String COMM_TYPE = "comm_type";
     public static final int PHONE_TYPE = 0;
     public static final int SMS_TYPE = 1;
     public static final int OTHER_TYPE = 2;
     
     public static final String RECIPIENT_URI = "recipient_uri";
     public static final String RECIPIENT_NAME = "recipient_name";

    /*
     * Private members
     */
    
    /** The COMMLOG used for LogCat logging. */
    private String COMMLOG = "commLog";
    
    /** The m_contact index. */
    private long m_contactIndex = -1;
    private String m_Name;
    
    
    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    
        /* get the parameters passed in by the intent */
        Bundle extras = getIntent().getExtras();

        /* check to make sure our bundle contains data */
        if (null != extras) {

            /* check if we have a index to the DB in the bundle and check if it is valid */ 
            if ( (extras.containsKey(BaseColumns._ID)) &&
                 ((m_contactIndex = extras.getLong(BaseColumns._ID)) > 0) ) {   
                if(!extras.containsKey(Contacts.PeopleColumns.NAME)) {
                    Log.e(COMMLOG, "Contacts.PeopleColumns.NAME not specified in the intent");
                    return;
                }
                m_Name = extras.getString(Contacts.PeopleColumns.NAME);
                if(!extras.containsKey(COMM_TYPE))  {
                    Log.e(COMMLOG, "COMM_TYPE not specified in the intent");
                    return;
                }
                int comm_type = extras.getInt(COMM_TYPE);
                
                    
                /* determine which key to act on */
                switch(comm_type) {
                case PHONE_TYPE:
                    /* get the phone type from the bundle */                   
                    placeCallToPhoneType(extras.getInt(Contacts.PhonesColumns.TYPE));
                    break;
                case SMS_TYPE:
                    /* get the mobile number to send the TXT message */
                    String smsUri = getPhoneNumberFromDB(extras.getInt(Contacts.PhonesColumns.TYPE));
                    if (null != smsUri)
                    {
                        /* Use the SMS generator class to handle SMS messages */
                        Intent i = new Intent(this, SmsGenerator.class);
                        /* pass the contact name and number from the contact app */
                        i.putExtra(RECIPIENT_URI, smsUri); //contact mobile number
                        i.putExtra(RECIPIENT_NAME, m_Name); //contact name
                        startActivity(i);
                    } else {
                        Toast.makeText(this, m_Name + ": " + getText(R.string.sms_cannot_send) + " " + getText(R.string.call_without_number), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case OTHER_TYPE:
                    /* use helper function to determine which comm kind the user is interested in starting 
                     * and which comm type to pull from the DB. */
                   handleCommunicationKind(extras.getInt(Contacts.ContactMethodsColumns.KIND), extras);
                }
             
            }
        }
    }

    /**
     * Start the calling app to the desired phone number associated with the phone type.
     * 
     * @param phoneType the phone type
     */    
    private void placeCallToPhoneType(int phoneType){
        
        String telUri = getPhoneNumberFromDB(phoneType);
        
        /* check to make sure we have a valid entry for contact */
        if (null != telUri) {
            
            /* Get the phone number key from the query */
            //TOOD - format the tel number for international calling etc.
            telUri = "tel:" + telUri;
            
            Log.i(COMMLOG, "Using phone number: " + telUri);
            
            /* launch the phone dialing app (not the dialer) */
            String commAction = Intent.CALL_ACTION;
            
            /* increment the frequency of use for this contact method */
            updateContactPhoneFrequency(phoneType);
            
            /* start the dialing view with the contact's URI */
            startCommActivity(commAction, telUri);            
           
        } else {
            
            Toast.makeText(this, m_Name + ": " + getText(R.string.call_without_number), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getPhoneNumberFromDB(int phoneType){
        /* build the projection to check only return the phone type and number key (unformatted number) */
        String[] projection = new String[] { 
                Contacts.PhonesColumns.TYPE, /* 0 - mobile, home, work, etc. */
                Contacts.PhonesColumns.NUMBER_KEY}; /* 1 - the phone number */
        
        /* build the uri based on the contact index (i.e. the contact selected) from the contact app */
        //content://org.magnus.contentprovider.Contacts/people/9/phones
        Uri uri = Contacts.People.CONTENT_URI.buildUpon().appendPath(""+m_contactIndex).appendPath(Contacts.People.Phones.CONTENT_DIRECTORY).build();
       
        /* using the URI created, query the DB for the phone type and retrieve the phone number*/
        Cursor managedCursor = managedQuery(
                uri, projection,
                Contacts.PhonesColumns.TYPE + "=" + phoneType,
                null);

        /* check to make sure we have a valid entry for contact */
        if (managedCursor.first() == true) {
            /* return the phone number found in the DB */
            return managedCursor.getString(1);
        }
        else{
            Log.e(COMMLOG, "Did not find contact phone number in DB.");
            return null;
        }
            
    }
    
    /**
     * Handle communication kind.
     * 
     * @param commKind the comm kind
     * @param extras the extras
     */
    private void handleCommunicationKind(int kind, Bundle extras) {
            /* get the comm "kind" defined in the content provider*/
                                
         switch (kind){
         case Contacts.ContactMethods.EMAIL_KIND:
             /* get the email type and start the email composer */
             if (extras.containsKey(Contacts.ContactMethodsColumns.TYPE) == true)
             {
                 composeEmailToEmailType(Contacts.ContactMethods.EMAIL_KIND, extras.getInt(Contacts.ContactMethodsColumns.TYPE));
             }
             else
             {
                 Log.i(COMMLOG, "Invalid Email Type");
             }
             break;
         case Contacts.ContactMethods.JABBER_IM_KIND:
             /* get the jabber id to start the chat */
             String jabberUri = getJabberIdFromDB();
             if (null != jabberUri)
             {
                 /* Use the Gtalk class to handle chat messages */
                 Intent i = new Intent(this, GTalkClient.class);
                 /* pass the contact name and number from the contact app */
                 i.putExtra(RECIPIENT_URI, jabberUri); //contact jabber uri
                 i.putExtra(RECIPIENT_NAME, m_Name); //contact name
                 startActivity(i);
             } else {
                 Toast.makeText(this,m_Name + ": " + getText(R.string.gtalk_cannot_send), Toast.LENGTH_SHORT).show();
             }
             
             break;
         }
    }
    
    /**
     * Start the email app to the desired email address associated with the email type.
     * 
     * @param EmailType the email type.
     */    
     private void composeEmailToEmailType(int emailKind, int emailType){
         /* build the projection to get the contact method type and email address */
         String[] projection = new String[] { 
                 android.provider.Contacts.ContactMethodsColumns.TYPE, /* 0 - email type (i.e. general, home, work, etc.) */
                 android.provider.Contacts.ContactMethodsColumns.DATA, /* 1 - the email address */
                 };

         /* build the uri based on the contact index (i.e. the contact selected) from the contact app */
         //content://org.magnus.contentprovider.Contacts/people/9/contact_methods
         Uri uri = Contacts.People.CONTENT_URI.buildUpon().appendPath(""+m_contactIndex).appendPath(Contacts.People.ContactMethods.CONTENT_DIRECTORY).build();
  
         /* using the URI created, query the DB for the email type and retrieve the email address */
         Cursor managedCursor = managedQuery(
                 uri, projection,
                 Contacts.ContactMethodsColumns.KIND + "=" + emailKind
                 + " AND " + 
                 Contacts.ContactMethodsColumns.TYPE + "=" + emailType,
                 null);
         
         /* check to make sure we have a valid entry for contact */
         if (managedCursor.first() == true) {
            
             /* Get the email address from the query */
             //TODO - verify valid address
           //  String commUri = "mailto:" + managedCursor.getString(1);
             String email = managedCursor.getString(1);
             
             Log.i(COMMLOG, "Using email address: " + email);
             
             /* launch the composer application with the TO address filled out */
             //http://code.google.com/android/reference/android/content/Intent.html#VIEW_ACTION
             //String commAction = Intent.VIEW_ACTION;
             
             /* increment the frequency of use for this contact method */
             updateContactEmailFrequency(emailKind,emailType);
             
             /* start the composer view with the contact's URI in TO field */
             //TODO - startCommActivity(commAction, commUri);            
             Intent i = new Intent(this,SendMail.class);
             /* pass the index of the contact from the contact app */
             i.putExtra(RECIPIENT_URI, email); //contact db id
             startActivity(i);
         } else { 
             Toast.makeText(this, m_Name + ": " + getText(R.string.contact_without_email), Toast.LENGTH_SHORT).show();
         }
     }
    
     private String getJabberIdFromDB(){
         
         String jabberId = null;
         
         /* build the projection to get the contact method type and email address */
         String[] projection = new String[] { 
                  android.provider.Contacts.ContactMethodsColumns.KIND, /* 0 - jabber kind */
                  android.provider.Contacts.ContactMethodsColumns.DATA, /* 1 - the jabber address */
                 };

         /* build the uri based on the contact index (i.e. the contact selected) from the contact app */
         //content://org.magnus.contentprovider.Contacts/people/9/contact_methods
         Uri uri = Contacts.People.CONTENT_URI.buildUpon().appendPath(""+m_contactIndex).appendPath(Contacts.People.ContactMethods.CONTENT_DIRECTORY).build();
  
         /* using the URI created, query the DB for the email type and retrieve the email address */
         Cursor managedCursor = managedQuery(
                 uri, projection,
                 Contacts.ContactMethodsColumns.KIND + "=" + Contacts.ContactMethods.JABBER_IM_KIND,
                 null);
         
         /* check to make sure we have a valid entry for contact */
         if (managedCursor.first() == true) {
            
             /* Get the email address from the query */
             //TODO - verify valid address
             jabberId = managedCursor.getString(1);
             
             /* currently supporting gmail.com only */
             jabberId = jabberId + "@gmail.com";
             
             Log.i(COMMLOG, "Using jabber address: " + jabberId);           

         }         
         return jabberId;
             
     }
     

    /**
     * Start comm activity by creating a new intent.
     * 
     * @param commAction the communication action
     * @param commUri the uri used to start the intent
     */
    private void startCommActivity(String commAction, String commUri){
        /* create a new intent */
        Intent commIntent = new Intent();

        if ((null != commAction) && 
            (null != commUri)){
            
            /* Set the action type for the intent */
            commIntent.setAction(commAction);
    
            /* associate the URI with the intent */
            commIntent.setData(Uri.parse(commUri));
            startActivity(commIntent);
        } else
        {
            Log.e(COMMLOG, "Action or URI are invalid, cannot start comm intent");
        }
    }
        
    
    private void updateContactPhoneFrequency(int phoneType)
    {
       
        // update the frequency of contact's mobile number
        long freq=-1;
        long phone_id=-1;
        String[] projection = new String[] { 
                android.provider.BaseColumns._ID,
                Contacts.PhonesColumns.FREQUENCY
                };
        Uri uri = Contacts.People.CONTENT_URI.buildUpon().appendPath(""+m_contactIndex).appendPath(Contacts.People.Phones.CONTENT_DIRECTORY).build();
        Cursor managedCursor = managedQuery(
                uri, projection,
                Contacts.PhonesColumns.TYPE + "=" + phoneType,
                null);
        if(!managedCursor.isLast()) {
            managedCursor.next();
            phone_id = managedCursor.getLong(0);
            freq = managedCursor.getLong(1);
        }
        if(phone_id<0) {
            Log.e(COMMLOG,"Mobile number not found");
        } else {
            ContentValues values = new ContentValues();
            values = new ContentValues();
            values.put(Contacts.PhonesColumns.FREQUENCY,freq+1);
            getContentResolver().update(
                    Uri.withAppendedPath(Contacts.Phones.CONTENT_URI,""+phone_id), values, null, null);
        }
        
    }
    
    private void updateContactEmailFrequency(int emailKind, int emailType)
    {
       
        // update the frequency on contact's email address
        long freq=-1;
        long email_id=-1;
        String[] projection = new String[] { 
                android.provider.BaseColumns._ID,
                Contacts.ContactMethodsColumns.FREQUENCY
                };
        Uri uri = Contacts.People.CONTENT_URI.buildUpon().appendPath(""+m_contactIndex).appendPath(Contacts.People.ContactMethods.CONTENT_DIRECTORY).build();
        Cursor managedCursor = managedQuery(
                uri, projection,
                Contacts.ContactMethodsColumns.KIND + "=" + emailKind
                + " AND " +
                Contacts.ContactMethodsColumns.TYPE + "=" + emailType,
                null);
        if(!managedCursor.isLast()) {
            managedCursor.next();
            email_id = managedCursor.getLong(0);
            freq = managedCursor.getLong(1);
        }
        if(email_id<0) {
            Log.e(COMMLOG,"Email address not found");
        } else {
            ContentValues values = new ContentValues();
            values = new ContentValues();
            values.put(Contacts.ContactMethodsColumns.FREQUENCY,freq+1);
            getContentResolver().update(
                    Uri.withAppendedPath(Contacts.ContactMethods.CONTENT_URI,""+email_id)
                    ,values, null, null);
        }
        
    }
    

    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        /* when end key is pressed (or app exists) we set RESULT to OK */
        setResult(RESULT_OK);
        /* we are done with the comm app "activity is done and should be closed" so just exit */
        finish();
    }

}
