package open.hui.ren.spx.library;

import android.util.Log;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Gauss 使用OGG封装并写文件
 */
public class SpeexWriter implements Runnable {

    private final Object mutex = new Object();

    // 写文件
    private SpeexWriteClient client = new SpeexWriteClient();
    private volatile boolean             isRecording;
    private          processedData       pData;
    private          List<processedData> list;

    public static int write_packageSize = 1024;

    public SpeexWriter(File file) {
        super();
        list = Collections.synchronizedList(new LinkedList<processedData>());

        client.setSampleRate(8000);

        client.start(file);
    }

    public void run() {
        Log.d("SpeexWriter", "write thread runing");
        while (this.isRecording() || list.size() > 0) {

            if (list.size() > 0) {
                pData = list.remove(0);
                // gauss_packageSize/2
                Log.i("SpeexWriter", "pData size=" + pData.size);

                client.writeTag(pData.processed, pData.size);

                Log.d("SpeexWriter", "list size = {}" + String.valueOf(list.size()));
            } else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        Log.d("SpeexWriter", "write thread exit");
        stop();
    }

    public void putData(final byte[] buf, int size) {

        Log.d("SpeexWriter", "after convert. size=====================[640]:" + size);

        processedData data = new processedData();
        // data.ts = ts;
        data.size = size;
        System.arraycopy(buf, 0, data.processed, 0, size);
        list.add(data);
    }

    public void stop() {
        client.stop();
    }

    public void setRecording(boolean isRecording) {
        synchronized (mutex) {
            this.isRecording = isRecording;
            if (this.isRecording) {
                mutex.notify();
            }
        }
    }

    public boolean isRecording() {
        synchronized (mutex) {
            return isRecording;
        }
    }

    class processedData {
        // private long ts;
        private int size;
        private byte[] processed = new byte[write_packageSize];
    }

}
