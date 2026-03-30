package com.example.smart_food_system.service;

import com.example.smart_food_system.model.*;
import com.example.smart_food_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrustScoreService {

    private final UserRepository        userRepository;
    private final RoleHistoryRepository roleHistoryRepository;
    private final TrustScoreLogRepository trustLogRepository;

    // ── SCORE CHANGE VALUES 
    private static final int POINTS_ORDER_COMPLETE  = +5;
    private static final int POINTS_CANCELLATION    = -8;
    private static final int POINTS_COMPLAINT       = -12;
    private static final int POINTS_TASK_DONE       = +4;
    private static final int POINTS_GOOD_RATING     = +6;
    private static final int POINTS_MANUAL_POSITIVE = +10;
    private static final int POINTS_MANUAL_NEGATIVE = -10;

    // ── PROMOTION THRESHOLDS 
    // Customer promotion: needs all 3
    private static final int    CUSTOMER_MIN_ORDERS   = 20;
    private static final double CUSTOMER_MIN_SPENDING  = 15000.0;
    private static final int    CUSTOMER_MIN_TRUST     = 75;

    // Staff promotion: needs all 3
    private static final int    STAFF_MIN_TASKS        = 100;
    private static final double STAFF_MIN_RATING       = 4.5;
    private static final int    STAFF_MIN_TRUST        = 80;
    private static final int    STAFF_MAX_COMPLAINTS   = 0; // zero complaints

    // Restriction threshold
    private static final int RESTRICT_THRESHOLD = 40;
    // ─────────────────────────────────────────────────

    // ══════════════════════════════════════════════════
    // 1. ADJUST TRUST SCORE
    // ══════════════════════════════════════════════════
    @Transactional
    public User adjustScore(Long userId, int points, String reason, String actionType) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int oldScore = user.getTrustScore();
        // Keep score within 0–100
        int newScore = Math.min(100, Math.max(0, oldScore + points));

        user.setTrustScore(newScore);
        user.setTrustLevel(User.calcTrustLevel(newScore));

        // Restrict if score falls below threshold
        user.setIsRestricted(newScore < RESTRICT_THRESHOLD);

        userRepository.save(user);

        // Log the change
        trustLogRepository.save(TrustScoreLog.builder()
                .userId(userId)
                .oldScore(oldScore)
                .newScore(newScore)
                .changeBy(points)
                .reason(reason)
                .actionType(actionType)
                .build());

        log.info("Trust score for user {} changed: {} → {} ({})", userId, oldScore, newScore, reason);

        // After every score change, check if promotion/demotion triggered
        checkAndApplyPromotion(user);

        return user;
    }

    // ══════════════════════════════════════════════════
    // 2. RECORD EVENTS — called from other modules
    // ══════════════════════════════════════════════════

    public User recordOrderCompleted(Long userId, double orderAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTotalOrders(user.getTotalOrders() + 1);
        user.setTotalSpending(user.getTotalSpending().add(
            java.math.BigDecimal.valueOf(orderAmount)));
        userRepository.save(user);
        return adjustScore(userId, POINTS_ORDER_COMPLETE,
            "Order completed (Rs. " + (int)orderAmount + ")", "ORDER_COMPLETE");
    }

    public User recordCancellation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setCancellations(user.getCancellations() + 1);
        userRepository.save(user);
        return adjustScore(userId, POINTS_CANCELLATION,
            "Order cancelled by user", "CANCELLATION");
    }

    public User recordComplaint(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setComplaintsCount(user.getComplaintsCount() + 1);
        userRepository.save(user);
        return adjustScore(userId, POINTS_COMPLAINT,
            "Complaint filed against user", "COMPLAINT");
    }

    public User recordTaskCompleted(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setCompletedTasks(user.getCompletedTasks() + 1);
        userRepository.save(user);
        return adjustScore(userId, POINTS_TASK_DONE,
            "Task completed successfully", "TASK_DONE");
    }

    public User recordGoodRating(Long userId, double rating) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPerformanceRating(rating);
        userRepository.save(user);
        return adjustScore(userId, POINTS_GOOD_RATING,
            "Received good rating: " + rating, "GOOD_RATING");
    }

    // Admin manually adjusts score
    public User adminAdjustScore(Long userId, String direction, String reason) {
        int pts = direction.equals("UP") ? POINTS_MANUAL_POSITIVE : POINTS_MANUAL_NEGATIVE;
        return adjustScore(userId, pts, "Admin manual: " + reason, "ADMIN_ADJUST");
    }

    // ══════════════════════════════════════════════════
    // 3. PROMOTION ENGINE — checks criteria and acts
    // ══════════════════════════════════════════════════
    @Transactional
    public void checkAndApplyPromotion(User user) {
        if (user.getRole() == User.Role.CUSTOMER) {
            checkCustomerPromotion(user);
        } else if (user.getRole() == User.Role.STAFF) {
            checkStaffPromotion(user);
        }
    }

    private void checkCustomerPromotion(User user) {
        boolean meetsOrders   = user.getTotalOrders()   >= CUSTOMER_MIN_ORDERS;
        boolean meetsSpending = user.getTotalSpending().doubleValue() >= CUSTOMER_MIN_SPENDING;
        boolean meetsTrust    = user.getTrustScore()    >= CUSTOMER_MIN_TRUST;

        if (meetsOrders && meetsSpending && meetsTrust) {
            // Customer qualifies — save promotion as pending (Admin approves)
            saveRoleHistory(user, "CUSTOMER", "PREMIUM_CUSTOMER", "SYSTEM",
                String.format("Auto-promotion: %d orders, Rs.%.0f spending, %d trust score",
                    user.getTotalOrders(),
                    user.getTotalSpending().doubleValue(),
                    user.getTrustScore()));
            log.info("PROMOTION PENDING: User {} qualifies for PREMIUM_CUSTOMER", user.getId());
        }
    }

    private void checkStaffPromotion(User user) {
        boolean meetsTasks      = user.getCompletedTasks()    >= STAFF_MIN_TASKS;
        boolean meetsRating     = user.getPerformanceRating() >= STAFF_MIN_RATING;
        boolean meetsTrust      = user.getTrustScore()        >= STAFF_MIN_TRUST;
        boolean meetsComplaints = user.getComplaintsCount()   <= STAFF_MAX_COMPLAINTS;

        if (meetsTasks && meetsRating && meetsTrust && meetsComplaints) {
            saveRoleHistory(user, "STAFF", "SENIOR_STAFF", "SYSTEM",
                String.format("Auto-promotion: %d tasks, %.1f rating, %d trust score, %d complaints",
                    user.getCompletedTasks(),
                    user.getPerformanceRating(),
                    user.getTrustScore(),
                    user.getComplaintsCount()));
            log.info("PROMOTION PENDING: User {} qualifies for SENIOR_STAFF", user.getId());
        }
    }

    // Admin approves a pending promotion
    @Transactional
    public User approvePromotion(Long userId, String newRole, String adminName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String oldRole = user.getRole().name();

        // Apply the role change
        try {
            user.setRole(User.Role.valueOf(newRole));
        } catch (IllegalArgumentException e) {
            // newRole might be PREMIUM_CUSTOMER or SENIOR_STAFF — keep as note
            log.info("Promoted to sub-role: {}", newRole);
        }

        userRepository.save(user);

        saveRoleHistory(user, oldRole, newRole, "ADMIN:" + adminName,
            "Admin approved promotion to " + newRole);

        log.info("Admin {} approved promotion: user {} → {}", adminName, userId, newRole);
        return user;
    }

    // ══════════════════════════════════════════════════
    // 4. READ DATA
    // ══════════════════════════════════════════════════

    public List<TrustScoreLog> getScoreHistory(Long userId) {
        return trustLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<RoleHistory> getRoleHistory(Long userId) {
        return roleHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<RoleHistory> getPendingPromotions() {
        return roleHistoryRepository.findByChangedByOrderByCreatedAtDesc("SYSTEM");
    }

    // Full profile with trust data
    public Map<String, Object> getTrustProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean promotionReady = false;
        String  promotionMsg   = "";

        if (user.getRole() == User.Role.CUSTOMER) {
            promotionReady =
                user.getTotalOrders()   >= CUSTOMER_MIN_ORDERS &&
                user.getTotalSpending().doubleValue() >= CUSTOMER_MIN_SPENDING &&
                user.getTrustScore()    >= CUSTOMER_MIN_TRUST;
            promotionMsg = promotionReady ? "Eligible for Premium Customer upgrade!" : "";
        } else if (user.getRole() == User.Role.STAFF) {
            promotionReady =
                user.getCompletedTasks()    >= STAFF_MIN_TASKS &&
                user.getPerformanceRating() >= STAFF_MIN_RATING &&
                user.getTrustScore()        >= STAFF_MIN_TRUST &&
                user.getComplaintsCount()   <= STAFF_MAX_COMPLAINTS;
            promotionMsg = promotionReady ? "Eligible for Senior Staff promotion!" : "";
        }

        int progress = calcPromotionProgress(user);

        Map<String, Object> result = new HashMap<>();
        result.put("userId",            user.getId());
        result.put("name",              user.getFirstName() + " " + user.getLastName());
        result.put("email",             user.getEmail());
        result.put("role",              user.getRole().name());
        result.put("trustScore",        user.getTrustScore());
        result.put("trustLevel",        user.getTrustLevel());
        result.put("isRestricted",      user.getIsRestricted());
        result.put("totalOrders",       user.getTotalOrders());
        result.put("totalSpending",     user.getTotalSpending());
        result.put("completedTasks",    user.getCompletedTasks());
        result.put("performanceRating", user.getPerformanceRating());
        result.put("complaintsCount",   user.getComplaintsCount());
        result.put("cancellations",     user.getCancellations());
        result.put("promotionReady",    promotionReady);
        result.put("promotionMsg",      promotionMsg);
        result.put("promotionProgress", progress);
        result.put("scoreHistory",      getScoreHistory(userId));
        result.put("roleHistory",       getRoleHistory(userId));
        return result;
    }

    // ══════════════════════════════════════════════════
    // 5. HELPER
    // ══════════════════════════════════════════════════

    private void saveRoleHistory(User user, String oldRole, String newRole,
                                  String changedBy, String reason) {
        roleHistoryRepository.save(RoleHistory.builder()
                .userId(user.getId())
                .oldRole(oldRole)
                .newRole(newRole)
                .changedBy(changedBy)
                .reason(reason)
                .trustScoreAt(user.getTrustScore())
                .build());
    }

    private int calcPromotionProgress(User user) {
        if (user.getRole() == User.Role.CUSTOMER) {
            int orderPct   = Math.min(100, user.getTotalOrders() * 100 / CUSTOMER_MIN_ORDERS);
            int spendPct   = Math.min(100, (int)(user.getTotalSpending().doubleValue() * 100 / CUSTOMER_MIN_SPENDING));
            int trustPct   = Math.min(100, user.getTrustScore() * 100 / CUSTOMER_MIN_TRUST);
            return (orderPct + spendPct + trustPct) / 3;
        } else if (user.getRole() == User.Role.STAFF) {
            int taskPct    = Math.min(100, user.getCompletedTasks() * 100 / STAFF_MIN_TASKS);
            int ratingPct  = (int)(Math.min(1.0, user.getPerformanceRating() / STAFF_MIN_RATING) * 100);
            int trustPct   = Math.min(100, user.getTrustScore() * 100 / STAFF_MIN_TRUST);
            return (taskPct + ratingPct + trustPct) / 3;
        }
        return 100; // Admin always 100%
    }
}