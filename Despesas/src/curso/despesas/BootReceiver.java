package curso.despesas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().contentEquals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent service = new Intent(context, DespesasService.class);
			service.putExtra("SLEEP", 1000);
			context.startService(service);
		}
	}

}
