# Double Loading Loaders (1.1.0)
## Introduction

Loader to implement the following flow:

- Load in the background the cache and present the data
- Load in the background the data via a network request, update the cache and represent the data
- ForceLoad() on the loader to restart the flow from the network request only
- Determine on onLoadFinished() in which Load State we are
![alt text](https://github.com/galex/double-loading-loaders/raw/master/double-loading-loaders.png "Double Loading Loaders Flow")
## Usage

In your app module build.gradle add the dependency, published in jCenter:

```gradle
repositories {
    jcenter()
}
dependencies {
    (...)
    compile 'il.co.galex:double-loading-loaders:1.1.0'
}
```

## Example

Extend the class **DoubleLoadingAsyncLoader<T>** to implement the double loading flow

```java
public class ChatLoader extends DoubleLoadingAsyncLoader<List<Chat>> {

    private final ChatDao chatDao;
    private String uuid;

    public ChatLoader(Context context, String uuid) {
        super(context);
        this.uuid = uuid;
        chatDao = new ChatDao(getContext());
    }
    @Override
    public List<Chat> loadCacheInBackground() {
        return chatDao.findAll();
    }

    @Override
    public List<Chat> loadNetworkInBackground() {
        chatDao.insertOrUpdate(ApiHelper.getUserChats(uuid)); // API call + Update of Database
        return chatDao.findAll(); // reload from database as the data is merged by the previous line
    }
}
```


in **onLoadFinished()**, to determine in what step of the flow we are, use **DoubleLoaderUtils.getLoadState(loader)**
```java
 @Override
    public void onLoadFinished(Loader<List<Comment>> loader, List<Chat> data) {

        final LoadState loadState = DoubleLoaderUtils.getLoadState(loader);
        android.util.Log.d(TAG, "onLoadFinished() called with: load state = " + loadState);
        if (loadState == LoadState.NETWORK_LOADED || loadState == LoadState.NETWORK_RELOADED) {
            //TODO remove the loading indicator
        }

        // TODO set data in your adapter
    }
```

## Roadmap

- CursorLoader with the same concept

## License
This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

(c) All rights reserved Alexander Gherschon