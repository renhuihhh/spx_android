package com.example.renhui.spx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.renhui.spx.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import open.hui.ren.spx.library.SpeexDecoder;
import open.hui.ren.spx.library.SpeexRecorder;
import open.hui.ren.spx.player.RecordPlayController;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private final int MY_PERMISSIONS_WRITE_STORAGE = 2;
    private static final String TAG = "MainActivity";

    @BindView(R.id.title_txt)
    TextView titleTxt;
    @BindView(R.id.btn_rcd)
    ImageButton btnRcd;
    @BindView(R.id.btn_play)
    ImageButton btnPlay;
    @BindView(R.id.volume_anim)
    ImageView volumeAnimView;

    private RecordPlayController rcdController;
    private SpeexRecorder recorder;
    private String voiceName = "";
    private String voiceFilePath = null;
    private AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        rcdController = new RecordPlayController();
        animationDrawable = (AnimationDrawable) volumeAnimView.getDrawable();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @OnTouch({R.id.btn_play, R.id.btn_rcd})
    boolean whenTouch(View view, MotionEvent event) {
        switch (view.getId()) {
            case R.id.btn_rcd:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        doRecord();
                        Log.d(TAG, "doRecord()1");
                        break;
                    case MotionEvent.ACTION_UP:
                        stopVoiceRecoding();
                        Log.d(TAG, "stopVoiceRecoding()1");
                        break;
                    default:
                        break;
                }
                break;
            case R.id.btn_play:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        doPlay();
                        Log.d(TAG, "doPlay()2");
                        break;
                    case MotionEvent.ACTION_UP:
                        stopPlaying();
                        Log.d(TAG, "stopPlaying()2");
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return false;
    }

    private void doPlay() {
        if (rcdController.isPlaying()) {
            titleTxt.setText("is playing...");
            stopPlaying();
            return;
        }
        if (recorder != null)
            if (recorder.isRecording()) {
                titleTxt.setText("is recording...");
                stopVoiceRecoding();
                return;
            }
        titleTxt.setText("playing...");
        startPlaying();
    }

    //开始播放
    private void startPlaying() {
        rcdController.play(voiceFilePath, new SpeexDecoder.SpeexDecoderFinish() {
            @Override
            public void onSpeexDecoderFinish() {
                Log.d(TAG, voiceFilePath + " playing finished");
            }
        });
    }

    //停止播放
    private void stopPlaying() {
        titleTxt.setText("stop playing...");
        rcdController.stop();
    }

    private void doRecord() {
        if (rcdController.isPlaying()) {
            titleTxt.setText("is playing...");
            stopPlaying();
            return;
        }
        if (recorder != null)
            if (recorder.isRecording()) {
                titleTxt.setText("is recording...");
                stopVoiceRecoding();
                return;
            }
        titleTxt.setText("recording...");
        requestStoragePermission();
    }

    /**
     * 开始录制
     */
    private void startRecoding() {
        voiceName = System.currentTimeMillis() + ".spx"; // spx格式
        recorder = new SpeexRecorder();

        try {
            voiceFilePath = FileUtils.createCustomFile(this, voiceName)
                                     .getPath();
            Log.d(TAG, "voiceFilePath " + voiceFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.setFile(new File(voiceFilePath));
        recorder.setCallback(new SpeexRecorder.Callback() {
            @Override
            public void onEnd() {
                //mHandler.sendMessage(mHandler.obtainMessage(MSG_RELEASE_REC, recorder));
            }
        });
        recorder.setRecording(true);
        requestAudioPermissions();
        animationDrawable.start();
    }

    /**
     * 停止录制
     */
    private void stopVoiceRecoding() {
        titleTxt.setText("stop recording...");
        recorder.setRecording(false);
        animationDrawable.stop();
    }

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG)
                     .show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_RECORD_AUDIO);
            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_RECORD_AUDIO);
            }
        }//If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            recorder.start();
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG)
                     .show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_STORAGE);
            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_STORAGE);
            }
        } else if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            //Go ahead with recording audio now
            startRecoding();
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    recorder.start();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG)
                         .show();
                }
                return;
            }
            case MY_PERMISSIONS_WRITE_STORAGE: {
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    startRecoding();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to write storage", Toast.LENGTH_LONG)
                         .show();
                }
                return;
            }
        }
    }
}