package com.english.api.forum.mapper;

import com.english.api.forum.dto.response.ForumPostResponse;
import com.english.api.forum.entity.ForumPost;
import com.english.api.user.model.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ForumPostMapper {

    @Mapping(target = "threadId", source = "thread.id")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "authorAvatarUrl", ignore = true)
    ForumPostResponse toResponse(ForumPost post);

    @Mapping(target = "threadId", source = "post.thread.id")
    @Mapping(target = "parentId", source = "post.parent.id")
    @Mapping(target = "authorName", expression = "java(extractAuthorName(post, userMap))")
    @Mapping(target = "authorAvatarUrl", expression = "java(extractAuthorAvatarUrl(post, userMap))")
    ForumPostResponse toResponse(ForumPost post, @Context Map<UUID, User> userMap);

    default String extractAuthorName(ForumPost post, Map<UUID, User> userMap) {
        if (post.getAuthorId() == null) {
            return null;
        }
        User user = userMap.get(post.getAuthorId());
        return user != null ? user.getFullName() : null;
    }

    default String extractAuthorAvatarUrl(ForumPost post, Map<UUID, User> userMap) {
        if (post.getAuthorId() == null) {
            return null;
        }
        User user = userMap.get(post.getAuthorId());
        return user != null ? user.getAvatarUrl() : null;
    }
}
