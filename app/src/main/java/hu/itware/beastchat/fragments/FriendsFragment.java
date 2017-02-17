package hu.itware.beastchat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hu.itware.beastchat.R;
import hu.itware.beastchat.services.LiveFriendsServices;
import hu.itware.beastchat.utils.Constants;
import hu.itware.beastchat.views.FriendsViewPagerAdapter;

/**
 * Created by gyongyosit on 2017.01.25..
 */

public class FriendsFragment extends BaseFragment {
    @BindView(R.id.bottomBar)
    BottomBar bottomBar;

    @BindView(R.id.fragment_friends_tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.fragment_friends_viewPager)
    ViewPager viewPager;

    private LiveFriendsServices liveFriendsServices;
    private DatabaseReference allFriendRequestsReference;
    private ValueEventListener allFriendRequestsListener;
    private String userEmailString;
    private Unbinder unbinder;
    private DatabaseReference usersNewMessagesReference;
    private ValueEventListener usersNewMessagesListener;

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        liveFriendsServices = LiveFriendsServices.getInstance();
        userEmailString = sharedPreferences.getString(Constants.USER_EMAIL, "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        unbinder = ButterKnife.bind(this, view);
        bottomBar.selectTabWithId(R.id.tab_friends);
        setUpBottomBar(bottomBar, 2);

        FriendsViewPagerAdapter friendsViewPagerAdapter = new FriendsViewPagerAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(friendsViewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        allFriendRequestsListener = liveFriendsServices.getFriendRequestBottom(bottomBar, R.id.tab_friends);
        allFriendRequestsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_FRIEND_REQUESTS_RECEIVED)
                .child(Constants.encodeEmail(userEmailString));
        allFriendRequestsReference.addValueEventListener(allFriendRequestsListener);

        usersNewMessagesReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_NEW_MESSAGES).child(Constants.encodeEmail(userEmailString));
        usersNewMessagesListener = liveFriendsServices.getAllNewMessages(bottomBar, R.id.tab_inBoxMessages);
        usersNewMessagesReference.addValueEventListener(usersNewMessagesListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        if (allFriendRequestsListener != null) {
            allFriendRequestsReference.removeEventListener(allFriendRequestsListener);
        }

        if (usersNewMessagesListener != null) {
            usersNewMessagesReference.removeEventListener(usersNewMessagesListener);
        }
    }
}
