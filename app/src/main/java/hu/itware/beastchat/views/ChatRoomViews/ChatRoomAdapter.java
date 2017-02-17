package hu.itware.beastchat.views.ChatRoomViews;

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
import hu.itware.beastchat.entities.ChatRoom;

/**
 * Created by gyongyosit on 2017.02.01..
 */

public class ChatRoomAdapter extends Adapter {

    private BaseFragmentActivity activity;
    private List<ChatRoom> chatRooms;
    private LayoutInflater inflater;
    private ChatRoomListener listener;
    private String currentUserEmailString;

    public ChatRoomAdapter(BaseFragmentActivity activity, ChatRoomListener listener, String currentUserEmailString) {
        this.activity = activity;
        this.listener = listener;
        this.currentUserEmailString = currentUserEmailString;
        inflater = activity.getLayoutInflater();
        chatRooms = new ArrayList<>();
    }

    public void setChatRooms(List<ChatRoom> chatRooms) {
        this.chatRooms.clear();
        this.chatRooms.addAll(chatRooms);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_chat_room, parent, false);
        final ChatRoomViewHolder chatRoomViewHolder = new ChatRoomViewHolder(view);
        chatRoomViewHolder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatRoom chatRoom = (ChatRoom) chatRoomViewHolder.itemView.getTag();
                listener.onChatRoomClicked(chatRoom);
            }
        });
        return chatRoomViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((ChatRoomViewHolder) holder).populate(activity, chatRooms.get(position), currentUserEmailString);
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public interface ChatRoomListener {
        void onChatRoomClicked(ChatRoom chatRoom);
    }
}
