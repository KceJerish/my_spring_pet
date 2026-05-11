package com.springweb.model;

import com.springweb.repository.PetDocument;

public record PetsRequest(String petName, PetType petType) {

    public PetDocument toDocument(){
        return new PetDocument(petName, petType);
    }
}
