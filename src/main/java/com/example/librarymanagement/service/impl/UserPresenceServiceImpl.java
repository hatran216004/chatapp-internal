package com.example.librarymanagement.service.impl;

import com.example.librarymanagement.service.inter.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPresenceServiceImpl implements UserPresenceService {
    @Override
    public void userConnected(Integer userId, String sessionId) {
        
    }

    @Override
    public void userDisconnected(Integer userId, String sessionId) {

    }

    @Override
    public boolean isUserOnline(Integer userId) {
        return false;
    }

    @Override
    public Set<String> getOnlineUsers() {
        return Set.of();
    }

    @Override
    public void updateLastSeen(Integer userId) {

    }

    @Override
    public void broadcastPresence(Integer userId, boolean online) {

    }
}
