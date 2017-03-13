package edu.auburn.eng.csse.comp3710.josephfleming;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import edu.auburn.eng.csse.comp3710.josephfleming.R;



 
public class SplashScreen extends Activity {
 
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
 
        //Thread for splash screen display
        Thread splash_screen = new Thread(){
        	public void run(){
        		try{
        			sleep(5000);
        			Log.i("Sleepy","slept the thread");
        			Intent i = new Intent("edu.auburn.csse.comp3710.josephfleming.MAINACTIVITY");
        			startActivity(i);
        		}
        		catch(Exception e){
        			Log.i("broken", "try failed");
        			e.printStackTrace();
        		}
        		finally{
        			
        			finish();
        		}
        	}
        };
        splash_screen.start();
    }

 

    
 
}
