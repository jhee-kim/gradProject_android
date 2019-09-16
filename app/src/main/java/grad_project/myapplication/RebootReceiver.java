package grad_project.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class RebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent rebootIntent = new Intent(context, NotiService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(rebootIntent);
            } else {
                context.startService(rebootIntent);
            }
        }
        // an Intent broadcast.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
