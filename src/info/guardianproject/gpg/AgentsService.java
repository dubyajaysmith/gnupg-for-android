package info.guardianproject.gpg;

import info.guardianproject.gpg.pinentry.PinEntryActivity;
import info.guardianproject.gpg.pinentry.ServerSocketThread;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AgentsService extends Service {
	public static final String TAG = "AgentsService";

	/** For showing and hiding our notification. */
	NotificationManager mNM;

	private GpgAgentThread gpgAgentThread;
	private ServerSocketThread pinentryHelperThread;

	private void startDaemons() {
		Log.i(TAG, "start daemons in " + NativeHelper.app_opt.getAbsolutePath());
		synchronized (this) {
			gpgAgentThread = new GpgAgentThread();
			gpgAgentThread.start();

			pinentryHelperThread = new ServerSocketThread(this);
			pinentryHelperThread.start();
		}
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		// since this service is a separate process, it has its own instance of
		// NativeHelper
		NativeHelper.setup(this);

		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		showNotification();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
//		if( pinentryHelperThread != null )
//			pinentryHelperThread.setStopThread(true);
		// Cancel the persistent notification.
		mNM.cancel(R.string.remote_service_started);
		Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
	}

	public class LocalBinder extends Binder {
		public AgentsService getService() {
			return AgentsService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		startDaemons();
		return START_STICKY;
	}

	public void startPinentry() {
		Log.d(TAG, "starting activity!");
		Intent intent = new Intent(this, PinEntryActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    startActivity(intent);
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.remote_service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				GnuPrivacyGuard.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.remote_service_label),
				text, contentIntent);

		// Send the notification.
		// We use a string id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(R.string.remote_service_started, notification);
	}

	class GpgAgentThread extends Thread {

		@Override
		public void run() {
			NativeHelper.kill9(NativeHelper.gpg_agent);
			String gpgAgentCmd = NativeHelper.gpg_agent
                    + " --pinentry-program " + NativeHelper.pinentry_android
					+ " --daemon --write-env-file " + "--debug-level guru --log-file "
					+ NativeHelper.app_log + "/gpg-agent.log";
			Log.i(TAG, gpgAgentCmd);
			try {
				Runtime.getRuntime()
						.exec(gpgAgentCmd, NativeHelper.envp, NativeHelper.app_home)
						.waitFor();
			} catch (Exception e) {
				Log.e(TAG, "Could not start gpg-agent", e);
			} finally {
				stopSelf();
				synchronized (AgentsService.this) {
					gpgAgentThread = null;
				}
			}
		}
	}
}
