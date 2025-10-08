package com.english.api.common.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Created by hungpham on 10/1/2025
 */
@Entity
@Table(name = "taggings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tagging {
    @EmbeddedId
    private TaggingId id;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;
}

