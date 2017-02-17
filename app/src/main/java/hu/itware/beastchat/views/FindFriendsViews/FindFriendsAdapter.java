package hu.itware.beastchat.views.FindFriendsViews;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hu.itware.beastchat.R;
import hu.itware.beastchat.activities.BaseFragmentActivity;
import hu.itware.beastchat.entities.User;

/**
 * Created by gyongyosit on 2017.01.26..
 */

public class FindFriendsAdapter extends RecyclerView.Adapter {

    private BaseFragmentActivity activity;
    private List<User> users;
    private LayoutInflater inflater;
    private UserListener listener;
    private HashMap<String, User> friendRequestSentMap;
    private HashMap<String, User> friendRequestReceivedMap;
    private HashMap<String, User> currentUserFriendsMap;

    public FindFriendsAdapter(BaseFragmentActivity activity, UserListener listener) {
        this.activity = activity;
        this.listener = listener;
        inflater = activity.getLayoutInflater();
        users = new ArrayList<>();
        friendRequestSentMap = new HashMap<>();
        friendRequestReceivedMap = new HashMap<>();
        currentUserFriendsMap = new HashMap<>();
    }

    public void setUsers(List<User> users) {
        this.users.clear();
        this.users.addAll(users);
        notifyDataSetChanged();
    }

    public void setFriendRequestsSentMap(HashMap<String, User> friendRequestSentMap) {
        this.friendRequestSentMap.clear();
        this.friendRequestSentMap.putAll(friendRequestSentMap);
        notifyDataSetChanged();
    }

    public void setFriendRequestReceivedMap(HashMap<String, User> friendRequestReceivedMap) {
        this.friendRequestReceivedMap.clear();
        this.friendRequestReceivedMap.putAll(friendRequestReceivedMap);
        notifyDataSetChanged();
    }

    public void setCurrentUserFriendsMap(HashMap<String, User> currentUserFriendsMap) {
        this.currentUserFriendsMap.clear();
        this.currentUserFriendsMap.putAll(currentUserFriendsMap);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View userView = inflater.inflate(R.layout.list_user, parent, false);
        final FindFriendsViewHolder findFriendsViewHolder = new FindFriendsViewHolder(userView);
        findFriendsViewHolder.addFriend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = (User) findFriendsViewHolder.itemView.getTag();
                listener.onUserClicked(user);
            }
        });
        return findFriendsViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((FindFriendsViewHolder) holder).populate(activity, users.get(position), friendRequestSentMap, friendRequestReceivedMap, currentUserFriendsMap);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface UserListener {
        void onUserClicked(User user);
    }
}
