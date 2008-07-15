package org.magnus.contentprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.widget.ImageView;


public class Contacts
{
    public static final class Intents
    {
        public static final class Insert
        {

            public static final String ACTION = "android.intent.action.INSERT";
            public static final String FULL_MODE = "full_mode";
            public static final String NAME = "name";
            public static final String COMPANY = "company";
            public static final String JOB_TITLE = "job_title";
            public static final String NOTES = "notes";
            public static final String PHONE = "phone";
            public static final String PHONE_TYPE = "phone_type";
            public static final String EMAIL = "email";
            public static final String EMAIL_TYPE = "email_type";
            public static final String POSTAL = "postal";
            public static final String POSTAL_TYPE = "postal_type";

            public Insert()
            {
            }
        }


        public Intents()
        {
        }
    }
    
    public static class Calls
            implements BaseColumns, Contacts.PeopleColumns
        {

            public static final Uri CONTENT_URI = Uri.parse("content://org.magnus.contentprovider.Contacts/calls");
            public static final Uri CONTENT_FILTER_URI = Uri.parse("content://org.magnus.contentprovider.Contacts/calls/filter");
            public static final String DEFAULT_SORT_ORDER = "date DESC";
            public static final String CONTENT_TYPE = "vnd.magnus.cursor.dir/calls";
            public static final String CONTENT_ITEM_TYPE = "vnd.magnus.cursor.item/calls";
            public static final String TYPE = "type";
            public static final int INCOMING_TYPE = 1;
            public static final int OUTGOING_TYPE = 2;
            public static final int MISSED_TYPE = 3;
            public static final String NUMBER = "number";
            public static final String NUMBER_KEY = "number_key";
            public static final String NUMBER_TYPE = "number_type";
            public static final String DATE = "date";
            public static final String DURATION = "duration";
            public static final String NEW = "new";
            public static final String PERSON_ID = "person";


            public Calls()
            {
            }
     }

    public static final class Presence
        implements BaseColumns, PresenceColumns, PeopleColumns
    {

        public static final void setPresenceIcon(ImageView icon, int serverStatus)
        {
            switch(serverStatus)
            {
            case 1: // '\001'
                icon.setImageResource(17170678);
                break;

            case 2: // '\002'
                icon.setImageResource(17170680);
                break;

            case 3: // '\003'
                icon.setImageResource(17170679);
                break;

            case 4: // '\004'
                icon.setImageResource(17170681);
                break;

            case 5: // '\005'
            default:
                icon.setImageResource(17170681);
                break;
            }
        }

        public static final Uri CONTENT_URI = Uri.parse("content://org.magnus.contentprovider.Contacts/presence");
        public static final String PERSON_ID = "person";
        public static final String CONTENT_TYPE = "vnd.magnus.cursor.dir/presence";
        public static final String CONTENT_ITEM_TYPE = "vnd.magnus.cursor.item/presence";

        public Presence()
        {
        }
    }

    public static interface PresenceColumns
    {

        public static final String PROVIDER = "provider";
        public static final String ACCOUNT = "account";
        public static final String SERVER_STATUS = "server_status";
        public static final int AVAILABLE = 1;
        public static final int IDLE = 2;
        public static final int AWAY = 3;
        public static final int DO_NOT_DISTURB = 4;
        public static final int OFFLINE = 5;
        public static final String USER_STATUS = "user_status";
    }

