package org.opslog.repositories;

import org.opslog.entities.Tag;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TagRepository implements PanacheRepository<Tag> {

    // Find a tag by its exact title
    public Tag findByTitle(String title) {
        return find("title", title).firstResult();
    }

    // Find all tags that contain a substring in the title (case-insensitive)
    public List<Tag> findByTitleContains(String substring) {
        return list("lower(title) like ?1", "%" + substring.toLowerCase() + "%");
    }

    // Find all tags by color
    public List<Tag> findByColor(String color) {
        return list("color", color);
    }

    // Persist a new tag and flush immediately
    public Tag addTag(Tag tag) {
        persist(tag);
        flush();
        return tag;
    }

    // Delete a tag by its ID
    public boolean deleteById(long id) {
        return delete("id", id) > 0;
    }

    // Delete a tag by object reference
    public boolean deleteTag(Tag tag) {
        if (tag == null) return false;
        delete(tag);
        return true;
    }
}