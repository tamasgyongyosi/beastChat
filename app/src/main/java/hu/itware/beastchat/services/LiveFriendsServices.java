package hu.itware.beastchat.services;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.roughike.bottombar.BottomBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hu.itware.beastchat.activities.BaseFragmentActivity;
import hu.itware.beastchat.entities.ChatRoom;
import hu.itware.beastchat.entities.Message;
import hu.itware.beastchat.entities.User;
import hu.itware.beastchat.fragments.FindFriendsFragment;
import hu.itware.beastchat.utils.Constants;
import hu.itware.beastchat.views.ChatRoomViews.ChatRoomAdapter;
import hu.itware.beastchat.views.FindFriendsViews.FindFriendsAdapter;
import hu.itware.beastchat.views.FriendRequestViews.FriendRequestAdapter;
import hu.itware.beastchat.views.MessagesViews.MessagesAdapter;
import hu.itware.beastchat.views.UserFriendsViews.UserFriendAdapter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.socket.client.Socket;

/**
 * Created by gyongyosit on 2017.01.26..
 */

public class LiveFriendsServices {
    private static LiveFriendsServices liveFriendsServices;
    private final int SERVER_SUCCESS = 6;
    private final int SERVER_FAILURE = 7;

    public static LiveFriendsServices getInstance() {
        if (liveFriendsServices == null) {
            liveFriendsServices = new LiveFriendsServices();
        }
        return liveFriendsServices;
    }

    public ValueEventListener getAllFriends(final UserFriendAdapter adapter, final RecyclerView recyclerView, final TextView textView, final BaseFragmentActivity activity) {
        final List<User> users = new ArrayList<>();
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    users.add(user);
                }

