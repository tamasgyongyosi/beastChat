package hu.itware.beastchat.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import hu.itware.beastchat.entities.User;

/**
 * Created by gyongyosit on 2017.01.23..
 */

public class Constants {

    public static final String IP_LOCAL_HOST = "http://192.168.64.127:8080";
    public static final String USER_INFO_REFERENCE = "USER_INFO_REFERENCE";
    public static final String USER_EMAIL = "USER_EMAIL";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_PICTURE = "USER_PICTURE";
    public static final String FIRE_BASE_PATH_USERS = "users";
    public static final String FIRE_BASE_PATH_FRIEND_REQUESTS_SENT = "friendRequestsSent";
    public static final String FIRE_BASE_PATH_FRIEND_REQUESTS_RECEIVED = "friendRequestsReceived";
    public static final String FIRE_BASE_PATH_USER_FRIENDS = "userFriends";
    public static final String FIRE_BASE_PATH_USER_TOKEN = "userToken";
    public static final String FIRE_BASE_PATH_USER_MESSAGES = "userMessages";
    public static final String FIRE_BASE_PATH_USER_NEW_MESSAGES = "userNewMessages";
    public static final String FIRE_BASE_PATH_USER_CHAT_ROOMS = "userChatRooms";
    public static final String FIRE_BASE_PATH_USER_PROFILE_PICTURES = "userProfilesPics";

    public static String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    public static boolean isIncludedInMap(HashMap<String, User> userHashMap, User user) {
        return userHashMap != null && userHashMap.size() != 0 && userHashMap.containsKey(user.getEmail());
    }

    public static File getOutputFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BeastChat");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getParent() + File.separator + "IMG_" + timeStamp + "-jpg");
    }
}
