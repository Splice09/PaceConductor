package edu.auburn.eng.csse.comp3710.josephfleming;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

/**
 * Created by Patrick on 3/17/2017.
 *
 * This class is the service responsible for handling back ground media operations.
 */

public class MyService extends Service implements SensorEventListener {
//, AudioManager.OnAudioFocusChangeListener
    /*  ==============================
        VARIABLES
        ==============================
     */
    //system
    private boolean activityRunning = false;

    //media
    private MediaPlayer playerTrack1;
    private MediaPlayer playerTrack2;
    private MediaPlayer playerTrack3;
    private MediaPlayer playerTrack4;

    //audio manager
    private AudioManager.OnAudioFocusChangeListener focusListener = null;
    private AudioManager am = null;



    //sensor
    private SensorManager sensorManager;
    private long timeStamp;

    //telephony
    private boolean isPausedInCall = true;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    //math
    private static long CONVERTER = 1000000;
    private static double FIVE_MPH = 324.7;
    private static double SIX_MPH = 261.1;
    private static double EIGHT_MPH = 196.9;
    private static double TEN_MPH = 171.5;

    //======================================

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        activityRunning = true;

        //initialize sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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

        Log.v("TAG", "Starting telephony.");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.v("TAG", "Starting listener.");
        //set phone state listener to pause audio when handling calls
        phoneStateListener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber){
                Log.v("TAG", "Starting CallStateChange.");
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
            //stop all other tracks
            if(playerTrack2.isPlaying() == true){
                playerTrack2.pause();
            }
            if(playerTrack3.isPlaying() == true){
                playerTrack3.pause();
            }
            if(playerTrack4.isPlaying() == true){
                playerTrack4.pause();
            }
            //play music track 1
            playerTrack1.start();
        }
        else if((conversion < FIVE_MPH) && (conversion >= SIX_MPH)){
            //stop all other tracks
            if(playerTrack1.isPlaying() == true){
                playerTrack1.pause();
            }
            if(playerTrack3.isPlaying() == true){
                playerTrack3.pause();
            }
            if(playerTrack4.isPlaying() == true){
                playerTrack4.pause();
            }
            //play music track 2
            playerTrack2.start();
        }
        else if((conversion < SIX_MPH) && (conversion >= EIGHT_MPH)){
            //stop all other tracks
            if(playerTrack2.isPlaying() == true){
                playerTrack2.pause();
            }
            if(playerTrack1.isPlaying() == true){
                playerTrack1.pause();
            }
            if(playerTrack4.isPlaying() == true){
                playerTrack4.pause();
            }
            //play music track 3
            playerTrack3.start();
        }
        else if((conversion < EIGHT_MPH) && (conversion >= TEN_MPH)){
            //stop all other tracks
            if(playerTrack2.isPlaying() == true){
                playerTrack2.pause();
            }
            if(playerTrack3.isPlaying() == true){
                playerTrack3.pause();
            }
            if(playerTrack1.isPlaying() == true){
                playerTrack1.pause();
            }
            //play music track 4
            playerTrack4.start();
        }
    }

    public void pauseMedia(){
        if(playerTrack1.isPlaying() == true){
            playerTrack1.pause();
        }
        if(playerTrack2.isPlaying() == true){
            playerTrack2.stop();
        }
        if(playerTrack3.isPlaying() == true){
            playerTrack3.stop();
        }
        if(playerTrack4.isPlaying() == true){
            playerTrack4.stop();
        }
    }

    public void playMedia(){
        //play music track 1
        playerTrack1.start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //unused override
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //clear phone state listener (for phone calls)
        if(phoneStateListener != null){
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        //release audio focus (for giving focus to other apps)
        am.abandonAudioFocus(focusListener);
        am = null;
        focusListener = null;


        //stop playing all audio tracks & release media players
        if(playerTrack1.isPlaying() == true){
            playerTrack1.stop();
        }
        if(playerTrack2.isPlaying() == true){
            playerTrack2.stop();
        }
        if(playerTrack3.isPlaying() == true){
            playerTrack3.stop();
        }
        if(playerTrack4.isPlaying() == true){
            playerTrack4.stop();
        }
        playerTrack1.release();
        playerTrack2.release();
        playerTrack3.release();
        playerTrack4.release();



    }

    /*@Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange)
        {
            case AudioManager.AUDIOFOCUS_GAIN:
                //play
                playMedia();
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                //stop
                if(playerTrack1.isPlaying() == true){
                    playerTrack1.pause();
                }
                if(playerTrack2.isPlaying() == true){
                    playerTrack2.stop();
                }
                if(playerTrack3.isPlaying() == true){
                    playerTrack3.stop();
                }
                if(playerTrack4.isPlaying() == true){
                    playerTrack4.stop();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //pause
                //stop
                if(playerTrack1.isPlaying() == true){
                    playerTrack1.pause();
                }
                if(playerTrack2.isPlaying() == true){
                    playerTrack2.stop();
                }
                if(playerTrack3.isPlaying() == true){
                    playerTrack3.stop();
                }
                if(playerTrack4.isPlaying() == true){
                    playerTrack4.stop();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //reduce volume
                break;
        }
    }*/
}
