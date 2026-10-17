package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.common.BizException;
import com.cloudrecord.dto.AnswerInput;
import com.cloudrecord.dto.CreateQuizRequest;
import com.cloudrecord.dto.OptionInput;
import com.cloudrecord.dto.QuestionInput;
import com.cloudrecord.dto.SubmitAnswerRequest;
import com.cloudrecord.entity.Quiz;
import com.cloudrecord.entity.QuizAnswer;
import com.cloudrecord.entity.QuizAnswerItem;
import com.cloudrecord.entity.QuizOption;
import com.cloudrecord.entity.QuizQuestion;
import com.cloudrecord.entity.User;
import com.cloudrecord.mapper.QuizAnswerItemMapper;
import com.cloudrecord.mapper.QuizAnswerMapper;
import com.cloudrecord.mapper.QuizMapper;
import com.cloudrecord.mapper.QuizOptionMapper;
import com.cloudrecord.mapper.QuizQuestionMapper;
import com.cloudrecord.mapper.UserMapper;
import com.cloudrecord.service.FriendService;
import com.cloudrecord.service.QuizService;
import com.cloudrecord.vo.AnswerItemResultVO;
import com.cloudrecord.vo.AnswerResultVO;
import com.cloudrecord.vo.FeedQuizVO;
import com.cloudrecord.vo.FriendVO;
import com.cloudrecord.vo.PlayOptionVO;
import com.cloudrecord.vo.PlayQuestionVO;
import com.cloudrecord.vo.PlayQuizVO;
import com.cloudrecord.vo.QuizDetailVO;
import com.cloudrecord.vo.QuizOptionVO;
import com.cloudrecord.vo.QuizQuestionVO;
import com.cloudrecord.vo.QuizVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 问答游戏服务实现。
 */
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String CHOICE = "CHOICE";
    private static final String JUDGE = "JUDGE";

    private final QuizMapper quizMapper;
    private final QuizQuestionMapper quizQuestionMapper;
    private final QuizOptionMapper quizOptionMapper;
    private final QuizAnswerMapper quizAnswerMapper;
    private final QuizAnswerItemMapper quizAnswerItemMapper;
    private final FriendService friendService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public QuizDetailVO create(Long userId, CreateQuizRequest req) {
        validate(req);
        Quiz quiz = new Quiz();
        quiz.setOwnerId(userId);
        quiz.setTitle(req.getTitle().trim());
        quiz.setDescription(req.getDescription());
        quiz.setStatus(DRAFT);
        quizMapper.insert(quiz);
        saveQuestions(quiz.getId(), req.getQuestions());
        return buildDetail(quiz);
    }

    @Override
    public List<QuizVO> list(Long userId) {
        List<Quiz> quizzes = quizMapper.selectList(new LambdaQueryWrapper<Quiz>()
                .eq(Quiz::getOwnerId, userId)
                .orderByDesc(Quiz::getCreatedAt));
        if (quizzes.isEmpty()) {
            return List.of();
        }
        List<Long> ids = quizzes.stream().map(Quiz::getId).toList();
        Map<Long, Long> counts = quizQuestionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>()
                        .in(QuizQuestion::getQuizId, ids))
                .stream()
                .collect(Collectors.groupingBy(QuizQuestion::getQuizId, Collectors.counting()));
        return quizzes.stream()
                .map(q -> toVO(q, counts.getOrDefault(q.getId(), 0L).intValue()))
                .toList();
    }

    @Override
    public List<FeedQuizVO> feed(Long userId) {
        List<FriendVO> friends = friendService.listFriends(userId);
        Map<Long, FriendVO> friendById = friends.stream()
                .collect(Collectors.toMap(FriendVO::getId, f -> f));
        Set<Long> ownerIds = new LinkedHashSet<>();
        ownerIds.add(userId);
        friends.forEach(f -> ownerIds.add(f.getId()));

        List<Quiz> quizzes = quizMapper.selectList(new LambdaQueryWrapper<Quiz>()
                .eq(Quiz::getStatus, PUBLISHED)
                .in(Quiz::getOwnerId, ownerIds)
                .orderByDesc(Quiz::getPublishedAt));
        if (quizzes.isEmpty()) {
            return List.of();
        }
        List<Long> quizIds = quizzes.stream().map(Quiz::getId).toList();
        Map<Long, Long> counts = quizQuestionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>()
                        .in(QuizQuestion::getQuizId, quizIds))
                .stream()
                .collect(Collectors.groupingBy(QuizQuestion::getQuizId, Collectors.counting()));
        Set<Long> answeredQuizIds = quizAnswerMapper.selectList(new LambdaQueryWrapper<QuizAnswer>()
                        .eq(QuizAnswer::getUserId, userId)
                        .in(QuizAnswer::getQuizId, quizIds))
                .stream()
                .map(QuizAnswer::getQuizId)
                .collect(Collectors.toSet());
        User me = userMapper.selectById(userId);

        return quizzes.stream().map(q -> {
            String nickname;
            String avatarUrl;
            if (q.getOwnerId().equals(userId) && me != null) {
                nickname = me.getNickname();
                avatarUrl = me.getAvatarUrl();
            } else {
                FriendVO f = friendById.get(q.getOwnerId());
                nickname = f == null ? null : f.getNickname();
                avatarUrl = f == null ? null : f.getAvatarUrl();
            }
            return FeedQuizVO.builder()
                    .id(q.getId())
                    .title(q.getTitle())
                    .description(q.getDescription())
                    .questionCount(counts.getOrDefault(q.getId(), 0L).intValue())
                    .publishedAt(q.getPublishedAt())
                    .ownerId(q.getOwnerId())
                    .ownerNickname(nickname)
                    .ownerAvatarUrl(avatarUrl)
                    .answered(answeredQuizIds.contains(q.getId()))
                    .build();
        }).toList();
    }

    @Override
    public PlayQuizVO play(Long userId, Long quizId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null || !PUBLISHED.equals(quiz.getStatus())) {
            throw BizException.notFound("题目不存在");
        }
        if (!quiz.getOwnerId().equals(userId) && !friendService.isFriend(userId, quiz.getOwnerId())) {
            throw BizException.forbidden("仅好友可答");
        }
        List<QuizQuestion> questions = quizQuestionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>()
                .eq(QuizQuestion::getQuizId, quizId)
                .orderByAsc(QuizQuestion::getSortOrder));
        List<Long> questionIds = questions.stream().map(QuizQuestion::getId).toList();
        Map<Long, List<QuizOption>> optionsByQuestion = quizOptionMapper.selectList(
                        new LambdaQueryWrapper<QuizOption>().in(QuizOption::getQuestionId, questionIds))
                .stream()
                .collect(Collectors.groupingBy(QuizOption::getQuestionId));
        List<PlayQuestionVO> questionVOs = questions.stream().map(q -> PlayQuestionVO.builder()
                .id(q.getId())
                .type(q.getType())
                .content(q.getContent())
                .options(optionsByQuestion.getOrDefault(q.getId(), List.of()).stream()
                        .map(o -> PlayOptionVO.builder().id(o.getId()).label(o.getLabel()).build())
                        .toList())
                .build()).toList();
        return PlayQuizVO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .questionCount(questions.size())
                .questions(questionVOs)
                .build();
    }

    @Override
    public QuizDetailVO getDetail(Long userId, Long quizId) {
        return buildDetail(requireOwned(userId, quizId));
    }

    @Override
    @Transactional
    public QuizDetailVO update(Long userId, Long quizId, CreateQuizRequest req) {
        validate(req);
        Quiz quiz = requireOwned(userId, quizId);
        if (PUBLISHED.equals(quiz.getStatus())) {
            throw BizException.badRequest("已发布不可修改");
        }
        quiz.setTitle(req.getTitle().trim());
        quiz.setDescription(req.getDescription());
        quizMapper.updateById(quiz);
        clearQuestions(quizId);
        saveQuestions(quizId, req.getQuestions());
        return buildDetail(quiz);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long quizId) {
        Quiz quiz = requireOwned(userId, quizId);
        clearQuestions(quizId);
        List<QuizAnswer> answers = quizAnswerMapper.selectList(new LambdaQueryWrapper<QuizAnswer>()
                .eq(QuizAnswer::getQuizId, quizId));
        List<Long> answerIds = answers.stream().map(QuizAnswer::getId).toList();
        if (!answerIds.isEmpty()) {
            quizAnswerItemMapper.delete(new LambdaQueryWrapper<QuizAnswerItem>()
                    .in(QuizAnswerItem::getAnswerId, answerIds));
        }
        quizAnswerMapper.delete(new LambdaQueryWrapper<QuizAnswer>().eq(QuizAnswer::getQuizId, quizId));
        quizMapper.deleteById(quiz.getId());
    }

    @Override
    public QuizDetailVO publish(Long userId, Long quizId) {
        Quiz quiz = requireOwned(userId, quizId);
        if (PUBLISHED.equals(quiz.getStatus())) {
            throw BizException.badRequest("已发布");
        }
        quiz.setStatus(PUBLISHED);
        quiz.setPublishedAt(LocalDateTime.now());
        quizMapper.updateById(quiz);
        return buildDetail(quiz);
    }

    @Override
    @Transactional
    public AnswerResultVO submit(Long userId, Long quizId, SubmitAnswerRequest req) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw BizException.notFound("题目不存在");
        }
        if (!quiz.getOwnerId().equals(userId)) {
            // 好友：仅已发布且互为好友才可答。
            if (!PUBLISHED.equals(quiz.getStatus())) {
                throw BizException.notFound("题目不存在");
            }
            if (!friendService.isFriend(userId, quiz.getOwnerId())) {
                throw BizException.forbidden("仅好友可答");
            }
        }
        return doSubmit(userId, quiz, req);
    }

    @Override
    public AnswerResultVO myAnswer(Long userId, Long quizId) {
        QuizAnswer answer = quizAnswerMapper.selectOne(new LambdaQueryWrapper<QuizAnswer>()
                .eq(QuizAnswer::getQuizId, quizId)
                .eq(QuizAnswer::getUserId, userId));
        if (answer == null) {
            throw BizException.notFound("你还没有作答");
        }
        return buildAnswerResult(answer);
    }

    // ---------- helpers ----------

    private Quiz requireOwned(Long userId, Long quizId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null || !quiz.getOwnerId().equals(userId)) {
            throw BizException.notFound("题目不存在");
        }
        return quiz;
    }

    private void validate(CreateQuizRequest req) {
        List<QuestionInput> questions = req.getQuestions();
        if (questions == null || questions.isEmpty()) {
            throw BizException.badRequest("至少需要一道题");
        }
        for (QuestionInput q : questions) {
            if (q.getContent() == null || q.getContent().isBlank()) {
                throw BizException.badRequest("题目内容不能为空");
            }
            String type = q.getType();
            if (CHOICE.equals(type)) {
                List<OptionInput> options = q.getOptions();
                if (options == null || options.size() < 2) {
                    throw BizException.badRequest("选择题至少需要两个选项");
                }
                if (options.stream().anyMatch(o -> o.getLabel() == null || o.getLabel().isBlank())) {
                    throw BizException.badRequest("选项内容不能为空");
                }
                long correctCount = options.stream().filter(o -> Boolean.TRUE.equals(o.getIsCorrect())).count();
                if (correctCount != 1) {
                    throw BizException.badRequest("选择题需且仅需一个正确选项");
                }
            } else if (JUDGE.equals(type)) {
                if (q.getJudgeAnswer() == null) {
                    throw BizException.badRequest("判断题需给出答案");
                }
            } else {
                throw BizException.badRequest("题目类型不正确");
            }
        }
    }

    private void saveQuestions(Long quizId, List<QuestionInput> inputs) {
        int sort = 0;
        for (QuestionInput in : inputs) {
            QuizQuestion q = new QuizQuestion();
            q.setQuizId(quizId);
            q.setType(in.getType());
            q.setContent(in.getContent().trim());
            q.setJudgeAnswer(JUDGE.equals(in.getType()) ? (Boolean.TRUE.equals(in.getJudgeAnswer()) ? 1 : 0) : null);
            q.setSortOrder(sort++);
            quizQuestionMapper.insert(q);
            if (CHOICE.equals(in.getType()) && in.getOptions() != null) {
                int optSort = 0;
                for (OptionInput o : in.getOptions()) {
                    QuizOption opt = new QuizOption();
                    opt.setQuestionId(q.getId());
                    opt.setLabel(o.getLabel().trim());
                    opt.setIsCorrect(Boolean.TRUE.equals(o.getIsCorrect()) ? 1 : 0);
                    opt.setSortOrder(optSort++);
                    quizOptionMapper.insert(opt);
                }
            }
        }
    }

    private void clearQuestions(Long quizId) {
        List<QuizQuestion> questions = quizQuestionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>()
                .eq(QuizQuestion::getQuizId, quizId));
        List<Long> questionIds = questions.stream().map(QuizQuestion::getId).toList();
        if (!questionIds.isEmpty()) {
            quizOptionMapper.delete(new LambdaQueryWrapper<QuizOption>()
                    .in(QuizOption::getQuestionId, questionIds));
        }
        quizQuestionMapper.delete(new LambdaQueryWrapper<QuizQuestion>().eq(QuizQuestion::getQuizId, quizId));
    }

    private QuizDetailVO buildDetail(Quiz quiz) {
        List<QuizQuestion> questions = quizQuestionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>()
                .eq(QuizQuestion::getQuizId, quiz.getId())
                .orderByAsc(QuizQuestion::getSortOrder));
        List<QuizQuestionVO> questionVOs = new ArrayList<>();
        if (!questions.isEmpty()) {
            List<Long> questionIds = questions.stream().map(QuizQuestion::getId).toList();
            Map<Long, List<QuizOption>> optionsByQuestion = quizOptionMapper.selectList(
                            new LambdaQueryWrapper<QuizOption>().in(QuizOption::getQuestionId, questionIds))
                    .stream()
                    .collect(Collectors.groupingBy(QuizOption::getQuestionId));
            for (QuizQuestion q : questions) {
                questionVOs.add(toQuestionVO(q, optionsByQuestion.getOrDefault(q.getId(), List.of())));
            }
        }
        return QuizDetailVO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .status(quiz.getStatus())
                .publishedAt(quiz.getPublishedAt())
                .createdAt(quiz.getCreatedAt())
                .questions(questionVOs)
                .build();
    }

    private QuizVO toVO(Quiz quiz, int questionCount) {
        return QuizVO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .status(quiz.getStatus())
                .questionCount(questionCount)
                .publishedAt(quiz.getPublishedAt())
                .createdAt(quiz.getCreatedAt())
                .build();
    }

    private QuizQuestionVO toQuestionVO(QuizQuestion q, List<QuizOption> options) {
        return QuizQuestionVO.builder()
                .id(q.getId())
                .type(q.getType())
                .content(q.getContent())
                .judgeAnswer(q.getJudgeAnswer() == null ? null : q.getJudgeAnswer() == 1)
                .options(options.stream().map(this::toOptionVO).toList())
                .build();
    }

    private QuizOptionVO toOptionVO(QuizOption o) {
        return QuizOptionVO.builder()
                .id(o.getId())
                .label(o.getLabel())
                .isCorrect(o.getIsCorrect() != null && o.getIsCorrect() == 1)
                .build();
    }

    private AnswerResultVO doSubmit(Long userId, Quiz quiz, SubmitAnswerRequest req) {
        List<QuizQuestion> questions = quizQuestionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>()
                .eq(QuizQuestion::getQuizId, quiz.getId())
                .orderByAsc(QuizQuestion::getSortOrder));

        QuizAnswer existing = quizAnswerMapper.selectOne(new LambdaQueryWrapper<QuizAnswer>()
                .eq(QuizAnswer::getQuizId, quiz.getId())
                .eq(QuizAnswer::getUserId, userId));
        if (existing != null) {
            if (!quiz.getOwnerId().equals(userId)) {
                // 好友限答一次。
                throw BizException.badRequest("你已经答过了");
            }
            // 出题人自测可重答：删除旧作答。
            quizAnswerMapper.deleteById(existing.getId());
            quizAnswerItemMapper.delete(new LambdaQueryWrapper<QuizAnswerItem>()
                    .eq(QuizAnswerItem::getAnswerId, existing.getId()));
        }

        Map<Long, AnswerInput> byQuestion = new HashMap<>();
        if (req.getAnswers() != null) {
            for (AnswerInput a : req.getAnswers()) {
                byQuestion.put(a.getQuestionId(), a);
            }
        }
        List<Long> questionIds = questions.stream().map(QuizQuestion::getId).toList();
        Map<Long, List<QuizOption>> optionsByQuestion = quizOptionMapper.selectList(
                        new LambdaQueryWrapper<QuizOption>().in(QuizOption::getQuestionId, questionIds))
                .stream()
                .collect(Collectors.groupingBy(QuizOption::getQuestionId));

        QuizAnswer answer = new QuizAnswer();
        answer.setQuizId(quiz.getId());
        answer.setUserId(userId);
        answer.setAnonymous(Boolean.TRUE.equals(req.getAnonymous()) ? 1 : 0);
        answer.setScore(0);
        quizAnswerMapper.insert(answer);

        int score = 0;
        List<AnswerItemResultVO> items = new ArrayList<>();
        for (QuizQuestion q : questions) {
            AnswerInput in = byQuestion.get(q.getId());
            if (in == null) {
                throw BizException.badRequest("请完成所有题目");
            }
            boolean correct;
            Long correctOptionId = null;
            Boolean correctBool = null;
            if (JUDGE.equals(q.getType())) {
                if (in.getChosenBool() == null) {
                    throw BizException.badRequest("判断题未作答");
                }
                boolean expected = q.getJudgeAnswer() != null && q.getJudgeAnswer() == 1;
                correct = in.getChosenBool() == expected;
                correctBool = expected;
            } else {
                if (in.getOptionId() == null) {
                    throw BizException.badRequest("选择题未作答");
                }
                List<QuizOption> opts = optionsByQuestion.getOrDefault(q.getId(), List.of());
                QuizOption correctOpt = opts.stream()
                        .filter(o -> o.getIsCorrect() != null && o.getIsCorrect() == 1)
                        .findFirst()
                        .orElse(null);
                correctOptionId = correctOpt == null ? null : correctOpt.getId();
                correct = in.getOptionId().equals(correctOptionId);
            }
            if (correct) {
                score++;
            }
            QuizAnswerItem item = new QuizAnswerItem();
            item.setAnswerId(answer.getId());
            item.setQuestionId(q.getId());
            if (JUDGE.equals(q.getType())) {
                item.setChosenBool(in.getChosenBool() ? 1 : 0);
            } else {
                item.setChosenOptionId(in.getOptionId());
            }
            item.setIsCorrect(correct ? 1 : 0);
            quizAnswerItemMapper.insert(item);
            items.add(AnswerItemResultVO.builder()
                    .questionId(q.getId())
                    .isCorrect(correct)
                    .correctOptionId(correctOptionId)
                    .correctBool(correctBool)
                    .build());
        }
        answer.setScore(score);
        quizAnswerMapper.updateById(answer);
        return AnswerResultVO.builder().score(score).total(questions.size()).items(items).build();
    }

    private AnswerResultVO buildAnswerResult(QuizAnswer answer) {
        List<QuizAnswerItem> items = quizAnswerItemMapper.selectList(new LambdaQueryWrapper<QuizAnswerItem>()
                .eq(QuizAnswerItem::getAnswerId, answer.getId()));
        if (items.isEmpty()) {
            return AnswerResultVO.builder()
                    .score(answer.getScore() == null ? 0 : answer.getScore())
                    .total(0)
                    .items(List.of())
                    .build();
        }
        List<Long> questionIds = items.stream().map(QuizAnswerItem::getQuestionId).toList();
        Map<Long, QuizQuestion> questionMap = quizQuestionMapper.selectList(
                        new LambdaQueryWrapper<QuizQuestion>().in(QuizQuestion::getId, questionIds))
                .stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));
        Map<Long, List<QuizOption>> optionsByQuestion = quizOptionMapper.selectList(
                        new LambdaQueryWrapper<QuizOption>().in(QuizOption::getQuestionId, questionIds))
                .stream()
                .collect(Collectors.groupingBy(QuizOption::getQuestionId));

        List<AnswerItemResultVO> vos = items.stream().map(it -> {
            QuizQuestion q = questionMap.get(it.getQuestionId());
            Long correctOptionId = null;
            Boolean correctBool = null;
            if (JUDGE.equals(q.getType())) {
                correctBool = q.getJudgeAnswer() != null && q.getJudgeAnswer() == 1;
            } else {
                correctOptionId = optionsByQuestion.getOrDefault(q.getId(), List.of()).stream()
                        .filter(o -> o.getIsCorrect() != null && o.getIsCorrect() == 1)
                        .findFirst()
                        .map(QuizOption::getId)
                        .orElse(null);
            }
            return AnswerItemResultVO.builder()
                    .questionId(it.getQuestionId())
                    .isCorrect(it.getIsCorrect() != null && it.getIsCorrect() == 1)
                    .correctOptionId(correctOptionId)
                    .correctBool(correctBool)
                    .build();
        }).toList();
        return AnswerResultVO.builder()
                .score(answer.getScore() == null ? 0 : answer.getScore())
                .total(items.size())
                .items(vos)
                .build();
    }
}
