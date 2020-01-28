package com.kokteyl.android.bumerang.response;

public interface Cacheable {
    String putToCache();
    Cacheable getFromCache();
    String getCacheKey();
    long getExpiresAt(); //ms
    boolean isExpired();
    int getRemainingExpirationTime(); //sec
}
