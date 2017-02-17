package hu.itware.beastchat.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.roughike.bottombar.BottomBar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import hu.itware.beastchat.R;
import hu.itware.beastchat.services.LiveAccountServices;
import hu.itware.beastchat.services.LiveFriendsServices;
import hu.itware.beastchat.utils.Constants;
import hu.itware.beastchat.utils.MashMellowPermissions;
import io.socket.client.IO;
import io.socket.client.Socket;

import static android.app.Activity.RESULT_OK;

/**
 * Created by gyongyosit on 2017.01.25..
 */

public class ProfileFragment extends BaseFragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private static final int REQUEST_CODE_CAMERA = 100;
    private static final int REQUEST_CODE_PICTURE = 101;
    @BindView(R.id.bottomBar)
    BottomBar bottomBar;
    @BindView(R.id.fragment_profile_cameraPicture)
    ImageButton cameraIv;
    @BindView(R.id.fragment_profile_imagePicture)
    ImageButton imageIv;
    @BindView(R.id.fragment_profile_userPicture)
    RoundedImageView profileIv;
    @BindView(R.id.fragment_profile_userEmail)
    TextView userEmailTv;
    @BindView(R.id.fragment_profile_userName)
    TextView userNameTv;
    @BindView(R.id.fragment_profile_progressBar)
    ProgressBar progressBar;
    @BindView(R.id.fragment_profile_signOut)
    Button signOutBtn;

    private Unbinder unbinder;
    private LiveFriendsServices liveFriendsServices;
    private DatabaseReference allFriendRequestsReference;
    private ValueEventListener allFriendRequestsListener;
    private String userEmailString;
    private DatabaseReference usersNewMessagesReference;
    private ValueEventListener usersNewMessagesListener;
    private Uri tempUri;
    private MashMellowPermissions mashMellowPermissions;
    private LiveAccountServices liveAccountServices;
    private Socket socket;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            socket = IO.socket(Constants.IP_LOCAL_HOST);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            Toast.makeText(getActivity(), "Can't connect to the server", Toast.LENGTH_SHORT).show();
        }
        socket.connect();
        liveFriendsServices = LiveFriendsServices.getInstance();
        liveAccountServices = LiveAccountServices.getInstance();
        userEmailString = sharedPreferences.getString(Constants.USER_EMAIL, "");
        mashMellowPermissions = new MashMellowPermissions(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, view);

        bottomBar.selectTabWithId(R.id.tab_profile);
        setUpBottomBar(bottomBar, 3);

        allFriendRequestsListener = liveFriendsServices.getFriendRequestBottom(bottomBar, R.id.tab_friends);
        allFriendRequestsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_FRIEND_REQUESTS_RECEIVED)
                .child(Constants.encodeEmail(userEmailString));
        allFriendRequestsReference.addValueEventListener(allFriendRequestsListener);

        usersNewMessagesReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_NEW_MESSAGES).child(Constants.encodeEmail(userEmailString));
        usersNewMessagesListener = liveFriendsServices.getAllNewMessages(bottomBar, R.id.tab_inBoxMessages);
        usersNewMessagesReference.addValueEventListener(usersNewMessagesListener);

        Picasso.with(activity)
                .load(sharedPreferences.getString(Constants.USER_PICTURE, ""))
                .into(profileIv);

        userEmailTv.setText(sharedPreferences.getString(Constants.USER_EMAIL, ""));
        userNameTv.setText(sharedPreferences.getString(Constants.USER_NAME, ""));

        return view;
    }

    @OnClick(R.id.fragment_profile_signOut)
    public void setSignOutBtn() {
        sharedPreferences.edit().remove(Constants.USER_PICTURE).apply();
        sharedPreferences.edit().remove(Constants.USER_EMAIL).apply();
        sharedPreferences.edit().remove(Constants.USER_NAME).apply();

        FirebaseAuth.getInstance().signOut();
        activity.finish();
    }

    @OnClick(R.id.fragment_profile_imagePicture)
    public void setImageIv() {
        if (!mashMellowPermissions.checkPermissionForWriteExternalStorage()) {
            mashMellowPermissions.requestPermissionForWriteExternalStorage();
        } else if (!mashMellowPermissions.checkPermissionForReadExternalStorage()) {
            mashMellowPermissions.requestPermissionForReadExternalStorage();
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Choose image with"), REQUEST_CODE_PICTURE);
        }
    }

    @OnClick(R.id.fragment_profile_cameraPicture)
    public void setCameraIv() {
        if (!mashMellowPermissions.checkPermissionForCamera()) {
            mashMellowPermissions.requestPermissionForCamera();
        } else if (!mashMellowPermissions.checkPermissionForWriteExternalStorage()) {
            mashMellowPermissions.requestPermissionForWriteExternalStorage();
        } else if (!mashMellowPermissions.checkPermissionForReadExternalStorage()) {
            mashMellowPermissions.requestPermissionForReadExternalStorage();
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File tempFile = Constants.getOutputFile();
            if (tempFile == null) {
                return;
            }
            tempUri = Uri.fromFile(Constants.getOutputFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PICTURE) {
            Uri selectedImageUri = data.getData();
            uploadImageUri(selectedImageUri);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAMERA) {
            Uri selectedImageUri = tempUri;
            uploadImageUri(selectedImageUri);
        }
    }

    private void uploadImageUri(Uri selectedImageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_PROFILE_PICTURES)
                .child(Constants.encodeEmail(userEmailString))
                .child(selectedImageUri.getLastPathSegment());
        compositeDisposable.add(liveAccountServices.changeProfilePhoto(storageReference, selectedImageUri, activity, userEmailString, profileIv, sharedPreferences, progressBar, socket));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        if (allFriendRequestsListener != null) {
            allFriendRequestsReference.removeEventListener(allFriendRequestsListener);
        }

        if (usersNewMessagesListener != null) {
            usersNewMessagesReference.removeEventListener(usersNewMessagesListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}
