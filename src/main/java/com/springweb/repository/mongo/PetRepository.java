package com.springweb.repository.mongo;

import com.springweb.model.PetType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends MongoRepository<PetDocument, String> {

    public void deleteByType(PetType petType);

    List<PetDocument> findByType(PetType petType);
}
