package hu.itware.beastchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import hu.itware.beastchat.R;
import hu.itware.beastchat.activities.BaseFragmentActivity;
import hu.itware.beastchat.activities.RegisterActivity;
import hu.itware.beastchat.services.LiveAccountServices;
import hu.itware.beastchat.utils.Constants;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by gyongyosit on 2017.01.23..
 */

public class LoginFragment extends BaseFragment {

    private static final String TAG = LoginFragment.class.getSimpleName();
    @BindView(R.id.fragment_login_userEmail)
    EditText userEmailEt;
    @BindView(R.id.fragment_login_userPassword)
    EditText userPasswordEt;
    @BindView(R.id.fragment_login_loginButton)
    Button loginBtn;
    @BindView(R.id.fragment_login_registerButton)
    Button registerBtn;
    @BindView(R.id.fragment_login_pb)
    ProgressBar progressBar;
    @BindView(R.id.fragment_login_input)
    LinearLayout linearLayout;
    private Unbinder unbinder;
    private Socket socket;
    private LiveAccountServices liveAccountServices;
    private BaseFragmentActivity activity;

    public static LoginFragment newInstance() {
        return new LoginFragment();
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

        liveAccountServices = LiveAccountServices.getInstance();
        socket.on("token", tokenListener());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.fragment_login_loginButton)
    public void setLoginBtn() {
        compositeDisposable.add(liveAccountServices.sendLoginInfo(userEmailEt, userPasswordEt, socket, activity, progressBar, linearLayout));
    }

    @OnClick(R.id.fragment_login_registerButton)
    public void setRegisterBtn() {
        startActivity(new Intent(getActivity(), RegisterActivity.class));
    }

    private Emitter.Listener tokenListener() {
        return new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = (JSONObject) args[0];
                compositeDisposable.add(liveAccountServices.getAuthToken(jsonObject, activity, sharedPreferences, progressBar, linearLayout));
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (BaseFragmentActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}
