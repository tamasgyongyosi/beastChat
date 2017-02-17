package hu.itware.beastchat.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hu.itware.beastchat.activities.BaseFragmentActivity;
import hu.itware.beastchat.activities.InBoxActivity;
import hu.itware.beastchat.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Socket;

/**
 * Created by gyongyosit on 2017.01.24..
 */

public class LiveAccountServices {

    private static final String TAG = LiveAccountServices.class.getSimpleName();
    private static LiveAccountServices liveAccountServices;
    private final int USER_ERROR_EMPTY_PASSWORD = 1;
    private final int USER_ERROR_EMPTY_EMAIL = 2;
    private final int USER_ERROR_EMPTY_USERNAME = 3;
    private final int USER_ERROR_PASSWORD_SHORT = 4;
    private final int USER_ERROR_EMAIL_BAD_FORMAT = 5;
    private final int SERVER_SUCCESS = 6;
    private final int SERVER_FAILURE = 7;
    private final int USER_NO_ERRORS = 8;

    public static LiveAccountServices getInstance() {
        if (liveAccountServices == null) {
            liveAccountServices = new LiveAccountServices();
        }
        return liveAccountServices;
    }

    public Disposable changeProfilePhoto(final StorageReference storageReference, Uri pictureUri, final BaseFragmentActivity activity, final String userEmailString,
                                         final ImageView imageView, final SharedPreferences sharedPreferences, final ProgressBar progressBar, final Socket socket) {
        Observable<Uri> uriObservable = Observable.just(pictureUri);
        imageView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        return uriObservable
                .subscribeOn(Schedulers.io())
                .map(new Function<Uri, byte[]>() {
                    @Override
                    public byte[] apply(Uri uri) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                            int outPutHeight = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 512, outPutHeight, true);
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            scaledBitmap.compress(CompressFormat.JPEG, 100, byteArrayOutputStream);
                            return byteArrayOutputStream.toByteArray();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) {
                        if (bytes == null) {
                            imageView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(activity, "Error during optimize picture", Toast.LENGTH_SHORT).show();
                        } else {
                            UploadTask uploadTask = storageReference.putBytes(bytes);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    imageView.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(activity, "Error during upload picture", Toast.LENGTH_SHORT).show();
                                }
                            });
                            uploadTask.addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                                @Override
                                public void onSuccess(TaskSnapshot taskSnapshot) {
                                    imageView.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    JSONObject sendData = new JSONObject();
                                    try {
                                        sendData.put("email", userEmailString);
                                        sendData.put("picUrl", taskSnapshot.getDownloadUrl().toString());
                                        socket.emit("userUpdatedPicture", sendData);
                                        sharedPreferences.edit().putString(Constants.USER_PICTURE, taskSnapshot.getDownloadUrl().toString()).apply();
                                        Picasso.with(activity).load(taskSnapshot.getDownloadUrl().toString()).into(imageView);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(activity, "Error during update picture", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
    }

    public Disposable getAuthToken(JSONObject data, final BaseFragmentActivity activity, final SharedPreferences sharedPreferences,
                                   final ProgressBar progressBar, final LinearLayout linearLayout) {
        Observable<JSONObject> jsonObjectObservable = Observable.just(data);
        return jsonObjectObservable
                .subscribeOn(Schedulers.io())
                .map(new Function<JSONObject, List<String>>() {
                    @Override
                    public List<String> apply(JSONObject jsonObject) {
                        List<String> userDetails = new ArrayList<>();
                        try {
                            JSONObject serverData = jsonObject.getJSONObject("token");
                            String token = (String) serverData.get("authToken");
                            String email = (String) serverData.get("email");
                            String photo = (String) serverData.get("photo");
                            String userName = (String) serverData.get("displayName");

                            userDetails.add(token);
                            userDetails.add(email);
                            userDetails.add(photo);
                            userDetails.add(userName);
                            return userDetails;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return userDetails;
                        }

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Exception {
                        String token = strings.get(0);
                        final String email = strings.get(1);
                        final String photo = strings.get(2);
                        final String userName = strings.get(3);

                        if (!email.equals("error")) {
                            FirebaseAuth.getInstance().signInWithCustomToken(token)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.INVISIBLE);
                                                linearLayout.setVisibility(View.VISIBLE);
                                            } else {
                                                sharedPreferences.edit().putString(Constants.USER_EMAIL, email).apply();
                                                sharedPreferences.edit().putString(Constants.USER_NAME, userName).apply();
                                                sharedPreferences.edit().putString(Constants.USER_PICTURE, photo).apply();

                                                Intent intent = new Intent(activity, InBoxActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                activity.startActivity(intent);
                                                activity.finish();
                                            }
                                        }
                                    });
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            linearLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public Disposable sendLoginInfo(final EditText userEmailEt, final EditText userPasswordEt, final Socket socket, final BaseFragmentActivity activity,
                                    final ProgressBar progressBar, final LinearLayout linearLayout) {
        List<String> userDetails = new ArrayList<>();
        userDetails.add(userEmailEt.getText().toString());
        userDetails.add(userPasswordEt.getText().toString());

        Observable<List<String>> userDetailsObservable = Observable.just(userDetails);
        return userDetailsObservable
                .subscribeOn(Schedulers.io())
                .map(new Function<List<String>, Integer>() {
                    @Override
                    public Integer apply(List<String> strings) {
                        final String userEmail = strings.get(0);
                        String userPassword = strings.get(1);
                        if (userEmail.isEmpty()) {
                            return USER_ERROR_EMPTY_EMAIL;
                        } else if (userPassword.isEmpty()) {
                            return USER_ERROR_EMPTY_PASSWORD;
                        } else if (userPassword.length() < 6) {
                            return USER_ERROR_PASSWORD_SHORT;
                        } else if (!isEmailValid(userEmail)) {
                            return USER_ERROR_EMAIL_BAD_FORMAT;
                        } else {
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(userEmail, userPassword)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.INVISIBLE);
                                                linearLayout.setVisibility(View.VISIBLE);
                                            } else {
                                                JSONObject sendData = new JSONObject();
                                                try {
                                                    sendData.put("email", userEmail);
                                                    socket.emit("userInfo", sendData);
                                                } catch (JSONException e) {
                                                    Log.e(TAG, e.getMessage());
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });

                            try {
                                FirebaseInstanceId.getInstance().deleteInstanceId();
                                FirebaseInstanceId.getInstance().getToken();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                                e.printStackTrace();
                            }

                            FirebaseAuth.getInstance().signOut();
                            return USER_NO_ERRORS;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer.equals(USER_ERROR_EMPTY_EMAIL)) {
                            userEmailEt.setError("Email address can't be empty");
                        } else if (integer.equals(USER_ERROR_EMAIL_BAD_FORMAT)) {
                            userEmailEt.setError("Please check your email format");
                        } else if (integer.equals(USER_ERROR_EMPTY_PASSWORD)) {
                            userPasswordEt.setError("Password can't be blank");
                        } else if (integer.equals(USER_ERROR_PASSWORD_SHORT)) {
                            userPasswordEt.setError("Password must be at least 6 characters long");
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            linearLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    public Disposable registerResponse(JSONObject data, final BaseFragmentActivity activity) {
        Observable<JSONObject> jsonObjectObservable = Observable.just(data);
        return jsonObjectObservable
                .subscribeOn(Schedulers.io())
                .map(new Function<JSONObject, String>() {
                    @Override
                    public String apply(JSONObject jsonObject) {
                        String message;
                        try {
                            JSONObject json = jsonObject.getJSONObject("message");
                            message = (String) json.get("text");
                            return message;
                        } catch (JSONException e) {
                            return e.getMessage();
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String stringResponse) throws Exception {
                        if (stringResponse.equals("Success")) {
                            activity.finish();
                            Toast.makeText(activity, "Registration successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, stringResponse, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public Disposable sendRegistrationInfo(final EditText userNameEt, final EditText userEmailEt, final EditText userPasswordEt, final Socket socket) {
        List<String> userDetails = new ArrayList<>();
        userDetails.add(userNameEt.getText().toString());
        userDetails.add(userEmailEt.getText().toString());
        userDetails.add(userPasswordEt.getText().toString());

        Observable<List<String>> userDetailsObservable = Observable.just(userDetails);
        return userDetailsObservable
                .subscribeOn(Schedulers.io())
                .map(new Function<List<String>, Integer>() {
                    @Override
                    public Integer apply(List<String> strings) {
                        String userName = strings.get(0);
                        String userEmail = strings.get(1);
                        String userPassword = strings.get(2);
                        if (userName.isEmpty()) {
                            return USER_ERROR_EMPTY_USERNAME;
                        } else if (userEmail.isEmpty()) {
                            return USER_ERROR_EMPTY_EMAIL;
                        } else if (userPassword.isEmpty()) {
                            return USER_ERROR_EMPTY_PASSWORD;
                        } else if (userPassword.length() < 6) {
                            return USER_ERROR_PASSWORD_SHORT;
                        } else if (!isEmailValid(userEmail)) {
                            return USER_ERROR_EMAIL_BAD_FORMAT;
                        } else {
                            JSONObject sendData = new JSONObject();
                            try {
                                sendData.put("email", userEmail);
                                sendData.put("userName", userName);
                                sendData.put("password", userPassword);
                                socket.emit("userData", sendData);
                                return SERVER_SUCCESS;
                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                                e.printStackTrace();
                                return SERVER_FAILURE;
                            }
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer.equals(USER_ERROR_EMPTY_EMAIL)) {
                            userEmailEt.setError("Email address can't be empty");
                        } else if (integer.equals(USER_ERROR_EMAIL_BAD_FORMAT)) {
                            userEmailEt.setError("Please check your email format");
                        } else if (integer.equals(USER_ERROR_EMPTY_PASSWORD)) {
                            userPasswordEt.setError("Password can't be blank");
                        } else if (integer.equals(USER_ERROR_PASSWORD_SHORT)) {
                            userPasswordEt.setError("Password must be at least 6 characters long");
                        } else if (integer.equals(USER_ERROR_EMPTY_USERNAME)) {
                            userNameEt.setError("Name can't be empty");
                        }
                    }
                });
    }

    private boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
