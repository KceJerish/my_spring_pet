package com.springweb.service;

import com.springweb.exception.PetNotFoundException;
import com.springweb.model.PetResponse;
import com.springweb.model.PetType;
import com.springweb.model.PetsRequest;
import com.springweb.repository.PetDocument;
import com.springweb.repository.PetRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final ApplicationContext applicationContext;

    public PetResponse findPetById(String petId) {
        var pet = petRepository.findById(petId);
        return pet.map(PetDocument::toPets).orElse(null);
    }

    public PetResponse save(PetsRequest pets) {
        return petRepository.save(pets.toDocument()).toPets();
    }

    public void delete(PetType petType) {
        petRepository.deleteByType(petType);
    }

    @Cacheable(value = "get.pet.types", key = "#petType")
    public List<PetResponse> findPetByType(PetType petType) {
        var pets = petRepository.findByType(petType);
        return pets.stream().map(PetDocument::toPets).collect(Collectors.toList());
    }

    public List<PetResponse> checkRetry() throws PetNotFoundException {
        PetService self = applicationContext.getBean(PetService.class);
        for (PetType petType : PetType.values()) {
            log.info("Checking petType: {}", petType);
            return self.checkRetry(petType);
        }
        return null;
    }


    // bulkhead is used for like thread pool
    @Bulkhead(name = "findPetByType", fallbackMethod = "bulkPets")
    @Retry(name = "findPetByType", fallbackMethod = "defaultPets")
    public List<PetResponse> checkRetry(PetType petType) throws PetNotFoundException {
        log.info("Checking petType: {}", petType);
        List<PetResponse> val = this.findPetByType(petType);
            if (val.isEmpty()) {
                throw new PetNotFoundException();
            }

        return val;
    }

    public List<PetResponse> bulkPets(PetType petType, Throwable ex) {
        log.warn("Bulkhead full, rejecting call. Exception: {}", ex.getClass().getName());
        return Collections.emptyList();
    }

    public List<PetResponse> defaultPets(PetType petType, PetNotFoundException ex) {
        log.warn("Retry exhausted, returning default empty list. Exception: {}", ex.getMessage());
        return Collections.emptyList();
    }
}
