package com.english.api.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaggingId implements Serializable {
    @Column(name = "tag_id")
    private UUID tagId;
    private String entity;
    private UUID entityId;
}

