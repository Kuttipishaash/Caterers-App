package com.caterbazar;

import android.app.Application;

import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.android.utils.FlipperUtils;
import com.facebook.flipper.android.FlipperClient;
//import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.soloader.SoLoader;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SoLoader.INSTANCE.init(this, false);
        if (BuildConfig.DEBUG && FlipperUtils.INSTANCE.shouldEnableFlipper(this)) {
            final FlipperClient client = AndroidFlipperClient.INSTANCE.getInstance(this);
            client.addPlugin(new InspectorFlipperPlugin(this, DescriptorMapping.INSTANCE.withDefaults()));
            client.addPlugin(new SharedPreferencesFlipperPlugin(this));
            client.start();
        }
    }
}
