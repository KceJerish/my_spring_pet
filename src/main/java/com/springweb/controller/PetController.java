package com.springweb.controller;

import com.springweb.service.PetService;
import com.springweb.exception.PetNotFoundException;
import com.springweb.model.PetResponse;
import com.springweb.model.PetType;
import com.springweb.model.PetsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping("pets/{id}")
    public PetResponse findPetById(@PathVariable("id") String petId) {
        System.out.println(Thread.currentThread().getName());
        return petService.findPetById(petId);
    }

    @GetMapping("pets/type/{type}")
    public List<PetResponse> findPetById(@PathVariable("type") PetType petType) {
        System.out.println(Thread.currentThread().getName());
        return petService.findPetByType(petType);
    }

    @GetMapping("pets/retry")
    public List<PetResponse> checkRetry() throws PetNotFoundException {

        return petService.checkRetry();
    }

    @PostMapping("pets")
    public  EntityModel<PetResponse> save(@RequestBody PetsRequest pets) {
        var response = petService.save(pets);
        System.out.println("saved response "+ response);
        EntityModel<PetResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(PetController.class).findPetById(response.getId()))
                .withSelfRel());
        model.add(linkTo(methodOn(PetController.class).findPetById(response.getPetType())).withSelfRel());
        return model;
    }

    @DeleteMapping("pets/{petType}")
    public ResponseEntity<Void> save(@PathVariable PetType petType) {
        petService.delete(petType);
        return ResponseEntity.noContent().build();
    }

}
