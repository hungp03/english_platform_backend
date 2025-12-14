package com.english.api.quiz.service.impl;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.quiz.dto.request.QuestionCreateRequest;
import com.english.api.quiz.dto.request.QuestionOptionCreateRequest;
import com.english.api.quiz.dto.request.QuestionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionOptionResponse;
import com.english.api.quiz.dto.response.QuestionResponse;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.QuestionOption;
import com.english.api.quiz.model.Quiz;
import com.english.api.quiz.repository.QuestionRepository;
import com.english.api.quiz.repository.QuizRepository;
import com.english.api.quiz.service.QuestionService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public QuestionResponse create(QuestionCreateRequest request) {
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (questionRepository.existsByQuiz_IdAndOrderIndex(request.quizId(), request.orderIndex())) {
            throw new ResourceAlreadyExistsException(
                    "Question with orderIndex " + request.orderIndex() + " already exists in this quiz");
        }

        Question question = Question.builder()
                .quiz(quiz)
                .content(request.content())
                .orderIndex(request.orderIndex())
                .explanation(request.explanation())
                .build();

        if (request.options() != null && !request.options().isEmpty()) {
            List<QuestionOption> options = new ArrayList<>();
            for (QuestionOptionCreateRequest optionRequest : request.options()) {
                QuestionOption option = QuestionOption.builder()
                        .question(question)
                        .content(optionRequest.content())
                        .correct(Boolean.TRUE.equals(optionRequest.correct()))
                        .orderIndex(optionRequest.orderIndex() == null ? 1 : optionRequest.orderIndex())
                        .build();
                options.add(option);
            }
            question.setOptions(new LinkedHashSet<>(options));
        }

        question = questionRepository.save(question);
        return toResponse(question);
    }

    @Transactional
    public QuestionResponse update(UUID id, QuestionUpdateRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        if (request.quizId() != null) {
            Quiz quiz = quizRepository.findById(request.quizId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
            question.setQuiz(quiz);
        }
        if (request.content() != null)
            question.setContent(request.content());
        if (request.explanation() != null)
            question.setExplanation(request.explanation());
        if (request.orderIndex() != null) {
            UUID quizId = request.quizId() != null ? request.quizId() : question.getQuiz().getId();
            if (!request.orderIndex().equals(question.getOrderIndex()) &&
                    questionRepository.existsByQuiz_IdAndOrderIndex(quizId, request.orderIndex())) {
                throw new ResourceAlreadyExistsException(
                        "Question with orderIndex " + request.orderIndex() + " already exists in this quiz");
            }
            question.setOrderIndex(request.orderIndex());
        }

        if (request.options() != null) {
            if (question.getOptions() == null) {
                question.setOptions(new LinkedHashSet<>());
            }
            question.getOptions().clear();
            for (QuestionOptionCreateRequest optionRequest : request.options()) {
                QuestionOption option = QuestionOption.builder()
                        .question(question)
                        .content(optionRequest.content())
                        .correct(Boolean.TRUE.equals(optionRequest.correct()))
                        .orderIndex(optionRequest.orderIndex() == null ? 1 : optionRequest.orderIndex())
                        .build();
                question.getOptions().add(option);
            }
        }

        question = questionRepository.save(question);
        return toResponse(question);
    }

    @Transactional
    public void delete(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        questionRepository.delete(question);
    }

    @Transactional(readOnly = true)
    public QuestionResponse get(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        return toResponse(question);
    }

    @Transactional(readOnly = true)
    public PaginationResponse listByQuiz(UUID quizId, Pageable pageable) {
        Page<Question> page = questionRepository.findByQuiz_IdOrderByOrderIndexAsc(quizId, pageable);
        return PaginationResponse.from(page.map(this::toResponse), pageable);
    }

    private QuestionResponse toResponse(Question question) {
        List<QuestionOptionResponse> options = question.getOptions() != null
                ? question.getOptions().stream()
                        .map(option -> new QuestionOptionResponse(option.getId(), option.getContent(), option.isCorrect(),
                                option.getOrderIndex()))
                        .collect(Collectors.toList())
                : new ArrayList<>();
        return new QuestionResponse(
                question.getId(),
                question.getQuiz().getId(),
                question.getContent(),
                question.getOrderIndex(),
                question.getExplanation(),
                options,
                question.getCreatedAt(),
                question.getUpdatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listBySection(UUID sectionId, Pageable pageable) {
        Page<Question> page = questionRepository.findByQuiz_QuizSection_Id(sectionId, pageable);
        return PaginationResponse.from(page.map(this::toResponse), pageable);
    }

    @Override
    @Transactional
    public void importFromCsv(UUID quizId, MultipartFile file) throws IOException {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        Integer maxIndexDb = questionRepository.findMaxOrderIndexByQuizId(quizId);
        int currentMaxIndex = (maxIndexDb == null) ? 0 : maxIndexDb;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (line.trim().isEmpty()) continue;

                List<String> columns = parseCsvLine(line);
                if (columns.size() < 2) continue; 

                try {
                    // --- 1. XỬ LÝ ORDER INDEX (Logic cũ) ---
                    String indexStr = columns.get(0).trim();
                    int orderIndex;

                    if (indexStr.isEmpty()) {
                        currentMaxIndex++; 
                        orderIndex = currentMaxIndex;
                    } else {
                        try {
                            orderIndex = Integer.parseInt(indexStr);
                            if (orderIndex > currentMaxIndex) currentMaxIndex = orderIndex;
                        } catch (NumberFormatException e) {
                            currentMaxIndex++;
                            orderIndex = currentMaxIndex;
                        }
                    }

                    // --- 2. TẠO QUESTION ---
                    String content = columns.get(1).trim();
                    String explanation = (columns.size() > 2) ? columns.get(2).trim() : "";

                    Question question = Question.builder()
                            .quiz(quiz)
                            .content(content)
                            .explanation(explanation)
                            .orderIndex(orderIndex)
                            .build(); // Lúc này chưa có options

                    // --- 3. XỬ LÝ OPTIONS (LINH HOẠT) ---
                    List<QuestionOption> options = new ArrayList<>();
                    
                    // Chỉ chạy logic tìm options nếu dòng CSV có đủ dữ liệu cột option
                    if (columns.size() >= 4) { 
                        // Cột chứa đáp án đúng (A, B, C, D) nằm ở vị trí index 7 (nếu file đủ cột)
                        String correctAnswer = "";
                        if (columns.size() >= 8) {
                            correctAnswer = columns.get(7).trim().toUpperCase();
                        }

                        // Duyệt 4 cột option tiềm năng (index 3, 4, 5, 6)
                        for (int i = 0; i < 4; i++) {
                             if (3 + i >= columns.size()) break; // Hết cột thì dừng
                             
                             String optContent = columns.get(3 + i).trim();
                             if (optContent.isEmpty()) continue; // Ô trống thì bỏ qua

                             boolean isCorrect = false;
                             if (i == 0 && "A".equals(correctAnswer)) isCorrect = true;
                             else if (i == 1 && "B".equals(correctAnswer)) isCorrect = true;
                             else if (i == 2 && "C".equals(correctAnswer)) isCorrect = true;
                             else if (i == 3 && "D".equals(correctAnswer)) isCorrect = true;

                             options.add(QuestionOption.builder()
                                     .question(question)
                                     .content(optContent)
                                     .correct(isCorrect)
                                     .orderIndex(i + 1)
                                     .build());
                        }
                    }
                    
                    // Gán options vào question (nếu có), nếu không thì danh sách rỗng
                    if (!options.isEmpty()) {
                        question.setOptions(new LinkedHashSet<>(options));
                    }

                    // --- 4. LƯU QUESTION (QUAN TRỌNG: Lưu bất kể có option hay không) ---
                    questionRepository.save(question);

                } catch (Exception e) {
                    System.err.println("Lỗi dòng CSV: " + line + " - " + e.getMessage());
                }
            }
        }
    }

    // Hàm hỗ trợ parse CSV xử lý dấu phẩy trong ngoặc kép
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder curVal = new StringBuilder();
        boolean inQuotes = false;
        
        for (char ch : line.toCharArray()) {
            if (inQuotes) {
                if (ch == '"') {
                    inQuotes = false;
                } else {
                    curVal.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    result.add(curVal.toString());
                    curVal.setLength(0);
                } else {
                    curVal.append(ch);
                }
            }
        }
        result.add(curVal.toString());
        return result;
    }
}
