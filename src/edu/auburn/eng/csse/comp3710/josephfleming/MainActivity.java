package edu.auburn.eng.csse.comp3710.josephfleming;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import edu.auburn.eng.csse.comp3710.josephfleming.R;



public class MainActivity extends Activity implements SensorEventListener {
	
	private static final String BUTTONVAL = null;
	private static final String ACTIVITYVAL = null;
	private static final String BUTTONTEXT = null;
//	private static final String RT1PLAY = null;
//	private static final String RT2PLAY = null;
//	private static final String RT3PLAY = null;
//	private static final String RT4PLAY = null;
	private SensorManager sensorManager;
	private Button mStartButton;
	private boolean buttonState = true;
	private boolean activityRunning;
	private long timeStamp;
	private static long CONVERTER = 1000000;
	private static double FIVE_MPH = 324.7;
	private static double SIX_MPH = 261.1;
	private static double EIGHT_MPH = 196.9;
	private static double TEN_MPH = 171.5;
	static MediaPlayer rT1 = null;
	static MediaPlayer rT2 = null;
	static MediaPlayer rT3 = null;
	static MediaPlayer rT4 = null;
//	private boolean rt1playing = false;
//	private boolean rt2playing = false;
//	private boolean rt3playing = false;
//	private boolean rt4playing = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //initialize start button
        mStartButton = (Button)findViewById(R.id.start_button);
        if(savedInstanceState != null){
        	mStartButton.setText(savedInstanceState.getString(BUTTONTEXT));
        }
        mStartButton.setOnClickListener(new View.OnClickListener(){
        	@Override
        	public void onClick(View v){
        		//change text of button to stop
        		if(buttonState == true){
        			mStartButton.setText("Stop");
        			//button control boolean
        			buttonState = false;
        			//step detection boolean
        	        activityRunning = true;
        		}
        		else{
        			mStartButton.setText("Start");
        			//button control boolean
        			buttonState = true;
        			//step detection boolean
        	        activityRunning = false;
        			if(rT1.isPlaying() == true){
        				rT1.pause();
        			}
        	        if(rT2.isPlaying() == true){
        				rT2.pause();
        			}
        			if(rT3.isPlaying() == true){
        				rT3.pause();
        			}
        			if(rT4.isPlaying() == true){
        				rT4.pause();
        			}
        		}
        	}
        });
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	//initialize media players
    	rT1 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack1);
    	rT1.setLooping(true);
    		
    	rT2 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack2);
    	rT2.setLooping(true);
    		
    	rT3 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack3);
    	rT3.setLooping(true);
    	
    	rT4 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack4);
    	rT4.setLooping(true);

    	
    	//initialize sensor
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }

    }
    
    @Override
    protected void onPause() {
        super.onPause();
        //think about moving this statement inside the stop button "if" statement.
        activityRunning = false;
        //Pause any active media
		if(rT1.isPlaying() == true){
			rT1.pause();
		}
        if(rT2.isPlaying() == true){
			rT2.pause();
		}
		if(rT3.isPlaying() == true){
			rT3.pause();
		}
		if(rT4.isPlaying() == true){
			rT4.pause();
		}
        // if you unregister the last listener, the hardware will stop detecting step events
        // sensorManager.unregisterListener(this); 
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityRunning) {
        	recordTimeStamp(event);
        }

    }
    
    public void recordTimeStamp(SensorEvent event){
    	Log.i("StepDetected","You have stepped!");
    	if(timeStamp != 0){
    		String temp1 = Long.toString(event.timestamp);
    		Log.i("current step timestamp", temp1);
    		final long deltaT = (event.timestamp - timeStamp);
    		String temp2 = Long.toString(deltaT);
    		Log.i("timestamp before conversion", temp2);
    		mediaStarter(deltaT);
    	}
    	this.timeStamp = event.timestamp;
    }
    
    public void mediaStarter(long deltaTime){
    	//handle playing music here
    	long conversion = deltaTime/CONVERTER;
    	String temp = Long.toString(conversion);
    	Log.i("conversion", temp);
    	if(conversion >= FIVE_MPH){
    		//stop all other tracks
    		if(rT2.isPlaying() == true){
    			rT2.pause();
    		}
    		if(rT3.isPlaying() == true){
    			rT3.pause();
    		}
    		if(rT4.isPlaying() == true){
    			rT4.pause();
    		}
    		//play music track 1
    		rT1.start();
//    		rt2playing = false;
//    		rt3playing = false;
//    		rt4playing = false;
//    		rt1playing = true;
    		
    	}
    	else if((conversion < FIVE_MPH) && (conversion >= SIX_MPH)){
    		//stop all other tracks
    		if(rT1.isPlaying() == true){
    			rT1.pause();
    		}
    		if(rT3.isPlaying() == true){
    			rT3.pause();
    		}
    		if(rT4.isPlaying() == true){
    			rT4.pause();
    		}
    		//play music track 2
    		rT2.start();
//    		rt1playing = false;
//    		rt3playing = false;
//    		rt4playing = false;
//    		rt2playing = true;
    		
    	}
    	else if((conversion < SIX_MPH) && (conversion >= EIGHT_MPH)){
    		//stop all other tracks
    		if(rT2.isPlaying() == true){
    			rT2.pause();
    		}
    		if(rT1.isPlaying() == true){
    			rT1.pause();
    		}
    		if(rT4.isPlaying() == true){
    			rT4.pause();
    		}
    		//play music track 3
    		rT3.start();
    		
//    		rt2playing = false;
//    		rt1playing = false;
//    		rt4playing = false;
//    		rt3playing = true;
    		
    	}
    	else if((conversion < EIGHT_MPH) && (conversion >= TEN_MPH)){
    		//stop all other tracks
    		if(rT2.isPlaying() == true){
    			rT2.pause();
    		}
    		if(rT3.isPlaying() == true){
    			rT3.pause();
    		}
    		if(rT1.isPlaying() == true){
    			rT1.pause();
    		}
    		//play music track 4
    			rT4.start();
    			
//    		rt2playing = false;
//    		rt3playing = false;
//    		rt1playing = false;
//    		rt4playing = true;
    		
    	}
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
//		rt2playing = false;
//		rt3playing = false;
//		rt1playing = false;
//		rt4playing = false;
    	rT1.release();
    	rT2.release();
    	rT3.release();
    	rT4.release();
    }
    
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
//    	outState.putBoolean(RT1PLAY, rt1playing);
//    	outState.putBoolean(RT2PLAY, rt2playing);
//    	outState.putBoolean(RT3PLAY, rt3playing);
//    	outState.putBoolean(RT4PLAY, rt4playing);
    	outState.putString(BUTTONTEXT, mStartButton.getText().toString());
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
    	super.onRestoreInstanceState(savedInstanceState);
    	buttonState = savedInstanceState.getBoolean(BUTTONVAL);
    	activityRunning = savedInstanceState.getBoolean(ACTIVITYVAL);
//    	rt1playing = savedInstanceState.getBoolean(RT1PLAY);
//    	rt2playing = savedInstanceState.getBoolean(RT2PLAY);
//    	rt3playing = savedInstanceState.getBoolean(RT3PLAY);
//    	rt4playing = savedInstanceState.getBoolean(RT4PLAY);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    
    
}
