package il.co.galex.dll.loader;

import android.content.Context;

/**
 * Async Loader to manages two different background calls (usually to a cache (sqlite) then to a server).
 * A initLoader on it will make a call to loadCacheInBackground() then to loadNetworkInBackground(),
 * and restartLoader will only call loadNetworkInBackground().
 *
 */
@SuppressWarnings("unused")
public abstract class DoubleLoadingAsyncLoader<T> extends DoubleLoadingBaseAsyncLoader<T> {

    private T mResult;

    public DoubleLoadingAsyncLoader(Context context) {
        super(context);
    }
    
    @Override
    public void deliverResult(T data) {

        if (isReset()) {
            return;
        }

        mResult = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }

        if (mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        mResult = null;
    }
}
