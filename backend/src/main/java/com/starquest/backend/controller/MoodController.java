package com.starquest.backend.controller;

import com.starquest.backend.model.MoodLog;
import com.starquest.backend.service.MoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
public class MoodController {

    private final MoodService moodService;

    @PostMapping
    public ResponseEntity<MoodLog> recordMood(@RequestParam Long kidId,
                                             @RequestParam MoodLog.MoodType moodType,
                                             @RequestParam(required = false) String note) {
        MoodLog moodLog = moodService.recordMood(kidId, moodType, note);
        return ResponseEntity.ok(moodLog);
    }

    @GetMapping("/kid/{kidId}")
    public ResponseEntity<List<MoodLog>> getMoodHistory(@PathVariable Long kidId) {
        List<MoodLog> history = moodService.getMoodHistory(kidId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/kid/{kidId}/range")
    public ResponseEntity<List<MoodLog>> getMoodHistoryByDateRange(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<MoodLog> history = moodService.getMoodHistoryByDateRange(kidId, start, end);
        return ResponseEntity.ok(history);
    }
}
