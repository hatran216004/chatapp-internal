package com.example.librarymanagement.service.inter;

import java.util.Set;

public interface UserPresenceService {
    void userConnected(Integer userId, String sessionId);

    void userDisconnected(Integer userId, String sessionId);

    boolean isUserOnline(Integer userId);

    Set<String> getOnlineUsers();

    void updateLastSeen(Integer userId);

    void broadcastPresence(Integer userId, boolean online);
}
