package cn.nianron.netgenis;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import cn.nianron.netgenis.utils.CheckUtils;

public class MainActivity extends AppCompatActivity {
    public static boolean reload = false;
    protected boolean mobileConnected = false;
    private TextView now_ip;
    private boolean wifiConnected = false;

    public static void setAirplaneModeOn(Context context, boolean enabling) {
        Settings.Global.putInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, enabling ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabling);
        context.sendBroadcast(intent);

    }

    public static String getPsdnIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }

        SetIP();
        updateConnectedFlags();

        if (mobileConnected) {
            IPHunter();
            if (new CheckUtils().checkifinner(getPsdnIp())) {
                Toast.makeText(getApplicationContext(), "检测到10开头的IP地址", Toast.LENGTH_LONG).show();
                Log.d("1", String.valueOf(reload));
                if (reload) {
                    reload = false;
                    finish();
                }
            }
        }

    }

    public void SetIP() {
        now_ip = (TextView) findViewById(R.id.now_ip);
        now_ip.setText("IP:" + getPsdnIp());
    }

    protected void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }

    public void IPHunter() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                CheckUtils check = new CheckUtils();
                try {
                    while (getPsdnIp().equals(""))
                        Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mobileConnected && !check.checkifinner(getPsdnIp())) {
                    if (CheckUtils.haveRoot()) {
                        CheckUtils.execRootCmdSilent("busybox killall com.android.phone");
                    } else {
                        try {
                            setAirplaneModeOn(getApplicationContext(), true);
                            Thread.sleep(2000);//每一秒输出一次
                            setAirplaneModeOn(getApplicationContext(), false);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }).start();
    }
}
