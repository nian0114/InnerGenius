package cn.nianron.netgenis.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import cn.nianron.netgenis.MainActivity;
import cn.nianron.netgenis.utils.CheckUtils;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    String packnameString = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        packnameString = context.getPackageName();

        ConnectivityManager connectMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        CheckUtils check = new CheckUtils();
        if (mobNetInfo.isConnected()) {
            if (check.isAppForground(context) || !check.checkifinner(MainActivity.getPsdnIp())) {
                MainActivity.reload = true;
                new CheckUtils().reloadActivity(context);
            }
        }

        if (wifiNetInfo.isConnected() && check.isAppForground(context)) {
            new CheckUtils().reloadActivity(context);
        }
    }
}