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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hu.itware.beastchat.R;
import hu.itware.beastchat.entities.User;
import hu.itware.beastchat.services.LiveFriendsServices;
import hu.itware.beastchat.utils.Constants;
import hu.itware.beastchat.views.FindFriendsViews.FindFriendsAdapter;
import io.reactivex.subjects.PublishSubject;
import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by gyongyosit on 2017.01.26..
 */

public class FindFriendsFragment extends BaseFragment implements FindFriendsAdapter.UserListener {

    private static final String TAG = FindFriendsFragment.class.getSimpleName();
    public HashMap<String, User> friendRequestsSentmap;
    @BindView(R.id.fragment_find_friends_searchBar)
    EditText searchBarEt;
    @BindView(R.id.fragment_find_friends_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_find_friends_noResults)
    TextView noResultsTv;
    private ValueEventListener getAllUserListener;
    private DatabaseReference getAllUserReference;
    private DatabaseReference getAllFriendRequestsSentReference;
    private DatabaseReference getAllFriendRequestsReceivedReference;
    private String userEmailString;
    private Unbinder unbinder;
    private List<User> allUsers;
    private FindFriendsAdapter adapter;
    private ValueEventListener getAllFriendRequestsSentListener;
    private ValueEventListener getAllFriendRequestsReceivedListener;
    private LiveFriendsServices liveFriendsServices;
    private Socket socket;
    private PublishSubject<String> searchBarSubject = PublishSubject.create();
    private DatabaseReference getAllCurrentUserFriendsReference;
    private ValueEventListener getAllCurrentUserFriendsListener;

    public static FindFriendsFragment newInstance() {
        return new FindFriendsFragment();
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

        userEmailString = sharedPreferences.getString(Constants.USER_EMAIL, "");
        liveFriendsServices = LiveFriendsServices.getInstance();
        friendRequestsSentmap = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_friends, container, false);
        unbinder = ButterKnife.bind(this, view);
        allUsers = new ArrayList<>();
        adapter = new FindFriendsAdapter(activity, this);

        getAllUserListener = liveFriendsServices.getAllUsers(adapter, userEmailString, allUsers, activity);
        getAllUserReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIRE_BASE_PATH_USERS);
        getAllUserReference.addValueEventListener(getAllUserListener);

        getAllFriendRequestsSentReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_FRIEND_REQUESTS_SENT)
                .child(Constants.encodeEmail(userEmailString));
        getAllFriendRequestsSentListener = liveFriendsServices.getFriendRequestSent(adapter, activity, this);
        getAllFriendRequestsSentReference.addValueEventListener(getAllFriendRequestsSentListener);

        getAllFriendRequestsReceivedReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_FRIEND_REQUESTS_RECEIVED)
                .child(Constants.encodeEmail(userEmailString));
        getAllFriendRequestsReceivedListener = liveFriendsServices.getFriendRequestReceived(adapter, activity);
        getAllFriendRequestsReceivedReference.addValueEventListener(getAllFriendRequestsReceivedListener);

        getAllCurrentUserFriendsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_FRIENDS)
                .child(Constants.encodeEmail(userEmailString));
        getAllCurrentUserFriendsListener = liveFriendsServices.getAllCurrentUsersFriendsMap(adapter, activity);
        getAllCurrentUserFriendsReference.addValueEventListener(getAllCurrentUserFriendsListener);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);

        compositeDisposable.add(liveFriendsServices.createSearchBarDisposable(searchBarSubject, allUsers, recyclerView, noResultsTv, adapter));
        listenToSearchBar();

        return view;
    }

    private void listenToSearchBar() {
        searchBarEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchBarSubject.onNext(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void setFriendRequestsSentMap(HashMap<String, User> friendRequestsSentmap) {
        this.friendRequestsSentmap.clear();
        this.friendRequestsSentmap.putAll(friendRequestsSentmap);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (getAllUserListener != null) {
            getAllUserReference.removeEventListener(getAllUserListener);
        }

        if (getAllFriendRequestsSentListener != null) {
            getAllFriendRequestsSentReference.removeEventListener(getAllFriendRequestsSentListener);
        }

        if (getAllFriendRequestsReceivedListener != null) {
            getAllFriendRequestsReceivedReference.removeEventListener(getAllFriendRequestsReceivedListener);
        }

        if (getAllCurrentUserFriendsListener != null) {
            getAllCurrentUserFriendsReference.removeEventListener(getAllCurrentUserFriendsListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    @Override
    public void onUserClicked(User user) {
        if (Constants.isIncludedInMap(friendRequestsSentmap, user)) {
            getAllFriendRequestsSentReference.child(Constants.encodeEmail(user.getEmail())).removeValue();
            compositeDisposable.add(liveFriendsServices.addOrRemoveFriendRequest(socket, userEmailString, user.getEmail(), "1", activity));
        } else {
            getAllFriendRequestsSentReference.child(Constants.encodeEmail(user.getEmail())).setValue(user);
            compositeDisposable.add(liveFriendsServices.addOrRemoveFriendRequest(socket, userEmailString, user.getEmail(), "0", activity));
        }
    }
}
