package com.english.api.common.model;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Created by hungpham on 10/1/2025
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    private UUID id;

    @Column(unique = true)
    private String name;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch(); // UUIDv7
        }
    }
}

