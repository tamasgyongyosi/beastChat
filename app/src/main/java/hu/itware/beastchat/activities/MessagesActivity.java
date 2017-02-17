package hu.itware.beastchat.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import java.util.ArrayList;

import hu.itware.beastchat.R;
import hu.itware.beastchat.fragments.MessagesFragment;

/**
 * Created by gyongyosit on 2017.01.31..
 */

public class MessagesActivity extends BaseFragmentActivity {

    public static final String EXTRA_FRIEND_DETAILS = "EXTRA_FRIEND_DETAILS";

    public static Intent newInstance(ArrayList<String> friendDetails, Context context) {
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putStringArrayListExtra(EXTRA_FRIEND_DETAILS, friendDetails);
        return intent;
    }

    @Override
    Fragment createFragment() {
        ArrayList<String> friendDetails = getIntent().getStringArrayListExtra(EXTRA_FRIEND_DETAILS);
        getSupportActionBar().setTitle(friendDetails.get(2));
        return MessagesFragment.newInstance(friendDetails);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
