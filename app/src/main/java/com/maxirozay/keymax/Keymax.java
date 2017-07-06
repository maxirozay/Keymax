package com.maxirozay.keymax;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by max on 6/22/17.
 */

public class Keymax extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("keymax.realm")
                .schemaVersion(0)
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
