package hu.itware.beastchat.views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import hu.itware.beastchat.fragments.FindFriendsFragment;
import hu.itware.beastchat.fragments.FriendRequestFragment;
import hu.itware.beastchat.fragments.UserFriendsFragment;

/**
 * Created by gyongyosit on 2017.01.26..
 */

public class FriendsViewPagerAdapter extends FragmentStatePagerAdapter {

    public FriendsViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = UserFriendsFragment.newInstance();
                break;
            case 1:
                fragment = FriendRequestFragment.newInstance();
                break;
            case 2:
                fragment = FindFriendsFragment.newInstance();
                break;

            default:
                return null;
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        CharSequence title;
        switch (position) {
            case 0:
                title = "Friends";
                break;
            case 1:
                title = "Requests";
                break;
            case 2:
                title = "Find Friends";
                break;

            default:
                return null;
        }
        return title;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
