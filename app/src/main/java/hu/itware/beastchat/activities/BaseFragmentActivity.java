package hu.itware.beastchat.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hu.itware.beastchat.R;
import hu.itware.beastchat.utils.Constants;

/**
 * Created by gyongyosit on 2017.01.23..
 */

public abstract class BaseFragmentActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    abstract Fragment createFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_base);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_INFO_REFERENCE, Context.MODE_PRIVATE);
        final String userEmail = sharedPreferences.getString(Constants.USER_EMAIL, "");
        firebaseAuth = FirebaseAuth.getInstance();

        if (!((this instanceof LoginActivity) || (this instanceof RegisterActivity))) {
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser == null) {
                        Intent intent = new Intent(getApplication(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else if (userEmail.equals("")) {
                        firebaseAuth.signOut();
                        finish();
                    }
                }
            };
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.activity_fragment_base_fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.activity_fragment_base_fragmentContainer, fragment)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!((this instanceof LoginActivity) || (this instanceof RegisterActivity))) {
            firebaseAuth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!((this instanceof LoginActivity) || (this instanceof RegisterActivity))) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
