package com.saville.wink.BleTest1;

import android.bluetooth.le.ScanRecord;
import android.os.Message;
import android.os.Process;
import android.util.SparseArray;

import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by wink on 8/20/15.
 */
public class BleScanSm extends StateMachine {
    private final static int BaseCommand = 0x0;
    public final static int CMD_STATUS_OK = BaseCommand + 0;
    public final static int CMD_START_SCAN = BaseCommand + 1;
    public final static int CMD_STOP_SCAN = BaseCommand + 2;
    public final static int CMD_GET_SCAN_RESULTS = BaseCommand + 3;
    public final static int CMD_GET_SCAN_RESULTS_REPLY = BaseCommand + 4;

    private ArrayList<ScanRecord> mScanRecords = new ArrayList<>();

    private String[] whatToString = {
            "CMD_STATUS_OK", "CMD_START_SCAN", "CMD_STOP_SCAN",
            "CMD_GET_SCAN_RESULTS", "CMD_GET_SCAN_RESULTS_REPLY"
    };

    @Override
    protected String getWhatToString(int what) {
        what -= BaseCommand;
        if (what >= 0 && what < whatToString.length) {
            return whatToString[what];
        } else {
            return super.getWhatToString(what);
        }
    }

    private BleScanSm(String name) {
        super(name);
        setDbg(true);
        setLogRecSize(100);

        addState(mDefaultState);
        addState(mIdleState, mDefaultState);
        setInitialState(mIdleState);
    }

    public static BleScanSm makeBleSm(String name) {
        BleScanSm sm = new BleScanSm(name);
        sm.start();
        return sm;
    }

    class DefaultState extends State {
        @Override
        public boolean processMessage(Message message) {
            logAndAddLogRec("DefaultState: what=" + getWhatToString(message.what));
            switch (message.what) {
                case CMD_START_SCAN:
                case CMD_STOP_SCAN: {
                    logAndAddLogRec("Ignore " + getWhatToString(message.what));
                    break;
                }
                case CMD_GET_SCAN_RESULTS: {
                    Message result = new Message();
                    result.what = CMD_GET_SCAN_RESULTS_REPLY;
                    result.obj = mScanRecords.clone();
                    try {
                        message.replyTo.send(result);
                    } catch (android.os.RemoteException e) {
                        logAndAddLogRec("Unable to send scan results");
                    } catch (Exception e) {
                        logAndAddLogRec("Unexpected exception e:" + e);
                    }
                    break;
                }
                default: {
                    break;
                }
            }
            return HANDLED;
        }
    }
    DefaultState mDefaultState = new DefaultState();

    class IdleState extends State {
        @Override
        public boolean processMessage(Message message) {
            getWhatToString(message.what);
            logAndAddLogRec("IdleState: what=" + getWhatToString(message.what));
            return NOT_HANDLED;
        }
    }
    IdleState mIdleState = new IdleState();

    class ScanningState extends State {
        @Override
        public boolean processMessage(Message message) {
            logAndAddLogRec("ScanningState: what=" + getWhatToString(message.what));
            return NOT_HANDLED;
        }
    }
    ScanningState mScanningState = new ScanningState();

}

