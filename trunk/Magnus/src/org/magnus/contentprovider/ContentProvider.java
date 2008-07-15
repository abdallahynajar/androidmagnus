package org.magnus.contentprovider;




import java.util.HashMap;

import java.util.Vector;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.UriMatcher;
import android.content.ContentValues;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * The Class ContentProvider.
 */
public class ContentProvider extends android.content.ContentProvider {

    public static final String AUTHORITY = "org.magnus.contentprovider.Contacts";
   // public static final ContentURI CONTENT_URI = ContentURI.create("content://org.magnus.contacts");
    //public static final ContentURI PEOPLE_URI = ContentURI.create("content://org.magnus.contacts/people");
  
    private SQLiteDatabase mDB;
    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 7;
    /** identifier for the logger */
    private static String TAG = "contacts";


    /**
     * The Class DatabaseHelper.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        /*
         * (non-Javadoc)
         * 
         * @see android.content.ContentProviderDatabaseHelper#onCreate(android.database.sqlite.SQLiteDatabase)
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE people (_id INTEGER PRIMARY KEY AUTOINCREMENT,_sync_account TEXT,_sync_id TEXT,_sync_time TEXT,_sync_version TEXT,_sync_local_id INTEGER,_sync_dirty INTEGER,_sync_mark INTEGER,name TEXT NOT NULL,notes TEXT,photo TEXT,company TEXT,title TEXT,preferred_phone INTEGER,preferred_email INTEGER, frequency INTEGER NOT NULL default 0);";
            db.execSQL(sql);
            sql = "CREATE TABLE tagLookup (tag TEXT,person_id INTEGER);";
            db.execSQL(sql);
            sql = "CREATE INDEX tagIndex ON tagLookup (tag);";
            db.execSQL(sql);
            sql = "CREATE TABLE peopleLookup (token TEXT,source INTEGER);";
            db.execSQL(sql);
            sql = "CREATE INDEX peopleLookupIndex ON peopleLookup (token,source);";
            db.execSQL(sql);
            sql = "CREATE INDEX peopleSyncIdIndex ON people (_sync_id);";
            db.execSQL(sql);
            sql = "CREATE TABLE _deleted_people (_sync_version TEXT,_sync_id TEXT,_sync_account TEXT,_sync_mark INTEGER)";
            db.execSQL(sql);
            sql = "CREATE TABLE phones (_id INTEGER PRIMARY KEY AUTOINCREMENT,person INTEGER,type INTEGER,number TEXT,number_key TEXT,label TEXT, frequency INTEGER NOT NULL default 0);";
            db.execSQL(sql);
            sql = "CREATE INDEX phonesIndex1 ON phones (person);";
            db.execSQL(sql);
            sql = "CREATE INDEX phonesIndex2 ON phones (number_key);";
            db.execSQL(sql);
            sql = "CREATE TABLE contact_methods (_id INTEGER PRIMARY KEY AUTOINCREMENT,person INTEGER,kind INTEGER,data TEXT,aux_data TEXT,type INTEGER,label TEXT, frequency INTEGER NOT NULL default 0);";
            db.execSQL(sql);
            sql = "CREATE INDEX contactMethodsPeopleIndex ON contact_methods (person);";
            db.execSQL(sql);
            sql = "CREATE TABLE calls (_id INTEGER PRIMARY KEY AUTOINCREMENT,number TEXT,number_key TEXT,number_type TEXT,date INTEGER,duration INTEGER,type INTEGER,person INTEGER,new INTEGER);";
            db.execSQL(sql);
            sql = "CREATE TRIGGER preferred_phone_cleanup DELETE ON phones BEGIN UPDATE people SET preferred_phone = NULL WHERE preferred_phone = old._id; END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER contact_cleanup DELETE ON people BEGIN DELETE FROM peopleLookup WHERE source = old._id;DELETE FROM phones WHERE person = old._id;DELETE FROM contact_methods WHERE person = old._id;DELETE FROM presence WHERE person = old._id;UPDATE calls SET person = NULL WHERE person = old._id;END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER peopleLookup_update UPDATE OF name ON people BEGIN DELETE FROM peopleLookup WHERE source = new._id;SELECT _TOKENIZE('peopleLookup', new._id, new.name, ' ');END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER peopleLookup_insert AFTER INSERT ON people BEGIN SELECT _TOKENIZE('peopleLookup', new._id, new.name, ' ');END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER phones_update UPDATE ON phones BEGIN UPDATE people SET _sync_dirty=1 WHERE people._id=old.person;END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER phones_insert INSERT ON phones BEGIN UPDATE people SET _sync_dirty=1 WHERE people._id=new.person;END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER phones_delete DELETE ON phones BEGIN UPDATE people SET _sync_dirty=1 WHERE people._id=old.person;END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER contact_methods_update UPDATE ON contact_methods BEGIN UPDATE people SET _sync_dirty=1 WHERE people._id=old.person;END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER contact_methods_insert INSERT ON contact_methods BEGIN UPDATE people SET _sync_dirty=1 WHERE people._id=new.person;END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER contact_methods_delete DELETE ON contact_methods BEGIN UPDATE people SET _sync_dirty=1 WHERE people._id=old.person;END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER update_frequency_phones UPDATE OF frequency ON phones BEGIN UPDATE people set frequency = frequency + 1 where _id = old.person;END";
            db.execSQL(sql);
            sql = "CREATE TRIGGER update_frequency_contact_methods UPDATE OF frequency ON contact_methods BEGIN UPDATE people set frequency = frequency + 1 where _id = old.person;END";
            db.execSQL(sql);
            sql = "CREATE TABLE IF NOT EXISTS presence (_id INTEGER PRIMARY KEY AUTOINCREMENT,person INTEGER,provider TEXT,account TEXT,server_status INTEGER,user_status TEXT)";
            db.execSQL(sql);
            Vector<String> names = new Vector<String>();
            
            names.add("Indra");
            names.add("Vijay");
            names.add("Nick");
            names.add("Evgeniy");
            names.add("Puno");
            names.add("Kars");
            names.add("Magnus");
            names.add("Albertus");
            preInsert(names,db);
            
        }
        
        private void preInsert(Vector<String> names, SQLiteDatabase db) {
            ContentValues values;
            for(String name: names) {
                values = new ContentValues();
                values.put(Contacts.People.NAME,name);            
                db.insert("people", "company", values);
            }
            
            //fully fill out info for Travis
            values = new ContentValues();
            values.put(Contacts.People.NAME,"Travis");
            long travis_id = db.insert("people", "company", values);
            
            //Travis' phone numbers
            values = new ContentValues();
            values.put("person", travis_id);
            values.put("type", Contacts.People.Phones.MOBILE_TYPE);
            values.put("number", "+31 (0)6 4074 63 47");
            values.put("number_key", "31640746347");
            values.put("label", "Mobile Phone");
            long preferred_phone = db.insert("phones", "label", values);
            values = new ContentValues();
            values.put("person", travis_id);
            values.put("type", Contacts.People.Phones.HOME_TYPE);
            values.put("number", "+31 (0)20 330 0588");
            values.put("number_key", "31203300588");
            values.put("label", "Home Phone");
            db.insert("phones", "label", values);
            
            //Travis' contact methods
            values = new ContentValues();
            values.put("person", travis_id);
            values.put("kind", Contacts.People.ContactMethods.JABBER_IM_KIND);
            values.put("data", "travischoma");
            values.put("label", "GTalk");
            db.insert("contact_methods", "label", values);
            
            values = new ContentValues();
            values.put("person", travis_id);
            values.put("kind", Contacts.People.ContactMethods.EMAIL_KIND);
            values.put("type", Contacts.People.ContactMethods.EMAIL_KIND_HOME_TYPE);
            values.put("data", "travischoma@gmail.com");
            values.put("label", "GMail");
            long preferred_email = db.insert("contact_methods", "label", values);
            
            values = new ContentValues();
            values.put("number", "0800-1212");
            values.put("date",System.currentTimeMillis());
            values.put("duration",600);
            values.put("person", travis_id);
            values.put("new", true);
            db.insert("calls", "number", values); 
            
            values = new ContentValues();
            values.put("provider", "GTalk");
            values.put("account","travischoma@gmail.com");
            values.put("server_status",Contacts.Presence.AVAILABLE);
            values.put("user_status", "On Planet Earth");
            values.put("person", travis_id);
            db.insert("presence", "provider", values); 
            
          
            values = new ContentValues();
            values.put("notes","Remeber to call him on June 8th for his birthday.");
            values.put("company", "Mogelli Ltd");
            values.put("title","Mr");
            values.put("preferred_phone", preferred_phone);
            values.put("preferred_email", preferred_email);
            db.update("people", values, "people._id="+travis_id, null);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.ContentProviderDatabaseHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
         *      int, int)
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            String sql = "DROP TABLE IF EXISTS people";
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS peopleLookup";
            db.execSQL(sql);
            sql = "DROP INDEX IF EXISTS peopleLookupIndex";
            db.execSQL(sql);
            sql = "DROP INDEX IF EXISTS peopleSyncIdIndex";
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS _deleted_people";
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS phones";
            db.execSQL(sql);
            sql = "DROP INDEX IF EXISTS phonesIndex1";
            db.execSQL(sql);
            sql = "DROP INDEX IF EXISTS phonesIndex2";
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS contact_methods";
            db.execSQL(sql);
            sql = "DROP INDEX IF EXISTS contactMethodsPeopleIndex";
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS calls";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS preferred_phone_cleanup";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS contact_cleanup";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS peopleLookup_update";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS peopleLookup_insert";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS phones_update";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS phones_insert";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS phones_delete";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS contact_methods_update";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS contact_methods_insert";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS contact_methods_delete";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS update_frequency_phones";
            db.execSQL(sql);
            sql = "DROP TRIGGER IF EXISTS update_frequency_contact_methods";
            db.execSQL(sql);
            sql = "DROP TABLE IF EXISTS presence";
            onCreate(db);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#delete(android.net.ContentURI,
     *      java.lang.String, java.lang.String[])
     */
    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        int count;
        
