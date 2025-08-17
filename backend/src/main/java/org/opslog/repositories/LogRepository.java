package org.opslog.repositories;

import org.opslog.entities.Account;
import org.opslog.entities.Group;
import org.opslog.entities.Log;
import org.opslog.entities.Tag;
import org.opslog.enums.AppGroup;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;

/**
 * Repository for querying and managing Log entities.
 * <p>
 * All queries enforce **group-level confidentiality**:
 * an Account requesting logs will only see logs created by accounts
 * in groups that the account belongs to.
 * </p>
 * <p>
 * Features include:
 * <ul>
 *     <li>Queries by time range or specific ZonedDateTime</li>
 *     <li>Queries by single or multiple groups</li>
 *     <li>Queries by single or multiple accounts</li>
 *     <li>Queries by tags (single or multiple)</li>
 *     <li>Title and description search (exact and partial match)</li>
 *     <li>Retrieving all revisions sorted from most recent to original</li>
 * </ul>
 * </p>
 */
@ApplicationScoped
public class LogRepository implements PanacheRepository<Log> {

    // --------------------------------------------
    // --- Account-scoped Queries for Visibility ---
    // --------------------------------------------

    /**
     * Returns all logs visible to the given account.
     * Only logs created by accounts in groups that the account belongs to will be returned.
     */
    public List<Log> findAllVisibleLogs(Account account) {
        return list(
            "EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g " +
            "WHERE g IN ?1 AND a = createdBy" +
            ")",
            account.getGroups()
        );
    }

    // --------------------------------------------
    // --- Time-based Queries ---
    // --------------------------------------------

    /**
     * Finds logs visible to the account within a specific time range.
     */
    public List<Log> findByTimeRange(Account account, ZonedDateTime from, ZonedDateTime to) {
        return list(
            "timeOfEvent between ?1 and ?2 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?3 AND a = createdBy" +
            ")",
            from, to, account.getGroups()
        );
    }

    /**
     * Finds logs visible to the account for a specific ZonedDateTime.
     */
    public List<Log> findByTime(Account account, ZonedDateTime time) {
        return list(
            "timeOfEvent = ?1 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy" +
            ")",
            time, account.getGroups()
        );
    }

    // --------------------------------------------
    // --- Group Queries ---
    // --------------------------------------------

    /**
     * Returns all logs for a specific group, visible to the given account.
     */
    public List<Log> findByGroup(Account account, Group group) {
        return list(
            "EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g = ?1 AND a = createdBy" +
            ")",
            group
        );
    }

    /**
     * Returns all logs for all groups the account belongs to.
     */
    public List<Log> findByAllGroups(Account account) {
        return findAllVisibleLogs(account);
    }

    // --------------------------------------------
    // --- Account Queries ---
    // --------------------------------------------

    /**
     * Returns all logs for a specific account, visible to the requesting account.
     */
    public List<Log> findByAccount(Account account, Account targetAccount) {
        return list(
            "createdBy = ?1 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy" +
            ")",
            targetAccount, account.getGroups()
        );
    }

    /**
     * Returns all logs for a set of accounts, visible to the requesting account.
     */
    public List<Log> findByAccounts(Account account, Set<Account> targetAccounts) {
        return list(
            "createdBy in ?1 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy" +
            ")",
            targetAccounts, account.getGroups()
        );
    }

    // --------------------------------------------
    // --- Tag Queries ---
    // --------------------------------------------

    /**
     * Returns all logs associated with a single tag, respecting account visibility.
     */
    public List<Log> findByTag(Account account, Tag tag) {
        return list(
            "EXISTS (" +
            "SELECT t FROM Log l JOIN l.tags t " +
            "WHERE t = ?1 AND l = this AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy))",
            tag, account.getGroups()
        );
    }

