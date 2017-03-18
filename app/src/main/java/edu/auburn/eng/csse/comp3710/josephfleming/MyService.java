package edu.auburn.eng.csse.comp3710.josephfleming;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

/**
 * Created by Patrick on 3/17/2017.
 */

public class MyService extends Service {
    private MediaPlayer player;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //initialize media players
        rT1 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack1);
        rT1.setLooping(true);

        rT2 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack2);
        rT2.setLooping(true);

        rT3 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack3);
        rT3.setLooping(true);

        rT4 = MediaPlayer.create(getApplicationContext(), R.raw.runtrack4);
        rT4.setLooping(true);
        //explicitly start and stopped
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        player.stop();
    }
}
