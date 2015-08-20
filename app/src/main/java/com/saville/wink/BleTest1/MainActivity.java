package com.saville.wink.BleTest1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final static String TAG = "BleTest1";

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_PERMISSIONS = 2;

    private boolean mHasFeatureBle = false;
    private boolean mBtLeSupported = false;
    private boolean mBtAllowed = false;
    private BluetoothManager mBtManager = null;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothLeScanner mBtScanner = null;
    private BleSm1 mSm1 = null;

    private void startScan() {
        mBtScanner = mBtAdapter.getBluetoothLeScanner();
        mMyScanCallBack = new MyScanCallback();
        mBtScanner.startScan(mMyScanCallBack);
    }

    private void getPermissions() {
        Log.i(TAG, "getPermissions:+");

        ArrayList<String> permissionsNeeded = new ArrayList<String>();
        if (checkSelfPermission("android.permission.BLUETOOTH") ==
                PackageManager.PERMISSION_DENIED) {
            permissionsNeeded.add("android.permission.BLUETOOTH");
        }
        if (checkSelfPermission("android.permission.BLUETOOTH_ADMIN") ==
                PackageManager.PERMISSION_DENIED) {
            permissionsNeeded.add("android.permission.BLUETOOTH_ADMIN");
        }
        if (checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") ==
                PackageManager.PERMISSION_DENIED) {
            permissionsNeeded.add("android.permission.ACCESS_COARSE_LOCATION");
        }
        if (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") ==
                PackageManager.PERMISSION_DENIED) {
            permissionsNeeded.add("android.permission.ACCESS_FINE_LOCATION");
        }
        if (permissionsNeeded.size() > 0) {
            Log.i(TAG, "getPermissions: needed=" + permissionsNeeded);
            mBtAllowed = false;
            String[] permissionsNeededStringArray =
                    permissionsNeeded.toArray(new String[permissionsNeeded.size()]);
            requestPermissions(permissionsNeededStringArray, REQUEST_PERMISSIONS);
        } else {
            mBtAllowed = true;
        }
        Log.i(TAG, "getPermissions:-");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult:+");
        if (requestCode == REQUEST_PERMISSIONS) {
            if (permissions.length == grantResults.length) {
                int grantedCount = 0;
                for (int i = 0; i < grantResults.length; i++) {
                    Log.i(TAG, String.format(
                            "onRequestPermissionsResult: %d:permission=%s grantResults=%d",
                            i, permissions[i], grantResults[i]));
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        grantedCount += 1;
                    }
                }
                if (grantedCount != permissions.length) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            Log.i(TAG, String.format(
                                    "onRequestPermissionsResult: permission:%s%sgranted",
                                    permissions[i],
                                    grantResults[i] == PackageManager.PERMISSION_DENIED ?
                                            " not " : " "));
                        }
                    }
                    mBtAllowed = false;
                } else {
                    mBtAllowed = true;
                    startScan();
                }
            } else {
                Log.wtf(TAG, String.format("onRequestPermissionsResult: permissions.length:%d !=" +
                        "grantResults.length:%d", permissions.length, grantResults.length));
            }
        } else {
            Log.i(TAG, "onRequestPermissionsResult: ignore unknown requestCode=" + requestCode);
        }
        Log.i(TAG, "onRequestPermissionsResult:-");
    }

    /**
     * OnCreate: Activity is created.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate X:w id=" + Thread.currentThread().getId() + " tid=" + Process.myTid());
        mSm1 = BleSm1.makeBleSm1("mSm1");
        mSm1.sendMessage(BleSm1.ON_CREATE);

        mHasFeatureBle = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        Log.i(TAG, "onCreate: mHasFeatureBle=" + mHasFeatureBle);
        if (mHasFeatureBle) {
            Log.i(TAG, "onCreate: has Bluetooth LE");
            mBtLeSupported = true;
            mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBtAdapter = mBtManager.getAdapter();
            getPermissions();
        } else {
            Log.i(TAG, "onCreate: has Bluetooth LE");
            mBtLeSupported = false;
            mBtAllowed = false;
            mBtManager = null;
            mBtAdapter = null;
        }
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume:+");
        if (mBtLeSupported) {
            if (!mBtAdapter.isEnabled()) {
                Log.i(TAG, "onResume: bt is NOT enabled, request that it be enabled.");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Log.i(TAG, "onResume: bt is enabled");
                if (mBtAllowed) {
                    startScan();
                } else {
                    Log.i(TAG, "onResume: BT is not allowed, at least not yet");
                }
            }
        } else {
            Log.i(TAG, "onResume: mBtLe is not supported");
        }
        Log.i(TAG, "onResume:-");
    }

    private MyScanCallback mMyScanCallBack;
    class MyScanCallback extends ScanCallback {
        public MyScanCallback() {
            super();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            printScanResult("onScanResult", result);
        }

        private void printScanResult(String onScanResult, ScanResult result) {
            Log.i(TAG, onScanResult + ": result=" + result.toString());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                printScanResult("onBatchScanResults", result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(TAG, "onScanFailed: errorCode=0x" + Integer.toHexString(errorCode));
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
        Log.i(TAG, "onPause:+");
        if (mMyScanCallBack != null && mBtScanner != null) {
            Log.i(TAG, "onPause: stopScan");
            mBtScanner.stopScan(mMyScanCallBack);
        }
        Log.i(TAG, "mSm1: dump");
        Log.i(TAG, mSm1.toString());
        Log.i(TAG, "onPause:-");
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
