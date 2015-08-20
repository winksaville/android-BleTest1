package com.saville.wink.BleTest1;

import android.os.*;
import android.os.Process;

import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

/**
 * Created by wink on 8/20/15.
 */
public class BleSm1 extends StateMachine {
    public static int ON_CREATE = 1;

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
            logAndAddLogRec("State1: what=" + message.what + " tid=" + Process.myTid());
            return HANDLED;
        }
    }
    State1 mState1 = new State1();
}
