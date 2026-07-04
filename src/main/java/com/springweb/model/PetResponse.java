package com.springweb.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

@Getter
@JsonPropertyOrder(value = {"id", "petName","petType","_links"})
public class PetResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String id;
    private String petName;
    private PetType petType;

    @JsonCreator
    public PetResponse(String id, String petName, PetType petType) {
        this.id = id;
        this.petName = petName;
        this.petType = petType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("petName", petName)
                .append("petType", petType)
                .toString();
    }
}
