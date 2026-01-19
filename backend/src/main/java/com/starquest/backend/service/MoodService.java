package com.starquest.backend.service;

import com.starquest.backend.model.MoodLog;
import com.starquest.backend.repository.MoodLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MoodService {

    private final MoodLogRepository moodLogRepository;

    @Transactional
    public MoodLog recordMood(Long kidId, MoodLog.MoodType moodType, String note) {
        // prevent more than one mood log per kid per calendar day
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(java.time.LocalTime.MAX);
        java.util.List<MoodLog> todays = moodLogRepository.findByKidIdAndCreateTimeBetween(kidId, startOfDay, endOfDay);
        if (todays != null && !todays.isEmpty()) {
            throw new RuntimeException("今天已打卡");
        }

        MoodLog moodLog = new MoodLog();
        moodLog.setKidId(kidId);
        moodLog.setMoodType(moodType);
        moodLog.setNote(note);
        return moodLogRepository.save(moodLog);
    }

    public List<MoodLog> getMoodHistory(Long kidId) {
        return moodLogRepository.findByKidIdOrderByCreateTimeDesc(kidId);
    }

    public List<MoodLog> getMoodHistoryByDateRange(Long kidId, LocalDateTime start, LocalDateTime end) {
        return moodLogRepository.findByKidIdAndCreateTimeBetween(kidId, start, end);
    }
}
