package hu.itware.beastchat.views.UserFriendsViews;

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import hu.itware.beastchat.R;
import hu.itware.beastchat.activities.BaseFragmentActivity;
import hu.itware.beastchat.entities.User;

/**
 * Created by gyongyosit on 2017.01.30..
 */

public class UserFriendAdapter extends Adapter {

    private BaseFragmentActivity activity;
    private List<User> users;
    private LayoutInflater inflater;
    private UserClickedListener listener;

    public UserFriendAdapter(BaseFragmentActivity activity, UserClickedListener listener) {
        this.activity = activity;
        this.listener = listener;
        inflater = activity.getLayoutInflater();
        users = new ArrayList<>();
    }

    public void setUsers(List<User> users) {
        this.users.clear();
        this.users.addAll(users);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_user_friends, parent, false);
        final UserFriendViewHolder userFriendViewHolder = new UserFriendViewHolder(view);
        userFriendViewHolder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = (User) userFriendViewHolder.itemView.getTag();
                listener.onUserClicked(user);
            }
        });
        return userFriendViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((UserFriendViewHolder) holder).populate(activity, users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface UserClickedListener {
        void onUserClicked(User user);
    }
}
