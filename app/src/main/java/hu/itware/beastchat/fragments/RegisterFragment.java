package hu.itware.beastchat.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import hu.itware.beastchat.R;
import hu.itware.beastchat.services.LiveAccountServices;
import hu.itware.beastchat.utils.Constants;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.emitter.Emitter.Listener;

/**
 * Created by gyongyosit on 2017.01.24..
 */

public class RegisterFragment extends BaseFragment {

    private static final String TAG = RegisterFragment.class.getSimpleName();
    @BindView(R.id.fragment_register_userName)
    EditText userNameEt;
    @BindView(R.id.fragment_register_userEmail)
    EditText userEmailEt;
    @BindView(R.id.fragment_register_userPassword)
    EditText userPasswordEt;
    @BindView(R.id.fragment_register_loginButton)
    Button loginBtn;
    @BindView(R.id.fragment_register_registerButton)
    Button registerBtn;
    private LiveAccountServices liveAccountServices;
    private Unbinder unbinder;
    private Socket mSocket;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        liveAccountServices = LiveAccountServices.getInstance();
        try {
            mSocket = IO.socket(Constants.IP_LOCAL_HOST);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            Toast.makeText(activity, "Can't connect to the server", Toast.LENGTH_SHORT).show();
        }

        mSocket.connect();
        mSocket.on("message", accountResponse());
    }

    @OnClick(R.id.fragment_register_registerButton)
    public void setRegisterBtn() {
        compositeDisposable.add(liveAccountServices.sendRegistrationInfo(userNameEt, userEmailEt, userPasswordEt, mSocket));
    }

    @OnClick(R.id.fragment_register_loginButton)
    public void setLoginBtn() {
        activity.finish();
    }

    private Emitter.Listener accountResponse() {
        return new Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                compositeDisposable.add(liveAccountServices.registerResponse(data, activity));
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }
}
