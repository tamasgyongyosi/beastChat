package hu.itware.beastchat.activities;

import android.support.v4.app.Fragment;

import hu.itware.beastchat.fragments.LoginFragment;

public class LoginActivity extends BaseFragmentActivity {

    @Override
    Fragment createFragment() {
        return LoginFragment.newInstance();
    }
}
