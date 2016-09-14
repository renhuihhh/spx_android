package open.hui.ren.spx.player;

import android.util.Log;

import java.io.File;

import open.hui.ren.spx.library.SpeexDecoder;


/**
 * 录音播放控制器
 * 
 */
public class RecordPlayController {

    private static final String TAG = "Im";
    private RecordPlayThread thread;

    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return thread != null && thread.playing;
    }

    /**
     * 播放
     */
    public void play(String filename, SpeexDecoder.SpeexDecoderFinish listener) {
        // 启动线程播放
        thread = new RecordPlayThread(filename);
        thread.speexdec.setSpeexDecoderFinish(listener);
        thread.start();
    }

    /**
     * 停止
     */
    public void stop() {
        // 中断线程会自动停止播放
        if (thread != null && thread.playing) {
            thread.interrupt();
            thread = null;
        }
    }

    /**
     * 播放线程
     */
    private class RecordPlayThread extends Thread {
        private String filename;
        private SpeexDecoder speexdec;
        private boolean playing;

        public RecordPlayThread(String filename) {
            speexdec = new SpeexDecoder();
            this.filename = filename;
        }

        public void run() {
            try {
                playing = true;
                speexdec.decode(new File(filename));
            } catch (Throwable e) {
                Log.w(TAG, "", e);
            } finally {
                speexdec.getSpeexDecoderFinish().onSpeexDecoderFinish();
                playing = false;
            }
        }
    }
}
