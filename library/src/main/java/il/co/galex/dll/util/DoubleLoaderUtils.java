package il.co.galex.dll.util;

import android.support.v4.content.Loader;

import il.co.galex.dll.loader.DoubleLoading;
import il.co.galex.dll.model.LoadState;

/**
 * Utiliy to get the loading state out of class extending DoubleLoadingBaseAsyncLoader to know in which
 * loading state we are.
 */

public class DoubleLoaderUtils {

    public static LoadState getLoadState(Loader loader){

        if (loader instanceof DoubleLoading){
            DoubleLoading doubleLoading = (DoubleLoading) loader;
            return doubleLoading.getLoadState();
        }
        return null;
    }
}
