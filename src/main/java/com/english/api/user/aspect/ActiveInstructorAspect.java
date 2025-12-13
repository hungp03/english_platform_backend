package com.english.api.user.aspect;

import com.english.api.user.util.InstructorValidationUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ActiveInstructorAspect {
    
    private final InstructorValidationUtil instructorValidationUtil;
    
    @Before("@annotation(com.english.api.user.annotation.ActiveInstructor)")
    public void validateActiveInstructor() {
        instructorValidationUtil.validateActiveInstructor();
    }
}
