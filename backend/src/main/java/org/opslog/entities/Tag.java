package org.opslog.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Tags are used to flag logs for searchability and visual identification.
 * Each Tag has a title, optional description, and a color for visual cues.
 * Tags are meant to be unique per log and can be associated with multiple logs.
 */
@Entity
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String description;
    private String color;

    protected Tag() {}

    public Tag(String title, String description, String color) {
        this.title = title;
        this.description = description;
        this.color = color;
    }

    // --- Getters & Setters ---
    public long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
