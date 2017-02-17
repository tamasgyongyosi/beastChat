package hu.itware.beastchat.activities;

import android.support.v4.app.Fragment;

import hu.itware.beastchat.fragments.ProfileFragment;

/**
 * Created by gyongyosit on 2017.01.25..
 */

public class ProfileActivity extends BaseFragmentActivity {
    @Override
    Fragment createFragment() {
        return ProfileFragment.newInstance();
    }
}
