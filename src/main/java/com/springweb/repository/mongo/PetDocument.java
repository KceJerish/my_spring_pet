package com.springweb.repository.mongo;

import com.springweb.model.PetResponse;
import com.springweb.model.PetType;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(value = "pets")
public class PetDocument {

    @Id
    private String id;

    private final String name;

    private final PetType type;

    @PersistenceCreator
    public PetDocument(String id, String name, PetType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public PetDocument(String name, PetType type) {
        this.name = name;
        this.type = type;
    }

    public PetResponse toPets() {
        return new PetResponse(this.id, this.name, this.type);
    }
}


