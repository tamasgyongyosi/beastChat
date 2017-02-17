package hu.itware.beastchat.views.FindFriendsViews;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.itware.beastchat.R;
import hu.itware.beastchat.entities.User;
import hu.itware.beastchat.utils.Constants;

/**
 * Created by gyongyosit on 2017.01.26..
 */

public class FindFriendsViewHolder extends ViewHolder {

    @BindView(R.id.list_users_addFriend)
    public ImageView addFriend;
    @BindView(R.id.list_users_userPicture)
    RoundedImageView userPicture;
    @BindView(R.id.list_users_userName)
    TextView userName;
    @BindView(R.id.list_users_userStatus)
    TextView userStatus;

    public FindFriendsViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void populate(Context context, User user,
                         HashMap<String, User> friendRequestSentMap,
                         HashMap<String, User> friendRequestReceivedMap,
                         HashMap<String, User> currentUserFriendsMap) {
        itemView.setTag(user);
        userName.setText(user.getUserName());
        Picasso.with(context)
                .load(user.getUserPicture())
                .into(userPicture);

        if (Constants.isIncludedInMap(friendRequestSentMap, user)) {
            userStatus.setVisibility(View.VISIBLE);
            userStatus.setText("Friend request sent");
            addFriend.setImageResource(R.mipmap.ic_cancel_request);
            addFriend.setVisibility(View.VISIBLE);
        } else if (Constants.isIncludedInMap(friendRequestReceivedMap, user)) {
            userStatus.setText("This user has requested you");
            userStatus.setVisibility(View.VISIBLE);
            addFriend.setVisibility(View.INVISIBLE);
        } else if (Constants.isIncludedInMap(currentUserFriendsMap, user)) {
            userStatus.setText("User added");
            userStatus.setVisibility(View.VISIBLE);
            addFriend.setVisibility(View.INVISIBLE);
        } else {
            userStatus.setVisibility(View.INVISIBLE);
            addFriend.setImageResource(R.mipmap.ic_add);
            addFriend.setVisibility(View.VISIBLE);
        }
    }

}
