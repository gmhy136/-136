package com.cloudrecord.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.common.BizException;
import com.cloudrecord.entity.AiUsageLog;
import com.cloudrecord.entity.DailyQuote;
import com.cloudrecord.entity.Mood;
import com.cloudrecord.mapper.AiUsageLogMapper;
import com.cloudrecord.mapper.DailyQuoteMapper;
import com.cloudrecord.mapper.MoodMapper;
import com.cloudrecord.mapper.RecordCardMapper;
import com.cloudrecord.vo.AiRemainingVO;
import com.cloudrecord.vo.DailyQuoteVO;
import com.cloudrecord.vo.StyleRecommendationVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI 能力实现：每场景每日限流 + 计次 + prompt 构建 + 结构化输出容错解析 + 失败降级。
 */
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private static final int DAILY_LIMIT = 20;
    private static final String SCENE_EXPAND = "EXPAND";
    private static final String SCENE_STYLE = "STYLE";
    private static final String SCENE_SPLIT = "IDEAL_SPLIT";
    private static final String SCENE_QUOTE = "DAILY_QUOTE";
    private static final String DEFAULT_STYLE_KEY = "paper-white";
    private static final String FALLBACK_REASON = "随心一点，素白也不错";
    private static final String FALLBACK_QUOTE = "今天也记得对自己好一点。";

    /** 与方案设计文档 §14.3 一致的长 key。 */
    private static final Set<String> STYLE_KEYS = Set.of(
            "warm-sunny", "quiet-blue", "mint-fresh", "sakura-pink", "night-purple", "paper-white");

    private static final String SYSTEM_PROMPT =
            "你是『云记录』的温柔 AI 助手。语气温和、真诚、不浮夸、不评判，帮助用户记录生活和规划理想。回答用简体中文，口语自然，像朋友一样。";

    private final DeepSeekClient deepSeekClient;
    private final AiUsageLogMapper aiUsageLogMapper;
    private final MoodMapper moodMapper;
    private final RecordCardMapper recordCardMapper;
    private final DailyQuoteMapper dailyQuoteMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ExpandResult expand(Long userId, String content, Integer moodId) {
        int remaining = checkAndCount(userId, SCENE_EXPAND);
        String moodName = resolveMoodName(moodId);
        String userPrompt = buildExpandPrompt(content, moodName);
        Flux<String> deltas = deepSeekClient.stream(SYSTEM_PROMPT, userPrompt, AiParams.expand());
        return new ExpandResult(deltas, remaining);
    }

    @Override
    public StyleRecommendationVO recommendStyle(Long userId, String content, Integer moodId) {
        int remaining = checkAndCount(userId, SCENE_STYLE);
        String moodName = resolveMoodName(moodId);
        String userPrompt = buildStylePrompt(content, moodName);
        try {
            String raw = deepSeekClient.call(SYSTEM_PROMPT, userPrompt, AiParams.style());
            return parseStyle(raw, remaining);
        } catch (Exception e) {
            return fallbackStyle(remaining);
        }
    }

    @Override
    public SplitIdealResult splitIdeal(Long userId, String title, String description) {
        int remaining = checkAndCount(userId, SCENE_SPLIT);
        String userPrompt = buildSplitPrompt(title, description);
        try {
            String raw = deepSeekClient.call(SYSTEM_PROMPT, userPrompt, AiParams.split());
            return new SplitIdealResult(parseTasks(raw), remaining);
        } catch (Exception e) {
            return new SplitIdealResult(List.of(), remaining);
        }
    }

    @Override
    public AiRemainingVO remaining(Long userId) {
        int min = DAILY_LIMIT;
        for (String scene : List.of(SCENE_EXPAND, SCENE_STYLE, SCENE_SPLIT, SCENE_QUOTE)) {
            long count = aiUsageLogMapper.selectCount(new LambdaQueryWrapper<AiUsageLog>()
                    .eq(AiUsageLog::getUserId, userId)
                    .eq(AiUsageLog::getScene, scene)
                    .ge(AiUsageLog::getCreatedAt, LocalDate.now().atStartOfDay()));
            min = Math.min(min, (int) (DAILY_LIMIT - count));
        }
        return new AiRemainingVO(DAILY_LIMIT, Math.max(0, min));
    }

    @Override
    public DailyQuoteVO dailyQuote(Long userId) {
        LocalDate today = LocalDate.now();
        DailyQuote existing = dailyQuoteMapper.selectOne(new LambdaQueryWrapper<DailyQuote>()
                .eq(DailyQuote::getUserId, userId)
                .eq(DailyQuote::getQuoteDate, today));
        if (existing != null) {
            return DailyQuoteVO.builder().content(existing.getContent()).build();
        }

        checkAndCount(userId, SCENE_QUOTE);
        String content = FALLBACK_QUOTE;
        try {
            String raw = deepSeekClient.call(SYSTEM_PROMPT, buildQuotePrompt(recentMoodSummary(userId)), AiParams.quote());
            if (raw != null && !raw.isBlank()) {
                content = raw.trim();
                if (content.length() > 120) {
                    content = content.substring(0, 120);
                }
            }
        } catch (Exception ignored) {
            // 失败降级为固定句
        }

        DailyQuote quote = new DailyQuote();
        quote.setUserId(userId);
        quote.setQuoteDate(today);
        quote.setContent(content);
        dailyQuoteMapper.insert(quote);
        return DailyQuoteVO.builder().content(content).build();
    }

    /** 校验今日额度并计次；超限抛 429。返回本次调用后的剩余次数。 */
    private int checkAndCount(Long userId, String scene) {
        long todayCount = aiUsageLogMapper.selectCount(new LambdaQueryWrapper<AiUsageLog>()
                .eq(AiUsageLog::getUserId, userId)
                .eq(AiUsageLog::getScene, scene)
                .ge(AiUsageLog::getCreatedAt, LocalDate.now().atStartOfDay()));
        if (todayCount >= DAILY_LIMIT) {
            throw BizException.tooManyRequests("今日 AI 次数已用完，明天再来～");
        }
        AiUsageLog log = new AiUsageLog();
        log.setUserId(userId);
        log.setScene(scene);
        aiUsageLogMapper.insert(log);
        return (int) (DAILY_LIMIT - todayCount - 1);
    }

    private String resolveMoodName(Integer moodId) {
        if (moodId == null) {
            return null;
        }
        Mood mood = moodMapper.selectById(moodId);
        return mood == null ? null : mood.getName();
    }

    /** 用户最近几条记录的心情名（去重、顿号拼接），供今日一句参考；无则返回 null。 */
    private String recentMoodSummary(Long userId) {
        List<Integer> moodIds = recordCardMapper.selectRecentMoodIds(userId, 5);
        if (moodIds.isEmpty()) {
            return null;
        }
        Map<Integer, Mood> map = moodMap();
        List<String> names = new ArrayList<>();
        for (Integer moodId : moodIds) {
            Mood mood = map.get(moodId);
            if (mood != null && !names.contains(mood.getName())) {
                names.add(mood.getName());
            }
        }
        return names.isEmpty() ? null : String.join("、", names);
    }

    private Map<Integer, Mood> moodMap() {
        Map<Integer, Mood> map = new HashMap<>();
        for (Mood mood : moodMapper.selectList(null)) {
            map.put(mood.getId(), mood);
        }
        return map;
    }

    private String buildQuotePrompt(String moodSummary) {
        if (moodSummary == null) {
            return "请写一句温柔、真诚、不鸡汤的话，鼓励用户记录今天的生活，30 字以内。"
                    + "直接输出这句话本身，不要引号，不要任何解释。";
        }
        return "用户最近的心情是：" + moodSummary + "。请结合这些心情，写一句温柔、真诚、不鸡汤的话，"
                + "鼓励用户记录今天的生活，30 字以内。直接输出这句话本身，不要引号，不要任何解释。";
    }

    private String buildExpandPrompt(String content, String moodName) {
        StringBuilder sb = new StringBuilder("用户写了一段生活记录");
        if (moodName != null) {
            sb.append("，心情是「").append(moodName).append("」");
        }
        sb.append("。\n请在保留原意和语气的基础上，把这段话扩写/润色得更生动自然，")
          .append("适当补充细节和感受，但不要编造事实，长度控制在原文的 1.5~2 倍。")
          .append("直接输出润色后的文本，不要任何解释。\n\n原文：\n")
          .append(content);
        return sb.toString();
    }

    private String buildStylePrompt(String content, String moodName) {
        StringBuilder sb = new StringBuilder("根据用户");
        if (moodName != null) {
            sb.append("的心情「").append(moodName).append("」和");
        }
        sb.append("记录内容，从以下风格中推荐最合适的一个卡片风格，并给出 5~15 字的推荐理由。\n")
          .append("风格列表：warm-sunny(暖阳橙)、quiet-blue(静谧蓝)、mint-fresh(薄荷绿)、sakura-pink(樱花粉)、night-purple(夜空紫)、paper-white(素白)。\n\n")
          .append("严格按 JSON 输出：{\"style_key\":\"...\",\"reason\":\"...\"}，不要输出其他内容。\n\n")
          .append("记录内容：").append(content);
        return sb.toString();
    }

    private String buildSplitPrompt(String title, String description) {
        StringBuilder sb = new StringBuilder("用户的理想是「").append(title).append("」");
        if (description != null && !description.isBlank()) {
            sb.append("，描述：").append(description);
        }
        sb.append("。\n请把这个理想拆解成 5~8 个可执行的小步骤/每日任务，")
          .append("每个任务具体、可衡量、适合每天完成。")
          .append("严格按 JSON 数组输出：[{\"content\":\"任务内容\"}, ...]，不要输出其他内容。");
        return sb.toString();
    }

    /** 解析模型输出的 JSON 数组，失败/非法返回空列表（降级铁律）。 */
    private List<String> parseTasks(String raw) {
        String json = extractJsonArray(raw);
        if (json.isEmpty()) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                return List.of();
            }
            List<String> tasks = new ArrayList<>();
            for (JsonNode item : node) {
                String content = item.path("content").asText(null);
                if (content != null && !content.isBlank()) {
                    tasks.add(content.trim());
                }
            }
            return tasks;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String extractJsonArray(String raw) {
        if (raw == null) {
            return "";
        }
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return "";
    }

    /** 解析模型输出，非法/未知风格回退默认风格（降级铁律）。 */
    private StyleRecommendationVO parseStyle(String raw, int remaining) {
        String json = extractJsonObject(raw);
        if (json.isEmpty()) {
            return fallbackStyle(remaining);
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            String styleKey = node.path("style_key").asText(null);
            String reason = node.path("reason").asText(null);
            if (styleKey != null && STYLE_KEYS.contains(styleKey)) {
                return new StyleRecommendationVO(styleKey, reason == null ? "" : reason, remaining);
            }
        } catch (Exception ignored) {
            // 解析失败走回退
        }
        return fallbackStyle(remaining);
    }

    private String extractJsonObject(String raw) {
        if (raw == null) {
            return "";
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return "";
    }

    private StyleRecommendationVO fallbackStyle(int remaining) {
        return new StyleRecommendationVO(DEFAULT_STYLE_KEY, FALLBACK_REASON, remaining);
    }
}
