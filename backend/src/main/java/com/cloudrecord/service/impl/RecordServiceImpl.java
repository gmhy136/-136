package com.cloudrecord.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudrecord.common.BizException;
import com.cloudrecord.dto.CreateRecordRequest;
import com.cloudrecord.dto.UpdateRecordRequest;
import com.cloudrecord.entity.CardImage;
import com.cloudrecord.entity.Mood;
import com.cloudrecord.entity.RecordCard;
import com.cloudrecord.mapper.CardImageMapper;
import com.cloudrecord.mapper.MoodMapper;
import com.cloudrecord.mapper.RecordCardMapper;
import com.cloudrecord.service.RecordService;
import com.cloudrecord.vo.PageVO;
import com.cloudrecord.vo.RecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private static final int MAX_IMAGES = 3;

    private final RecordCardMapper recordCardMapper;
    private final CardImageMapper cardImageMapper;
    private final MoodMapper moodMapper;

    @Override
    public PageVO<RecordVO> list(Long userId, long page, long size) {
        Page<RecordCard> p = new Page<>(page, size);
        Page<RecordCard> result = recordCardMapper.selectPage(p, new LambdaQueryWrapper<RecordCard>()
                .eq(RecordCard::getUserId, userId)
                .orderByDesc(RecordCard::getCreatedAt));

        Map<Integer, Mood> moods = moodMap();
        Map<Long, List<String>> images = imagesMap(
                result.getRecords().stream().map(RecordCard::getId).toList());

        List<RecordVO> vos = result.getRecords().stream()
                .map(c -> toVO(c, c.getMoodId() != null ? moods.get(c.getMoodId()) : null,
                        images.getOrDefault(c.getId(), List.of())))
                .toList();
        return new PageVO<>(vos, result.getTotal(), page, size, result.getTotal() > page * size);
    }

    @Override
    public RecordVO get(Long userId, Long id) {
        RecordCard card = requireOwned(userId, id);
        Mood mood = card.getMoodId() != null ? moodMapper.selectById(card.getMoodId()) : null;
        return toVO(card, mood, imagesOf(id));
    }

    @Override
    @Transactional
    public RecordVO create(Long userId, CreateRecordRequest request) {
        List<String> urls = request.getImageUrls() == null ? List.of() : request.getImageUrls();
        validateImages(urls);
        Mood mood = requireMood(request.getMoodId());

        RecordCard card = new RecordCard();
        card.setUserId(userId);
        card.setContent(request.getContent());
        card.setMoodId(request.getMoodId());
        card.setStyleKey(request.getStyleKey());
        card.setAiType(request.getAiType());
        recordCardMapper.insert(card);

        if (!urls.isEmpty()) {
            insertImages(card.getId(), urls);
        }
        return toVO(card, mood, urls);
    }

    @Override
    @Transactional
    public RecordVO update(Long userId, Long id, UpdateRecordRequest request) {
        RecordCard card = requireOwned(userId, id);
        List<String> urls = request.getImageUrls() == null ? List.of() : request.getImageUrls();
        validateImages(urls);
        Mood mood = requireMood(request.getMoodId());

        card.setContent(request.getContent());
        card.setMoodId(request.getMoodId());
        card.setStyleKey(request.getStyleKey());
        card.setAiType(request.getAiType());
        recordCardMapper.updateById(card);

        // 整体替换图片
        cardImageMapper.delete(new LambdaQueryWrapper<CardImage>().eq(CardImage::getCardId, id));
        if (!urls.isEmpty()) {
            insertImages(id, urls);
        }
        return toVO(card, mood, urls);
    }

    @Override
    public void delete(Long userId, Long id) {
        requireOwned(userId, id);
        recordCardMapper.deleteById(id);
    }

    private RecordCard requireOwned(Long userId, Long id) {
        RecordCard card = recordCardMapper.selectOne(new LambdaQueryWrapper<RecordCard>()
                .eq(RecordCard::getId, id)
                .eq(RecordCard::getUserId, userId));
        if (card == null) {
            throw BizException.notFound("记录不存在");
        }
        return card;
    }

    private Mood requireMood(Integer moodId) {
        if (moodId == null) {
            return null;
        }
        Mood mood = moodMapper.selectById(moodId);
        if (mood == null) {
            throw BizException.badRequest("心情不存在");
        }
        return mood;
    }

    private void validateImages(List<String> urls) {
        if (urls.size() > MAX_IMAGES) {
            throw BizException.badRequest("最多上传 3 张图片");
        }
    }

    private void insertImages(Long cardId, List<String> urls) {
        for (int i = 0; i < urls.size(); i++) {
            CardImage image = new CardImage();
            image.setCardId(cardId);
            image.setUrl(urls.get(i));
            image.setSortOrder(i);
            cardImageMapper.insert(image);
        }
    }

    private Map<Integer, Mood> moodMap() {
        Map<Integer, Mood> map = new HashMap<>();
        for (Mood mood : moodMapper.selectList(null)) {
            map.put(mood.getId(), mood);
        }
        return map;
    }

    private Map<Long, List<String>> imagesMap(List<Long> cardIds) {
        if (cardIds.isEmpty()) {
            return Map.of();
        }
        List<CardImage> images = cardImageMapper.selectList(new LambdaQueryWrapper<CardImage>()
                .in(CardImage::getCardId, cardIds)
                .orderByAsc(CardImage::getSortOrder));
        Map<Long, List<String>> map = new HashMap<>();
        for (CardImage image : images) {
            map.computeIfAbsent(image.getCardId(), k -> new ArrayList<>()).add(image.getUrl());
        }
        return map;
    }

    private List<String> imagesOf(Long cardId) {
        return cardImageMapper.selectList(new LambdaQueryWrapper<CardImage>()
                        .eq(CardImage::getCardId, cardId)
                        .orderByAsc(CardImage::getSortOrder))
                .stream()
                .map(CardImage::getUrl)
                .toList();
    }

    private RecordVO toVO(RecordCard card, Mood mood, List<String> urls) {
        return RecordVO.builder()
                .id(card.getId())
                .content(card.getContent())
                .moodId(card.getMoodId())
                .moodName(mood != null ? mood.getName() : null)
                .moodIcon(mood != null ? mood.getWeatherIcon() : null)
                .styleKey(card.getStyleKey())
                .aiType(card.getAiType())
                .imageUrls(urls)
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }
}
