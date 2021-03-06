package com.bitcoinlabs.android;

import com.bitcoinlabs.android.settings.Preferences;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Bitcoin extends Activity
{
    private static final int REQUEST_CODE_PREFERENCES = 1;

    public static final String BITCOIN_EXIT_NODE_BALANCE = "http://97.107.139.194:8000/api/unspent-outpoints.js";
    private TextView balanceStatusView;
    private TextView balanceView;
    private TextView balanceUnconfirmedView;
    

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        balanceStatusView = (TextView)findViewById(R.id.balance_status);
        balanceView = (TextView)findViewById(R.id.balance);
//        balanceUnconfirmedView = (TextView)findViewById(R.id.balance_unconfirmed);
        
        final View refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                refreshOutpoints();
            }
        });
        findViewById(R.id.recButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Receive.callMe(Bitcoin.this);
            }
        });
        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                callScan();
            }
        });
        balanceStatusView.setText("WARNING: This is an experimental/alpha release. Things could go wrong. Please do not use it for non-trivial amounts of Bitcoin yet!");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        boolean hasFocusProGuardWorkAround = hasFocus;
        super.onWindowFocusChanged(hasFocusProGuardWorkAround);
        WalletOpenHelper wallet = new WalletOpenHelper(getApplicationContext());
        long balance = wallet.getBalance();
        balanceView.setText(MoneyUtils.formatSatoshisAsBtcString(balance) + "BTC");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            refreshOutpoints();
            return true;
        case R.id.settings:

            // When the button is clicked, launch an activity through this intent
            Intent launchPreferencesIntent = new Intent().setClass(this, Preferences.class);

            // Make it a subactivity so we know when it returns
            startActivityForResult(launchPreferencesIntent, REQUEST_CODE_PREFERENCES);
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void refreshOutpoints() {
//      refreshButton.setEnabled(false);
//        balanceStatusView.setText("Refreshing...");
        startService(new Intent(getApplicationContext(), OutpointService.class));
    }

    private void callScan()
    {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        try
        {
            startActivityForResult(intent, 0);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Install the barcode scanner!!!", Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (requestCode == 0)
        {
            if (resultCode == RESULT_OK)
            {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Uri bitcoinUri = Uri.parse(contents);

                ConfirmPay.callMe(this, bitcoinUri);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                // Handle cancel
            }
        }
    }
//    private OutpointService outpointService;
//
//    private ServiceConnection outpointServiceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            // This is called when the connection with the service has been
//            // established, giving us the service object we can use to
//            // interact with the service.  Because we have bound to a explicit
//            // service that we know is running in our own process, we can
//            // cast its IBinder to a concrete class and directly access it.
//            outpointService = ((OutpointService.OutpointBinder)service).getService();
//
//            // Tell the user about this for our demo.
//            Toast.makeText(Bitcoin.this, R.string.outpoint_service_connected, Toast.LENGTH_SHORT).show();
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            // This is called when the connection with the service has been
//            // unexpectedly disconnected -- that is, its process crashed.
//            // Because it is running in our same process, we should never
//            // see this happen.
//            outpointService = null;
//            Toast.makeText(Bitcoin.this, R.string.outpoint_service_disconnected, Toast.LENGTH_SHORT).show();
//        }
//    };
//    private boolean isOutpointServiceBound;
//
//    void doBindService() {
//        // Establish a connection with the service.  We use an explicit
//        // class name because we want a specific service implementation that
//        // we know will be running in our own process (and thus won't be
//        // supporting component replacement by other applications).
//        bindService(new Intent(Bitcoin.this, OutpointService.class), outpointServiceConnection, Context.BIND_AUTO_CREATE);
//        isOutpointServiceBound = true;
//    }
//
//    void doUnbindService() {
//        if (isOutpointServiceBound) {
//            // Detach our existing connection.
//            unbindService(outpointServiceConnection);
//            isOutpointServiceBound = false;
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        doUnbindService();
//    }
}
