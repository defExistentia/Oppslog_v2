package org.opslog.repositories;

import org.opslog.entities.Account;
import org.opslog.entities.Group;
import org.opslog.enums.AppGroup;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Group entities.
 * <p>
 * Supports queries for:
 * <ul>
 *     <li>All groups</li>
 *     <li>User-defined groups</li>
 *     <li>System-defined application groups</li>
 *     <li>Finding groups by name</li>
 *     <li>Checking if an account is a member of a group</li>
 * </ul>
 * </p>
 */
@ApplicationScoped
public class GroupRepository implements PanacheRepository<Group> {

    // --------------------------------------------
    // --- Basic Queries ---
    // --------------------------------------------

    /**
     * Returns all groups.
     */
    public List<Group> findAllGroups() {
        return listAll();
    }

    /**
     * Finds all user-defined groups (i.e., applicationGroup is null).
     */
    public List<Group> findUserGroups() {
        return list("applicationGroup is null");
    }

    /**
     * Finds a system-defined group by its ApplicationGroup enum.
     */
    public Optional<Group> findByApplicationGroup(AppGroup appGroup) {
        return find("applicationGroup", appGroup).firstResultOptional();
    }

    /**
     * Finds a group by its name (case-insensitive).
     */
    public Optional<Group> findByName(String name) {
        return find("lower(name) = ?1", name.toLowerCase()).firstResultOptional();
    }

    // --------------------------------------------
    // --- Account Membership ---
    // --------------------------------------------

    /**
     * Checks if the given account is a member of the specified group.
     */
    public boolean isAccountMemberOfGroup(Account account, Group group) {
        return account.getGroups().contains(group);
    }

    /**
     * Adds an account to a group if not already a member.
     */
    public void addAccountToGroup(Account account, Group group) {
        if (!isAccountMemberOfGroup(account, group)) {
            account.getGroups().add(group);
        }
    }

    /**
     * Removes an account from a group if it is a user-defined group.
     * System-defined application groups cannot be removed.
     */
    public boolean removeAccountFromGroup(Account account, Group group) {
        if (group.getAppGroup() != null) {
            // Cannot remove from system-defined group
            return false;
        }
        return account.getGroups().remove(group);
    }
}
