package hu.itware.beastchat.views.MessagesViews;

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import hu.itware.beastchat.R;
import hu.itware.beastchat.activities.BaseFragmentActivity;
import hu.itware.beastchat.entities.Message;

/**
 * Created by gyongyosit on 2017.01.31..
 */

public class MessagesAdapter extends Adapter {

    private BaseFragmentActivity activity;
    private List<Message> messages;
    private LayoutInflater inflater;
    private String currentUserEmail;

    public MessagesAdapter(BaseFragmentActivity activity, String currentUserEmail) {
        this.activity = activity;
        this.currentUserEmail = currentUserEmail;
        messages = new ArrayList<>();
        inflater = activity.getLayoutInflater();
    }

    public void setMessages(List<Message> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_messages, parent, false);
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((MessagesViewHolder) holder).populate(activity, messages.get(position), currentUserEmail);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
