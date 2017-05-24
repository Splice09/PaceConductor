package edu.auburn.eng.csse.comp3710.josephfleming;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Patrick on 3/17/2017.
 *
 * This class is the service responsible for handling back ground media operations.
 */

public class MyService extends Service implements SensorEventListener {

    //SYSTEM
    private boolean activityRunning = false;
    private static final String TAG = "TELSERVICE";

    //MEDIA
    private MediaPlayer playerTrack1;
    private MediaPlayer playerTrack2;
    private MediaPlayer playerTrack3;
    private MediaPlayer playerTrack4;

    //AUDIO MANAGER
    private AudioManager.OnAudioFocusChangeListener focusListener = null;
    private AudioManager am = null;

    //HEADSET
    private int headsetSwitch = 1;

    //BROADCAST NOTIFICATION
    public static final String BROADCAST_NOTIFICTION = "edu.auburn.eng.csse.comp3710.josephfleming.broadcastalert";
    private Intent broadcastIntent;

    //SENSOR
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private long timeStamp;

    //TELEPHONY
    private boolean isPausedInCall = true;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    //APP RELATED CONSTANTS
    private static long CONVERTER = 1000000;
    private static double FIVE_MPH = 324.7;
    private static double SIX_MPH = 261.1;
    private static double EIGHT_MPH = 196.9;
    private static double TEN_MPH = 171.5;

    /*
    =========================================================================
    SERVICE CONTROL OVERRIDES
    =========================================================================
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        Log.v(TAG, "Creating Service");
        //register headset receiver
        registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));


        //instantiate intent
        broadcastIntent = new Intent(BROADCAST_NOTIFICTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        activityRunning = true;

        //initialize sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //register sensor listener
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
        //initialize media players

        playerTrack1 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack1);
        playerTrack1.setLooping(true);

        playerTrack2 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack2);
        playerTrack2.setLooping(true);

        playerTrack3 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack3);
        playerTrack3.setLooping(true);

        playerTrack4 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack4);
        playerTrack4.setLooping(true);

        focusListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focus) {
                switch (focus) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        // continue playback and raise volume (if it was previously lowered)
                        playMedia();
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS:
                        // stop playback, de-register buttons, clean up
                        pauseMedia();
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // consider switching from pause to setting ducking volume
                        pauseMedia();
                        break;
                }
            }
        };

        Log.v(TAG, "Starting telephony.");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.v(TAG, "Starting listener.");
        //set phone state listener to pause audio when handling calls
        phoneStateListener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber){
                Log.v(TAG, "Starting CallStateChange.");
                switch(state){
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        pauseMedia();
                        isPausedInCall = true;
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if(isPausedInCall){
                            //set audio manager
                            isPausedInCall = false;
                            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                            // Request focus for music stream and pass AudioManager.OnAudioFocusChangeListener
                            // implementation reference
                            int result = am.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC,
                                    AudioManager.AUDIOFOCUS_GAIN);

                            if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                            {
                                // Play
                                playMedia();
                            }

                        }
                        break;
                }
            }
        };

        //Register the listener with the telephony manager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //clear phone state listener (for phone calls)
        if(phoneStateListener != null){
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        //release audio focus (for giving focus to other apps)
        if(focusListener != null){
            am.abandonAudioFocus(focusListener);
            am = null;
            focusListener = null;
        }

        //unregister headsetReceiver
        if(headsetReceiver != null){
            unregisterReceiver(headsetReceiver);
        }

        //unregister sensor listener
        if(stepSensor != null){
            sensorManager.unregisterListener(this);
        }

        //stop playing all audio tracks & release media players
        if(playerTrack1.isPlaying()){
            playerTrack1.stop();
        }
        if(playerTrack2.isPlaying()){
            playerTrack2.stop();
        }
        if(playerTrack3.isPlaying()){
            playerTrack3.stop();
        }
        if(playerTrack4.isPlaying()){
            playerTrack4.stop();
        }

        playerTrack1.release();
        playerTrack2.release();
        playerTrack3.release();
        playerTrack4.release();
    }

    /*
    =========================================================================
    SENSOR MANAGER OVERRIDES
    =========================================================================
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityRunning) {
            recordTimeStamp(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //unused override
    }

    /*
    =========================================================================
    INITIALIZE BROADCAST RECEIVER - handle changes for headset unplug
    =========================================================================
     */
    private BroadcastReceiver headsetReceiver = new BroadcastReceiver(){
        private boolean headsetConnected = false;

        @Override
        public void onReceive(Context context, Intent intent){
            //Log.v(TAG, "ACTION_HEADSET_PLUG Intent Received");
            if(intent.hasExtra("state")){
                if(headsetConnected && intent.getIntExtra("state", 0) == 0){
                    headsetConnected = false;
                    headsetSwitch = 0;
                    PlayButtonBroadcast();
                    //Log.v(TAG,"State = headset disconnected");
                }
                else if(!headsetConnected && intent.getIntExtra("state", 0) == 1){
                    headsetConnected = true;
                    headsetSwitch = 1;
                    //Log.v(TAG,"State = headset connected");
                }
            }
            switch(headsetSwitch){
                case (0):
                    headsetDisconnected();
                    break;
                case (1):
                    break;
            }
        }
    };