                if (users.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);
                }
                adapter.setUsers(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(activity, "Can't get friends", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public Disposable approceDeclineFriendRequest(final Socket socket, String userEmail, String friendEmail, String requestCode, final BaseFragmentActivity activity) {
        List<String> details = new ArrayList<>();
        details.add(userEmail);
        details.add(friendEmail);
        details.add(requestCode);

        Observable<List<String>> listObservable = Observable.just(details);

        return listObservable
                .subscribeOn(Schedulers.io())
                .map(new Function<List<String>, Integer>() {
                    @Override
                    public Integer apply(List<String> strings) {
                        JSONObject sendData = new JSONObject();
                        try {
                            sendData.put("userEmail", strings.get(0));
                            sendData.put("friendEmail", strings.get(1));
                            sendData.put("requestCode", strings.get(2));
                            socket.emit("friendRequestResponse", sendData);
                            return SERVER_SUCCESS;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return SERVER_FAILURE;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer == SERVER_FAILURE) {
                            Toast.makeText(activity, "Can't send friend requests response", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public ValueEventListener getAllFriendRequests(final FriendRequestAdapter adapter, final RecyclerView recyclerView, final TextView textView, final BaseFragmentActivity activity) {
        final List<User> users = new ArrayList<>();
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    users.add(user);
                }

                if (users.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);
                }
                adapter.setUsers(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(activity, "Can't get received friend requests", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public ValueEventListener getAllCurrentUsersFriendsMap(final FindFriendsAdapter adapter, final BaseFragmentActivity activity) {
        final HashMap<String, User> userHashMap = new HashMap<>();
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userHashMap.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    userHashMap.put(user.getEmail(), user);
                }

                adapter.setCurrentUserFriendsMap(userHashMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(activity, "Can't get received friend requests", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public Disposable addOrRemoveFriendRequest(final Socket socket, String userEmail, String friendEmail, String requestCode, final BaseFragmentActivity activity) {
        List<String> details = new ArrayList<>();
        details.add(userEmail);
        details.add(friendEmail);
        details.add(requestCode);

        Observable<List<String>> listObservable = Observable.just(details);

        return listObservable
                .subscribeOn(Schedulers.io())
                .map(new Function<List<String>, Integer>() {
                    @Override
                    public Integer apply(List<String> strings) {
                        JSONObject sendData = new JSONObject();
                        try {
                            sendData.put("email", strings.get(1));
                            sendData.put("userEmail", strings.get(0));
                            sendData.put("requestCode", strings.get(2));
                            socket.emit("friendRequest", sendData);
                            return SERVER_SUCCESS;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return SERVER_FAILURE;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer == SERVER_FAILURE) {
                            Toast.makeText(activity, "Can't send friend requests", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public ValueEventListener getFriendRequestReceived(final FindFriendsAdapter adapter, final BaseFragmentActivity activity) {
        final HashMap<String, User> userHashMap = new HashMap<>();

        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userHashMap.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    userHashMap.put(user.getEmail(), user);
                }

                adapter.setFriendRequestReceivedMap(userHashMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(activity, "Can't get received friend requests", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public ValueEventListener getFriendRequestSent(final FindFriendsAdapter adapter, final BaseFragmentActivity activity, final FindFriendsFragment fragment) {
        final HashMap<String, User> userHashMap = new HashMap<>();

        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userHashMap.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    userHashMap.put(user.getEmail(), user);
                }

                adapter.setFriendRequestsSentMap(userHashMap);
                fragment.setFriendRequestsSentMap(userHashMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(activity, "Can't get sent friend requests", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public ValueEventListener getAllUsers(final FindFriendsAdapter adapter, final String userEmailString, final List<User> allUsers, final BaseFragmentActivity activity) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (!user.getEmail().equals(userEmailString) && user.isHasLoggedIn()) {
                        allUsers.add(user);
                    }
                }
                adapter.setUsers(allUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(activity, "Can't load users", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public Disposable createChatRoomDisposable(PublishSubject<String> messageSubject, final String friendEmailString,
                                               final String friendPictureString, final String friendNameString, final String userEmailString, final DatabaseReference userChatRoomReference) {
        return messageSubject
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String message) throws Exception {
                        if (!message.isEmpty()) {
                            ChatRoom chatRoom = new ChatRoom(friendPictureString, friendNameString, friendEmailString, message, userEmailString, true, false);

                            userChatRoomReference.setValue(chatRoom);
                        }
                    }
                });
    }

    public Disposable createSearchBarDisposable(PublishSubject<String> searchBarSubject, final List<User> allUsers, final RecyclerView recyclerView, final TextView noResultsTv, final FindFriendsAdapter adapter) {
        return searchBarSubject
                .debounce(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(new Function<String, List<User>>() {
                    @Override
                    public List<User> apply(String searchString) {
                        return liveFriendsServices.getMatchingUsers(allUsers, searchString);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Exception {
                        if (users.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            noResultsTv.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            noResultsTv.setVisibility(View.GONE);
                        }
                        adapter.setUsers(users);
                    }
                });
    }

    public List<User> getMatchingUsers(List<User> users, String userEmail) {
        if (userEmail.isEmpty()) {
            return users;
        }

        List<User> usersFound = new ArrayList<>();
        for (User user : users) {
            if (user.getEmail().toLowerCase().startsWith(userEmail.toLowerCase())) {
                usersFound.add(user);
            }
        }

        return usersFound;
    }

    public ValueEventListener getAllChatRooms(final RecyclerView recyclerView, final TextView textView, final ChatRoomAdapter adapter) {
        final List<ChatRoom> chatRooms = new ArrayList<>();
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatRooms.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);
                    chatRooms.add(chatRoom);
                }

                if (chatRooms.isEmpty()) {
                    textView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    textView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                adapter.setChatRooms(chatRooms);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public ValueEventListener getAllMessages(final RecyclerView recyclerView, final TextView textView, final RoundedImageView roundedImageView, final MessagesAdapter adapter, final String userEmailString) {
        final List<Message> messages = new ArrayList<>();
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messages.clear();
                DatabaseReference newMessagesReference = FirebaseDatabase.getInstance().getReference()
                        .child(Constants.FIRE_BASE_PATH_USER_NEW_MESSAGES).child(Constants.encodeEmail(userEmailString));
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    newMessagesReference.child(message.getMessageId()).removeValue();
                    messages.add(message);
                }

                adapter.setMessages(messages);

                if (messages.isEmpty()) {
                    roundedImageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    roundedImageView.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public ValueEventListener getFriendRequestBottom(final BottomBar bottomBar, final int tagId) {
        final List<User> users = new ArrayList<>();
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    users.add(user);
                }

                if (!users.isEmpty()) {
                    bottomBar.getTabWithId(tagId).setBadgeCount(users.size());
                } else {
                    bottomBar.getTabWithId(tagId).removeBadge();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public Disposable sendMessage(final Socket socket, String messageSenderEmail, String messageSenderPicture, String messageText, String friendEmail, String messageSenderName) {
        List<String> details = new ArrayList<>();
        details.add(messageSenderEmail);
        details.add(messageSenderPicture);
        details.add(messageText);
        details.add(friendEmail);
        details.add(messageSenderName);

        Observable<List<String>> listObservable = Observable.just(details);

        return listObservable
                .subscribeOn(Schedulers.io())
                .map(new Function<List<String>, Integer>() {
                    @Override
                    public Integer apply(List<String> strings) {
                        JSONObject sendData = new JSONObject();
                        try {
                            sendData.put("senderEmail", strings.get(0));
                            sendData.put("senderPicture", strings.get(1));
                            sendData.put("messageText", strings.get(2));
                            sendData.put("friendEmail", strings.get(3));
                            sendData.put("senderName", strings.get(4));
                            socket.emit("message", sendData);
                            return SERVER_SUCCESS;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return SERVER_FAILURE;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {

                    }
                });
    }

    public ValueEventListener getCurrentChatRoom(final DatabaseReference userChatRoomReference, final String friendEmailString) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                if (chatRoom != null) {
                    userChatRoomReference.child("lastMessageRead").setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public ValueEventListener getAllNewMessages(final BottomBar bottomBar, final int tagId) {
        final List<Message> messages = new ArrayList<>();
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messages.add(message);
                }

                if (!messages.isEmpty()) {
                    bottomBar.getTabWithId(tagId).setBadgeCount(messages.size());
                } else {
                    bottomBar.getTabWithId(tagId).removeBadge();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}
