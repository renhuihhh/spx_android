package open.hui.ren.spx.library;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class SpeexRecorder extends Thread {

	private volatile boolean isRecording;
	private static final int frequency = 8000;
	private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private static final String TAG = "SpeexRecorder";
	public static int packagesize = 160;
	private String fileName = null;
	private double amplitude = 0;
	private double DB;

	private AudioRecord recordInstance;

	private double MaxRecode;
	double dB;

	public SpeexRecorder() {
		super("SpeexRecorder");
	}

	public void run() {
		try {
			// 启动编码线程
			SpeexEncoder encoder = new SpeexEncoder(this.getFileName());
			Thread encodeThread = new Thread(encoder, "SpeexEncoder");
			encoder.setRecording(true);
			encodeThread.start();
			Log.i(TAG, "start to recording 1");

			if (!isRecording) {
				Log.i(TAG, "stop to recording 1");
				encoder.setRecording(false);
				return;
			}

			Log.i(TAG, "start to recording 2");
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

			int bufferRead = 0;
			int bufferSize = AudioRecord.getMinBufferSize(frequency,
					AudioFormat.CHANNEL_IN_MONO, audioEncoding);

			short[] tempBuffer = new short[packagesize];

			if (recordInstance == null) {
				recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC,
						frequency, AudioFormat.CHANNEL_IN_MONO, audioEncoding,
						bufferSize);
			}
			recordInstance.startRecording();

			while (this.isRecording) {
				Log.i(TAG, "start to recording.........");
				bufferRead = recordInstance.read(tempBuffer, 0, packagesize);

				int v = 0;
				for (int i = 0; i < tempBuffer.length; i++) {
					v += tempBuffer[i] * tempBuffer[i];
				}
				// 获取振幅
				setAmplitude((v / (double) bufferRead) % 11);

				// recordInstance.get
				if (MaxRecode < (v / (double) bufferRead)) {
					MaxRecode = (v / (double) bufferRead);
				}
				Log.d(TAG, "音量大小=" + (v / (double) bufferRead));
				if (dB < (10 * Math.log10(v / (double) bufferRead))) {
					dB = 10 * Math.log10(v / (double) bufferRead);
				}
				Log.d(TAG, "分贝DB=" + dB);

				// bufferRead = recordInstance.read(tempBuffer, 0, 320);
				if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
					throw new IllegalStateException(
							"read() returned AudioRecord.ERROR_INVALID_OPERATION");
				} else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
					throw new IllegalStateException(
							"read() returned AudioRecord.ERROR_BAD_VALUE");
				} else if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
					throw new IllegalStateException(
							"read() returned AudioRecord.ERROR_INVALID_OPERATION");
				}
				Log.i(TAG, "put data into encoder collector....");
				encoder.putData(tempBuffer, bufferRead);
			}
			// tell encoder to stop.
			encoder.setRecording(false);
			recordInstance.stop();
		} catch (Throwable e) {
			Log.e(TAG, "", e);
		} finally {
			Log.e(TAG, "finally " + callback);
			Log.e(TAG, "MaxRecord=" + MaxRecode);
			Log.e(TAG, "MaxdB=" + dB);
			setDB(dB);
			if (callback != null) {
				callback.onEnd();
			}
		}
	}

	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	public boolean isRecording() {
		return isRecording;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public double getDB() {
		return DB;
	}

	public void setDB(double dB) {
		DB = dB;
	}

	/**
	 * 获取振幅大小
	 * 
	 * @name 陈大龙
	 * @date 2013-9-13
	 * @time 下午12:36:03
	 * @return
	 */
	public double getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}

	public AudioRecord getRecordInstance() {
		return recordInstance;
	}

	public void setRecordInstance(AudioRecord recordInstance) {
		this.recordInstance = recordInstance;
	}

	public static interface Callback {
		void onEnd();
	}

	private Callback callback;

	public Callback getCallback() {
		return callback;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}
}