    /*
    =========================================================================
    LOCAL MEDIA HANDLING FUNCTIONS
    =========================================================================
     */
    private void headsetDisconnected(){
        stopSelf();
    }

    public void playMedia(){
        //play music track 1
        playerTrack1.start();
    }

    public void pauseMedia(){
        if(playerTrack1.isPlaying()){
            playerTrack1.pause();
        }
        if(playerTrack2.isPlaying()){
            playerTrack2.stop();
        }
        if(playerTrack3.isPlaying()){
            playerTrack3.stop();
        }
        if(playerTrack4.isPlaying()){
            playerTrack4.stop();
        }
    }

    private void PlayButtonBroadcast(){
        //Log.v(TAG, "Headset broadcast sent");
        broadcastIntent.putExtra("stop", "1");
        sendBroadcast(broadcastIntent);
    }
    /*
    =========================================================================
    CORE APP FUNCTIONALITY RELATED FUNCTIONS
    =========================================================================
     */
    public void recordTimeStamp(SensorEvent event){
        Log.i("StepDetected","You have stepped!");
        if(timeStamp != 0){
            String temp1 = Long.toString(event.timestamp);
            Log.i("current step timestamp", temp1);
            final long deltaT = (event.timestamp - timeStamp);
            String temp2 = Long.toString(deltaT);
            Log.i("timestamp pre convert", temp2);
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
            Log.i("conversion", "5mph");
            //stop all other tracks
            if(playerTrack2.isPlaying()){
                playerTrack2.pause();
            }
            if(playerTrack3.isPlaying()){
                playerTrack3.pause();
            }
            if(playerTrack4.isPlaying()){
                playerTrack4.pause();
            }
            //play music track 2
            if(!playerTrack1.isPlaying()){
                playerTrack1.start();
            }
        }
        else if((conversion < FIVE_MPH) && (conversion >= SIX_MPH)){
            Log.i("conversion", "6mph");
            //stop all other tracks
            if(playerTrack1.isPlaying()){
                playerTrack1.pause();
            }
            if(playerTrack3.isPlaying()){
                playerTrack3.pause();
            }
            if(playerTrack4.isPlaying()){
                playerTrack4.pause();
            }
            //play music track 2
            if(!playerTrack2.isPlaying()){
                playerTrack2.start();
            }
        }
        else if((conversion < SIX_MPH) && (conversion >= EIGHT_MPH)){
            Log.i("conversion", "7mph");
            //stop all other tracks
            if(playerTrack1.isPlaying()){
                playerTrack1.pause();
            }
            if(playerTrack2.isPlaying()){
                playerTrack2.pause();
            }
            if(playerTrack4.isPlaying()){
                playerTrack4.pause();
            }
            //play music track 3
            if(!playerTrack3.isPlaying()){
                playerTrack3.start();
            }
        }
        else if((conversion < EIGHT_MPH) && (conversion >= TEN_MPH)){
            Log.i("conversion", "8mph");
            //stop all other tracks
            if(playerTrack1.isPlaying()){
                playerTrack1.pause();
            }
            if(playerTrack2.isPlaying()){
                playerTrack2.pause();
            }
            if(playerTrack3.isPlaying()){
                playerTrack3.pause();
            }
            //play music track 4
            if(!playerTrack4.isPlaying()){
                playerTrack4.start();
            }
        }
    }
}