    public static final class ContactMethods
        implements BaseColumns, ContactMethodsColumns, PeopleColumns
    {

        public static final CharSequence getDisplayLabel(Context context, int kind, int type, CharSequence label)
        {
            CharSequence display = "";
            switch(kind)
            {
            case 1: // '\001'
                if(type != 3)
                {
                    CharSequence labels[] = context.getResources().getTextArray(17498113);
                    try
                    {
                        display = labels[type];
                    }
                    catch(ArrayIndexOutOfBoundsException e)
                    {
                        display = labels[4];
                    }
                    break;
                }
                if(!TextUtils.isEmpty(label))
                    display = label;
                break;

            case 2: // '\002'
                if(type != 3)
                {
                    CharSequence labels[] = context.getResources().getTextArray(17498114);
                    try
                    {
                        display = labels[type];
                    }
                    catch(ArrayIndexOutOfBoundsException e)
                    {
                        display = labels[4];
                    }
                    break;
                }
                if(!TextUtils.isEmpty(label))
                    display = label;
                break;

            default:
                display = context.getString(17432579);
                break;
            }
            return display;
        }

        public void addPostalLocation(Context context, long postalId, double latitude, double longitude)
        {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues(2);
            values.put("data", Double.valueOf(latitude));
            values.put("aux_data", Double.valueOf(longitude));
            Uri loc = resolver.insert(CONTENT_URI, values);
            long locId = Long.parseLong(loc.getLastPathSegment());
            values.clear();
            values.put("aux_data", Long.valueOf(locId));
            resolver.update(Uri.withAppendedPath(CONTENT_URI,"" + postalId), values, null, null);
        }

        public static final String POSTAL_LOCATION_LATITUDE = "data";
        public static final String POSTAL_LOCATION_LONGITUDE = "aux_data";
        public static final Uri CONTENT_URI = Uri.parse("content://org.magnus.contentprovider.Contacts/contact_methods");
        public static final Uri CONTENT_EMAIL_URI = Uri.parse("content://org.magnus.contentprovider.Contacts/contact_methods/email");
        public static final String CONTENT_TYPE = "vnd.magnus.cursor.dir/contact-method";
        public static final String CONTENT_ITEM_TYPE = "vnd.magnus.cursor.item/contact-method";
        public static final String CONTENT_EMAIL_TYPE = "vnd.magnus.cursor.dir/email";
        public static final String CONTENT_EMAIL_ITEM_TYPE = "vnd.magnus.cursor.item/email";
        public static final String CONTENT_POSTAL_ITEM_TYPE = "vnd.magnus.cursor.item/postal-address";
        public static final String CONTENT_JABBER_ITEM_TYPE = "vnd.magnus.cursor.item/jabber-im";
        public static final String DEFAULT_SORT_ORDER = LABEL + " ASC";
        public static final String PERSON_ID = "person";


        private ContactMethods()
        {
        }
    }

    public static interface ContactMethodsColumns
    {

        public static final String KIND = "kind";
        public static final int EMAIL_KIND = 1;
        public static final int POSTAL_KIND = 2;
        public static final int LOCATION_KIND = 3;
        public static final int JABBER_IM_KIND = 100;
        public static final String TYPE = "type";
        public static final int EMAIL_KIND_GENERAL_TYPE = 0;
        public static final int EMAIL_KIND_HOME_TYPE = 1;
        public static final int EMAIL_KIND_WORK_TYPE = 2;
        public static final int EMAIL_KIND_OTHER_TYPE = 3;
        public static final int POSTAL_KIND_GENERAL_TYPE = 0;
        public static final int POSTAL_KIND_HOME_TYPE = 1;
        public static final int POSTAL_KIND_WORK_TYPE = 2;
        public static final int POSTAL_KIND_OTHER_TYPE = 3;
        public static final String LABEL = "label";
        public static final String DATA = "data";
        public static final String AUX_DATA = "aux_data";
        public static final String FREQUENCY = "frequency";
    }

    public static final class Phones
        implements BaseColumns, PhonesColumns, PeopleColumns
    {

        public static final CharSequence getDisplayLabel(Context context, int type, CharSequence label)
        {
            CharSequence display;
            if(type != 8)
            {
                CharSequence labels[] = context.getResources().getTextArray(17498112);
                try
                {
                    display = labels[type];
                }
                catch(ArrayIndexOutOfBoundsException e)
                {
                    display = labels[4];
                }
            } else
            {
                display = label;
            }
            return display;
        }

        public static final Uri CONTENT_URI = Uri.parse("content://org.magnus.contentprovider.Contacts/phones");
        public static final Uri CONTENT_FILTER_URL = Uri.parse("content://org.magnus.contentprovider.Contacts/phones/filter");
        public static final String CONTENT_TYPE = "vnd.magnus.cursor.dir/phone";
        public static final String CONTENT_ITEM_TYPE = "vnd.magnus.cursor.item/phone";
        public static final String DEFAULT_SORT_ORDER = LABEL + " ASC";
        public static final String PERSON_ID = "person";


        private Phones()
        {
        }
    }

    public static interface PhonesColumns
    {

        public static final String TYPE = "type";
        public static final int HOME_TYPE = 0;
        public static final int MOBILE_TYPE = 1;
        public static final int WORK_TYPE = 2;
        public static final int FAX_TYPE = 3;
        public static final int GENERAL_TYPE = 4;
        public static final int PAGER_TYPE = 5;
        public static final int CAR_TYPE = 6;
        public static final int SATELLITE_TYPE = 7;
        public static final int OTHER_TYPE = 8;
        public static final int INTERNAL_EXTENSION_TYPE = 9;
        public static final String LABEL = "label";
        public static final String NUMBER = "number";
        public static final String NUMBER_KEY = "number_key";
        public static final String FREQUENCY = "frequency";
    }

