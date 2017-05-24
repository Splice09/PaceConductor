package edu.auburn.eng.csse.comp3710.josephfleming;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import edu.auburn.eng.csse.comp3710.josephfleming.R;

/*
=========================================================================
MAIN ACTIVITY
=========================================================================
 */
public class MainActivity extends Activity {

	//SYSTEM
	private boolean activityRunning;

	//GLOBALS
	private static final String BUTTONVAL = null;
	private static final String ACTIVITYVAL = null;
	private static final String BUTTONTEXT = null;

	//BROADCAST RECEIVER
	private boolean isReceiverRegistered = false;
	private Intent serviceIntent = null;

	//BUTTON
	private Button mStartButton;
	private boolean buttonState = false; //false = inactive

	/*
    =========================================================================
    ACTIVITY CONTROL OVERRIDES
    =========================================================================
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		//Register broadcast receiver
		if(!isReceiverRegistered){
			registerReceiver(broadcastNotificationReceiver, new IntentFilter(MyService.BROADCAST_NOTIFICTION));
			isReceiverRegistered = true;
		}

		//initialize sensor manager
        //sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		//initialize service intent
		serviceIntent = new Intent(this, MyService.class);

		//initialize start button
        mStartButton = (Button)findViewById(R.id.start_button);
        if(savedInstanceState != null){
        	mStartButton.setText(savedInstanceState.getString(BUTTONTEXT));
        }
        mStartButton.setOnClickListener(new View.OnClickListener(){
        	@Override
        	public void onClick(View v){
        		//if music is active
        		if(buttonState){
        			mStartButton.setText("Start");
        			//button control boolean
        			buttonState = false;
                    //stop the service
                    stopService(serviceIntent);
        			//step detection boolean
        	        activityRunning = false;
        		}
        		//if music is inactive
        		else{
        			mStartButton.setText("Stop");
        			//button control boolean
        			buttonState = true;
        			//step detection boolean
        	        activityRunning = true;

					startService(serviceIntent);
        		}
        	}
        });
    }

    @Override
    protected void onPause() {
		super.onPause();
    }

	@Override
	protected void onResume() {
		super.onResume();
	}

    @Override
    public void onDestroy(){
    	super.onDestroy();

		//unregister broadcast receiver
		if(isReceiverRegistered){
			unregisterReceiver(broadcastNotificationReceiver);
			isReceiverRegistered = false;
		}

		//stop the service
		if(serviceIntent != null) {
			stopService(serviceIntent);
		}
    }

	/*
    =========================================================================
    INSTANCE STATE CONTROL OVERRIDES
    =========================================================================
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	outState.putBoolean(BUTTONVAL, buttonState);
    	outState.putBoolean(ACTIVITYVAL, activityRunning);
    	outState.putString(BUTTONTEXT, mStartButton.getText().toString());
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
    	super.onRestoreInstanceState(savedInstanceState);
    	buttonState = savedInstanceState.getBoolean(BUTTONVAL);
    	activityRunning = savedInstanceState.getBoolean(ACTIVITYVAL);
    }

	/*
    =========================================================================
    CONFIGURATION CONTROL OVERRIDES
    =========================================================================
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

	/*
    =========================================================================
    BROADCAST RECEIVER CONTROLS
    =========================================================================
     */
	private void ChangeButton(Intent broadcastIntent){
		String broadcastVal = broadcastIntent.getStringExtra("stop");
		int broadcastIntVal = Integer.parseInt(broadcastVal);

		if(broadcastIntVal == 1){
			//Toast.makeText(this, "Broadcast is working!", Toast.LENGTH_LONG).show();
			mStartButton.setText("Start");
			//button control boolean
			buttonState = false;
			//step detection boolean
			activityRunning = false;
		}
	}

	//set up broadcast receiver
	private BroadcastReceiver broadcastNotificationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent broadcastIntent) {
			ChangeButton(broadcastIntent);
		}
	};
}
