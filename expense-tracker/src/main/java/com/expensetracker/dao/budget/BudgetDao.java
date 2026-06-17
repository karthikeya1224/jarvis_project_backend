package com.expensetracker.dao.budget;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.sharedlibs.constants.ExpenseCategory;
import com.expensetracker.sharedlibs.logger.AppLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BudgetDao {
    
    private static final Logger logger = AppLogger.getLogger(BudgetDao.class);
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    public Budget setBudget(String userId, String category, Double budgetLimit) {
        AppLogger.logInfo(logger, "setBudget", "Setting budget for userId: " + userId + ", category: " + category);
        
        if (!ExpenseCategory.isValid(category)) {
            throw new RuntimeException("Invalid category");
        }
        
        if (budgetLimit == null || budgetLimit < 0) {
            throw new RuntimeException("Budget limit must be positive");
        }
        
        Budget budget = budgetRepository.findByUserIdAndCategory(userId, category)
            .orElse(new Budget());
        
        budget.setUserId(userId);
        budget.setCategory(category);
        budget.setBudgetLimit(budgetLimit);
        
        Budget saved = budgetRepository.save(budget);
        AppLogger.logInfo(logger, "setBudget", "Budget set successfully");
        return saved;
    }
    
    public Map<String, Object> getBudgetComparison(String userId) {
        AppLogger.logInfo(logger, "getBudgetComparison", "Fetching budget comparison for userId: " + userId);
        
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        
        if (budgets.isEmpty()) {
            throw new RuntimeException("No budgets found. Please set budgets first.");
        }
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(1).plusMonths(1).minusSeconds(1);
        
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);
        
        Map<String, Double> actualSpending = expenses.stream()
            .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));
        
        List<Map<String, Object>> comparison = new ArrayList<>();
        
        for (Budget budget : budgets) {
            double spent = actualSpending.getOrDefault(budget.getCategory(), 0.0);
            double limit = budget.getBudgetLimit();
            double remaining = limit - spent;
            double percentage = (spent / limit) * 100;
            
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("category", budget.getCategory());
            categoryData.put("budgetLimit", limit);
            categoryData.put("spent", spent);
            categoryData.put("remaining", remaining);
            categoryData.put("percentageUsed", percentage);
            categoryData.put("status", percentage > 100 ? "EXCEEDED" : percentage > 80 ? "WARNING" : "SAFE");
            
            comparison.add(categoryData);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("budgets", comparison);
        
        AppLogger.logInfo(logger, "getBudgetComparison", "Budget comparison generated successfully");
        return result;
    }
    
    public List<Map<String, Object>> getAlerts(String userId) {
        AppLogger.logInfo(logger, "getAlerts", "Fetching budget alerts for userId: " + userId);
        
        Map<String, Object> comparison = getBudgetComparison(userId);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> budgets = (List<Map<String, Object>>) comparison.get("budgets");
        
        List<Map<String, Object>> alerts = budgets.stream()
            .filter(b -> {
                String status = (String) b.get("status");
                return status.equals("EXCEEDED") || status.equals("WARNING");
            })
            .map(b -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("category", b.get("category"));
                alert.put("message", b.get("status").equals("EXCEEDED") 
                    ? "Budget exceeded for " + b.get("category")
                    : "Warning: You've used " + String.format("%.0f", b.get("percentageUsed")) + "% of " + b.get("category") + " budget");
                alert.put("severity", b.get("status"));
                return alert;
            })
            .collect(Collectors.toList());
        
        AppLogger.logInfo(logger, "getAlerts", "Found " + alerts.size() + " alerts");
        return alerts;
    }
}
