package org.opslog.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import org.opslog.enums.AppGroup;

import jakarta.persistence.Column;

/**
 * Represents a user group in the system.
 * Accounts can belong to multiple groups.
 * Some groups are built-in application groups (non-editable / non-removable).
 */
@Entity
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable name for the group */
    private String name;

    /** Optional description for the group */
    private String description;

    /**
     * Built-in system group type, if applicable.
     * Null if this is a user-defined group.
     */
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = true)
    private AppGroup appGroup;

    // --- Constructors ---
    protected Group() {}

    /** Constructor for user-defined groups */
    public Group(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** Constructor for built-in application groups */
    public Group(AppGroup appGroup) {
        this.appGroup = appGroup;
        this.name = appGroup.name();
        this.description = "System-defined group: " + appGroup.name();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AppGroup getAppGroup() { return appGroup; }
    public void setAppGroup(AppGroup appGroup) { this.appGroup = appGroup; }

}

