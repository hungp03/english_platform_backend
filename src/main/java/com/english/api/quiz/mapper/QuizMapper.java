package com.english.api.quiz.mapper;

import com.english.api.quiz.dto.response.*;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.QuestionOption;
import com.english.api.quiz.model.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    @Mapping(target = "quizTypeId", source = "quiz.quizType.id")
    @Mapping(target = "quizTypeName", source = "quiz.quizType.name")
    @Mapping(target = "quizSectionId", source = "quiz.quizSection.id")
    @Mapping(target = "quizSectionName", source = "quiz.quizSection.name")
    @Mapping(target = "skill", source = "quiz.quizSection.skill")
    @Mapping(target = "questions", source = "questions")
    PublicQuizDetailResponse toPublicQuizDetailResponse(Quiz quiz, List<Question> questions);

    @Mapping(target = "skill", source = "quizSection.skill")
    @Mapping(target = "quizTypeId", source = "quizType.id")
    @Mapping(target = "quizSectionId", source = "quizSection.id")
    @Mapping(target = "quizSectionName", source = "quizSection.name")
    @Mapping(target = "quizTypeName", source = "quizType.name")
    QuizResponse toQuizResponse(Quiz quiz);

    @Mapping(target = "skill", source = "quizSection.skill")
    @Mapping(target = "quizTypeId", source = "quizType.id")
    @Mapping(target = "quizSectionId", source = "quizSection.id")
    @Mapping(target = "quizSectionName", source = "quizSection.name")
    @Mapping(target = "quizTypeName", source = "quizType.name")
    QuizListResponse toQuizListResponse(Quiz quiz);

    PublicQuestion toPublicQuestion(Question question);

    List<PublicQuestion> toPublicQuestionList(List<Question> questions);

    PublicOption toPublicOption(QuestionOption option);
}
