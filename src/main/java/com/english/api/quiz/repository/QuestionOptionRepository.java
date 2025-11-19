package com.english.api.quiz.repository;

import com.english.api.quiz.model.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, UUID> {
    List<QuestionOption> findByQuestion_IdIn(Collection<UUID> questionIds);
}