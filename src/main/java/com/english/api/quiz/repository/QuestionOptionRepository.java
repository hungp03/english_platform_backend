package com.english.api.quiz.repository;

import com.english.api.quiz.model.QuestionOption;
import java.util.UUID;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, UUID> {
    List<QuestionOption> findByQuestion_IdIn(Collection<UUID> questionIds);
}