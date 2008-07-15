package org.magnus.search;

import java.util.ArrayList;

import org.magnus.R;
import org.magnus.view.BubbleView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


/**
 * 
 * @author Indra
 * This is the search activity for the contact provider.
 */



public class SearchContacts {
	
	
	/**
     * Standard projection for the interesting columns of Contacts table.
     */
	private static final String[] PROJECTION = new String[]{ 
		org.magnus.contentprovider.Contacts.People._ID,org.magnus.contentprovider.Contacts.People.NAME };
	
	/** Cursor which holds all the contacts details*/
	private Cursor mCursor;
	
	/**EditText view used to get the search string*/
	private EditText searchBox;
	/**The array of all the names in Contacts table*/
	private ArrayList<String> names;
	/**the array of 'id's of entries corresponding to names*/
	private ArrayList<Long> ids;
	
	
	
	private BubbleView view;
	public SearchContacts(BubbleView view) {
	    this.view = view;
	}
	public void search(Context ctx) {
		
		
		//setContentView(R.layout.contact_search);
		final Dialog searchForm = new Dialog(ctx);
		searchForm.setContentView(R.layout.contact_search);
		searchForm.show();
		searchForm.setTitle(ctx.getText(R.string.search_box_title));
	    Button closeButton = (Button)searchForm.findViewById(R.id.search_box_button);
	    closeButton.setOnClickListener( new OnClickListener(){
            public void onClick(View v){
               searchForm.dismiss();       
            }
	    }   
	    
	    );
            
		/**Initialize fields.*/
		searchBox = (EditText)searchForm.findViewById(R.id.search_text);
		searchBox.addTextChangedListener(onSearchTextChange);
		mCursor = ((Activity)ctx).managedQuery(org.magnus.contentprovider.Contacts.People.CONTENT_URI, PROJECTION,
				null, org.magnus.contentprovider.Contacts.People.NAME + " COLLATE NOCASE ASC");
		names = new ArrayList<String>();
		ids = new ArrayList<Long>();
		
		/**Populate the names and ids arrays with cursor results*/
		if(mCursor.first()) {
			int nameColumnIndex=mCursor.getColumnIndex(org.magnus.contentprovider.Contacts.People.NAME);
			int idColumnIndex=mCursor.getColumnIndex(org.magnus.contentprovider.Contacts.People._ID);
			do {
				ids.add(mCursor.getLong(idColumnIndex));
				names.add(mCursor.getString(nameColumnIndex));
			}while(mCursor.next());
		}		
		

	}


		
	private TextWatcher onSearchTextChange = new TextWatcher(){
		public void beforeTextChanged(CharSequence s, int start, int count, int after){}
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		    
		    int startIndex = getStartIndex(searchBox.getText().toString().toUpperCase(),0);
		    int endIndex = getEndIndex(searchBox.getText().toString().toUpperCase(),startIndex);
		    if(startIndex<0) startIndex = 0;
		    view.updateContacts(ids.subList(startIndex, endIndex+1), names.subList(startIndex, endIndex+1));
		}
	};
	
	/**Binary search to get index of the first name beginning with searchString*/
	private int getStartIndex(String searchString, int low){
	   
		int mid,high=names.size()-1;
		while(low<=high) {
			mid=(low+high)/2;
			if(mid==low) {
				if(names.get(low).toUpperCase().startsWith(searchString))
					return low;
				if(names.get(high).toUpperCase().startsWith(searchString))
					return high;
				return -1;
			}
			if(names.get(mid).compareToIgnoreCase(searchString)<0) {
				low=mid;
				continue;
			}
			else {
				high=mid;
				continue;
			}
		}
		return -1;//Should not reach here.
	}
	
	/**Binary search to get index of the last name beginning with searchString*/
	private int getEndIndex(String searchString,int low) {
		int mid,high=names.size()-1;
		String searchString2=searchString+"zzzzz";//Used to find the end position. Need to find a better way to do it.
		while(low<=high) {
			mid=(low+high)/2;
			if(mid==low) {
				if(names.get(high).toUpperCase().startsWith(searchString))
					return high;
				if(names.get(low).toUpperCase().startsWith(searchString))
					return low;
				return -1;
			}
			if(names.get(mid).compareToIgnoreCase(searchString2)<0) {
				low=mid;
				continue;
			}
			else {
				high=mid;
				continue;
			}
		}
		return -1;//Should not reach here.
	}
	
}
