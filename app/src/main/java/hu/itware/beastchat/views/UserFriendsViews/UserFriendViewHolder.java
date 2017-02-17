package hu.itware.beastchat.views.UserFriendsViews;

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
import hu.itware.beastchat.entities.User;

/**
 * Created by gyongyosit on 2017.01.30..
 */

public class UserFriendViewHolder extends ViewHolder {
    @BindView(R.id.list_user_friends_userName)
    TextView userName;
    @BindView(R.id.list_user_friends_friendPicture)
    RoundedImageView friendPicture;
    @BindView(R.id.list_user_friends_startChat)
    ImageView startChat;

    public UserFriendViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void populate(Context context, User user) {
        itemView.setTag(user);
        userName.setText(user.getUserName());
        Picasso.with(context)
                .load(user.getUserPicture())
                .into(friendPicture);
    }

}
