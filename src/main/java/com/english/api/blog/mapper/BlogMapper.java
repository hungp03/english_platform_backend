package com.english.api.blog.mapper;

import com.english.api.blog.dto.request.BlogCategoryCreateRequest;
import com.english.api.blog.dto.response.*;
import com.english.api.blog.model.BlogCategory;
import com.english.api.blog.model.BlogComment;
import com.english.api.blog.model.BlogPost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlogMapper {
    BlogCategoryResponse toResponse(BlogCategory blogCategory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "slug", source = "slug")
    BlogCategory toEntity(BlogCategoryCreateRequest request, String slug);
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "categories", source = "categories")
    PostResponse toResponse(BlogPost post);

    @Mapping(target = "categories", source = "categories")
    PublicPostSummaryResponse toPublicSummary(BlogPost post);

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "categories", source = "categories")
    @Mapping(target = "authorName", source = "author.fullName")
    @Mapping(target = "authorAvatarUrl", source = "author.avatarUrl")
    PublicPostDetailResponse toPublicDetail(BlogPost post);

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.fullName")
    @Mapping(target = "authorAvatarUrl", source = "author.avatarUrl")
    @Mapping(target = "postTitle", source = "post.title")
    @Mapping(target = "postSlug", source = "post.slug")
    CommentResponse toCommentResponse(BlogComment comment);
}
