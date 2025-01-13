package com.ani.taku_backend.shorts_interaction.repository;

import com.ani.taku_backend.shorts.domain.entity.Interaction;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InteractionRepository extends MongoRepository<Interaction, ObjectId>, CustomInteractionRepository {

}
