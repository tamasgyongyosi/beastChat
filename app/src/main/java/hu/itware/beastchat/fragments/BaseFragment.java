package hu.itware.beastchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import hu.itware.beastchat.R;
import hu.itware.beastchat.activities.BaseFragmentActivity;
import hu.itware.beastchat.activities.FriendsActivity;
import hu.itware.beastchat.activities.InBoxActivity;
import hu.itware.beastchat.activities.ProfileActivity;
import hu.itware.beastchat.utils.Constants;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by gyongyosit on 2017.01.23..
 */

public class BaseFragment extends Fragment {

    protected CompositeDisposable compositeDisposable;
    protected SharedPreferences sharedPreferences;
    protected BaseFragmentActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(Constants.USER_INFO_REFERENCE, Context.MODE_PRIVATE);
        compositeDisposable = new CompositeDisposable();
    }

    public void setUpBottomBar(BottomBar bottomBar, final int index) {
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (index) {
                    case 1:
                        if (tabId == R.id.tab_profile) {
                            startActivity(ProfileActivity.class);
                        } else if (tabId == R.id.tab_friends) {
                            startActivity(FriendsActivity.class);
                        }
                        break;
                    case 2:
                        if (tabId == R.id.tab_profile) {
                            startActivity(ProfileActivity.class);
                        } else if (tabId == R.id.tab_inBoxMessages) {
                            startActivity(InBoxActivity.class);
                        }
                        break;
                    case 3:
                        if (tabId == R.id.tab_friends) {
                            startActivity(FriendsActivity.class);
                        } else if (tabId == R.id.tab_inBoxMessages) {
                            startActivity(InBoxActivity.class);
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (BaseFragmentActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    private void startActivity(Class clazz) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
