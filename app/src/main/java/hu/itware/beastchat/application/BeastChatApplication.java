package hu.itware.beastchat.application;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by gyongyosit on 2017.01.25..
 */

public class BeastChatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
