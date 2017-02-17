package hu.itware.beastchat.views.ChatRoomViews;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.itware.beastchat.R;
import hu.itware.beastchat.entities.ChatRoom;

/**
 * Created by gyongyosit on 2017.02.01..
 */

public class ChatRoomViewHolder extends ViewHolder {

    @BindView(R.id.list_chat_room_lastMessage)
    TextView lastMessageTv;
    @BindView(R.id.list_chat_room_newMessageIndicator)
    ImageView lastMessageIndicatorIv;
    @BindView(R.id.list_chat_room_userName)
    TextView userNameTv;
    @BindView(R.id.list_chat_room_userPicture)
    RoundedImageView userPictureIv;

    public ChatRoomViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void populate(Context context, ChatRoom chatRoom, String currentUserEmail) {
        itemView.setTag(chatRoom);

        Picasso.with(context)
                .load(chatRoom.getFriendPicture())
                .into(userPictureIv);

        userNameTv.setText(chatRoom.getFriendName());

        String lastMessageSent = chatRoom.getLastMessage();

        if (lastMessageSent.length() > 40) {
            lastMessageSent = lastMessageSent.substring(0, 40) + "...";
        }
        if (!chatRoom.isSentLastMessage()) {
            lastMessageSent = lastMessageSent + " (Draft)";
        }

        if (chatRoom.getLastMessageSenderEmail().equals(currentUserEmail)) {
            lastMessageSent = "Me: " + lastMessageSent;
        }

        if (!chatRoom.isLastMessageRead()) {
            lastMessageIndicatorIv.setVisibility(View.VISIBLE);
        } else {
            lastMessageIndicatorIv.setVisibility(View.INVISIBLE);
        }

        lastMessageTv.setText(lastMessageSent);
    }

}
