package curso.despesas;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class DespesasService extends IntentService {

	public DespesasService() {
		super("DespesasService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			Thread.sleep(intent.getIntExtra("SLEEP", 10000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				getApplicationContext());
		builder.setSmallIcon(R.drawable.notification_icon);
		builder.setAutoCancel(true);
		builder.setDefaults(builder.getNotification().defaults
				| Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
		builder.setLights(0xFF0000FF, 500, 1000);
		builder.setContentTitle("Despesas");
		builder.setContentText("Texto da notificação");
		builder.setTicker("Voce tem uma nova despesa");

		Intent activity = new Intent(this, ListaUsuariosActivity.class);
		PendingIntent launchIntent = PendingIntent.getActivity(this, 0,
				activity, 0);

		builder.setContentIntent(launchIntent);

		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(0, builder.getNotification());
	}

}












