package org.magnus.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.magnus.Magnus;
import org.magnus.R;
import org.magnus.contentprovider.Contacts;

import android.app.Activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Manager extends Activity {
    private ImageView thumbnail;
    private EditText name;
    private EditText mobileNum;
    private EditText homeNum;
    private EditText workNum;
    private EditText email;
    private EditText photo;
    
    // current values in the database;
    private String currentName;
    private String currentMobileNum;
    private String currentHomeNum;
    private String currentWorkNum;
    private String currentEmail;
    
    private Long contactId;
    
    private static final String logTag = "Manager";
    /*
     * Creating new Manager Activity
     */
    @Override
    protected void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        setContentView(R.layout.contact_edit);
        
        contactId = icicle != null ? icicle.getLong(Magnus.CONTACT_ID) : null;
        if (contactId == null) {
            Bundle extras = getIntent().getExtras();            
            contactId = extras != null ? extras.getLong(Magnus.CONTACT_ID) : null;
        }
        
        
        thumbnail = (ImageView)findViewById(R.id.contact_edit_thumbnail);
        name = (EditText)findViewById(R.id.contact_edit_name);
        mobileNum = (EditText)findViewById(R.id.contact_edit_mobilenum);
        homeNum = (EditText)findViewById(R.id.contact_edit_homenum);
        workNum = (EditText)findViewById(R.id.contact_edit_worknum);
        email = (EditText)findViewById(R.id.contact_edit_email);
        photo = (EditText)findViewById(R.id.contact_edit_photo);
      
        populateFields();
       
      
        
        // create view listeners that validate on focus out of the field it is attached to
        OnFocusChangeListener emailFieldListener = new OnFocusChangeListener() {
              public void onFocusChanged(View v, boolean hasFocus) {
                  if(!hasFocus) {
                    if(!isEMailValid(((EditText)v).getText().toString())) {  
                        Toast.makeText(Manager.this,getText(R.string.invalid_email), Toast.LENGTH_SHORT).show();
                        
                    }
                  }
              }
        };
        OnFocusChangeListener phoneFieldListener = new OnFocusChangeListener() {
            public void onFocusChanged(View v, boolean hasFocus) {
                if(!hasFocus) {
                  if(!isPhoneNumberValid(((EditText)v).getText().toString())) {  
                      Toast.makeText(Manager.this, getText(R.string.invalid_phone), Toast.LENGTH_SHORT).show();
                      ((EditText)v).setText(null);
                  }
                }
            }
      };
        email.setOnFocusChangeListener(emailFieldListener);
        mobileNum.setOnFocusChangeListener(phoneFieldListener);
        homeNum.setOnFocusChangeListener(phoneFieldListener);
        workNum.setOnFocusChangeListener(phoneFieldListener);
        
        OnClickListener buttonListener = new OnClickListener(){
            public void onClick(View v){
               Button clicked = (Button)v;

                switch(clicked.getId()) {
                case R.id.contact_edit_save:
                   
                    boolean flag = true;
                    String str = ""; 
                    if(!isEMailValid(email.getText().toString())){
                        if(!flag){str += '\n';}
                        str += getText(R.string.contact_edit_email) + " " + getText(R.string.invalid_email);
                        flag = false;
                    }
                    if(!isPhoneNumberValid(homeNum.getText().toString())){
                        if(!flag){str += '\n';}
                        str += getText(R.string.contact_edit_homenum) + " " + getText(R.string.contact_edit_homenum_hint)  + " " + getText(R.string.invalid_phone);
                        flag = false;
                    }
                    if(!isPhoneNumberValid(workNum.getText().toString())){
                        if(!flag){str += '\n';}
                        str += getText(R.string.contact_edit_worknum) + " " + getText(R.string.contact_edit_worknum_hint)  + " " + getText(R.string.invalid_phone);
                        flag = false;
                    }
                    if(!isPhoneNumberValid(mobileNum.getText().toString())){
                        if(!flag){str += '\n';}
                        str += getText(R.string.contact_edit_mobilenum) + " " + getText(R.string.contact_edit_mobilenum_hint)  + " " + getText(R.string.invalid_phone);
                        flag = false;
                    }
                    if(flag){
                       if(contactId == null) { 
                           saveNewContact();
                       } else {
                           updateContact();
                       }
                    }else{
                        Toast.makeText(Manager.this, str, Toast.LENGTH_SHORT).show();
                    }
                    break;
                    

                }

            }    

        };

        Button saveContact = (Button)findViewById(R.id.contact_edit_save);
        saveContact.setOnClickListener(buttonListener);
    } 
    
    @Override
    protected void onFreeze(Bundle outState) {
        super.onFreeze(outState);
        outState.putLong(Magnus.CONTACT_ID, contactId);
    }

    private void populateFields() {
        if (contactId != null) {
            
            // retrieve contact's man details
            String photoUri;
            String[] projection = new String[] { android.provider.BaseColumns._ID,
                    Contacts.PeopleColumns.NAME,
                    Contacts.PeopleColumns.PHOTO};
            Cursor managedCursor = managedQuery(
                    Uri.withAppendedPath(Contacts.People.CONTENT_URI,""+contactId), projection, //Which columns to return. 
                    null, // WHERE clause--we won't specify.
                    null); // Order-by clause.
            if(!managedCursor.isLast()) {
                managedCursor.next();
                name.setText(managedCursor.getString(1)); 
                photoUri = managedCursor.getString(2);
                if(photoUri != null) {
                    try {
                   /* thumbnail.setImageBitmap(
                            MediaStore.Images.Media.getBitmap(getContentResolver(),
                                    ContentURI.create(photoUri)));*/
                        InputStream is = openFileInput(photoUri);
                        thumbnail.setImageBitmap(BitmapFactory.decodeStream(is));
                    } catch(IOException e) {
                        Log.i(logTag, "Failed to retreive contact photo: IOException: " + e.getMessage());
                    }
                } else {
                    thumbnail.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
                }
                   
                
            }          
            
            // retrieve contact's phone numbers
            projection = new String[] { android.provider.BaseColumns._ID,
                    Contacts.PhonesColumns.TYPE,
                    Contacts.PhonesColumns.NUMBER };
            managedCursor = managedQuery(
                    Contacts.People.CONTENT_URI.buildUpon().appendPath(""+contactId).appendPath(Contacts.People.Phones.CONTENT_DIRECTORY).build(), 
                    projection, //Which columns to return. 
                    null, // WHERE clause--we won't specify.
                    null); // Order-by clause.
            while(!managedCursor.isLast()) {
                managedCursor.next();
                int type = managedCursor.getInt(1);
                switch(type) {
                    case Contacts.PhonesColumns.MOBILE_TYPE:
                        mobileNum.setText(managedCursor.getString(2));
                        break;
                    case Contacts.PhonesColumns.HOME_TYPE:
                        homeNum.setText(managedCursor.getString(2));
                        break;
                    case Contacts.PhonesColumns.WORK_TYPE:
                        workNum.setText(managedCursor.getString(2));
                        break;
                    
                }
            }
            
            // retrieve contact's email
            projection = new String[] { android.provider.BaseColumns._ID,
                    Contacts.ContactMethods.DATA };
            managedCursor = managedQuery(
                    Contacts.People.CONTENT_URI.buildUpon().appendPath(""+contactId).appendPath(Contacts.People.ContactMethods.CONTENT_DIRECTORY).build(), 
                    projection, //Which columns to return. 
                    Contacts.ContactMethods.KIND + "=" + Contacts.ContactMethods.EMAIL_KIND
                    + " AND " +
                    Contacts.ContactMethods.TYPE + "=" + Contacts.ContactMethods.EMAIL_KIND_GENERAL_TYPE,
                    null); // Order-by clause.
            if(!managedCursor.isLast()) {
                managedCursor.next();
                email.setText(managedCursor.getString(1));
            }
            
            if(name.getText() != null) {
                currentName = name.getText().toString();
            }
            if(mobileNum.getText() != null) {
                currentMobileNum = mobileNum.getText().toString();
            }
            if(homeNum.getText() != null) {
                currentHomeNum = homeNum.getText().toString();
            }
            if(workNum.getText() != null) {
                currentWorkNum = workNum.getText().toString();
            } 
            if(email.getText() != null) {
                currentEmail = email.getText().toString();
            }
        }
    }

    private void saveNewContact() {
        // get the photo content uri to save in the people table after creating a thumbnail
        String photoUri = null;
        
        if(photo.getText() != null && photo.getText().toString().startsWith("http")) {
           photoUri = saveThumbnail(photo.getText().toString());
        }
        
        // insert data into the people table
        ContentValues values = new ContentValues();
        values.put(Contacts.People.NAME, name.getText().toString());
        values.put(Contacts.People.PHOTO, photoUri);
        Uri contact = getContentResolver().insert(
                Contacts.People.CONTENT_URI, values);
        
        long contactId = Long.parseLong(contact.getLastPathSegment());
        
        // insert the mobile number
        values = phoneNumberContentValues(contactId, mobileNum.getText().toString(), 
                Contacts.PhonesColumns.MOBILE_TYPE);
        getContentResolver().insert(Contacts.Phones.CONTENT_URI,values);
        
        // insert the home number
        values = phoneNumberContentValues(contactId, homeNum.getText().toString(), 
                Contacts.PhonesColumns.HOME_TYPE);
        getContentResolver().insert(Contacts.Phones.CONTENT_URI,values);
        
        // insert the work number
        values = phoneNumberContentValues(contactId, workNum.getText().toString(), 
                Contacts.PhonesColumns.WORK_TYPE);
        getContentResolver().insert(Contacts.Phones.CONTENT_URI,values);
        
        // insert the email address
        values = new ContentValues();
        values.put(Contacts.ContactMethods.PERSON_ID, contactId);
        values.put(Contacts.ContactMethodsColumns.KIND, Contacts.ContactMethodsColumns.EMAIL_KIND);
        values.put(Contacts.ContactMethodsColumns.TYPE, Contacts.ContactMethodsColumns.EMAIL_KIND_GENERAL_TYPE);
        values.put(Contacts.ContactMethodsColumns.DATA, email.getText().toString());
        getContentResolver().insert(Contacts.ContactMethods.CONTENT_URI,values);
        
       
        
       
        Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
        
    }
    
    private void updateContact() {
        ContentValues values;
        
        // update contact's name 
        if(!currentName.equals(name.getText().toString())) {
           values = new ContentValues();
           values.put(org.magnus.contentprovider.Contacts.People.NAME,
               name.getText().toString());
        
           getContentResolver().update(
                Uri.withAppendedPath(Contacts.People.CONTENT_URI,""+contactId), values, null, null);
        
      
        }
        
        // update contact's phone numbers
        if(mobileNum.getText() != null) {
            updatePhoneNumber(mobileNum.getText().toString(),currentMobileNum,Contacts.PhonesColumns.MOBILE_TYPE);
        } 
        if(homeNum.getText() != null) {
            updatePhoneNumber(homeNum.getText().toString(),currentHomeNum,Contacts.PhonesColumns.HOME_TYPE);
        } 
        if(workNum.getText() != null) {
            updatePhoneNumber(workNum.getText().toString(),currentWorkNum,Contacts.PhonesColumns.WORK_TYPE);
        } 
        
        
        if(email.getText() != null) {
            // if this is a new email for which there is no db entry so we have to insert it
            if(currentEmail.equals("")) {
                values = new ContentValues();
                values.put(Contacts.ContactMethods.PERSON_ID, contactId);
                values.put(Contacts.ContactMethodsColumns.KIND, Contacts.ContactMethodsColumns.EMAIL_KIND);
                values.put(Contacts.ContactMethodsColumns.TYPE, Contacts.ContactMethodsColumns.EMAIL_KIND_GENERAL_TYPE);
                values.put(Contacts.ContactMethodsColumns.DATA, email.getText().toString());
                getContentResolver().insert(Contacts.ContactMethods.CONTENT_URI,values);
                // update existing db record for this number    
            } else if(!currentEmail.equals(mobileNum.getText().toString())) {
                values = new ContentValues();
                values.put(Contacts.ContactMethodsColumns.DATA, email.getText().toString());
                getContentResolver().update(Contacts.ContactMethods.CONTENT_URI,values
                        ,Contacts.ContactMethods.PERSON_ID + "=" + contactId
                        + " AND " +
                        Contacts.ContactMethodsColumns.KIND + "=" + Contacts.ContactMethodsColumns.EMAIL_KIND
                        + " AND " +
                        Contacts.ContactMethodsColumns.TYPE + "=" + Contacts.ContactMethodsColumns.EMAIL_KIND_GENERAL_TYPE
                        ,null);
            }
        }

        if(photo.getText() != null && photo.getText().toString().startsWith("http")) {
            String photoUri = saveThumbnail(photo.getText().toString());
           
               values = new ContentValues();
               values.put(org.magnus.contentprovider.Contacts.People.PHOTO,
                   photoUri);
            
               getContentResolver().update(
                    Uri.withAppendedPath(Contacts.People.CONTENT_URI,""+contactId), values, null, null);
            
               try {
                   InputStream is = openFileInput(photoUri);
                   thumbnail.setImageBitmap(BitmapFactory.decodeStream(is));
                   } catch(IOException e) {
                       Log.e(logTag,"Error retreiving new profile pic from fs: IOException" + e.getMessage());
                   }
            
        }
        
        
        Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
    }
    
    private void updatePhoneNumber(String phoneNum, String dbValue, int type) {
       ContentValues values;
       
       // this is a new number for which there is no db entry so we have to insert it 
       if(dbValue.equals("")) {
           values = phoneNumberContentValues(contactId, phoneNum, 
                   type);
           getContentResolver().insert(Contacts.Phones.CONTENT_URI,values);
       // update existing db record for this number    
       } else if(!currentMobileNum.equals(mobileNum.getText().toString())) {
            values = new ContentValues();
            values.put(Contacts.PhonesColumns.NUMBER, phoneNum);
            //TODO: normalize numbers before inserting them in NUMBER_KEY columns
            values.put(Contacts.PhonesColumns.NUMBER_KEY, phoneNum);
           
           getContentResolver().update(Contacts.Phones.CONTENT_URI,values
                       ,Contacts.Phones.PERSON_ID + "=" + contactId
                        + " AND " +
                        Contacts.PhonesColumns.TYPE + "=" + type,null);
        }
       
        
    }
    
    private ContentValues phoneNumberContentValues(long person, String number, int type) {
        ContentValues values = new ContentValues();
        values.put(Contacts.Phones.PERSON_ID, person);
        values.put(Contacts.PhonesColumns.TYPE, type);
        values.put(Contacts.PhonesColumns.NUMBER, number);
        //TODO: normalize numbers before inserting them in NUMBER_KEY columns
        values.put(Contacts.PhonesColumns.NUMBER_KEY, number);
        return values;
    }
  
    private String saveThumbnail(String url) {
        
        HttpClient httpClient = new HttpClient();
        InputStream is = null;
        int retCode;
        GetMethod getMethod = new GetMethod(url);
        try {
            retCode = httpClient.executeMethod(getMethod);
            if(retCode == HttpURLConnection.HTTP_OK) {
                is = getMethod.getResponseBodyAsStream();
            }
        } catch(HttpException e) {
            Log.i(logTag, "Failed to read photo from url " + url + ": HttpException: " + e.getMessage());
        } catch(IOException e) {
            Log.i(logTag, "Failed to read photo from url " + url + ": IOException: " + e.getMessage());
        }    
        
        if(is != null) {
            
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ContentResolver cr = getContentResolver();
            
            
           
          /*  values.put("name", name.getText().toString());
            values.put("mime_type", "image/jpeg");
            try
            {
                imageURI = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                OutputStream imageOut = cr.openOutputStream(imageURI);
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, imageOut);
                imageOut.close();
                if(bitmap != null)
                {
                    long id = imageURI.getPathLeafId();
                    thumbnailURI = StoreThumbnail(cr, bitmap, id, 122F, 150F, 1);
                  
                } else
                {
                    Log.e(logTag, "MediaStore failed to create thumbnail, removing original");
                   // cr.delete(url, null, null);
                    url = null;
                }
            }
            catch(Exception e)
            {
                Log.e(logTag, "MediaStore failed to insert image", e);
                if(url != null)
                {
                   // cr.delete(url, null, null);
                    url = null;
                }
            }
            */
            return StoreThumbnail(cr,bitmap,contactId,122F,150F,1);
           
            
           
        } else {
            Toast.makeText(this, getText(R.string.invalid_photo_url), Toast.LENGTH_SHORT).show();
        }
        return null;
        
    }
    
    private String StoreThumbnail(ContentResolver cr, Bitmap source, long id, float width, float height, int kind)
    {
        Matrix matrix = new Matrix();
        float scaleX = width / (float)source.width();
        float scaleY = height / (float)source.height();
        matrix.setScale(scaleX, scaleY);
        Bitmap thumb = Bitmap.createBitmap(source, 0, 0, source.width(), source.height(), matrix, true);
        String fileName = contactId + ".jpg";
       
        try
        {
            OutputStream thumbOut = openFileOutput(fileName,MODE_PRIVATE);
            thumb.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, thumbOut);
            
            thumbOut.flush();
            thumbOut.close();
            
        }
        catch(FileNotFoundException ex)
        {
           Log.e(logTag,"Error writing thumbnail to disk: FileNotFoundException" + ex.getMessage());
        }
        catch(IOException ex)
        {
           Log.e(logTag,"Error writing thumbnail to disk: IOException" + ex.getMessage());
        } 
        return fileName;
       
    }
    
    
    
    
    

    /*
     * Verifying E-Mail address for compliance with RFC 2822
     */
    private boolean isEMailValid( String email ) {
        //RFC 2822 symbol definitions for valid local names:
      /*  final String sp = "\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~";
        final String atext = "[a-zA-Z0-9" + sp + "]";
        final String atom = atext + "+";
        final String dotAtom = "\\." + atom;
        final String localPart = atom + "(" + dotAtom + ")*";

        //RFC 1035 symbol definitions for valid domain names:
        final String letter = "[a-zA-Z]";
        final String letDig = "[a-zA-Z0-9]";
        final String letDigHyp = "[a-zA-Z0-9-]";
        final String rfcLabel = letDig + "(" + letDigHyp + "{0,61}" + letDig + ")?";
        final String domain = rfcLabel + "(\\." + rfcLabel + ")*\\." + letter + "{2,6}";

        final String addrSpec = "^" + localPart + "@" + domain + "$";
        final Pattern VALID_PATTERN = Pattern.compile( addrSpec );

        return (VALID_PATTERN.matcher( email ).matches()||(email.length() == 0));
        */
        return true;
    }

    /*
     * Verifying phone number for compliance with RFC 3966
     */
    private static boolean isPhoneNumberValid( String phoneNumber ) {
        //RFC 3966 symbol definitions for valid phone URI
     /*   final String visual_separator     = "(" + "\\-|\\.|\\(|\\)" + ")";
        final String param_unreserved     = "(" + "\\[|\\]|/|:|&|\\+|\\$" + ")";
        final String phonedigit           = "(" + "[0-9]|" + visual_separator + ")";                                              
        final String phonedigit_hex       = "(" + "[0-9A-Fa-f]|\\*|#|" + visual_separator + ")";
        final String pct_encoded          = "(" + "%[0-9A-Fa-f][0-9A-Fa-f]" + ")";
        final String alphanum             = "(" + "[A-Za-z]|[0-9]" + ")";
        final String mark                 = "(" + "\\-|_|\\.|!|~|\\*|'|\\(|\\)" + ")";
        final String unreserved           = "(" + alphanum + "|" + mark + ")";
        final String reserved             = "(" + ";|/|\\?|:|@|&|=|\\+|\\$|," + ")";
        final String paramchar            = "(" + param_unreserved + "|" + unreserved + "|" + pct_encoded + ")";
        final String uric                 = "(" + reserved + "|" + unreserved + "|" + pct_encoded + ")";
        final String pvalue               = "(" + paramchar+"+" + ")";
        final String pname                = "(" + "(" + alphanum + "|\\-)+" + ")";
        final String parameter            = "(" + ";" + pname + "(=" + pvalue + ")?" + ")";
        final String toplabel             = "(" + "[A-Za-z]|([A-Za-z]" + "(" + alphanum + "|\\-)*" + alphanum+")" + ")";
        final String domainlabel          = "(" + alphanum + "|(" + alphanum + "(" + alphanum + "|\\-)*" + alphanum + ")" + ")";
        final String domainname           = "(" + "(" + domainlabel + "\\.)" + "*" + toplabel + "\\.?" + ")";
        final String local_number_digits  = "(" + phonedigit_hex + "*" + "([0-9A-Fa-f]|\\*|#)" + phonedigit_hex + "*" + ")";
        final String global_number_digits = "(" + "\\+" + phonedigit + "*" + "[0-9]" + phonedigit + "*" + ")";
        final String descriptor           = "(" + domainname + "|" + global_number_digits + ")";
        final String context              = "(" + ";phone-context=" + descriptor + ")";
        final String extension            = "(" + ";ext=" + phonedigit + "+" + ")";
        final String isdn_subaddress      = "(" + ";isub=" + uric + "+" + ")";
        final String par                  = "(" + parameter + "|" + extension + "|" + isdn_subaddress + ")";
        final String local_number         = "(" + local_number_digits + par + "*" + context + par + "*" + ")";
        final String global_number        = "(" + global_number_digits + par + "*" + ")";
        final String telephone_uri        = "(" + global_number + "|" + local_number + ")";
*/
        //final Pattern VALID_PATTERN = Pattern.compile( telephone_uri );
      
       // return (VALID_PATTERN.matcher( phoneNumber ).matches()||(phoneNumber.length() == 0));
        return true;
    }
}
