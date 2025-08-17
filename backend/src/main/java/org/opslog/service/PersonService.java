package org.opslog.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonService {
    public String fullName(String firstName,String lastName){
        return firstName + " " + lastName;
    }
}