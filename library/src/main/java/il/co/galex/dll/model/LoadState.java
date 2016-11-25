package il.co.galex.dll.model;

/**
 * Load State is used internally in the DoubleLoadingBaseAsyncLoader to keep track of the current loaded state.
 * Use @see il.co.galex.DoubleLoadingUtils#getLoadState() to get the state from the loader.
 */
public enum LoadState {
    CACHE_LOADED, NETWORK_LOADED, NETWORK_RELOADED
}