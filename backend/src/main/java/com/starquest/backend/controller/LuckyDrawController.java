package com.starquest.backend.controller;

import com.starquest.backend.model.LuckyDrawRecord;
import com.starquest.backend.service.LuckyDrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lucky-draw")
@RequiredArgsConstructor
public class LuckyDrawController {

    private final LuckyDrawService luckyDrawService;

    @PostMapping("/draw")
    public ResponseEntity<LuckyDrawService.LuckyDrawResult> performDraw(@RequestParam Long kidId) {
        LuckyDrawService.LuckyDrawResult result = luckyDrawService.performLuckyDraw(kidId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history/{kidId}")
    public ResponseEntity<List<LuckyDrawRecord>> getDrawHistory(@PathVariable Long kidId) {
        List<LuckyDrawRecord> history = luckyDrawService.getDrawHistory(kidId);
        return ResponseEntity.ok(history);
    }
}
