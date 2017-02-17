package hu.itware.beastchat.views.FriendRequestViews;

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

public class FriendRequestAdapter extends Adapter {

    private BaseFragmentActivity activity;
    private LayoutInflater inflater;
    private List<User> users;
    private OnOptionListener listener;

    public FriendRequestAdapter(BaseFragmentActivity activity, OnOptionListener listener) {
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
        View view = inflater.inflate(R.layout.list_friend_request, parent, false);
        final FriendRequestViewHolder friendRequestViewHolder = new FriendRequestViewHolder(view);
        friendRequestViewHolder.approveIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = (User) friendRequestViewHolder.itemView.getTag();
                listener.onOptionClicked(user, "0");
            }
        });

        friendRequestViewHolder.rejectIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = (User) friendRequestViewHolder.itemView.getTag();
                listener.onOptionClicked(user, "1");
            }
        });

        return friendRequestViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((FriendRequestViewHolder) holder).populate(activity, users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface OnOptionListener {
        void onOptionClicked(User user, String result);
    }
}
