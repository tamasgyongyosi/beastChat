package hu.itware.beastchat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import hu.itware.beastchat.R;
import hu.itware.beastchat.entities.ChatRoom;
import hu.itware.beastchat.entities.Message;
import hu.itware.beastchat.services.LiveFriendsServices;
import hu.itware.beastchat.utils.Constants;
import hu.itware.beastchat.views.MessagesViews.MessagesAdapter;
import io.reactivex.subjects.PublishSubject;
import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by gyongyosit on 2017.01.31..
 */

public class MessagesFragment extends BaseFragment {

    public static final String FRIEND_DETAILS_EXTRA = "FRIEND_DETAILS_EXTRA";
    private static final String TAG = MessagesFragment.class.getSimpleName();

    @BindView(R.id.fragment_messages_friendPicture)
    RoundedImageView friendPictureIv;
    @BindView(R.id.fragment_messages_friendName)
    TextView friendNameTv;
    @BindView(R.id.fragment_messages_messageBox)
    EditText messageBox;
    @BindView(R.id.fragment_messages_sendArrow)
    ImageView sendMessage;
    @BindView(R.id.fragment_messages_recyclerView)
    RecyclerView recyclerView;

    private Unbinder unbinder;
    private String friendEmailString;
    private String friendPictureString;
    private String friendNameString;
    private String userEmailString;
    private String userPictureString;
    private String userNameString;
    private Socket socket;
    private DatabaseReference getAllMessagesReference;
    private ValueEventListener getAllMessagesListener;
    private LiveFriendsServices liveFriendsServices;
    private PublishSubject<String> messageSubject = PublishSubject.create();
    private DatabaseReference userChatRoomReference;
    private MessagesAdapter adapter;
    private ValueEventListener userChatRoomListener;

    public static MessagesFragment newInstance(ArrayList<String> friendDetails) {
        Bundle arguments = new Bundle();
        arguments.putStringArrayList(FRIEND_DETAILS_EXTRA, friendDetails);
        MessagesFragment messagesFragment = new MessagesFragment();
        messagesFragment.setArguments(arguments);
        return messagesFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            socket = IO.socket(Constants.IP_LOCAL_HOST);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            Toast.makeText(getActivity(), "Can't connect to the server", Toast.LENGTH_SHORT).show();
        }
        socket.connect();
        ArrayList<String> friendDetails = getArguments().getStringArrayList(FRIEND_DETAILS_EXTRA);
        friendEmailString = friendDetails.get(0);
        friendPictureString = friendDetails.get(1);
        friendNameString = friendDetails.get(2);
        userEmailString = sharedPreferences.getString(Constants.USER_EMAIL, "");
        userPictureString = sharedPreferences.getString(Constants.USER_PICTURE, "");
        userNameString = sharedPreferences.getString(Constants.USER_NAME, "");
        liveFriendsServices = LiveFriendsServices.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        unbinder = ButterKnife.bind(this, view);

        Picasso.with(activity)
                .load(friendPictureString)
                .into(friendPictureIv);

        friendNameTv.setText(friendNameString);

        adapter = new MessagesAdapter(activity, userEmailString);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);

        getAllMessagesReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_MESSAGES)
                .child(Constants.encodeEmail(userEmailString))
                .child(Constants.encodeEmail(friendEmailString));
        getAllMessagesListener = liveFriendsServices.getAllMessages(recyclerView, friendNameTv, friendPictureIv, adapter, userEmailString);
        getAllMessagesReference.addValueEventListener(getAllMessagesListener);

        userChatRoomReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_CHAT_ROOMS)
                .child(Constants.encodeEmail(userEmailString))
                .child(Constants.encodeEmail(friendEmailString));
        userChatRoomListener = liveFriendsServices.getCurrentChatRoom(userChatRoomReference, friendEmailString);
        userChatRoomReference.addValueEventListener(userChatRoomListener);

        compositeDisposable.add(liveFriendsServices.createChatRoomDisposable(messageSubject, friendEmailString, friendPictureString, friendNameString, userEmailString, userChatRoomReference));
        setMessageBoxListener();

        return view;
    }

    private void setMessageBoxListener() {
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                messageSubject.onNext(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        if (getAllMessagesListener != null) {
            getAllMessagesReference.removeEventListener(getAllMessagesListener);
        }

        if (userChatRoomListener != null) {
            userChatRoomReference.removeEventListener(userChatRoomListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    @OnClick(R.id.fragment_messages_sendArrow)
    public void setSendMessage() {
        String messageText = messageBox.getText().toString();
        if (!messageText.isEmpty()) {

            ChatRoom chatRoom = new ChatRoom(friendPictureString, friendNameString, friendEmailString, messageText, userEmailString, true, true);
            userChatRoomReference.setValue(chatRoom);

            DatabaseReference newMessageReference = getAllMessagesReference.push();
            Message message = new Message(newMessageReference.getKey(), messageText, userEmailString, userPictureString);
            newMessageReference.setValue(message);

            compositeDisposable.add(liveFriendsServices.sendMessage(socket, userEmailString, userPictureString, messageText, friendEmailString, userNameString));

            messageBox.setText("");

            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        }
    }

}
