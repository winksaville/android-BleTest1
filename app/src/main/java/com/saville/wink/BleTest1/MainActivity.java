package com.saville.wink.BleTest1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final static String TAG = "BleTest1";

    private final static int REQUEST_ENABLE_BT = 1;

    private boolean mHasFeatureBle = false;
    private boolean mBtLeSupported = false;
    private BluetoothManager mBtManager = null;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothLeScanner mBtScanner = null;

    /**
     * OnCreate: Activity is created.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate X:w");
        mHasFeatureBle = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        Log.i(TAG, "onCreate: mHasFeatureBle=" + mHasFeatureBle);
        if (mHasFeatureBle) {
            Log.i(TAG, "onCreate: has Bluetooth LE");
            mBtLeSupported = true;
            mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBtAdapter = mBtManager.getAdapter();
        } else {
            Log.i(TAG, "onCreate: has Bluetooth LE");
            mBtLeSupported = false;
            mBtManager = null;
            mBtAdapter = null;
        }
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (mBtLeSupported) {
            if (!mBtAdapter.isEnabled()) {
                Log.i(TAG, "onResume: bt is NOT enabled, request that it be enabled.");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Log.i(TAG, "onResume: bt is enabled");
                mBtScanner = mBtAdapter.getBluetoothLeScanner();
            }
        } else {
            Log.i(TAG, "onResume: mBtLe is not supported");
        }
    }

    /**
     * A Result from startActivityForResult is being returned.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "onActivityResult: requestCode=REQUEST_ENABLE_BT RESULT_OK");
            } else {
                Log.i(TAG, "onActivityResult: requestCode=REQUEST_ENABLE_BT error resultCode="
                        + resultCode);
                Toast.makeText(MainActivity.this,
                        "Bluetooth must be enabled for this app to function",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Log.i(TAG, "onActivityResult: ignore unexpected requestCode=" + requestCode);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        // Do we need to "close" any of these?
        mBtScanner = null;
        mBtAdapter = null;
        mBtManager = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.i(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.i(TAG, "onOptionsItemSelected: id=" + id);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
