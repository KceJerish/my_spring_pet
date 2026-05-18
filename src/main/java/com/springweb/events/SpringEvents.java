package com.springweb.events;

import com.springweb.repository.mongo.PetDocument;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

@Configuration
public class SpringEvents {

    @EventListener(ApplicationStartedEvent.class)
    public void startupEvents(ApplicationStartedEvent event) {
        System.out.println("SpringEvents started "+ event.getTimeTaken());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startupEvents(ApplicationReadyEvent event) {
        System.out.println("ApplicationReadyEvent started "+ event.getTimeTaken());
    }

    @EventListener(ApplicationEnvironmentPreparedEvent.class)
    public void events(ApplicationEnvironmentPreparedEvent event) {
        System.out.println("ApplicationReadyEvent started "+ event.getEnvironment().getSystemEnvironment().entrySet());
    }

    @EventListener(AfterSaveEvent.class)
    public void events(AfterSaveEvent<PetDocument> event) {
        System.out.println("pet saved started "+ event.getSource());
    }

    @EventListener(BeforeSaveEvent.class)
    public void events(BeforeSaveEvent<PetDocument> event) {
        System.out.println("befor pet saved "+ event.getSource());
    }
}
