package com.ani.taku_backend.shorts_interaction.service;

import com.ani.taku_backend.user.model.entity.User;

public interface InteractionService {

    void addLike(User user, String shortsId);
}