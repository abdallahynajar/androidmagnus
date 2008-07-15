package org.magnus;


import org.magnus.communication.Communication;
import org.magnus.contentprovider.Contacts;
import org.magnus.search.SearchContacts;
import org.magnus.view.BubbleView;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Menu.Item;

public class Magnus extends Activity {
    
    private static final int SEARCH_ID = Menu.FIRST;
    private static final int NEWCONTACT_ID = Menu.FIRST + 1;
    private static final int TEST_GLVIEW = Menu.FIRST + 2;
    
    public static final int ACTIVITY_CONTACT_EDIT=0;
    
    public static final String CONTACT_ID = "contact_id";
    private BubbleView a;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        a = new BubbleView(Magnus.this);
        setContentView(a);
        
       

        //IGNORE - content-provider testing
       // testContentProvider();


    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SEARCH_ID, R.string.menu_search);
        menu.add(0, NEWCONTACT_ID, R.string.menu_newcontact);
        menu.add(0, TEST_GLVIEW, R.string.test_glview);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, Item item) {
        super.onMenuItemSelected(featureId, item);
        switch(item.getId()) {
        case SEARCH_ID:
           // Intent i = new Intent(Magnus.this, org.magnus.search.SearchActivity.class);
           // startSubActivity(i, Communication.ACTIVITY_COMMUNICATE);
            SearchContacts sc = new SearchContacts(a);
            sc.search(this);
            break;
        case NEWCONTACT_ID:
            Intent manage = new Intent(Magnus.this,org.magnus.manager.Manager.class);
            startActivity(manage);
            break;
        case TEST_GLVIEW:
            Intent testViewIntent = new Intent(Magnus.this,org.magnus.glview.testBubbles.class);
            startActivity(testViewIntent);
            break;
        }
        
        return true;
    }
    

    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, java.lang.String, android.os.Bundle)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            String data, Bundle extras) {
        super.onActivityResult(requestCode, resultCode, data, extras);

        switch(requestCode) {
        case Communication.ACTIVITY_COMMUNICATE:
            if (resultCode == RESULT_OK){
                //communication success
            }
        }
    }

    public void testContentProvider() {
        //Testing the content provider

        //IGNORE -content provider testing
        //insert
        // Save the name and description in a map. Key is the content provider's
        // column name, value is the value to save in that record field.
        //ContentValues values = new ContentValues();
        //values.put(org.magnus.contentprovider.Contacts.People.NAME,
        //        "Travis Lee Choma");
        // It returns the URI of the new record.
        //ContentURI uri = getContentResolver().insert(
        //        org.magnus.contentprovider.Contacts.People.CONTENT_URI, values);
        //query
        // An array specifying which columns to return. 
        // The provider exposes a list of column names it returns for a specific
        // query, or you can get all columns and iterate through them. 
        String testTag = "test";

        // change the name of the person with id 1
        ContentValues values = new ContentValues();
        values.put(org.magnus.contentprovider.Contacts.People.NAME,
        "Bob Dob");
       

        // delete the person with id 2
        getContentResolver().delete(
                Uri.withAppendedPath(Contacts.People.CONTENT_URI,""+ 2), null, null);

        // select all the people and print their names
        String[] projection = new String[] { android.provider.BaseColumns._ID,
                android.provider.Contacts.PeopleColumns.NAME };
        Cursor managedCursor = managedQuery(
                Contacts.People.CONTENT_URI, projection, //Which columns to return. 
                null, // WHERE clause--we won't specify.
                Contacts.People.NAME + " ASC"); // Order-by clause.
        while(!managedCursor.isLast()) {
            managedCursor.next();
            Log.i(testTag, "Result: " + managedCursor.getString(1));
        } 

        // select out the full details on Travis
        managedCursor = managedQuery(
                Contacts.People.CONTENT_URI, null, // null projection returns all columns. 
                Contacts.People.NAME + " LIKE '%ravis%'",
                null);
        managedCursor.first();
        long travis_id = managedCursor.getLong(0);
        Log.i(testTag, "Details on Travis:");
        managedCursor.first();
        String[] columnNames = managedCursor.getColumnNames();
        int numColumns = columnNames.length;
        for(int x=0; x < numColumns; x++) {
            Log.i(testTag, columnNames[x] + ": " + managedCursor.getString(x));
        }

        // this query returns a subdirectory containing all of Travis' phone numbers
        projection = new String[] { 
                android.provider.Contacts.PeopleColumns.NAME,
                android.provider.Contacts.PhonesColumns.LABEL,
                android.provider.Contacts.PhonesColumns.TYPE,
                android.provider.Contacts.PhonesColumns.NUMBER,
                android.provider.Contacts.PhonesColumns.NUMBER_KEY};
        Uri uri = Contacts.People.CONTENT_URI.buildUpon().appendPath(""+travis_id).appendPath(Contacts.People.Phones.CONTENT_DIRECTORY).build();
        managedCursor = managedQuery(
                uri, projection,
                null,
                null);


        Log.i(testTag, "Details on Travis' Phones:");
        while(!managedCursor.isLast())  {
            managedCursor.next(); 
            columnNames = managedCursor.getColumnNames();
            numColumns = columnNames.length;
            for(int x=0; x < numColumns; x++) {
                Log.i(testTag, columnNames[x] + ": " + managedCursor.getString(x));
            }

        }

        // this query returns a subdirectory of all travis' contact methods
        projection = new String[] { 
                android.provider.Contacts.PeopleColumns.NAME,
                android.provider.Contacts.ContactMethodsColumns.LABEL,
                android.provider.Contacts.ContactMethodsColumns.KIND,
                android.provider.Contacts.ContactMethodsColumns.DATA};

        uri = Contacts.People.CONTENT_URI.buildUpon().appendPath(""+travis_id).appendPath(Contacts.People.ContactMethods.CONTENT_DIRECTORY).build();
        managedCursor = managedQuery(
                uri, projection, 
                null,
                null);

        Log.i(testTag, "Details on Travis' Contact Methods:");
        while(!managedCursor.isLast())  {
            managedCursor.next(); 
            columnNames = managedCursor.getColumnNames();
            numColumns = columnNames.length;
            for(int x=0; x < numColumns; x++) {
                Log.i(testTag, columnNames[x] + ": " + managedCursor.getString(x));
            }
        }

        // select all the all the phones in the db
        projection = new String[] { 
                android.provider.Contacts.PhonesColumns.LABEL,
                android.provider.Contacts.PhonesColumns.TYPE,
                android.provider.Contacts.PhonesColumns.NUMBER,
                android.provider.Contacts.PhonesColumns.NUMBER_KEY};
        uri = Contacts.Phones.CONTENT_URI;
        managedCursor = managedQuery(
                uri, projection,
                null,
                null);


        Log.i(testTag, "All phones in the db:");
        while(!managedCursor.isLast())  {
            managedCursor.next(); 
            columnNames = managedCursor.getColumnNames();
            numColumns = columnNames.length;
            for(int x=0; x < numColumns; x++) {
                Log.i(testTag, columnNames[x] + ": " + managedCursor.getString(x));
            }

        }

        // select all  the calls logged in the db
        uri = Contacts.Calls.CONTENT_URI;
        managedCursor = managedQuery(
                uri, null,
                null,
                null);


        Log.i(testTag, "All calls logged in the db:");
        while(!managedCursor.isLast())  {
            managedCursor.next(); 
            columnNames = managedCursor.getColumnNames();
            numColumns = columnNames.length;
            for(int x=0; x < numColumns; x++) {
                Log.i(testTag, columnNames[x] + ": " + managedCursor.getString(x));
            }

        }

        // select all the all the presence data currently in the db
        uri = Contacts.Presence.CONTENT_URI;
        managedCursor = managedQuery(
                uri, null,
                null,
                null);

        long presence_id=-1;
        Log.i(testTag, "All presence data currently in the db:");
        while(!managedCursor.isLast())  {
            managedCursor.next(); 
            columnNames = managedCursor.getColumnNames();
            numColumns = columnNames.length;
            presence_id = managedCursor.getLong(0);
            for(int x=0; x < numColumns; x++) {
                Log.i(testTag, columnNames[x] + ": " + managedCursor.getString(x));
            }

        }

        // update presence data in db to reflect that we just got an xmpp update that Travis is busy
        uri = Uri.withAppendedPath(Contacts.Presence.CONTENT_URI,""+presence_id);
        values.clear();
        values.put(org.magnus.contentprovider.Contacts.PresenceColumns.SERVER_STATUS,
                org.magnus.contentprovider.Contacts.PresenceColumns.DO_NOT_DISTURB);
        getContentResolver().update(
                uri, values, null, null);
        Log.i(testTag, "Updated Travis' xmpp status:");

        // select all the all the presence data currently in the db
        uri = Contacts.Presence.CONTENT_URI;
        managedCursor = managedQuery(
                uri, null,
                null,
                null);

        presence_id=-1;
        Log.i(testTag, "All presence data currently in the db:");
        while(!managedCursor.isLast())  {
            managedCursor.next(); 
            columnNames = managedCursor.getColumnNames();
            numColumns = columnNames.length;
            presence_id = managedCursor.getLong(0);
            for(int x=0; x < numColumns; x++) {
                Log.i(testTag, columnNames[x] + ": " + managedCursor.getString(x));
            }

        }
        
        // update the frequency on travis' mobile number
        long freq=-1;
        long phone_id=-1;
        projection = new String[] { 
                android.provider.BaseColumns._ID,
                Contacts.PhonesColumns.FREQUENCY
                };
        uri = Contacts.People.CONTENT_URI.buildUpon().appendPath(""+travis_id).appendPath(Contacts.People.Phones.CONTENT_DIRECTORY).build();
        managedCursor = managedQuery(
                uri, projection,
                Contacts.PhonesColumns.TYPE + "=" + Contacts.PhonesColumns.MOBILE_TYPE,
                null);
        if(!managedCursor.isLast()) {
            managedCursor.next();
            phone_id = managedCursor.getLong(0);
            freq = managedCursor.getLong(1);
        }
        if(phone_id<0) {
            Log.e(testTag,"Travis' mobile number not found");
        } else {
            values = new ContentValues();
            values.put(Contacts.PhonesColumns.FREQUENCY,freq+1);
            getContentResolver().update(
                    Uri.withAppendedPath(Contacts.Phones.CONTENT_URI,"" +phone_id)
                    , values, null, null);

            // check the trigger for the aggregate frequency number for that contact in the people table;
            projection = new String[] {
                    android.provider.BaseColumns._ID,
                    Contacts.PeopleColumns.FREQUENCY
            };
            uri = Uri.withAppendedPath(Contacts.People.CONTENT_URI,""+travis_id);
            managedCursor = managedQuery(
                    uri, projection,
                    null,
                    null);
            if(!managedCursor.isLast()) {
                managedCursor.next();
                long agFreq = managedCursor.getLong(1);
                Log.i(testTag, "Frequency of contact with Travis:" + agFreq );
            }
            
            projection = new String[] {
                    android.provider.BaseColumns._ID,
                    Contacts.PeopleColumns.FREQUENCY
            };
            uri = Uri.withAppendedPath(Contacts.Phones.CONTENT_URI,""+phone_id);
            managedCursor = managedQuery(
                    uri, projection,
                    null,
                    null);
            if(!managedCursor.isLast()) {
                managedCursor.next();
                long mfreq = managedCursor.getLong(1);
                Log.i(testTag, "Frequency of contact with Travis via mobile:" + mfreq );
            }
        }

    }


}