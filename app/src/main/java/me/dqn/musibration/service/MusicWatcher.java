package me.dqn.musibration.service;


import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class MusicWatcher extends Service {


    public class WatcherBinder extends Binder {
        public MusicWatcher getService() {
            return MusicWatcher.this;
        }
    }

    public interface DataListener {
        void update(int max);
    }

    private DataListener listener;

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "onBind: ");
        return new WatcherBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "服务已经启动", Toast.LENGTH_LONG).show();
        Log.i(TAG, "onHandleIntent: start Music Watcher");
        new Thread(() -> {
            // 最小缓冲区
            int bufferSize = AudioRecord.getMinBufferSize(44100,
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);
            // 麦克风 44.1khz 双声道 16位
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    44100,
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);
            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();
            while (true) {
                int read = audioRecord.read(buffer, 0, bufferSize);
                int volume = buffer[0];
                // 取一个声道
                for (int i = 2; i < read; i += 2) {
                    volume = volume > buffer[i] ? volume : buffer[i];
                }
                // 削峰， 应该是 2^16 原本
                if (volume > 4096) volume = 4096;
                double rate = volume / 4096f;
                volume = (int) (255 * rate);

                listener.update(volume);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void setListener(DataListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "服务已经停止", Toast.LENGTH_LONG).show();
    }


}