        String segment = url.getLastPathSegment();
        switch (URI_MATCHER.match(url)) {
        case PEOPLE:
            count = mDB.delete("people", where, whereArgs);
            break;

        case PEOPLE_ID:
           
            
            count = mDB
                    .delete("people", "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;
        case PHONES:
            count = mDB.delete("phones", where, whereArgs);
            break;

        case PHONES_ID:
            
           
            count = mDB
                    .delete("phones", "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;
        case CONTACTMETHODS:
            count = mDB.delete("contact_methods", where, whereArgs);
            break;

        case CONTACTMETHODS_ID:
            
           
            count = mDB
                    .delete("contact_methods", "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;
        case CALLS:
            count = mDB.delete("calls", where, whereArgs);
            break;

        case CALLS_ID:
             
            
            count = mDB
                    .delete("calls", "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;
        case PRESENCE:
            count = mDB.delete("presence", where, whereArgs);
            break;

        case PRESENCE_ID:
             
           
            count = mDB
                    .delete("presence", "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URL " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#getType(android.net.ContentURI)
     */
    @Override
    public String getType(Uri url) {
        switch (URI_MATCHER.match(url)) {
        case PEOPLE:
            return "vnd.magnus.cursor.dir/person";
        case PEOPLE_ID:
            return "vnd.magnus.cursor.item/person";
        case PEOPLE_PHONES:
            return "vnd.magnus.cursor.dir/person/phone";
        case PEOPLE_PHONES_ID:
            return "vnd.magnus.cursor.item/person/phone";
        case PEOPLE_CONTACTMETHODS:
            return "vnd.magnus.cursor.dir/person/contact-method";
        case PEOPLE_CONTACTMETHODS_ID:
            return "vnd.magnus.cursor.item/person/contact-method";
        case PHONES:
            return "vnd.magnus.cursor.dir/phone";
        case PHONES_ID:
            return "vnd.magnus.cursour.item/phone";
        case CONTACTMETHODS:
            return "vnd.magnus.cursor.dir/contact-method";
        case CONTACTMETHODS_ID:
            return "vnd.magnus.cursour.item/contact-method";
        case CALLS:
            return "vnd.magnus.cursor.dir/call";
        case CALLS_ID:
            return "vnd.magnus.cursour.item/call";
        case PRESENCE:
            return "vnd.magnus.cursor.dir/presence";
        case PRESENCE_ID:
            return "vnd.magnus.cursour.item/presence";
        default:
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#insert(android.net.ContentURI,
     *      android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        long rowID;
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            throw new IllegalArgumentException("ContentValues is empty");
        }

        switch (URI_MATCHER.match(url)) {
        case PEOPLE:
           rowID = mDB.insert("people", "company", values);
           break;
        case PHONES:
            rowID = mDB.insert("phones", "label", values);
            break;
        case CONTACTMETHODS:
            rowID = mDB.insert("contact_methods", "data", values);
            break;
        case CALLS:
            rowID = mDB.insert("calls", "number", values);
            break;
        case PRESENCE:
            rowID = mDB.insert("presence", "account", values);
            break;
         default:
            throw new IllegalArgumentException("Unknown URL " + url);
        }
      
        if (rowID > 0) {
            Uri uri = Uri.withAppendedPath(url,"" + rowID);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }

        throw new SQLException("Failed to insert row into " + url);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {
        DatabaseHelper dbHelper = new DatabaseHelper();
        mDB = dbHelper.openDatabase(getContext(), DATABASE_NAME, null,
                DATABASE_VERSION);
        return (mDB == null) ? false : true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#query(android.net.ContentURI,
     *      java.lang.String[], java.lang.String, java.lang.String[],
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs,
            String sortOrder) {
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy = null;
        HashMap<String,String> projectionMap = new HashMap<String,String>();
        switch (URI_MATCHER.match(uri)) {
        case PEOPLE:
            orderBy = Contacts.People.DEFAULT_SORT_ORDER;
            qb.setTables("people");
            break;
        case PEOPLE_ID:
            qb.setTables("people");
            qb.appendWhere("_id=" + uri.getLastPathSegment());
            break;
        case PEOPLE_PHONES:
            orderBy = Contacts.Phones.DEFAULT_SORT_ORDER;
            qb.setTables("phones, people");
            qb.appendWhere("people._id = phones.person AND person=" + uri.getPathSegments().get(1));
            projectionMap.put(android.provider.BaseColumns._ID, "phones._id");
            projectionMap.put(Contacts.PeopleColumns.NAME, Contacts.PeopleColumns.NAME);
            projectionMap.put(Contacts.Phones.NUMBER, Contacts.Phones.NUMBER);
            projectionMap.put(Contacts.Phones.NUMBER_KEY, Contacts.Phones.NUMBER_KEY);
            projectionMap.put(Contacts.Phones.TYPE, Contacts.Phones.TYPE);
            projectionMap.put(Contacts.Phones.LABEL, Contacts.Phones.LABEL);
            projectionMap.put(Contacts.Phones.PERSON_ID, Contacts.Phones.PERSON_ID);
            projectionMap.put(Contacts.PhonesColumns.FREQUENCY, "phones.frequency");
            qb.setProjectionMap(projectionMap);
            break;
        case PEOPLE_PHONES_ID:
            qb.setTables("phones, people");
            qb.appendWhere("people._id = phones.person AND person=" + uri.getPathSegments().get(1) +
                     "AND phones._id=" + uri.getLastPathSegment());
            projectionMap.put(android.provider.BaseColumns._ID, "phones._id");
            projectionMap.put(Contacts.PeopleColumns.NAME, Contacts.PeopleColumns.NAME);
            projectionMap.put(Contacts.Phones.NUMBER, Contacts.Phones.NUMBER);
            projectionMap.put(Contacts.Phones.NUMBER_KEY, Contacts.Phones.NUMBER_KEY);
            projectionMap.put(Contacts.Phones.TYPE, Contacts.Phones.TYPE);
            projectionMap.put(Contacts.Phones.LABEL, Contacts.Phones.LABEL);
            projectionMap.put(Contacts.Phones.PERSON_ID, Contacts.Phones.PERSON_ID);
            projectionMap.put(Contacts.PhonesColumns.FREQUENCY, "phones.frequency");
            qb.setProjectionMap(projectionMap);
            break;
        case PEOPLE_CONTACTMETHODS:
            orderBy = Contacts.ContactMethods.DEFAULT_SORT_ORDER;
            qb.setTables("contact_methods, people");
            qb.appendWhere("people._id = contact_methods.person AND person=" + uri.getPathSegments().get(1));
            projectionMap.put(android.provider.BaseColumns._ID, "contact_methods._id");
            projectionMap.put(Contacts.PeopleColumns.NAME, Contacts.PeopleColumns.NAME);
            projectionMap.put(Contacts.ContactMethods.DATA, Contacts.ContactMethods.DATA);
            projectionMap.put(Contacts.ContactMethods.AUX_DATA, Contacts.ContactMethods.AUX_DATA);
            projectionMap.put(Contacts.ContactMethods.TYPE, Contacts.ContactMethods.TYPE);
            projectionMap.put(Contacts.ContactMethods.KIND, Contacts.ContactMethods.KIND);
            projectionMap.put(Contacts.ContactMethods.LABEL, Contacts.ContactMethods.LABEL);
            projectionMap.put(Contacts.ContactMethods.PERSON_ID, Contacts.ContactMethods.PERSON_ID);
            projectionMap.put(Contacts.ContactMethodsColumns.FREQUENCY, "contact_methods.frequency");
            qb.setProjectionMap(projectionMap);
            break;
        case PEOPLE_CONTACTMETHODS_ID:
            qb.setTables("contact_methods, people");
            qb.appendWhere("people._id = contact_methods.person AND person=" + uri.getPathSegments().get(1) +
                     "AND contact_methods._id=" + uri.getLastPathSegment());
            projectionMap.put(android.provider.BaseColumns._ID, "contact_methods._id");
            projectionMap.put(Contacts.PeopleColumns.NAME, Contacts.PeopleColumns.NAME);
            projectionMap.put(Contacts.ContactMethods.DATA, Contacts.ContactMethods.DATA);
            projectionMap.put(Contacts.ContactMethods.AUX_DATA, Contacts.ContactMethods.AUX_DATA);
            projectionMap.put(Contacts.ContactMethods.TYPE, Contacts.ContactMethods.TYPE);
            projectionMap.put(Contacts.ContactMethods.KIND, Contacts.ContactMethods.KIND);
            projectionMap.put(Contacts.ContactMethods.LABEL, Contacts.ContactMethods.LABEL);
            projectionMap.put(Contacts.ContactMethods.PERSON_ID, Contacts.ContactMethods.PERSON_ID);
            projectionMap.put(Contacts.ContactMethodsColumns.FREQUENCY, "contact_methods.frequency");
            qb.setProjectionMap(projectionMap);
            break;      
        case PHONES:
            orderBy = Contacts.Phones.DEFAULT_SORT_ORDER;
            qb.setTables("phones");
            break;
        case PHONES_ID:
            qb.setTables("phones");
            qb.appendWhere("_id=" + uri.getLastPathSegment());
            break;
        case CONTACTMETHODS:
            orderBy = Contacts.ContactMethods.DEFAULT_SORT_ORDER;
            qb.setTables("contact_methods");
            break;
        case CONTACTMETHODS_ID:
            qb.setTables("contact_methods");
            qb.appendWhere("_id=" + uri.getLastPathSegment());
            break;
        case CALLS:
            orderBy = Contacts.Calls.DEFAULT_SORT_ORDER;
            qb.setTables("calls");
            break;
        case CALLS_ID:
            qb.setTables("calls");
            qb.appendWhere("_id=" + uri.getLastPathSegment());
            break;
        case PRESENCE:
            qb.setTables("presence");
            break;
        case PRESENCE_ID:
            qb.setTables("presence");
            qb.appendWhere("_id=" + uri.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }

        // If sort order is specified override the default
        if (!TextUtils.isEmpty(sortOrder)) {
            orderBy = sortOrder;
        }

        Cursor c = qb.query(mDB, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
        
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.ContentProvider#update(android.net.ContentURI,
     *      android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public int update(Uri url, ContentValues values, String where,
            String[] whereArgs) {
        int count;
        String segment = url.getLastPathSegment();
        switch (URI_MATCHER.match(url)) {
        case PEOPLE:
            count = mDB.update("people", values, where, whereArgs);
            break;

        case PEOPLE_ID:
            
            count = mDB
                    .update("people", values, "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;
        case PHONES:
            count = mDB.update("phones", values, where, whereArgs);
            break;

        case PHONES_ID:
            
            count = mDB
                    .update("phones", values, "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;
        case CONTACTMETHODS:
            count = mDB.update("contact_methods", values, where, whereArgs);
            break;

        case CONTACTMETHODS_ID:
            
            count = mDB
                    .update("contact_methods", values, "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;
        case CALLS:
            count = mDB.update("calls", values, where, whereArgs);
            break;

        case CALLS_ID:
          
            count = mDB
                    .update("calls", values, "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;
        case PRESENCE:
            count = mDB.update("presence", values, where, whereArgs);
            break;

        case PRESENCE_ID:
          
            count = mDB
                    .update("presence", values, "_id="
                            + segment
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URL " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    private static final int PEOPLE = 1;
    private static final int PEOPLE_ID = 2;
    private static final int PEOPLE_PHONES = 3;
    private static final int PEOPLE_PHONES_ID = 4;
    private static final int PEOPLE_CONTACTMETHODS = 7;
    private static final int PEOPLE_CONTACTMETHODS_ID = 8;

    private static final int DELETED_PEOPLE = 20;

    private static final int PHONES = 9;
    private static final int PHONES_ID = 10;
    private static final int PHONES_FILTER = 14;

    private static final int CONTACTMETHODS = 18;
    private static final int CONTACTMETHODS_ID = 19;

    private static final int CALLS = 11;
    private static final int CALLS_ID = 12;
    private static final int CALLS_FILTER = 15;
    
    private static final int PRESENCE = 21;
    private static final int PRESENCE_ID = 22;

    private static final UriMatcher URI_MATCHER = new UriMatcher(-1);

    static
    {
        URI_MATCHER.addURI(AUTHORITY, "people", PEOPLE);
        URI_MATCHER.addURI(AUTHORITY, "people/#", PEOPLE_ID);
        URI_MATCHER.addURI(AUTHORITY, "people/#/phones", PEOPLE_PHONES);
        URI_MATCHER.addURI(AUTHORITY, "people/#/phones/#", PEOPLE_PHONES_ID);
        URI_MATCHER.addURI(AUTHORITY, "people/#/contact_methods", PEOPLE_CONTACTMETHODS);
        URI_MATCHER.addURI(AUTHORITY, "people/#/contact_methods/#", PEOPLE_CONTACTMETHODS_ID);
        URI_MATCHER.addURI(AUTHORITY, "deleted_people", DELETED_PEOPLE);
        URI_MATCHER.addURI(AUTHORITY, "phones", PHONES);
        URI_MATCHER.addURI(AUTHORITY, "phones/filter/*", PHONES_FILTER);
        URI_MATCHER.addURI(AUTHORITY, "phones/#", PHONES_ID);
        URI_MATCHER.addURI(AUTHORITY, "contact_methods", CONTACTMETHODS);
        URI_MATCHER.addURI(AUTHORITY, "contact_methods/#", CONTACTMETHODS_ID);
        URI_MATCHER.addURI(AUTHORITY, "calls", CALLS);
        URI_MATCHER.addURI(AUTHORITY, "calls/filter/*", CALLS_FILTER);
        URI_MATCHER.addURI(AUTHORITY, "calls/#", CALLS_ID);
        URI_MATCHER.addURI(AUTHORITY, "presence", PRESENCE);
        URI_MATCHER.addURI(AUTHORITY, "presence/#", PRESENCE_ID);
    }

}
