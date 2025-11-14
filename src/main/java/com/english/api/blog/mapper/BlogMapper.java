package com.english.api.blog.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.english.api.blog.dto.response.BlogCategoryResponse;
import com.english.api.blog.dto.response.CommentResponse;
import com.english.api.blog.dto.response.PostResponse;
import com.english.api.blog.dto.response.PublicPostDetailResponse;
import com.english.api.blog.dto.response.PublicPostSummaryResponse;
import com.english.api.blog.model.BlogCategory;
import com.english.api.blog.model.BlogComment;
import com.english.api.blog.model.BlogPost;

@Mapper(componentModel = "spring")
public interface BlogMapper {
    
    BlogCategoryResponse toCategoryResponse(BlogCategory category);
    
    List<BlogCategoryResponse> toCategoryResponseList(Set<BlogCategory> categories);
    
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
