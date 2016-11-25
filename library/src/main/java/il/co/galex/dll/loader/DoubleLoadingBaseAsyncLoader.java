package il.co.galex.dll.loader;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.content.Loader;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import il.co.galex.dll.model.LoadState;

/**
 *
 * @author Alexandre Gherschon
 */

public abstract class DoubleLoadingBaseAsyncLoader<D> extends Loader<D> implements DoubleLoading {

    private static final String TAG = DoubleLoadingBaseAsyncLoader.class.getSimpleName();

    private static final boolean DEBUG = true;

    private ExecutorService executorService;
    private static Handler mainHandler;

    private static int DELIVER_DATA_MSG = 1;
    private static LoadState loadState;

    private Future<?> cacheRunnable;
    private Future<?> networkRunnable;

    public DoubleLoadingBaseAsyncLoader(Context context) {
        super(context);
        executorService = Executors.newFixedThreadPool(1);
        mainHandler = new Handler(Looper.getMainLooper()) {

            @SuppressWarnings("unchecked")
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == DELIVER_DATA_MSG) {
                    deliverResult((D) msg.obj);
                }
            }
        };
    }

    @WorkerThread
    public abstract D loadCacheInBackground();

    @WorkerThread
    public abstract D loadNetworkInBackground();

    @Override
    synchronized protected void onForceLoad() {

        if (DEBUG) Log.d(TAG, "onForceLoad() called");

        if (loadState == null) {

            if (DEBUG) Log.d(TAG, "onForceLoad() submitted cache runnable");
            cacheRunnable = executorService.submit(new Runnable() {
                @Override
                public void run() {

                    if (DEBUG) Log.d(TAG, "onForceLoad() running loadCacheInBackground on Thread " + Thread.currentThread().getName());
                    mainHandler.sendMessage(createMessage(loadCacheInBackground()));
                    loadState = LoadState.CACHE_LOADED;
                }
            });

            if (DEBUG) Log.d(TAG, "onForceLoad() onForceLoad() submitted network runnable");
            networkRunnable = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "onForceLoad() running loadNetworkInBackground on Thread " + Thread.currentThread().getName());
                    mainHandler.sendMessage(createMessage(loadNetworkInBackground()));
                    loadState = LoadState.NETWORK_LOADED;
                }
            });

        } else if (loadState == LoadState.NETWORK_LOADED || loadState == LoadState.NETWORK_RELOADED) {

            if (DEBUG) Log.d(TAG, "onForceLoad() onForceLoad() re-submitted network runnable");
            networkRunnable = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "onForceLoad() re-running loadNetworkInBackground on Thread " + Thread.currentThread().getName());
                    mainHandler.sendMessage(createMessage(loadNetworkInBackground()));
                    loadState = LoadState.NETWORK_RELOADED;
                }
            });
        }
    }

    @Override
    protected boolean onCancelLoad() {

        if (DEBUG) Log.d(TAG, "onCancelLoad() called");

        boolean cancelled = true;
        if (cacheRunnable != null) cancelled = cacheRunnable.cancel(true);
        if (networkRunnable != null) cancelled &= networkRunnable.cancel(true);
        return cancelled;
    }

    @NonNull
    private Message createMessage(D data) {

        Message message = new Message();
        message.what = DELIVER_DATA_MSG;
        message.obj = data;
        return message;
    }

    @Override
    public LoadState getLoadState() {
        return loadState;
    }
}
