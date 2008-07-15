package org.magnus.view;

/**
 * 
 * @author Vijay amirisetty.vijayaraghavan@gmail.com
 * 
 * This class will display the bubbles on the screen. 
 * This class is the main part of the user interaction
 * 
 * Class is in progress ...
 * TODO
 *  9 patch drawable with text to be used instead as the image Resource
 *  Look at how to make the "non bubble" part of the image transparent
 *  Implement the Motion Listeners
 *  Interface with the content provider
 *  Implement the Layout Algorithm 
 *
 */
import java.util.ArrayList;
import java.util.List;

import org.magnus.R;

import android.app.Activity;
import android.database.Cursor;
import android.util.DisplayMetrics;
import android.widget.AbsoluteLayout;

public class BubbleView extends AbsoluteLayout {
    
    
    private ContactBubble[] contactBubbles;
    private List<String> contactNames;
    private List<Long> contactIds;
    private  double[] xPosition;
    private  double[] yPosition;
    private Activity ctx;
    AbsoluteLayout.LayoutParams pos; 
    public BubbleView(Activity ctx) {
        super(ctx);
        this.ctx = ctx;
        drawBubbleView();
    }
    
    public void updateContacts(List<Long> contactIds, List<String> contactNames) {
        this.contactNames = contactNames;
        this.contactIds = contactIds;
        removeAllViews();
        drawBubbleView();
        
    }
    
    public void drawBubbleView() {
        
        
        
        pos = new AbsoluteLayout.LayoutParams(0,0,25,25);
        
       
        //Adding the images and names to the Layout
        // populate the arrays with bubble size and position
        // Need to replace this with a layout algorithm
        // Assuming the data to be static for now. Get this from the provider later
        
        if(xPosition == null) {
        xPosition=new double[19]; //{100,110, 190, 200, 190, 100,40,35,40};
        yPosition=new double[19]; //{66,0,5,66,133,120,133,66,0};
        //hexagonal tiling
        DisplayMetrics dm = new DisplayMetrics();
        ctx.getWindowManager().getDefaultDisplay().getMetrics(dm);         
        int width =dm.widthPixels/8;
        int height = dm.heightPixels/5;
        
        for(int i=0;i<19;i++){
            switch(i){
            case 0:
                xPosition[i] = width *4;
                yPosition[i] = height * 2.5;
                break;
            case 1:
                xPosition[i] = width *4;
                yPosition[i] = height * 1.5;
                break;
            case 2:
                xPosition[i] = width *5.5;
                yPosition[i] = height * 2;
                break;
            case 3:
                xPosition[i] = width *5.5;
                yPosition[i] = height * 3;
                break;
            case 4:
                xPosition[i] = width *4;
                yPosition[i] = height * 3.5;
                break;
            case 5:
                xPosition[i] = width *2.28;
                yPosition[i] = height * 3;
                break;
            case 6:
                xPosition[i] = width *2.28;
                yPosition[i] = height * 2;
                break;
            case 7:
                xPosition[i] = width *4;
                yPosition[i] = height * 0.5;
                break;
            case 8:
                xPosition[i] = width *5.5;
                yPosition[i] = height ;
                break;
            case 9:
                xPosition[i] = width *7;
                yPosition[i] = height * 1.5;
                break;
            case 10:
                xPosition[i] = width *7;
                yPosition[i] = height * 2.5;
                break;
            case 11:
                xPosition[i] = width *7;
                yPosition[i] = height * 3.5;
                break;
            case 12:
                xPosition[i] = width *5.28;
                yPosition[i] = height * 4;
                break;
            case 13:
                xPosition[i] = width *4;
                yPosition[i] = height * 4.5;
                break;
            case 14:
                xPosition[i] = width *2.28;
                yPosition[i] = height * 4;
                break;
            case 15:
                xPosition[i] = width *0.5;
                yPosition[i] = height * 3.5;
                break;
            case 16:
                xPosition[i] = width *0.5;
                yPosition[i] = height * 2.5;
                break;
            case 17:
                xPosition[i] = width *0.5;
                yPosition[i] = height * 1.5;
                break;
            case 18:
                xPosition[i] = width *2.5;
                yPosition[i] = height * 1;
                break;                
            
            }
            //x and y offset
            xPosition[i]-= width /2;
            yPosition[i]-=height/2;
        }
        }
       
        if(contactNames == null) {
            contactNames = new ArrayList<String>();
            contactIds = new ArrayList<Long>();
            String[] projection = new String[] { android.provider.BaseColumns._ID,
                    android.provider.Contacts.PeopleColumns.NAME };
            Cursor managedCursor = ctx.managedQuery(
                    org.magnus.contentprovider.Contacts.People.CONTENT_URI, projection, //Which columns to return. 
                    null, // WHERE clause--we won't specify.
                    org.magnus.contentprovider.Contacts.People.NAME + " ASC"); // Order-by clause.
            int curPos=0;
            while(!managedCursor.isLast()) {
                managedCursor.next();
   
                contactIds.add(managedCursor.getLong(0));
                contactNames.add(managedCursor.getString(1));
                curPos++;
            
            } 

        }
       

        updateBubbles();
        this.setBackground(R.drawable.waterxp);
    }
    
    public void updateBubbles() {
        contactBubbles  = new ContactBubble[20];
        int numberOfTimesContacted[] = new int[]{86,84,76,74,72,72,72,80,80,80,80,80,80,80,80,80,80,80,80,80,};
        for(int i=0;i<contactIds.size();i++){
            
            contactBubbles[i]=new ContactBubble(ctx,contactIds.get(i).longValue(),i);
            

            
            contactBubbles[i].setText(contactNames.get(i));
         
            
            
            
            pos = new AbsoluteLayout.LayoutParams(numberOfTimesContacted[i],numberOfTimesContacted[i],(int)xPosition[i],(int)yPosition[i]);
            contactBubbles[i].setLayoutParams(pos);
            contactBubbles[i].setTextSize(10);
                      
           addView(contactBubbles[i], pos);
                        
        }
        
    }
}
