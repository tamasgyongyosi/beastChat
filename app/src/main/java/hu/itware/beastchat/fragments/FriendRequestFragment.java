package hu.itware.beastchat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hu.itware.beastchat.R;
import hu.itware.beastchat.entities.User;
import hu.itware.beastchat.services.LiveFriendsServices;
import hu.itware.beastchat.utils.Constants;
import hu.itware.beastchat.views.FriendRequestViews.FriendRequestAdapter;
import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by gyongyosit on 2017.01.26..
 */

public class FriendRequestFragment extends BaseFragment implements FriendRequestAdapter.OnOptionListener {

    private static final String TAG = FriendRequestFragment.class.getSimpleName();
    @BindView(R.id.fragment_friend_requests_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_friend_requests_noResults)
    TextView textView;
    private LiveFriendsServices liveFriendsServices;
    private DatabaseReference getAllUsersFriendRequestsReference;
    private ValueEventListener getAllUsersFriendRequestsListener;
    private Unbinder unbinder;
    private String userEmailString;
    private Socket socket;

    public static FriendRequestFragment newInstance() {
        return new FriendRequestFragment();
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
        liveFriendsServices = LiveFriendsServices.getInstance();
        userEmailString = sharedPreferences.getString(Constants.USER_EMAIL, "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_request, container, false);
        unbinder = ButterKnife.bind(this, view);

        FriendRequestAdapter adapter = new FriendRequestAdapter(activity, this);

        getAllUsersFriendRequestsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_FRIEND_REQUESTS_RECEIVED).child(Constants.encodeEmail(userEmailString));
        getAllUsersFriendRequestsListener = liveFriendsServices.getAllFriendRequests(adapter, recyclerView, textView, activity);
        getAllUsersFriendRequestsReference.addValueEventListener(getAllUsersFriendRequestsListener);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        if (getAllUsersFriendRequestsListener != null) {
            getAllUsersFriendRequestsReference.removeEventListener(getAllUsersFriendRequestsListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    @Override
    public void onOptionClicked(User user, String result) {
        if (result.equals("0")) {
            DatabaseReference userFriendsReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.FIRE_BASE_PATH_USER_FRIENDS).child(Constants.encodeEmail(userEmailString))
                    .child(Constants.encodeEmail(user.getEmail()));
            userFriendsReference.setValue(user);
            getAllUsersFriendRequestsReference.child(Constants.encodeEmail(user.getEmail())).removeValue();
            compositeDisposable.add(liveFriendsServices.approceDeclineFriendRequest(socket, userEmailString, user.getEmail(), result, activity));
        } else {
            getAllUsersFriendRequestsReference.child(Constants.encodeEmail(user.getEmail())).removeValue();
            compositeDisposable.add(liveFriendsServices.approceDeclineFriendRequest(socket, userEmailString, user.getEmail(), result, activity));
        }
    }
}
