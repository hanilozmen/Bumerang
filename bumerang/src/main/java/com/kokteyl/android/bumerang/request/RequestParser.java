package com.kokteyl.android.bumerang.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kokteyl.android.bumerang.annotations.BaseUrl;
import com.kokteyl.android.bumerang.annotations.Body;
import com.kokteyl.android.bumerang.annotations.CustomCacheKey;
import com.kokteyl.android.bumerang.annotations.DELETE;
import com.kokteyl.android.bumerang.annotations.FormURLEncoded;
import com.kokteyl.android.bumerang.annotations.GET;
import com.kokteyl.android.bumerang.annotations.Header;
import com.kokteyl.android.bumerang.annotations.Headers;
import com.kokteyl.android.bumerang.annotations.NoCache;
import com.kokteyl.android.bumerang.annotations.POST;
import com.kokteyl.android.bumerang.annotations.PUT;
import com.kokteyl.android.bumerang.annotations.Path;
import com.kokteyl.android.bumerang.annotations.Timeout;
import com.kokteyl.android.bumerang.core.Bumerang;
import com.kokteyl.android.bumerang.core.BumerangError;
import com.kokteyl.android.bumerang.core.BumerangLog;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RequestParser {

    private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{([a-zA-Z][a-zA-Z0-9_-]*)\\}");

    public static <T> Request<T> getRequestObject(Method method, Object[] parameters, String baseUrl) {
        try {
            JsonElement bodyJson = null;
            Class reqType = method.getAnnotation(GET.class) != null ? GET.class :
                    (method.getAnnotation(POST.class) != null ? POST.class :
                            (method.getAnnotation(DELETE.class) != null ? DELETE.class :
                                    (method.getAnnotation(PUT.class) != null ? PUT.class : null)));
            Annotation[][] annotations = method.getParameterAnnotations();

            String path = getPath(reqType, method);
            boolean formUrlEncoded = method.getAnnotation(FormURLEncoded.class) != null;
            boolean dontCache = method.getAnnotation(NoCache.class) != null;


            if (method.getAnnotation(BaseUrl.class) != null)
                baseUrl = method.getAnnotation(BaseUrl.class).value();

            int[] timeoutArray = null;
            if (method.getAnnotation(Timeout.class) != null) {
                timeoutArray = new int[2];
                timeoutArray[0] = method.getAnnotation(Timeout.class).connect();
                timeoutArray[1] = method.getAnnotation(Timeout.class).read();
            }

            Map<String, String> headerMap = null;
            String customCacheKey = null;
            for (int index = 0; index < annotations.length; index++) {
                if (annotations[index] == null || annotations[index].length == 0) continue;
                Annotation annotation = annotations[index][0];
                if (annotation instanceof Path) {
                    path = processPath(annotation, parameters[index], path);
                } else if (annotation instanceof Body && parameters[index] != null) {
                    String temp = Bumerang.get().gson().toJson(parameters[index], parameters[index].getClass());
                    bodyJson = JsonParser.parseString(temp);
                } else if (annotation instanceof Headers) {
                    try {
                        headerMap = (Map<String, String>) parameters[index];
                    } catch (Exception e) {
                        BumerangLog.w("Skipping headers. Cause: Headers should be the type of Map<String, String>", e);
                    }
                } else if (annotation instanceof Header) {
                    if (!(parameters[index] instanceof String)) {
                        BumerangLog.w("Skipping header. Cause: Header should be the type of String");
                        continue;
                    }
                    if (headerMap == null) {
                        headerMap = new HashMap<String, String>();
                    }
                    headerMap.put(((Header) annotation).value(), (String) parameters[index]);
                } else if (annotation instanceof CustomCacheKey) {
                    customCacheKey = (String) parameters[index];
                }
            }
            return getRequest(reqType, baseUrl + path, headerMap, bodyJson, timeoutArray, formUrlEncoded, dontCache, customCacheKey);
        } catch (Exception e) {
            BumerangLog.e(BumerangError.REQUEST_PARSE_EXCEPTION, e);
        }
        return null;
    }

    private static <T> Request<T> getRequest(Class reqType, String host, Map<String, String> headerMap, JsonElement bodyJson, int[] timeoutArray, boolean formUrlEncoded, boolean dontCache, String customCacheKey) {
        if (reqType == GET.class) {
            return new GetRequest<T>(customCacheKey, host, headerMap, bodyJson, dontCache, timeoutArray);
        } else if (reqType == POST.class) {
            return new PostRequest<T>(customCacheKey, host, headerMap, bodyJson, dontCache, formUrlEncoded, timeoutArray);
        } else if (reqType == PUT.class) {
            return new PutRequest<T>(customCacheKey, host, headerMap, bodyJson, dontCache, timeoutArray);
        } else {
            return new DeleteRequest<T>(customCacheKey, host, headerMap, bodyJson, dontCache, timeoutArray);
        }
    }

    private static String getPath(Class reqType, Method method) {
        String path = "";
        if (reqType == null) {
            throw new RuntimeException("Illegal request type");
        } else if (reqType == GET.class) {
            path = method.getAnnotation(GET.class).value();
        } else if (reqType == POST.class) {
            path = method.getAnnotation(POST.class).value();
        } else if (reqType == PUT.class) {
            path = method.getAnnotation(PUT.class).value();
        } else if (reqType == DELETE.class) {
            path = method.getAnnotation(DELETE.class).value();
        }
        return path;
    }

    public static List<String> parsePathParameters(String path) {
        Matcher m = PARAM_URL_REGEX.matcher(path);
        List<String> patterns = new ArrayList<String>();
        while (m.find()) {
            patterns.add(m.group(1));
        }
        return patterns;
    }

    public static String processPath(Annotation annotation, Object parameter, String endpointRaw) {
        Path pathHolder = (Path) annotation;
        List<String> pathKeys = parsePathParameters(endpointRaw);
        for (int i = 0; i < pathKeys.size(); i++) {
            if (pathKeys.contains(pathHolder.value())) {
                try {
                    if (!(parameter instanceof String)) break;
                    String paramValue = pathHolder.encoded() ? URLEncoder.encode((String) parameter, Request.UTF_8) : (String) parameter;
                    endpointRaw = endpointRaw.replace("{" + pathHolder.value() + "}", paramValue);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return endpointRaw;
    }


}
