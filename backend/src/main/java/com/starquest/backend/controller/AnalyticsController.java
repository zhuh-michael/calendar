package com.starquest.backend.controller;

import com.starquest.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/diligence/{kidId}")
    public ResponseEntity<List<AnalyticsService.DiligenceDataPoint>> getDiligenceCurve(@PathVariable Long kidId) {
        List<AnalyticsService.DiligenceDataPoint> data = analyticsService.getDiligenceCurve(kidId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/wealth/{kidId}")
    public ResponseEntity<AnalyticsService.WealthDistributionData> getWealthDistribution(@PathVariable Long kidId) {
        AnalyticsService.WealthDistributionData data = analyticsService.getWealthDistribution(kidId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/completion-rate/{kidId}")
    public ResponseEntity<AnalyticsService.TaskCompletionRate> getTodayTaskCompletionRate(@PathVariable Long kidId) {
        AnalyticsService.TaskCompletionRate data = analyticsService.getTodayTaskCompletionRate(kidId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/kids-stats")
    public ResponseEntity<List<AnalyticsService.KidStats>> getKidsStats() {
        List<AnalyticsService.KidStats> data = analyticsService.getKidsStats();
        return ResponseEntity.ok(data);
    }
}