    /**
     * Returns all logs associated with a set of tags, respecting account visibility.
     */
    public List<Log> findByTags(Account account, Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) return List.of();
        return list(
            "EXISTS (" +
            "SELECT t FROM Log l JOIN l.tags t " +
            "WHERE t IN ?1 AND l = this AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy))",
            tags, account.getGroups()
        );
    }

    // --------------------------------------------
    // --- Title Queries ---
    // --------------------------------------------

    /**
     * Returns logs with a title exactly matching the given string.
     */
    public List<Log> findByTitle(Account account, String title) {
        return list(
            "title = ?1 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy" +
            ")",
            title, account.getGroups()
        );
    }

    /**
     * Returns logs with a title containing the given substring (case-insensitive).
     */
    public List<Log> findByTitleContains(Account account, String substring) {
        return list(
            "lower(title) like ?1 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy" +
            ")",
            "%" + substring.toLowerCase() + "%", account.getGroups()
        );
    }

    // --------------------------------------------
    // --- Description Queries ---
    // --------------------------------------------

    /**
     * Returns logs with a description exactly matching the given string.
     */
    public List<Log> findByDescription(Account account, String description) {
        return list(
            "description = ?1 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy" +
            ")",
            description, account.getGroups()
        );
    }

    /**
     * Returns logs with a description containing the given substring (case-insensitive).
     */
    public List<Log> findByDescriptionContains(Account account, String substring) {
        return list(
            "lower(description) like ?1 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy" +
            ")",
            "%" + substring.toLowerCase() + "%", account.getGroups()
        );
    }

    // --------------------------------------------
    // --- Revision Queries ---
    // --------------------------------------------

    /**
     * Returns all revisions of a given log, sorted from most recent to original.
     */
    public Set<Log> findRevisions(Log parentLog) {
        TreeSet<Log> sortedRevisions = new TreeSet<>(Comparator.comparing(Log::getRevisedAt).reversed());
        sortedRevisions.addAll(parentLog.getRevisions());
        return sortedRevisions;
    }

    // --------------------------------------------
    // --- Persistence Helpers ---
    // --------------------------------------------

    /** Persists a log and immediately flushes it to the database. */
    public void persistAndFlush(Log log) {
        persist(log);
        flush();
    }

    /**
     * Adds a revision to a parent log, setting the reviser and revision time.
     */
    public void addRevision(Log parentLog, Log revision, Account reviser) {
        revision.setParent(parentLog);
        revision.setRevisedBy(reviser);
        revision.setRevisedAt(ZonedDateTime.now());
        persistAndFlush(revision);
    }

    // --------------------------------------------
    // --- Safe Deletion Methods (Admin Only) ---
    // --------------------------------------------

    /**
     * Checks if the given account belongs to the ADMINISTRATOR group.
     */
    private boolean isAdmin(Account account) {
        return account.getGroups().stream()
            .anyMatch(g -> g.getAppGroup() == AppGroup.ADMINISTRATOR);
    }

    /**
     * Deletes a single log if the account is an administrator and has access.
     */
    public boolean deleteLog(Account account, Log log) {
        if (!isAdmin(account)) return false;

        List<Log> visible = list(
            "id = ?1 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy" +
            ")",
            log.getId(), account.getGroups()
        );

        if (visible.isEmpty()) return false;

        delete("id", log.getId());
        return true;
    }

    /**
     * Deletes all logs for a specific account if the requesting account is an administrator.
     */
    public long deleteLogsForAccount(Account account, Account targetAccount) {
        if (!isAdmin(account)) return 0;

        return delete(
            "createdBy = ?1 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy" +
            ")",
            targetAccount, account.getGroups()
        );
    }

    /**
     * Deletes all logs for a specific group if the requesting account is an administrator.
     */
    public long deleteLogsForGroup(Account account, Group group) {
        if (!isAdmin(account)) return 0;

        return delete(
            "EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g = ?1 AND a = createdBy" +
            ")",
            group
        );
    }

    /**
     * Deletes all logs for a set of accounts if the requesting account is an administrator.
     */
    public long deleteLogsForAccounts(Account account, Set<Account> targetAccounts) {
        if (!isAdmin(account) || targetAccounts == null || targetAccounts.isEmpty()) return 0;

        return delete(
            "createdBy in ?1 AND EXISTS (" +
            "SELECT g FROM Account a JOIN a.groups g WHERE g IN ?2 AND a = createdBy" +
            ")",
            targetAccounts, account.getGroups()
        );
    }

}




