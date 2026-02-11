package com.amethyst.client;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private List<String> friends;

    public FriendManager() {
        friends = new ArrayList<>();
        loadFriends();
    }

    public void addFriend(String name) {
        if (!friends.contains(name.toLowerCase())) {
            friends.add(name.toLowerCase());
            saveFriends();
        }
    }

    public void removeFriend(String name) {
        friends.remove(name.toLowerCase());
        saveFriends();
    }

    public boolean isFriend(String name) {
        return friends.contains(name.toLowerCase());
    }

    public List<String> getFriends() {
        return friends;
    }

    private void saveFriends() {
        StringBuilder sb = new StringBuilder();
        for (String friend : friends) {
            sb.append(friend).append(",");
        }
        AmethystClient.config.set("friends", sb.toString());
        AmethystClient.config.save();
    }

    private void loadFriends() {
        String friendsStr = AmethystClient.config.getString("friends", "");
        if (!friendsStr.isEmpty()) {
            String[] friendArray = friendsStr.split(",");
            for (String friend : friendArray) {
                if (!friend.isEmpty()) {
                    friends.add(friend);
                }
            }
        }
    }
}
