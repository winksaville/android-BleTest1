package com.saville.wink.BleTest1;

import android.bluetooth.le.ScanRecord;
import android.os.*;
import android.os.Process;

import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

import java.util.ArrayList;

/**
 * Created by wink on 8/20/15.
 */
public class BleSm1 extends StateMachine {
    public final static int ON_CREATE = 1;

    private BleSm1(String name) {
        super(name);
        setDbg(true);
        setLogRecSize(100);

        addState(mState1);
        setInitialState(mState1);
    }

    public static BleSm1 makeBleSm1(String name) {
        BleSm1 sm = new BleSm1(name);
        sm.start();
        return sm;
    }

    class State1 extends State {
        @Override
        public boolean processMessage(Message message) {
            logAndAddLogRec("State1: what=" + getWhatToString(message.what));
            switch (message.what) {
                case ON_CREATE: {
                    logAndAddLogRec("State1.ON_CREATE: send get results");
                    BleScanSm bleScanSm = (BleScanSm)message.obj;
                    Message msg = new Message();
                    msg.what = BleScanSm.CMD_GET_SCAN_RESULTS;
                    msg.replyTo = new Messenger(getHandler());
                    bleScanSm.sendMessage(msg);
                    break;
                }
                case BleScanSm.CMD_GET_SCAN_RESULTS_REPLY: {
                    ArrayList<ScanRecord> results = (ArrayList<ScanRecord>)(message.obj);
                    logAndAddLogRec("State1.CMD_GET_SCAN_RESULTS_REPLY: size=" + results.size());
                    for(ScanRecord rec : results) {
                        logAndAddLogRec("State1.CMD_GET_SCAN_RESULTS_REPLY: rec=" + rec);
                    }
                    break;
                }
                default: {
                    logAndAddLogRec("Unknown message.what=" + getWhatToString(message.what));
                }
            }
            return HANDLED;
        }
    }
    State1 mState1 = new State1();
}
