package com.english.api.cart.mapper;

import com.english.api.cart.dto.response.CartItemResponse;
import com.english.api.cart.dto.response.CartCheckoutResponse;
import com.english.api.cart.dto.response.CourseInCartResponse;
import com.english.api.cart.model.CartItem;
import com.english.api.course.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "course", source = "course")
    @Mapping(target = "addedAt", source = "addedAt")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    @Mapping(target = "instructorName", expression = "java(course.getCreatedBy() != null ? course.getCreatedBy().getFullName() : null)")
    @Mapping(target = "isPurchased", source = "isPurchased")
    CourseInCartResponse toCourseInCartResponse(Course course, boolean isPurchased);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "thumbnail", source = "thumbnail")
    @Mapping(target = "priceCents", source = "priceCents")
    @Mapping(target = "currency", source = "currency")
    CartCheckoutResponse toCartCheckoutResponse(Course course);
}
