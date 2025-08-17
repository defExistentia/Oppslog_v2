package org.opslog.repositories;

import org.opslog.entities.Account;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {

    // Find by email (used for login)
    public Account findByEmail(String email) {
        return find("email", email).firstResult();
    }

    // Find by username
    public Account findByUsername(String username) {
        return find("username", username).firstResult();
    }

    // Find accounts by full name
    public List<Account> findByFullName(String firstName, String lastName) {
        return list("firstName = ?1 and lastName = ?2", firstName, lastName);
    }

    // Find all accounts in a group
    public List<Account> findByGroup(String groupName) {
        return list("groupName = ?1", groupName);
    }

    // Find accounts by username prefix within a group (useful for group searches)
    public List<Account> findByUsernamePrefix(String prefix, String groupName) {
        return list("username like ?1 and groupName = ?2", prefix + "%", groupName);
    }
}

