package me.dqn.musibration;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import me.dqn.musibration.service.MusicWatcher;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = "MainActivity";
    TextView showView = null;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                showView.setText("" + msg.obj);
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{20}, new int[]{
                        (int) msg.obj
                }, -1));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: 启动");
        requestPermission();
        showView = this.findViewById(R.id.show);
        bindService(new Intent(this, MusicWatcher.class), this, BIND_AUTO_CREATE);
    }

    private static final int REQUEST_PERMISSIONS = 1000;

    private void requestPermission() {
        String[] permissions = new String[]{
                Manifest.permission.RECORD_AUDIO//音频
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "用户曾拒绝xxxx", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicWatcher.WatcherBinder service1 = (MusicWatcher.WatcherBinder) service;
        service1.getService().setListener(max -> {
            Message message = new Message();
            message.what = 1;
            message.obj = max;
            handler.sendMessage(message);
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}

/**
 * sdk26版本之后可以设置震动强度了。为了响应沙雕网友，我做了个demo
 * 获取麦克风的音量转换成震动强度
 * 存在的问题：
 * 1. 只能麦克风内录不好做，安卓系统有限制，系统应用或自己编译的修改过的安卓系统才能内录）
 * 2. 震动不够平滑 （考虑 缓冲一个震动，然后两个震动强度之间插值）
 * 3. 。。。
 */
