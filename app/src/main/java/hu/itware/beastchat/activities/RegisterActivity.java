package hu.itware.beastchat.activities;

import android.support.v4.app.Fragment;

import hu.itware.beastchat.fragments.RegisterFragment;

/**
 * Created by gyongyosit on 2017.01.24..
 */

public class RegisterActivity extends BaseFragmentActivity {

    @Override
    Fragment createFragment() {
        return RegisterFragment.newInstance();
    }
}
