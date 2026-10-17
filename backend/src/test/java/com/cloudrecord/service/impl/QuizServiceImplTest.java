package com.cloudrecord.service.impl;

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
import com.cloudrecord.vo.AnswerItemResultVO;
import com.cloudrecord.vo.AnswerResultVO;
import com.cloudrecord.vo.FeedQuizVO;
import com.cloudrecord.vo.FriendVO;
import com.cloudrecord.vo.PlayQuizVO;
import com.cloudrecord.vo.QuizDetailVO;
import com.cloudrecord.vo.QuizVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuizMapper quizMapper;

    @Mock
    private QuizQuestionMapper quizQuestionMapper;

    @Mock
    private QuizOptionMapper quizOptionMapper;

    @Mock
    private QuizAnswerMapper quizAnswerMapper;

    @Mock
    private QuizAnswerItemMapper quizAnswerItemMapper;

    @Mock
    private FriendService friendService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private QuizServiceImpl quizService;

    // ---- helpers ----

    private Quiz quiz(Long id, Long ownerId, String status) {
        Quiz q = new Quiz();
        q.setId(id);
        q.setOwnerId(ownerId);
        q.setTitle("问卷");
        q.setStatus(status);
        return q;
    }

    private QuizQuestion question(Long id, Long quizId, String type, String content, Integer judgeAnswer, int sortOrder) {
        QuizQuestion q = new QuizQuestion();
        q.setId(id);
        q.setQuizId(quizId);
        q.setType(type);
        q.setContent(content);
        q.setJudgeAnswer(judgeAnswer);
        q.setSortOrder(sortOrder);
        return q;
    }

    private QuizOption option(Long id, Long questionId, String label, Integer isCorrect) {
        QuizOption o = new QuizOption();
        o.setId(id);
        o.setQuestionId(questionId);
        o.setLabel(label);
        o.setIsCorrect(isCorrect);
        return o;
    }

    private QuizAnswer answer(Long id, Long quizId, Long userId, Integer score) {
        QuizAnswer a = new QuizAnswer();
        a.setId(id);
        a.setQuizId(quizId);
        a.setUserId(userId);
        a.setScore(score);
        return a;
    }

    private QuizAnswerItem item(Long id, Long answerId, Long questionId, Long chosenOptionId, Integer chosenBool, Integer isCorrect) {
        QuizAnswerItem it = new QuizAnswerItem();
        it.setId(id);
        it.setAnswerId(answerId);
        it.setQuestionId(questionId);
        it.setChosenOptionId(chosenOptionId);
        it.setChosenBool(chosenBool);
        it.setIsCorrect(isCorrect);
        return it;
    }

    private User user(Long id, String email, String nickname) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setNickname(nickname);
        u.setAvatarUrl("http://a/" + id + ".png");
        return u;
    }

    private FriendVO friendVO(Long id, String nickname) {
        return FriendVO.builder()
                .id(id)
                .nickname(nickname)
                .avatarUrl("http://a/" + id + ".png")
                .build();
    }

    private CreateQuizRequest validRequest() {
        CreateQuizRequest req = new CreateQuizRequest();
        req.setTitle("测试问卷");
        QuestionInput judge = new QuestionInput();
        judge.setType("JUDGE");
        judge.setContent("地球是圆的");
        judge.setJudgeAnswer(true);
        QuestionInput choice = new QuestionInput();
        choice.setType("CHOICE");
        choice.setContent("1+1=?");
        OptionInput a = new OptionInput();
        a.setLabel("2");
        a.setIsCorrect(true);
        OptionInput b = new OptionInput();
        b.setLabel("3");
        b.setIsCorrect(false);
        choice.setOptions(List.of(a, b));
        req.setQuestions(List.of(judge, choice));
        return req;
    }

    // ---- create ----

    @Test
    void create_savesQuizAndQuestions() {
        CreateQuizRequest req = validRequest();
        req.setTitle("  测试问卷  ");

        doAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(1L);
            return 1;
        }).when(quizMapper).insert(any(Quiz.class));
        doAnswer(inv -> {
            QuizQuestion q = inv.getArgument(0);
            q.setId(10L + q.getSortOrder());
            return 1;
        }).when(quizQuestionMapper).insert(any(QuizQuestion.class));
        doAnswer(inv -> {
            QuizOption o = inv.getArgument(0);
            o.setId(100L + o.getSortOrder());
            return 1;
        }).when(quizOptionMapper).insert(any(QuizOption.class));
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of(
                question(10L, 1L, "JUDGE", "地球是圆的", 1, 0),
                question(11L, 1L, "CHOICE", "1+1=?", null, 1)));
        when(quizOptionMapper.selectList(any())).thenReturn(List.of(
                option(100L, 11L, "2", 1),
                option(101L, 11L, "3", 0)));

        QuizDetailVO result = quizService.create(1L, req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("测试问卷");
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getQuestions()).hasSize(2);
        assertThat(result.getQuestions().get(0).getJudgeAnswer()).isTrue();
        assertThat(result.getQuestions().get(1).getOptions()).hasSize(2);
        verify(quizQuestionMapper, times(2)).insert(any(QuizQuestion.class));
        verify(quizOptionMapper, times(2)).insert(any(QuizOption.class));
    }

    @Test
    void create_emptyQuestionsThrows() {
        CreateQuizRequest req = validRequest();
        req.setQuestions(List.of());

        assertThatThrownBy(() -> quizService.create(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("至少需要一道题");
    }

    @Test
    void create_choiceWithoutCorrectThrows() {
        CreateQuizRequest req = validRequest();
        req.getQuestions().get(1).getOptions().forEach(o -> o.setIsCorrect(false));

        assertThatThrownBy(() -> quizService.create(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("一个正确选项");
    }

    @Test
    void create_judgeWithoutAnswerThrows() {
        CreateQuizRequest req = validRequest();
        req.getQuestions().get(0).setJudgeAnswer(null);

        assertThatThrownBy(() -> quizService.create(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("判断题需给出答案");
    }

    @Test
    void create_invalidTypeThrows() {
        CreateQuizRequest req = validRequest();
        req.getQuestions().get(0).setType("TEXT");

        assertThatThrownBy(() -> quizService.create(1L, req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("题目类型不正确");
    }

    // ---- list / detail ----

    @Test
    void list_attachesQuestionCount() {
        when(quizMapper.selectList(any())).thenReturn(List.of(
                quiz(1L, 1L, "DRAFT"),
                quiz(2L, 1L, "PUBLISHED")));
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of(
                question(10L, 1L, "JUDGE", "a", 1, 0),
                question(11L, 1L, "CHOICE", "b", null, 1),
                question(12L, 2L, "JUDGE", "c", 0, 0)));

        List<QuizVO> result = quizService.list(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuestionCount()).isEqualTo(2);
        assertThat(result.get(1).getQuestionCount()).isEqualTo(1);
    }

    @Test
    void getDetail_notOwnedThrows404() {
        when(quizMapper.selectById(2L)).thenReturn(quiz(2L, 99L, "DRAFT"));

        assertThatThrownBy(() -> quizService.getDetail(1L, 2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("题目不存在");
    }

    // ---- update / publish / delete ----

    @Test
    void update_publishedThrows400() {
        when(quizMapper.selectById(2L)).thenReturn(quiz(2L, 1L, "PUBLISHED"));

        assertThatThrownBy(() -> quizService.update(1L, 2L, validRequest()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("已发布不可修改");
    }

    @Test
    void update_draftRebuildsQuestions() {
        CreateQuizRequest req = validRequest();
        QuestionInput onlyJudge = new QuestionInput();
        onlyJudge.setType("JUDGE");
        onlyJudge.setContent("新题");
        onlyJudge.setJudgeAnswer(false);
        req.setQuestions(List.of(onlyJudge));

        when(quizMapper.selectById(2L)).thenReturn(quiz(2L, 1L, "DRAFT"));
        doAnswer(inv -> {
            QuizQuestion q = inv.getArgument(0);
            q.setId(20L);
            return 1;
        }).when(quizQuestionMapper).insert(any(QuizQuestion.class));
        when(quizQuestionMapper.selectList(any())).thenReturn(
                List.of(question(10L, 2L, "JUDGE", "旧题", 1, 0)),
                List.of(question(20L, 2L, "JUDGE", "新题", 0, 0)));
        when(quizOptionMapper.selectList(any())).thenReturn(List.of());

        QuizDetailVO result = quizService.update(1L, 2L, req);

        verify(quizMapper).updateById(any(Quiz.class));
        verify(quizQuestionMapper).delete(any());
        assertThat(result.getQuestions()).hasSize(1);
        assertThat(result.getQuestions().get(0).getContent()).isEqualTo("新题");
        assertThat(result.getQuestions().get(0).getJudgeAnswer()).isFalse();
    }

    @Test
    void publish_setsPublished() {
        when(quizMapper.selectById(3L)).thenReturn(quiz(3L, 1L, "DRAFT"));
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of());

        QuizDetailVO result = quizService.publish(1L, 3L);

        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
        verify(quizMapper).updateById(argThat((Quiz q) -> "PUBLISHED".equals(q.getStatus())));
    }

    @Test
    void delete_cascadesChildren() {
        when(quizMapper.selectById(4L)).thenReturn(quiz(4L, 1L, "DRAFT"));
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of(question(10L, 4L, "JUDGE", "x", 1, 0)));
        when(quizAnswerMapper.selectList(any())).thenReturn(List.of(answer(9L, 4L, 1L, 1)));

        quizService.delete(1L, 4L);

        verify(quizOptionMapper).delete(any());
        verify(quizQuestionMapper).delete(any());
        verify(quizAnswerItemMapper).delete(any());
        verify(quizAnswerMapper).delete(any());
        verify(quizMapper).deleteById(4L);
    }

    // ---- feed ----

    @Test
    void feed_returnsFriendsAndMine() {
        when(friendService.listFriends(any())).thenReturn(List.of(
                friendVO(2L, "B"), friendVO(3L, "C")));
        when(quizMapper.selectList(any())).thenReturn(List.of(
                quiz(10L, 2L, "PUBLISHED"),
                quiz(11L, 1L, "PUBLISHED"),
                quiz(12L, 3L, "PUBLISHED")));
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of(
                question(100L, 10L, "JUDGE", "a", 1, 0),
                question(101L, 11L, "JUDGE", "b", 1, 0),
                question(102L, 11L, "JUDGE", "c", 0, 0)));
        when(quizAnswerMapper.selectList(any())).thenReturn(List.of(answer(9L, 11L, 1L, 1)));
        when(userMapper.selectById(1L)).thenReturn(user(1L, "me@test.com", "我"));

        List<FeedQuizVO> result = quizService.feed(1L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getOwnerNickname()).isEqualTo("B");
        assertThat(result.get(0).isAnswered()).isFalse();
        assertThat(result.get(1).getOwnerNickname()).isEqualTo("我");
        assertThat(result.get(1).getQuestionCount()).isEqualTo(2);
        assertThat(result.get(1).isAnswered()).isTrue();
        assertThat(result.get(2).getOwnerNickname()).isEqualTo("C");
    }

    @Test
    void feed_emptyReturnsEmpty() {
        when(friendService.listFriends(any())).thenReturn(List.of());
        when(quizMapper.selectList(any())).thenReturn(List.of());

        assertThat(quizService.feed(1L)).isEmpty();
    }

    // ---- play ----

    @Test
    void play_friendCanViewWithoutAnswers() {
        when(quizMapper.selectById(5L)).thenReturn(quiz(5L, 2L, "PUBLISHED"));
        when(friendService.isFriend(1L, 2L)).thenReturn(true);
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of(
                question(10L, 5L, "JUDGE", "地球是圆的", 1, 0),
                question(11L, 5L, "CHOICE", "1+1=?", null, 1)));
        when(quizOptionMapper.selectList(any())).thenReturn(List.of(
                option(100L, 11L, "2", 1),
                option(101L, 11L, "3", 0)));

        PlayQuizVO result = quizService.play(1L, 5L);

        assertThat(result.getQuestionCount()).isEqualTo(2);
        assertThat(result.getQuestions().get(0).getOptions()).isEmpty();
        assertThat(result.getQuestions().get(1).getOptions()).hasSize(2);
    }

    @Test
    void play_notFriendThrows403() {
        when(quizMapper.selectById(5L)).thenReturn(quiz(5L, 2L, "PUBLISHED"));
        when(friendService.isFriend(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> quizService.play(1L, 5L))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(403));
    }

    @Test
    void play_draftThrows404() {
        when(quizMapper.selectById(5L)).thenReturn(quiz(5L, 2L, "DRAFT"));

        assertThatThrownBy(() -> quizService.play(1L, 5L))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(404));
    }

    // ---- submit ----

    @Test
    void submit_friendCanAnswer() {
        when(quizMapper.selectById(5L)).thenReturn(quiz(5L, 2L, "PUBLISHED"));
        when(friendService.isFriend(1L, 2L)).thenReturn(true);
        when(quizAnswerMapper.selectOne(any())).thenReturn(null);
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of(
                question(10L, 5L, "JUDGE", "地球是圆的", 1, 0)));
        when(quizOptionMapper.selectList(any())).thenReturn(List.of());
        doAnswer(inv -> {
            QuizAnswer a = inv.getArgument(0);
            a.setId(5L);
            return 1;
        }).when(quizAnswerMapper).insert(any(QuizAnswer.class));

        SubmitAnswerRequest req = new SubmitAnswerRequest();
        AnswerInput judge = new AnswerInput();
        judge.setQuestionId(10L);
        judge.setChosenBool(true);
        req.setAnswers(List.of(judge));

        AnswerResultVO result = quizService.submit(1L, 5L, req);

        assertThat(result.getScore()).isEqualTo(1);
    }

    @Test
    void submit_nonFriendThrows403() {
        when(quizMapper.selectById(5L)).thenReturn(quiz(5L, 2L, "PUBLISHED"));
        when(friendService.isFriend(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> quizService.submit(1L, 5L, new SubmitAnswerRequest()))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(403));
    }

    @Test
    void submit_friendDraftThrows404() {
        when(quizMapper.selectById(5L)).thenReturn(quiz(5L, 2L, "DRAFT"));

        assertThatThrownBy(() -> quizService.submit(1L, 5L, new SubmitAnswerRequest()))
                .isInstanceOfSatisfying(BizException.class, e -> assertThat(e.getCode()).isEqualTo(404));
    }

    @Test
    void submit_scoresMixedQuestions() {
        when(quizMapper.selectById(1L)).thenReturn(quiz(1L, 1L, "PUBLISHED"));
        when(quizAnswerMapper.selectOne(any())).thenReturn(null);
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of(
                question(10L, 1L, "JUDGE", "地球是圆的", 1, 0),
                question(11L, 1L, "CHOICE", "1+1=?", null, 1)));
        when(quizOptionMapper.selectList(any())).thenReturn(List.of(
                option(100L, 11L, "2", 1),
                option(101L, 11L, "3", 0)));
        doAnswer(inv -> {
            QuizAnswer a = inv.getArgument(0);
            a.setId(5L);
            return 1;
        }).when(quizAnswerMapper).insert(any(QuizAnswer.class));

        SubmitAnswerRequest req = new SubmitAnswerRequest();
        req.setAnonymous(false);
        AnswerInput judge = new AnswerInput();
        judge.setQuestionId(10L);
        judge.setChosenBool(true);
        AnswerInput choice = new AnswerInput();
        choice.setQuestionId(11L);
        choice.setOptionId(100L);
        req.setAnswers(List.of(judge, choice));

        AnswerResultVO result = quizService.submit(1L, 1L, req);

        assertThat(result.getScore()).isEqualTo(2);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getItems()).extracting(AnswerItemResultVO::getIsCorrect)
                .containsExactly(true, true);
        verify(quizAnswerMapper).updateById(argThat((QuizAnswer a) -> a.getScore() == 2));
    }

    @Test
    void submit_missingAnswerThrows() {
        when(quizMapper.selectById(1L)).thenReturn(quiz(1L, 1L, "PUBLISHED"));
        when(quizAnswerMapper.selectOne(any())).thenReturn(null);
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of(
                question(10L, 1L, "JUDGE", "a", 1, 0),
                question(11L, 1L, "JUDGE", "b", 0, 0)));
        when(quizOptionMapper.selectList(any())).thenReturn(List.of());
        doAnswer(inv -> {
            QuizAnswer a = inv.getArgument(0);
            a.setId(5L);
            return 1;
        }).when(quizAnswerMapper).insert(any(QuizAnswer.class));

        SubmitAnswerRequest req = new SubmitAnswerRequest();
        AnswerInput judge = new AnswerInput();
        judge.setQuestionId(10L);
        judge.setChosenBool(true);
        req.setAnswers(List.of(judge));

        assertThatThrownBy(() -> quizService.submit(1L, 1L, req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("请完成所有题目");
    }

    @Test
    void submit_ownerRetestDeletesOldAnswer() {
        when(quizMapper.selectById(1L)).thenReturn(quiz(1L, 1L, "PUBLISHED"));
        when(quizAnswerMapper.selectOne(any())).thenReturn(answer(9L, 1L, 1L, 1));
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of(
                question(10L, 1L, "JUDGE", "a", 1, 0)));
        when(quizOptionMapper.selectList(any())).thenReturn(List.of());
        doAnswer(inv -> {
            QuizAnswer a = inv.getArgument(0);
            a.setId(5L);
            return 1;
        }).when(quizAnswerMapper).insert(any(QuizAnswer.class));

        SubmitAnswerRequest req = new SubmitAnswerRequest();
        AnswerInput judge = new AnswerInput();
        judge.setQuestionId(10L);
        judge.setChosenBool(true);
        req.setAnswers(List.of(judge));

        quizService.submit(1L, 1L, req);

        verify(quizAnswerMapper).deleteById(9L);
        verify(quizAnswerItemMapper).delete(any());
    }

    // ---- myAnswer ----

    @Test
    void myAnswer_returnsResult() {
        when(quizAnswerMapper.selectOne(any())).thenReturn(answer(9L, 1L, 1L, 2));
        when(quizAnswerItemMapper.selectList(any())).thenReturn(List.of(
                item(1L, 9L, 10L, null, 1, 1),
                item(2L, 9L, 11L, 100L, null, 1)));
        when(quizQuestionMapper.selectList(any())).thenReturn(List.of(
                question(10L, 1L, "JUDGE", "a", 1, 0),
                question(11L, 1L, "CHOICE", "b", null, 1)));
        when(quizOptionMapper.selectList(any())).thenReturn(List.of(
                option(100L, 11L, "2", 1),
                option(101L, 11L, "3", 0)));

        AnswerResultVO result = quizService.myAnswer(1L, 1L);

        assertThat(result.getScore()).isEqualTo(2);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getItems().get(0).getCorrectBool()).isTrue();
        assertThat(result.getItems().get(1).getCorrectOptionId()).isEqualTo(100L);
    }

    @Test
    void myAnswer_notAnsweredThrows404() {
        when(quizAnswerMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> quizService.myAnswer(1L, 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("你还没有作答");
    }
}
