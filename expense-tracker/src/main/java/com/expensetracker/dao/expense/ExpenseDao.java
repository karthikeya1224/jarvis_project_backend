package com.expensetracker.dao.expense;

import com.expensetracker.model.Expense;
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
public class ExpenseDao {
    
    private static final Logger logger = AppLogger.getLogger(ExpenseDao.class);
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    public Expense addExpense(Expense expense) {
        AppLogger.logInfo(logger, "addExpense", "Adding expense for userId: " + expense.getUserId());
        
        if (expense.getAmount() == null || expense.getAmount() < 0) {
            throw new RuntimeException("Expense amount must be positive");
        }
        
        if (!ExpenseCategory.isValid(expense.getCategory()) && !expense.getCategory().equals("Other")) {
            throw new RuntimeException("Invalid category. Use predefined categories or 'Other'");
        }
        
        if (expense.getCategory().equals("Other") && (expense.getCustomCategory() == null || expense.getCustomCategory().isBlank())) {
            throw new RuntimeException("Custom category name is required when category is 'Other'");
        }
        
        if (expense.getDate() == null) {
            expense.setDate(LocalDateTime.now());
        }
        
        Expense saved = expenseRepository.save(expense);
        AppLogger.logInfo(logger, "addExpense", "Expense added successfully with id: " + saved.getId());
        return saved;
    }
    
    public List<Expense> getExpenses(String userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        AppLogger.logInfo(logger, "getExpenses", "Fetching expenses for userId: " + userId);
        
        List<Expense> expenses;
        
        if (startDate != null && endDate != null) {
            expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
            AppLogger.logInfo(logger, "getExpenses", "Filtered by date range");
        } else if (category != null && !category.isBlank()) {
            expenses = expenseRepository.findByUserIdAndCategory(userId, category);
            AppLogger.logInfo(logger, "getExpenses", "Filtered by category: " + category);
        } else {
            expenses = expenseRepository.findByUserId(userId);
        }
        
        AppLogger.logInfo(logger, "getExpenses", "Found " + expenses.size() + " expenses");
        return expenses;
    }
    
    public List<String> getCategories() {
        AppLogger.logInfo(logger, "getCategories", "Fetching all predefined categories");
        return ExpenseCategory.ALL_CATEGORIES;
    }
    
    public Expense updateExpense(String expenseId, String userId, Expense updatedExpense) {
        AppLogger.logInfo(logger, "updateExpense", "Updating expense id: " + expenseId);
        
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));
        
        if (!expense.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this expense");
        }
        
        if (updatedExpense.getCategory() != null) expense.setCategory(updatedExpense.getCategory());
        if (updatedExpense.getCustomCategory() != null) expense.setCustomCategory(updatedExpense.getCustomCategory());
        if (updatedExpense.getDescription() != null) expense.setDescription(updatedExpense.getDescription());
        if (updatedExpense.getAmount() != null && updatedExpense.getAmount() >= 0) expense.setAmount(updatedExpense.getAmount());
        if (updatedExpense.getDate() != null) expense.setDate(updatedExpense.getDate());
        
        Expense updated = expenseRepository.save(expense);
        AppLogger.logInfo(logger, "updateExpense", "Expense updated successfully");
        return updated;
    }
    
    public void deleteExpense(String expenseId, String userId) {
        AppLogger.logInfo(logger, "deleteExpense", "Deleting expense id: " + expenseId);
        
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));
        
        if (!expense.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this expense");
        }
        
        expenseRepository.deleteById(expenseId);
        AppLogger.logInfo(logger, "deleteExpense", "Expense deleted successfully");
    }
    
    public Map<String, Object> getAnalytics(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        AppLogger.logInfo(logger, "getAnalytics", "Fetching analytics for userId: " + userId);
        
        List<Expense> expenses = startDate != null && endDate != null 
            ? expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
            : expenseRepository.findByUserId(userId);
        
        double totalAmount = expenses.stream().mapToDouble(Expense::getAmount).sum();
        
        Map<String, Double> categoryBreakdown = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> e.getCategory().equals("Other") ? e.getCustomCategory() : e.getCategory(),
                Collectors.summingDouble(Expense::getAmount)
            ));
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalExpenses", totalAmount);
        analytics.put("expenseCount", expenses.size());
        analytics.put("categoryBreakdown", categoryBreakdown);
        analytics.put("averageExpense", expenses.isEmpty() ? 0 : totalAmount / expenses.size());
        
        AppLogger.logInfo(logger, "getAnalytics", "Analytics generated successfully");
        return analytics;
    }
}
