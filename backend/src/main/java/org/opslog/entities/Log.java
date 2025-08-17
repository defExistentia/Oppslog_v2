package org.opslog.entities;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 * Represents a log entry in the opslog system.
 * <p>
 * Each Log records an event, along with metadata such as the creator, the time of the event,
 * associated tags, title, and description. Logs can be revised, forming a revision chain where
 * each revised log points to its parent log and maintains a set of child revisions.
 * </p>
 * <p>
 * Features:
 * <ul>
 *     <li><b>CreatedBy</b> - the person who originally created the log.</li>
 *     <li><b>RevisedBy</b> - the person who last revised the log.</li>
 *     <li><b>TimeOfEvent</b> - the timestamp when the event occurred.</li>
 *     <li><b>Tags</b> - a set of associated tags, stored in a many-to-many relationship. Each tag is unique per log.</li>
 *     <li><b>Revisions</b> - a set of Log entries that are revisions of this log. Each revision maintains a reference to its parent.</li>
 * </ul>
 * </p>
 * <p>
 * Usage:
 * <pre>
 *     // Create a new log
 *     Log original = new Log(alice, eventTime, tags, "Title", "Description");
 *
 *     // Create a revision
 *     Log revision = new Log(bob, eventTime, tags, "Updated Title", "Updated Description");
 *     original.addRevision(revision, bob);
 * </pre>
 * </p>
 * @see Calendar.java for calendar-related integrations with logs.
 */
@Entity
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "create_by_id", nullable = false)
    private Account createdBy;
    private ZonedDateTime createdAt;
    private ZonedDateTime timeOfEvent;
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "log_tags",
        joinColumns = @JoinColumn(name = "log_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    private String title;
    private String description;

    // -- Revision definitions
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revised_by_id", nullable = false)
    private Account revisedBy;
    private ZonedDateTime revisedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Log parent; // if log has been revised this is previous log

    // Set of log revisions
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Log> revisions = new HashSet<>();
    

    // --- Constructors --- //
    protected Log(){}

    // Original Log entry
    public Log(Account createdBy, ZonedDateTime timeOfEvent, Set<Tag> tags, String title, String description){
        this.createdBy = createdBy;
        this.createdAt = ZonedDateTime.now();
        this.timeOfEvent = timeOfEvent;
        this.tags = tags;
        this.title = title;
        this.description = description;
    }

    // --- Original Data --- //
    public Long getId() { return id; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getTimeOfEvent(){return timeOfEvent;}
    public void setTimeOfEvent(ZonedDateTime timeOfEvent) {this.timeOfEvent = timeOfEvent;}

    public Account getCreatedBy() { return createdBy; }
    public void setCreatedBy(Account createdBy) { this.createdBy = createdBy; }

    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // --- Revision Tracking --- //
    public Log getParent() { return parent; }
    public void setParent(Log parent) { this.parent = parent; }

    public Account getRevisedBy() { return revisedBy; }
    public void setRevisedBy(Account revisedBy) { this.revisedBy = revisedBy; }
    
    public ZonedDateTime getRevisedAt() { return revisedAt; }
    public void setRevisedAt(ZonedDateTime revisedAt) { this.revisedAt = revisedAt; }
    
    public Set<Log> getRevisions() { return revisions; }
    public void setRevisions(Set<Log> revisions) { this.revisions = revisions; }

    // --- Revising method --- //
    public void addRevision(Log revision, Account account) {
        revision.setParent(this);
        revision.setRevisedBy(account);
        revision.setRevisedAt(ZonedDateTime.now());
        this.revisions.add(revision);
    }
}