    public static final class People
        implements BaseColumns, PeopleColumns, PhonesColumns, PresenceColumns
    {
        public static final class ContactMethods
            implements BaseColumns, ContactMethodsColumns, PeopleColumns
        {

            public static final String CONTENT_DIRECTORY = "contact_methods";
            public static final String DEFAULT_SORT_ORDER = "data ASC";

            private ContactMethods()
            {
            }
        }

        public static final class Phones
            implements BaseColumns, PhonesColumns, PeopleColumns
        {

            public static final String CONTENT_DIRECTORY = "phones";
            public static final String DEFAULT_SORT_ORDER = "number ASC";

            private Phones()
            {
            }
        }


        public static final Uri CONTENT_URI = Uri.parse("content://org.magnus.contentprovider.Contacts/people");
        public static final Uri DELETED_CONTENT_URI = Uri.parse("content://org.magnus.contentprovider.Contacts/deleted_people");
        public static final String CONTENT_TYPE = "vnd.magnus.cursor.dir/person";
        public static final String CONTENT_ITEM_TYPE = "vnd.magnus.cursor.item/person";
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        public static final String PREFERRED_PHONE_ID = "preferred_phone";
        public static final String PREFERRED_EMAIL_ID = "preferred_email";
        


        private People()
        {
        }
    }

    public static interface PeopleColumns
    {

        public static final String NAME = "name";
        public static final String COMPANY = "company";
        public static final String TITLE = "title";
        public static final String NOTES = "notes";
        public static final String PHOTO = "photo";
        public static final String FREQUENCY = "frequency";
    }


    private Contacts()
    {
    }

    public static boolean mergeContacts(ContentResolver cr, long mergeSourceId, long mergeTargetId)
    {
        ContentValues values = new ContentValues();
        Uri urlSource = Uri.withAppendedPath(People.CONTENT_URI,"" + mergeSourceId);
        Uri urlTarget = Uri.withAppendedPath(People.CONTENT_URI,"" + mergeTargetId);
        Cursor cSource = cr.query(urlSource, null, null, null, null);
        Cursor cTarget = cr.query(urlTarget, null, null, null, null);
        if(!cSource.first() || !cTarget.first())
        {
            cSource.deactivate();
            cTarget.deactivate();
            return false;
        }
        mergeSourceToTarget(cSource, cTarget, "notes", values);
        mergeSourceToTarget(cSource, cTarget, "photo", values);
        mergeSourceToTarget(cSource, cTarget, "company", values);
        mergeSourceToTarget(cSource, cTarget, "title", values);
        Cursor c;
        for(c = cr.query(Phones.CONTENT_URI, null, (new StringBuilder()).append("person=").append(mergeSourceId).toString(), null, null); c.next(); cr.insert(People.CONTENT_URI.buildUpon().appendPath((new StringBuilder()).append(mergeTargetId).append("/phones").toString()).build(), values))
        {
            values.clear();
            DatabaseUtils.cursorStringToContentValues(c, "type", values);
            DatabaseUtils.cursorStringToContentValues(c, "number", values);
            DatabaseUtils.cursorStringToContentValues(c, "number_key", values);
        }

        c.deactivate();
        for(c = cr.query(ContactMethods.CONTENT_URI, null, (new StringBuilder()).append("person=").append(mergeSourceId).toString(), null, null); c.next(); cr.insert(People.CONTENT_URI.buildUpon().appendPath((new StringBuilder()).append(mergeTargetId).append("/contact_methods").toString()).build(), values))
        {
            values.clear();
            DatabaseUtils.cursorStringToContentValues(c, "label", values);
            DatabaseUtils.cursorStringToContentValues(c, "kind", values);
            DatabaseUtils.cursorStringToContentValues(c, "data", values);
        }

        c.deactivate();
        cSource.deleteRow();
        cSource.deactivate();
        cTarget.deactivate();
        return true;
    }

    private static void mergeSourceToTarget(Cursor mergeSourceCursor, Cursor mergeTargetCursor, String column, ContentValues values)
    {
        int columnIndex = mergeSourceCursor.getColumnIndex(column);
        String targetValue = mergeTargetCursor.getString(columnIndex);
        if(TextUtils.isEmpty(targetValue))
            values.put(column, mergeSourceCursor.getString(columnIndex));
    }

    public static final String AUTHORITY = "org.magnus.contentprovider.Contacts";
    public static final Uri CONTENT_URI = Uri.parse("content://org.magnus.contentprovider.Contacts");

}
