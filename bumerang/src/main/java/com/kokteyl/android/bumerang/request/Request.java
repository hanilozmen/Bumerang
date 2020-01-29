package com.kokteyl.android.bumerang.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.kokteyl.android.bumerang.core.Bumerang;
import com.kokteyl.android.bumerang.core.BumerangError;
import com.kokteyl.android.bumerang.core.BumerangLog;
import com.kokteyl.android.bumerang.response.HTTPCache;
import com.kokteyl.android.bumerang.response.Response;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Request<T> {
    public static final int READ_TIMEOUT_DEFAULT = 10 * 1000;
    public static final int CONNECT_TIMEOUT_DEFAULT = 10 * 1000;
    public static final int MIN_TIMEOUT_DEFAULT = 50;


    public static final String UTF_8 = "UTF-8";
    public static final String CONTENT_TYPE_KEY = "Content-Type";
    public static final String ACCEPT_KEY = "Accept";
    public static final String ACCEPT_CHARSET_KEY = "Accept-Charset";

    public static final String JSON_CONTENT_VALUE = "application/json";
    public static final String URL_ENCODED_CONTENT_VALUE = "application/x-www-form-urlencoded";

    private JsonElement params;
    private ConcurrentMap<String, String> headers;
    private String host = "";
    int connectTimeoutMs, readTimeoutMs;
    private String cacheKey;
    private boolean dontCache;

    public Response<T> performRequest(String requestType) {
        HttpURLConnection conn = null;
        Exception e = null;
        StringBuilder response = new StringBuilder();
        int responseCode = -1;
        int cacheDuration = 0;
        BufferedReader in = null;
        DataOutputStream outputStream = null;
        Map<String, List<String>> responseHeaders = null;
        try {
            conn = (HttpURLConnection) new URL(getHost()).openConnection();
            conn.setRequestMethod(requestType);

            conn.setConnectTimeout(connectTimeoutMs);
            conn.setReadTimeout(readTimeoutMs);
            conn.setUseCaches(false);
            Set<Map.Entry<String, String>> entrySet = getHeaders().entrySet();
            for (Map.Entry<String, String> header : entrySet) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
            if (requestType.equals("POST")) {
                conn.setDoOutput(true);
                outputStream = new DataOutputStream(conn.getOutputStream());
                if (getBody() != null)
                    outputStream.writeBytes(getBody());
            }

            responseCode = conn.getResponseCode();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            responseHeaders = conn.getHeaderFields();
            cacheDuration = getCacheDurationFromHeader(conn);
        } catch (Exception exception) {
            e = exception;
        }
        try {

            if (conn != null) conn.disconnect();
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        } catch (Exception ignored) {
        }
        if (e != null) {
            return new Response<T>(BumerangError.HTTP_EXCEPTION, e);
        } else {
            Response<T> responseObj = new Response<T>(response.toString(), responseCode, responseHeaders);
            putToCache(responseObj, cacheDuration);
            return responseObj;
        }
    }

    private <T> HTTPCache<T> putToCache(Response<T> response, int cacheDuration) {
        if(dontCache) return null;
        long localDate = System.currentTimeMillis();
        long localExpiresAt = localDate + (cacheDuration * 1000);
        if (cacheDuration > 0) {
            HTTPCache<T> cache = new HTTPCache<T>((Request<T>) this, response, localExpiresAt, getCacheKey());
            return Bumerang.putToCache(getCacheKey(), cache) ? cache : null;
        }
        return null;
    }

    public boolean isParamsEmpty() {
        if (getParams() == null || getParams().isJsonNull()) return true;
        if (getParams().isJsonObject()) {
            return getParams().getAsJsonObject().entrySet().isEmpty();
        } else if (getParams().isJsonArray()) {
            return getParams().getAsJsonArray().size() == 0;
        }
        return true;
    }

    JsonElement getParams() {
        return params;
    }

    void setHost(String host) {
        this.host = host;
    }

    void setCacheKey(String key) {
        if (key == null || key.trim().equals(""))
            cacheKey = String.valueOf(hashCode());
        else
            cacheKey = key;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    void setDontCache(boolean dontCache) {
        this.dontCache = dontCache;
    }

    public boolean dontCache() {
        return dontCache;
    }

    void setTimeout(int... timeoutValues) {
        this.connectTimeoutMs = CONNECT_TIMEOUT_DEFAULT;
        this.readTimeoutMs = READ_TIMEOUT_DEFAULT;
        if (timeoutValues == null) {
            return;
        } else if (timeoutValues.length == 1) {
            this.connectTimeoutMs = Math.max(MIN_TIMEOUT_DEFAULT, timeoutValues[0]);
            this.readTimeoutMs = Math.max(MIN_TIMEOUT_DEFAULT, timeoutValues[0]);
        } else if (timeoutValues.length == 2) {
            this.connectTimeoutMs = Math.max(MIN_TIMEOUT_DEFAULT, timeoutValues[0]);
            this.readTimeoutMs = Math.max(MIN_TIMEOUT_DEFAULT, timeoutValues[1]);
        }
    }

    void setParams(JsonElement object) {
        params = object;
    }

    void setHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) return;
        getHeaders().putAll(headers);
    }

    void addHeader(String key, String value) {
        if (key.isEmpty() || value.isEmpty()) return;
        getHeaders().put(key, value);
    }

    /**
     * Converts <httpCode>params</httpCode> into an application/x-www-form-urlencoded encoded string.
     */
    String urlEncodeParams(JsonElement params, String paramsEncoding) {
        if (params == null || params.isJsonNull()) return null;
        if (!params.isJsonObject()) throw new RuntimeException("");
        StringBuilder encodedParams = new StringBuilder();
        try {
            Set<Map.Entry<String, JsonElement>> entrySet = params.getAsJsonObject().entrySet();

            for (Map.Entry<String, JsonElement> entry : entrySet) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    BumerangLog.w("parameter or value is null. Skipping entry");
                    continue;
                }
                try {
                    StringBuilder itemBuilder = new StringBuilder();
                    JsonElement entryValue = entry.getValue();
                    String realValue = "";
                    if (entryValue.isJsonObject() || entryValue.isJsonArray()) {
                        realValue = Bumerang.get().gson().toJson(entryValue);
                    } else if (entryValue.isJsonPrimitive()) {
                        JsonPrimitive primitive = ((JsonPrimitive) entryValue);
                        if (primitive.isString())
                            realValue = primitive.getAsString();
                        else if (primitive.isBoolean())
                            realValue = String.valueOf(primitive.getAsBoolean());
                        else if (primitive.isNumber())
                            realValue = primitive.getAsNumber().toString();
                    } else if (entryValue.isJsonNull()) {
                        continue;
                    }
                    if (realValue.equals(""))
                        continue;
                    itemBuilder.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                    itemBuilder.append("=");
                    itemBuilder.append(URLEncoder.encode(realValue, paramsEncoding));
                    itemBuilder.append("&");
                    encodedParams.append(itemBuilder.toString());
                } catch (Exception e) {
                    BumerangLog.w("Error while adding parameters", e);
                }

            }
            if (!getTypeName().equals("POST") && !entrySet.isEmpty() && !encodedParams.toString().equals(""))
                encodedParams.insert(0, "?");
            if (encodedParams.toString().endsWith("&"))
                encodedParams.deleteCharAt(encodedParams.length() - 1);
            return encodedParams.toString();
        } catch (Exception ex) {
            if (ex instanceof RuntimeException)
                BumerangLog.w("Exception at urlEncodeParams. Params is not JsonObject: ", ex);
            else
                BumerangLog.w("Exception at urlEncodeParams. Encoding not supported: " + paramsEncoding, ex);
            return null;
        }
    }

    public String getTypeName() {
        throw new RuntimeException("type name is null");
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        String body = "body: " + (!isParamsEmpty() ? getParams().toString() : "");
        return String.format(Locale.ENGLISH, "-----> " + getTypeName() + " Request: %s, Cache Key: %s\n%s%s\n----->", host, getCacheKey(), headersAsString(), body);
    }

    public String headersAsString() {
        StringBuilder sb = new StringBuilder("headers: ");
        for (Map.Entry<String, String> header : getHeaders().entrySet()) {
            sb.append(" ").append(header.getKey()).append(" : ").append(header.getValue()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString() + "\n";
    }

    /**
     * Encodes post parameters and converts to byte array.
     * For instance requests, returns null
     **/
    public String getBody() {
        JsonElement params = getParams();
        if (!isParamsEmpty()) {
            try {
                return params.toString();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public String getEncoding() {
        return UTF_8;
    }

    public ConcurrentMap<String, String> getHeaders() {
        if (headers == null) {
            headers = new ConcurrentHashMap<String, String>();
            headers.put(ACCEPT_KEY, JSON_CONTENT_VALUE);
            headers.put(ACCEPT_CHARSET_KEY, UTF_8);
            headers.put(CONTENT_TYPE_KEY, JSON_CONTENT_VALUE);
        }
        return headers;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        ConcurrentMap<String, String> headers = getHeaders();
        hash = 31 * hash + getTypeName().hashCode();
        hash = 31 * hash + getHost().hashCode();
        hash = 31 * hash + (headers == null ? 0 : headers.toString().hashCode());
        hash = 31 * hash + (getParams() == null ? 0 : getParams().toString().hashCode());
        hash = 31 * hash + (getBody() == null ? 0 : getBody().hashCode());
        return hash;
    }

    /**
     * @param node HttpURLConnection node
     * @return duration time in seconds
     **/
    static int getCacheDurationFromHeader(HttpURLConnection node) {
        try {
            String cacheControl = node.getHeaderField("Cache-Control");
            String cacheControlSmall = node.getHeaderField("cache-control");
            String cacheDuration = node.getHeaderField("Cache-Duration");
            String cacheDurationSmall = node.getHeaderField("cache-duration");
            try {
                int duration = -1;
                if (!isNullOrEmpty(cacheControl))
                    duration = getDurationFromCacheControl(cacheControl);
                else if (!isNullOrEmpty(cacheControlSmall))
                    duration = getDurationFromCacheControl(cacheControlSmall);

                if (duration != -1)
                    return duration;
            } catch (Exception e) {
            }
            try {
                if (!isNullOrEmpty(cacheDuration))
                    return Integer.parseInt(cacheDuration);
            } catch (Exception e) {
            }
            try {
                if (!isNullOrEmpty(cacheDurationSmall))
                    return Integer.parseInt(cacheDurationSmall);
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
        return 0;
    }

    static boolean isNullOrEmpty(String x) {
        return x == null || x.equals("");
    }

    private static int getDurationFromCacheControl(String cacheControl) {
        try {
            String[] tokens = cacheControl.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (token.equalsIgnoreCase("no-cache") || token.equalsIgnoreCase("no-store")) {
                    return 0;
                } else if (token.startsWith("max-age=") || token.startsWith("Max-Age=")) {
                    return Integer.parseInt(token.substring(8));
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }
}
