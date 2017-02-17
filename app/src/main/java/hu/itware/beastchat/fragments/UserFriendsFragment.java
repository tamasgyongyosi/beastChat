package hu.itware.beastchat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hu.itware.beastchat.R;
import hu.itware.beastchat.activities.MessagesActivity;
import hu.itware.beastchat.entities.User;
import hu.itware.beastchat.services.LiveFriendsServices;
import hu.itware.beastchat.utils.Constants;
import hu.itware.beastchat.views.UserFriendsViews.UserFriendAdapter;

/**
 * Created by gyongyosit on 2017.01.26..
 */

public class UserFriendsFragment extends BaseFragment implements UserFriendAdapter.UserClickedListener {
    @BindView(R.id.fragment_user_friends_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_user_friends_noResults)
    TextView noResultsTv;
    private Unbinder unbinder;
    private String userEmailString;
    private LiveFriendsServices liveFriendsServices;
    private ValueEventListener getAllCurrentUserFriendsListener;
    private DatabaseReference getAllCurrentUserFriendsReference;

    public static UserFriendsFragment newInstance() {
        return new UserFriendsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userEmailString = sharedPreferences.getString(Constants.USER_EMAIL, "");
        liveFriendsServices = LiveFriendsServices.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_friends, container, false);
        unbinder = ButterKnife.bind(this, view);

        UserFriendAdapter adapter = new UserFriendAdapter(activity, this);
        getAllCurrentUserFriendsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_FRIENDS)
                .child(Constants.encodeEmail(userEmailString));
        getAllCurrentUserFriendsListener = liveFriendsServices.getAllFriends(adapter, recyclerView, noResultsTv, activity);
        getAllCurrentUserFriendsReference.addValueEventListener(getAllCurrentUserFriendsListener);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        if (getAllCurrentUserFriendsListener != null) {
            getAllCurrentUserFriendsReference.removeEventListener(getAllCurrentUserFriendsListener);
        }
    }

    @Override
    public void onUserClicked(User user) {
        ArrayList<String> friendDetails = new ArrayList<>();
        friendDetails.add(user.getEmail());
        friendDetails.add(user.getUserPicture());
        friendDetails.add(user.getUserName());
        Intent intent = MessagesActivity.newInstance(friendDetails, activity);
        startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_up, android.R.anim.fade_out);
    }
}
