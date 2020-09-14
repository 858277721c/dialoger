package com.sd.lib.dialoger.impl;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class FDialogerHolder
{
    private static final Map<Activity, Map<FDialoger, String>> MAP_ACTIVITY_DIALOG = new HashMap<>();
    private static final Map<Activity, ActivityConfig> MAP_ACTIVITY_CONFIG = new HashMap<>();

    static synchronized void addDialoger(FDialoger dialoger)
    {
        final Activity activity = dialoger.getOwnerActivity();

        Map<FDialoger, String> holder = MAP_ACTIVITY_DIALOG.get(activity);
        if (holder == null)
        {
            holder = new WeakHashMap<>();
            MAP_ACTIVITY_DIALOG.put(activity, holder);
        }

        holder.put(dialoger, "");
    }

    static synchronized void removeDialoger(FDialoger dialoger)
    {
        final Activity activity = dialoger.getOwnerActivity();

        final Map<FDialoger, String> holder = MAP_ACTIVITY_DIALOG.get(activity);
        if (holder == null)
            return;

        holder.remove(dialoger);
        if (holder.isEmpty())
        {
            MAP_ACTIVITY_DIALOG.remove(activity);
            restoreActivityConfig(activity);
        }
    }

    static synchronized List<FDialoger> get(Activity activity)
    {
        final Map<FDialoger, String> holder = MAP_ACTIVITY_DIALOG.get(activity);
        if (holder == null)
            return null;

        return new ArrayList<>(holder.keySet());
    }

    static synchronized void remove(Activity activity)
    {
        MAP_ACTIVITY_DIALOG.remove(activity);
        restoreActivityConfig(activity);
    }

    private static void restoreActivityConfig(Activity activity)
    {
        final ActivityConfig config = MAP_ACTIVITY_CONFIG.remove(activity);
        if (config != null)
            config.restore(activity);
    }

    /**
     * 返回activity对象对应的配置
     *
     * @param activity
     * @return
     */
    public static synchronized ActivityConfig getActivityConfig(Activity activity)
    {
        ActivityConfig config = MAP_ACTIVITY_CONFIG.get(activity);
        if (config == null)
        {
            config = new ActivityConfig();
            MAP_ACTIVITY_CONFIG.put(activity, config);
        }
        return config;
    }

    public static final class ActivityConfig
    {
        private Integer mSystemUiVisibility;

        public void setSystemUiVisibility(Integer systemUiVisibility)
        {
            mSystemUiVisibility = systemUiVisibility;
        }

        private void restore(Activity activity)
        {
            if (activity == null)
                return;

            if (mSystemUiVisibility != null)
                activity.getWindow().getDecorView().setSystemUiVisibility(mSystemUiVisibility);
        }
    }
}
