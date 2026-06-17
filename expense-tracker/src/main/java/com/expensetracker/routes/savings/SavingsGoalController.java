package com.expensetracker.routes.savings;

import com.expensetracker.dao.savings.SavingsGoalDao;
import com.expensetracker.model.SavingsGoal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/savings/goals")
@Validated
@Tag(name = "Savings Goals", description = "Savings Goal Tracking APIs")
public class SavingsGoalController {
    
    @Autowired
    private SavingsGoalDao savingsGoalDao;
    
    record UpdateGoalRequest(
        @Min(value = 0, message = "Current amount must be positive") Double currentAmount,
        String goalName,
        @Min(value = 0, message = "Target amount must be positive") Double targetAmount
    ) {}
    
    @PostMapping
    @Operation(summary = "Create a new savings goal")
    public ResponseEntity<SavingsGoal> createGoal(@Valid @RequestBody SavingsGoal goal) {
        return ResponseEntity.ok(savingsGoalDao.createGoal(goal));
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get all savings goals with progress tracking")
    public ResponseEntity<List<Map<String, Object>>> getGoals(@PathVariable String userId) {
        return ResponseEntity.ok(savingsGoalDao.getGoals(userId));
    }
    
    @PutMapping("/{goalId}")
    @Operation(summary = "Update savings goal progress or details")
    public ResponseEntity<SavingsGoal> updateGoal(
        @PathVariable String goalId,
        @RequestParam String userId,
        @Valid @RequestBody UpdateGoalRequest req
    ) {
        return ResponseEntity.ok(savingsGoalDao.updateGoal(goalId, userId, req.currentAmount(), req.goalName(), req.targetAmount()));
    }
}
