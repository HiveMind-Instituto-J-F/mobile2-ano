package com.aula.mobile_hivemind.auth;

import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";
    private static final String CLOUD_NAME = "djouiin10";

    private static boolean initialized = false;

    public static synchronized void init(Context context) {
        if (initialized) {
            return;
        }

        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("secure", true);
            MediaManager.init(context, config);
            initialized = true;
        } catch (NoSuchMethodError nsme) {
            try {
                MediaManager.init(context);
                initialized = true;
            } catch (Throwable t) {
                initialized = false;
            }
        } catch (Throwable t) {
            initialized = false;
        }
    }

    public static synchronized boolean isInitialized() {
        if (initialized) return true;
        try {
            Object mm = MediaManager.get();
            if (mm != null) {
                return initialized;
            }
        } catch (Throwable ignored) {}
        return initialized;
    }
}
