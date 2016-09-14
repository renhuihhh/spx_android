package open.hui.ren.spx.library;

import android.util.Log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 将recorder录制的声音数据流转码，并交给writer封装
 * 
 * @author Gauss
 * 
 */
public class SpeexEncoder implements Runnable {

	private final Object mutex = new Object();
	private Speex speex = new Speex();
	// private long ts;
	public static int encoder_packagesize = 1024;
	private byte[] processedData = new byte[encoder_packagesize];

	List<ReadData> list = null;
	private volatile boolean isRecording;
	private String fileName;

	public SpeexEncoder(String fileName) {
		super();
		speex.init();
		list = Collections.synchronizedList(new LinkedList<ReadData>());
		this.fileName = fileName;
	}

	public void run() {

		// 启动writer线程写speex文件。
		SpeexWriter fileWriter = new SpeexWriter(fileName);
		Thread consumerThread = new Thread((Runnable) fileWriter);
		fileWriter.setRecording(true);
		consumerThread.start();

		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		int getSize = 0;
		while (this.isRecording()) {
			if (list.size() == 0) {
				Log.d("SpeexEncoder","no data need to do encode");
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			if (list.size() > 0) {
				synchronized (mutex) {
					ReadData rawdata = list.remove(0);
					getSize = speex.encode(rawdata.ready, 0, processedData,
							rawdata.size);

					Log.i("SpeexEncoder","after encode......................before="
							+ rawdata.size + " after=" + processedData.length
							+ " getsize=" + getSize);
				}
				if (getSize > 0) {
					fileWriter.putData(processedData, getSize);
					Log.i("SpeexEncoder","............clear....................");
					processedData = new byte[encoder_packagesize];
				}
			}
		}
		Log.d("SpeexEncoder","encode thread exit");
		fileWriter.setRecording(false);
	}

	/**
	 * 供Recorder方待处理的数据
	 * 
	 * @param data
	 * @param size
	 */
	public void putData(short[] data, int size) {
		ReadData rd = new ReadData();
		synchronized (mutex) {
			rd.size = size;
			System.arraycopy(data, 0, rd.ready, 0, size);
			list.add(rd);
		}
	}

	public void setRecording(boolean isRecording) {
		synchronized (mutex) {
			this.isRecording = isRecording;
		}
	}

	public boolean isRecording() {
		synchronized (mutex) {
			return isRecording;
		}
	}

	class ReadData {
		private int size;
		private short[] ready = new short[encoder_packagesize];
	}
}
