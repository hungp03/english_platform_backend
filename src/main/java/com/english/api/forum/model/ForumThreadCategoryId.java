package com.english.api.forum.model;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ForumThreadCategoryId implements Serializable {
    private UUID thread;
    private UUID category;
}
