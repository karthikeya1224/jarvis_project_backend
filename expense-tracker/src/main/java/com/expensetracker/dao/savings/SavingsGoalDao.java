package com.expensetracker.dao.savings;

import com.expensetracker.model.SavingsGoal;
import com.expensetracker.repository.SavingsGoalRepository;
import com.expensetracker.sharedlibs.logger.AppLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SavingsGoalDao {
    
    private static final Logger logger = AppLogger.getLogger(SavingsGoalDao.class);
    
    @Autowired
    private SavingsGoalRepository savingsGoalRepository;
    
    public SavingsGoal createGoal(SavingsGoal goal) {
        AppLogger.logInfo(logger, "createGoal", "Creating savings goal for userId: " + goal.getUserId());
        
        if (goal.getTargetAmount() == null || goal.getTargetAmount() <= 0) {
            throw new RuntimeException("Target amount must be positive");
        }
        
        if (goal.getGoalName() == null || goal.getGoalName().isBlank()) {
            throw new RuntimeException("Goal name is required");
        }
        
        SavingsGoal saved = savingsGoalRepository.save(goal);
        AppLogger.logInfo(logger, "createGoal", "Savings goal created successfully with id: " + saved.getId());
        return saved;
    }
    
    public List<Map<String, Object>> getGoals(String userId) {
        AppLogger.logInfo(logger, "getGoals", "Fetching savings goals for userId: " + userId);
        
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);
        
        List<Map<String, Object>> goalsWithProgress = goals.stream().map(goal -> {
            Map<String, Object> goalData = new HashMap<>();
            goalData.put("id", goal.getId());
            goalData.put("goalName", goal.getGoalName());
            goalData.put("targetAmount", goal.getTargetAmount());
            goalData.put("currentAmount", goal.getCurrentAmount());
            goalData.put("targetDate", goal.getTargetDate());
            
            double progress = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;
            goalData.put("progress", Math.min(progress, 100));
            goalData.put("remaining", goal.getTargetAmount() - goal.getCurrentAmount());
            goalData.put("status", progress >= 100 ? "COMPLETED" : progress >= 75 ? "NEAR_COMPLETION" : "IN_PROGRESS");
            
            return goalData;
        }).collect(Collectors.toList());
        
        AppLogger.logInfo(logger, "getGoals", "Found " + goals.size() + " savings goals");
        return goalsWithProgress;
    }
    
    public SavingsGoal updateGoal(String goalId, String userId, Double currentAmount, String goalName, Double targetAmount) {
        AppLogger.logInfo(logger, "updateGoal", "Updating savings goal id: " + goalId);
        
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Savings goal not found"));
        
        if (!goal.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this goal");
        }
        
        if (currentAmount != null && currentAmount >= 0) {
            goal.setCurrentAmount(currentAmount);
        }
        if (goalName != null && !goalName.isBlank()) {
            goal.setGoalName(goalName);
        }
        if (targetAmount != null && targetAmount > 0) {
            goal.setTargetAmount(targetAmount);
        }
        
        SavingsGoal updated = savingsGoalRepository.save(goal);
        AppLogger.logInfo(logger, "updateGoal", "Savings goal updated successfully");
        return updated;
    }
}
