package com.kokteyl.android.bumerang.core;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kokteyl.android.bumerang.image.BumerangImageLoader;
import com.kokteyl.android.bumerang.request.Request;
import com.kokteyl.android.bumerang.request.RequestParser;
import com.kokteyl.android.bumerang.response.Cacheable;
import com.kokteyl.android.bumerang.response.HTTPCache;
import com.kokteyl.android.bumerang.response.ResponseListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class Bumerang {
    private String mBaseUrl;
    private ThreadPoolExecutor mExecutor;
    private static Bumerang mInstance;
    private Context mContext;
    private Gson mGson;
    private ConcurrentMap<String, Cacheable> mHttpCache = new ConcurrentHashMap<>();
    private BumerangLifecycleObserver mLifecycleObserver;

    public ConcurrentMap<String, Cacheable> getHttpCache() {
        if (mHttpCache == null)
            mHttpCache = new ConcurrentHashMap<String, Cacheable>();
        return mHttpCache;
    }

    public Gson gson() {
        if (mGson == null) {
            synchronized (Bumerang.class) {
                if (mGson == null) {
                    mGson = new GsonBuilder().serializeNulls().excludeFieldsWithModifiers(Modifier.STATIC).create();
                }
            }
        }
        return mGson;
    }

    public Context context() {
        return mContext;
    }

    private void setGson(Gson gsonExternal) {
        if (gsonExternal != null)
            mGson = gsonExternal;
        gson();
    }

    private Bumerang(String baseUrl, ThreadPoolExecutor executor, Context context) {
        mBaseUrl = baseUrl;
        mContext = context.getApplicationContext();
        mHttpCache = getHttpCache();
        if (executor == null)
            mExecutor = new ThreadPoolExecutor(10, 75, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(50));
        else
            mExecutor = executor;

        mLifecycleObserver = new BumerangLifecycleObserver(getListener());
        if (context instanceof Activity) {
            ((Activity) context).getApplication().unregisterActivityLifecycleCallbacks(mLifecycleObserver);
            ((Activity) context).getApplication().registerActivityLifecycleCallbacks(mLifecycleObserver);
        } else if (context instanceof Application) {
            ((Application) context).unregisterActivityLifecycleCallbacks(mLifecycleObserver);
            ((Application) context).registerActivityLifecycleCallbacks(mLifecycleObserver);
        }
    }

    private BumerangLifecycleObserver.Listener getListener() {
        return new BumerangLifecycleObserver.Listener() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        };
    }

    private Bumerang() {
        mBaseUrl = "";
        mExecutor = null;
    }

    public static Bumerang get() {
        if (mInstance == null) {
            synchronized (Bumerang.class) {
                if (mInstance == null) {
                    //error case
                    mInstance = new Bumerang();
                }
            }
        }
        return mInstance;
    }

    public static boolean putToCache(String key, Cacheable cacheObject) {
        if (key == null || key.trim().equals("") || cacheObject == null) return true;
        get().getHttpCache().put(key, cacheObject);
        BumerangPrefs.instance().put(key, cacheObject);
        return true;
    }

    public static <T extends Cacheable> T getFromCache(String key) {
        if (key == null || key.trim().equals("")) return null;
        try {
            Cacheable memoryCacheItem = get().getHttpCache().get(key);
            if (memoryCacheItem != null) {
                return (T) memoryCacheItem;
            } else {
                Cacheable prefCacheItem = BumerangPrefs.instance().get(key, HTTPCache.class);
                if (prefCacheItem != null)
                    get().getHttpCache().put(key, prefCacheItem);
                return (T) prefCacheItem;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bumerang set(String baseUrl, ThreadPoolExecutor executor, Gson gsonExternal, Context context) {
        if (mInstance == null) {
            synchronized (Bumerang.class) {
                if (mInstance == null) {
                    mInstance = new Bumerang(baseUrl, executor, context);
                    mInstance.setGson(gsonExternal);
                }
            }
        }
        return mInstance;
    }

    public ThreadPoolExecutor getExecutor() {
        return mExecutor;
    }

    public <T> T initAPI(final Class<T> service) {
        try {
            return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Request request = RequestParser.getRequestObject(method, args, mBaseUrl);
                    if (request == null || getExecutor() == null) {
                        BumerangLog.e("Request or Executor is null since Bumerang init failed!");
                        return null;
                    } else if (method.getReturnType() != Request.class) {
                        BumerangLog.e("Defined Interface return type should be Request.  You passed: " + method.getReturnType());
                        return null;
                    }
                    ResponseListener listener = (ResponseListener) args[args.length - 1];
                    new BumerangTask<Request<Object>, Object>(listener).executeOnExecutor(getExecutor(), request);
                    return request;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Image Loading Part */
    public void loadImage(ImageView imageView, String url) {
        if (context() == null || imageView == null) return;
        BumerangImageLoader.Core.getInstance(context()).displayImage(imageView, url);
    }

    public void loadImage(ImageView imageView, String url, BumerangImageLoader.ImageAnimation animation) {
        if (context() == null || imageView == null) return;
        BumerangImageLoader.Core.getInstance(context()).displayImage(imageView, url, animation);
    }

    public void loadImage(ImageView imageView, String url, BumerangImageLoader.ImageAnimation animation, int animationDuration) {
        if (context() == null || imageView == null) return;
        BumerangImageLoader.Core.getInstance(context()).displayImage(imageView, url, animation, animationDuration);
    }

    /* Image Loading Part */


    public void cancelAllImageTasks() {
        try {
            if (context() != null)
                BumerangImageLoader.Core.getInstance(context()).clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // TODO should not be public. We need to detect app exit
    private void cancelAllRequestTasks() {
        try {
            ThreadPoolExecutor executor = getExecutor();
            if (executor != null && !executor.isShutdown())
                executor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static final class Builder {
        private String mBaseUrl;
        private ThreadPoolExecutor mExecutor;
        private Gson mGson;
        private Context mContext;


        public Builder(Context context, String baseUrl) {
            mContext = context == null ? null : context.getApplicationContext();
            mBaseUrl = baseUrl;
        }

        public Builder executor(ThreadPoolExecutor executor) {
            mExecutor = executor;
            return this;
        }

        public Builder gson(Gson gson) {
            mGson = gson;
            return this;
        }

        public Bumerang build(InitListener initListener) {
            BumerangError error = checkBaseUrl(mBaseUrl);
            Bumerang.set(mBaseUrl, mExecutor, mGson, mContext);
            if (mContext == null)
                error = BumerangError.CONTEXT_NULL;
            if (error != null) {
                BumerangLog.e(error.toString());
                if (initListener != null)
                    initListener.onError(error);
            } else {
                BumerangLog.i("Bumerang init is successful");
                if (initListener != null)
                    initListener.onSuccess();

            }
            return get();
        }

        public Bumerang build() {
            return build(null);
        }

        static BumerangError checkBaseUrl(String baseUrl) {
            if (!baseUrl.startsWith("https"))
                BumerangLog.w("Base url does not start with HTTPS. In order to support Android 9+ devices, convert you base url to https or add network security config to AndroidManifest.xml");
            if (!baseUrl.endsWith("/") || !(baseUrl.startsWith("https://") || baseUrl.startsWith("http://")))
                return BumerangError.BASE_URL_ERROR;
            return null;
        }
    }

    public interface InitListener {
        void onError(BumerangError error);

        void onSuccess();
    }

}
