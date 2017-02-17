package hu.itware.beastchat.views.FriendRequestViews;

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

public class FriendRequestViewHolder extends ViewHolder {

    @BindView(R.id.list_friend_request_userPicture)
    RoundedImageView userPicture;
    @BindView(R.id.list_friend_request_userName)
    TextView userName;
    @BindView(R.id.list_friend_request_acceptRequest)
    ImageView approveIv;
    @BindView(R.id.list_friend_request_rejectRequest)
    ImageView rejectIv;

    public FriendRequestViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void populate(Context context, User user) {
        itemView.setTag(user);
        userName.setText(user.getUserName());
        Picasso.with(context)
                .load(user.getUserPicture())
                .into(userPicture);
    }
}
