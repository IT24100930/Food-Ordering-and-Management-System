package com.example.smart_food_system.controller;

import com.example.smart_food_system.service.TrustScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/trust")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TrustScoreController {

    private final TrustScoreService trustScoreService;

    // GET /api/trust/profile/{userId}
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(trustScoreService.getTrustProfile(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // GET /api/trust/score-history/{userId}
    @GetMapping("/score-history/{userId}")
    public ResponseEntity<?> getScoreHistory(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "history", trustScoreService.getScoreHistory(userId)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // GET /api/trust/role-history/{userId}
    // All role changes for one user
    @GetMapping("/role-history/{userId}")
    public ResponseEntity<?> getRoleHistory(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "history", trustScoreService.getRoleHistory(userId)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // GET /api/trust/pending-promotions
    @GetMapping("/pending-promotions")
    public ResponseEntity<?> getPendingPromotions() {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "promotions", trustScoreService.getPendingPromotions()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // POST /api/trust/event
    @PostMapping("/event")
    public ResponseEntity<?> recordEvent(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String event = body.get("event").toString();
            double value = body.containsKey("value")
                ? Double.parseDouble(body.get("value").toString()) : 0;

            Object result = switch (event) {
                case "ORDER_COMPLETE"  -> trustScoreService.recordOrderCompleted(userId, value);
                case "CANCELLATION"    -> trustScoreService.recordCancellation(userId);
                case "COMPLAINT"       -> trustScoreService.recordComplaint(userId);
                case "TASK_DONE"       -> trustScoreService.recordTaskCompleted(userId);
                case "GOOD_RATING"     -> trustScoreService.recordGoodRating(userId, value);
                default -> throw new RuntimeException("Unknown event: " + event);
            };

            return ResponseEntity.ok(Map.of("success", true, "message", "Event recorded", "user", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // POST /api/trust/admin-adjust
    @PostMapping("/admin-adjust")
    public ResponseEntity<?> adminAdjust(@RequestBody Map<String, Object> body) {
        try {
            Long   userId    = Long.valueOf(body.get("userId").toString());
            String direction = body.get("direction").toString();
            String reason    = body.get("reason").toString();

            Object result = trustScoreService.adminAdjustScore(userId, direction, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "Score adjusted", "user", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // POST /api/trust/approve-promotion
    @PostMapping("/approve-promotion")
    public ResponseEntity<?> approvePromotion(@RequestBody Map<String, Object> body) {
        try {
            Long   userId    = Long.valueOf(body.get("userId").toString());
            String newRole   = body.get("newRole").toString();
            String adminName = body.get("adminName").toString();

            Object result = trustScoreService.approvePromotion(userId, newRole, adminName);
            return ResponseEntity.ok(Map.of("success", true, "message", "Promotion approved!", "user", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}