package org.magnus.view;

import org.magnus.R;
import org.magnus.communication.Communication;
import org.magnus.contentprovider.Contacts;

import android.widget.Button;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class ContactBubble extends Button {
    private static Animation grow;
    public int state; // the state of interaction
    private int index;
    private long id;
    // 0 -> inactive state
    // 1 -> the user has clicked the bubble, and the bubble now shows the mode of communication
    // 2 -> user has chosen the  mode of communcation to communicate [CALL/SMS/email/chat]
    
    /* define the currently support communication types */ 
    public static final int CALL = 1;
    public static final int SMS = 2;
    public static final int EMAIL = 3;
    public static final int CHAT = 4;   

    public ContactBubble(final Context ctx, long id, int index){
        super(ctx);
        
        grow =  AnimationUtils.loadAnimation(ctx, R.anim.grow);
        state = 0;
        this.index = index;
        this.id = id;
        setMyDrawable(index);

        
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e){
        //ACTION_DOWN - 0
        //ACTION_UP - 1
        //ACTION_MOVE - 2
        //ACTION_CANCEL - 3
        
        Context ctx = this.getContext();
        int action = e.getAction();
        boolean status = false;
        
        if(action == 0 ){ // KEY DOWN        
            if(state == 0){
                // if a mouse event occurs when the state is 0, change it to state 1
                // change the image to show the mode of communication
                this.setBackground(R.drawable.greenselect);
                state = 1;
                status = true;
            }else if( state == 1){
            
                // get mode of communication
                int communicationMode = getCommunicationMode(e.getX(), e.getY());
            
                /* start new communication intent */
                Intent i = new Intent(ctx, Communication.class);
                
                /* pass the index of the contact */
                i.putExtra(android.provider.BaseColumns._ID, id); //contact db id
                
                /* pass the name of the contact */
                i.putExtra(Contacts.PeopleColumns.NAME, getText().toString());
                
                /* switch based on the comm type selected */
                switch (communicationMode){
                                
                case CALL:
                    //call
                    i.putExtra(Communication.COMM_TYPE, Communication.PHONE_TYPE);
                    i.putExtra(Contacts.PhonesColumns.TYPE, Contacts.PhonesColumns.MOBILE_TYPE);
                    ctx.startActivity(i);
                    //ctx.startSubActivity(i, Communication.ACTIVITY_COMMUNICATE);
                    setMyDrawable(index);
                    state = 0;
                    status = true;
                    break;
            
                case SMS:
                    //SMS
                    i.putExtra(Communication.COMM_TYPE, Communication.SMS_TYPE);
                    i.putExtra(Contacts.PhonesColumns.TYPE, Contacts.PhonesColumns.MOBILE_TYPE);
                    ctx.startActivity(i);
                    setMyDrawable(index);
                    state = 0;
                    status = true;
                    break;
                     
                case EMAIL:
                    // email
                    i.putExtra(Communication.COMM_TYPE, Communication.OTHER_TYPE);
                    i.putExtra(Contacts.ContactMethodsColumns.KIND, Contacts.ContactMethods.EMAIL_KIND);
                    i.putExtra(Contacts.ContactMethodsColumns.TYPE, Contacts.ContactMethods.EMAIL_KIND_GENERAL_TYPE);
                    ctx.startActivity(i);
                    setMyDrawable(index);
                    state = 0;
                    status = true;
                    break;
                
                case CHAT:
                    // gtalk
                    i.putExtra(Communication.COMM_TYPE, Communication.OTHER_TYPE);
                    i.putExtra(Contacts.ContactMethodsColumns.KIND, Contacts.ContactMethods.JABBER_IM_KIND);
                    ctx.startActivity(i);
                    setMyDrawable(index);
                    state = 0;
                    status = true;
                    break;                    
                }
            }
        } else if (action==2){ //ACTION_MOVE
            this.setAnimation(grow);
            
            status = true;
        }      
        return status;
    }
    
   int getCommunicationMode(float x, float y ){
       //gets the mode of communication based upon the location of
       //the user's click within a bubble.
       //for a 76 x 76 image,
       // 0 = no action. clicks outside area of interest
       // 1 = Call
       // 2 = E-mail
       // 3 = SMS   
       // 4 = Chat using gtalk
       // More modes / actions can be added in  the future
       float normX  = x*100/this.getWidth();
       float normY = y*100/this.getHeight();
//       this.getContext().showAlert(null, String.valueOf(normX)+","+String.valueOf(normY),"Cancel", null, true, null);       
       
       if(normY > 57 && normY < 77){
           if(normX >18 && normX <36){

               return CALL;
           }
           if(normX >40 && normX <61){
               return SMS;
           }
           if(normX >61 && normX <83){
               return EMAIL;
           }   
       }    
       
       //NCC - temp, click above SMS icon to start chat
       if ( (normY > 40 && normY < 60) &&
            (normX >40 && normX <61) )
       {
           return CHAT;
       }
           

       
       return 0;
   }
   
   private void setMyDrawable(int i){
       switch(index%6){
       case 0:
           setBackground(R.drawable.blue);
           break;
       case 1:
           setBackground(R.drawable.green);
           break;
       case 2:
           setBackground(R.drawable.orange);
           break;
       case 3:
           setBackground(R.drawable.purple);
           break;
       case 4:
           setBackground(R.drawable.red);
           break;
       case 5:
           setBackground(R.drawable.yellow);
           break;
       
           }
   }
    
    

}
