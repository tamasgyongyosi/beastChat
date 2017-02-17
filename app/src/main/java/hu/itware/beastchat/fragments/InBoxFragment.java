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
import com.roughike.bottombar.BottomBar;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hu.itware.beastchat.R;
import hu.itware.beastchat.activities.MessagesActivity;
import hu.itware.beastchat.entities.ChatRoom;
import hu.itware.beastchat.services.LiveFriendsServices;
import hu.itware.beastchat.utils.Constants;
import hu.itware.beastchat.views.ChatRoomViews.ChatRoomAdapter;

/**
 * Created by gyongyosit on 2017.01.25..
 */

public class InBoxFragment extends BaseFragment implements ChatRoomAdapter.ChatRoomListener {

    @BindView(R.id.bottomBar)
    BottomBar bottomBar;
    @BindView(R.id.fragment_inbox_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_inbox_noMessages)
    TextView textView;

    private Unbinder unbinder;
    private LiveFriendsServices liveFriendsServices;
    private DatabaseReference allFriendRequestsReference;
    private ValueEventListener allFriendRequestsListener;
    private String userEmailString;
    private DatabaseReference userChatRoomsReference;
    private ValueEventListener userChatRoomsListener;
    private DatabaseReference usersNewMessagesReference;
    private ValueEventListener usersNewMessagesListener;

    public static InBoxFragment newInstance() {
        return new InBoxFragment();
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
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        unbinder = ButterKnife.bind(this, view);
        bottomBar.selectTabWithId(R.id.tab_inBoxMessages);
        setUpBottomBar(bottomBar, 1);

        allFriendRequestsListener = liveFriendsServices.getFriendRequestBottom(bottomBar, R.id.tab_friends);
        allFriendRequestsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_FRIEND_REQUESTS_RECEIVED)
                .child(Constants.encodeEmail(userEmailString));
        allFriendRequestsReference.addValueEventListener(allFriendRequestsListener);

        ChatRoomAdapter adapter = new ChatRoomAdapter(activity, this, userEmailString);
        userChatRoomsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_CHAT_ROOMS)
                .child(Constants.encodeEmail(userEmailString));
        userChatRoomsListener = liveFriendsServices.getAllChatRooms(recyclerView, textView, adapter);
        userChatRoomsReference.addValueEventListener(userChatRoomsListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);

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

        if (userChatRoomsListener != null) {
            userChatRoomsReference.removeEventListener(userChatRoomsListener);
        }

        if (usersNewMessagesListener != null) {
            usersNewMessagesReference.removeEventListener(usersNewMessagesListener);
        }
    }

    @Override
    public void onChatRoomClicked(ChatRoom chatRoom) {
        ArrayList<String> friendDetails = new ArrayList<>();
        friendDetails.add(chatRoom.getFriendEmail());
        friendDetails.add(chatRoom.getFriendPicture());
        friendDetails.add(chatRoom.getFriendName());
        Intent intent = MessagesActivity.newInstance(friendDetails, activity);
        startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_up, android.R.anim.fade_out);
    }
}
