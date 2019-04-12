package in.altilogic.prayogeek.utils;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class WaitTimer {
    private static final String TAG = "YOUSCOPE-WAIT-TIME";

    private ICompleteListener mListener;
    private Timer mStateTimer;
    private WaitTimerTask mWaitTimerTask;
    private int mTimeout;

    public interface ICompleteListener {
        void omFinish();
    }

    public WaitTimer(ICompleteListener listener, int timeout) {
        mWaitTimerTask = new WaitTimerTask();
        mListener = listener;
        mTimeout = timeout;
    }

    private class WaitTimerTask extends TimerTask {
        public void run() {
            if(mListener != null)
                mListener.omFinish();
        }
    }

    public void stop() {
        if(mStateTimer != null) {
            try{
                mStateTimer.cancel();
                mStateTimer = null;
            }
            catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        try {
            mStateTimer = new Timer();
            if( mWaitTimerTask != null) {
                mWaitTimerTask.cancel();
                mWaitTimerTask = null;
            }
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void refresh(){
        stop();
        try {
            mWaitTimerTask = new WaitTimerTask();
            mStateTimer.schedule(mWaitTimerTask, mTimeout);
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
