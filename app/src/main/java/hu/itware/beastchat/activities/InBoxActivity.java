package hu.itware.beastchat.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import hu.itware.beastchat.R;
import hu.itware.beastchat.fragments.InBoxFragment;
import hu.itware.beastchat.utils.Constants;

/**
 * Created by gyongyosit on 2017.01.25..
 */

public class InBoxActivity extends BaseFragmentActivity {

    private static final String TAG = InBoxActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String messageToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_INFO_REFERENCE, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(Constants.USER_EMAIL, "");

        if (messageToken != null && !userEmail.equals("")) {
            DatabaseReference tokenReference = FirebaseDatabase.getInstance().getReference().
                    child(Constants.FIRE_BASE_PATH_USER_TOKEN).
                    child(Constants.encodeEmail(userEmail));
            tokenReference.child("token").setValue(messageToken);

            getSupportActionBar().setTitle(sharedPreferences.getString(Constants.USER_NAME, "") + "'s InBox");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_new_message:
                Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    Fragment createFragment() {
        return InBoxFragment.newInstance();
    }
}
