package hu.itware.beastchat.views.MessagesViews;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.itware.beastchat.R;
import hu.itware.beastchat.entities.Message;

/**
 * Created by gyongyosit on 2017.01.31..
 */

public class MessagesViewHolder extends ViewHolder {

    @BindView(R.id.list_messages_friendPicture)
    RoundedImageView friendPicture;
    @BindView(R.id.list_messages_userPicture)
    RoundedImageView userPicture;
    @BindView(R.id.list_messages_friendText)
    TextView friendText;
    @BindView(R.id.list_messages_userText)
    TextView userText;

    public MessagesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void populate(Context context, Message message, String currentUserEmail) {
        if (!currentUserEmail.equals(message.getMessageSenderEmail())) {
            userPicture.setVisibility(View.INVISIBLE);
            userText.setVisibility(View.INVISIBLE);
            friendPicture.setVisibility(View.VISIBLE);
            friendText.setVisibility(View.VISIBLE);

            Picasso.with(context)
                    .load(message.getMessageSenderPicture())
                    .into(friendPicture);

            friendText.setText(message.getMessageText());
        } else {
            userPicture.setVisibility(View.VISIBLE);
            userText.setVisibility(View.VISIBLE);
            friendPicture.setVisibility(View.INVISIBLE);
            friendText.setVisibility(View.INVISIBLE);

            Picasso.with(context)
                    .load(message.getMessageSenderPicture())
                    .into(userPicture);

            userText.setText(message.getMessageText());
        }
    }
}
