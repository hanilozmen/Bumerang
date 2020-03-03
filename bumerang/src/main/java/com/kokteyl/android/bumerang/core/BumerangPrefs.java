package com.kokteyl.android.bumerang.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.kokteyl.android.bumerang.response.HTTPCache;

import java.lang.reflect.Type;

public class BumerangPrefs {
    private static final String PREF_KEY = "BumerangSharedPreferences";

    public final SharedPreferences.Editor getEditor() {
        return instance().editor;
    }

    public final SharedPreferences getPreferences() {
        return instance().preferences;
    }

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private static BumerangPrefs instance;

    private BumerangPrefs() {
        preferences = Bumerang.get().context().getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public final static BumerangPrefs instance() {
        if (instance == null) {
            synchronized (HTTPCache.class) {
                if (instance == null) {
                    instance = new BumerangPrefs();
                }
            }
        }
        return instance;
    }

    final void clear() {
        if(editor !=null)
            editor.clear().apply();
    }

    public final <T> String put(String key, T value) {
        try {
            if (key == null || key.trim().equals("")) return "";
            if (value instanceof String) {
                getEditor().putString(key, (String) value);
            } else if (value instanceof Integer) {
                getEditor().putInt(key, (Integer) value);
            } else if (value instanceof Float ) {
                getEditor().putFloat(key, (Float) value);
            } else if (value instanceof Double){
                getEditor().putFloat(key, ((Double)value).floatValue());
            }else if (value instanceof Long) {
                getEditor().putLong(key, (Long) value);
            } else if (value instanceof Boolean) {
                getEditor().putBoolean(key, (Boolean) value);
            } else {
                String json = Bumerang.get().gson().toJson(value, value.getClass());
                getEditor().putString(key, json);
            }
            getEditor().apply();
            return key;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public final <T> T get(String key, Class<T> type) {
        try {
            SharedPreferences pref = getPreferences();
            if (!pref.contains(key)) return null;
            if (type == Integer.class) {
                return (T) Integer.valueOf(pref.getInt(key, -1));
            } else if (type == Float.class) {
                return (T) Float.valueOf(pref.getFloat(key, -1f));
            } else if (type == Double.class) {
                return (T) Double.valueOf(pref.getFloat(key, -1f));
            } else if (type == Long.class) {
                return (T) Long.valueOf(pref.getLong(key, -1));
            } else if (type == Boolean.class) {
                return (T) Boolean.valueOf(pref.getBoolean(key, false));
            } else {
                String value = pref.getString(key, "");
                if (value.equals("")) return null;
                return Bumerang.get().gson().fromJson(value, type);
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
