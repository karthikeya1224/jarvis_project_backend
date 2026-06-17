package com.expensetracker.dao.reports;

import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.sharedlibs.logger.AppLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportsDao {
    
    private static final Logger logger = AppLogger.getLogger(ReportsDao.class);
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    public Map<String, Object> getMonthlyReport(String userId, Integer year, Integer month) {
        AppLogger.logInfo(logger, "getMonthlyReport", "Generating monthly report for userId: " + userId + ", year: " + year + ", month: " + month);
        
        if (year == null) year = LocalDateTime.now().getYear();
        if (month == null) month = LocalDateTime.now().getMonthValue();
        
        if (month < 1 || month > 12) {
            throw new RuntimeException("Month must be between 1 and 12");
        }
        
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);
        
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        
        Map<String, Double> categoryBreakdown = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> e.getCategory().equals("Other") ? e.getCustomCategory() : e.getCategory(),
                Collectors.summingDouble(Expense::getAmount)
            ));
        
        Map<String, Long> categoryCount = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> e.getCategory().equals("Other") ? e.getCustomCategory() : e.getCategory(),
                Collectors.counting()
            ));
        
        Map<String, Object> report = new HashMap<>();
        report.put("year", year);
        report.put("month", month);
        report.put("totalExpenses", totalExpenses);
        report.put("expenseCount", expenses.size());
        report.put("categoryBreakdown", categoryBreakdown);
        report.put("categoryCount", categoryCount);
        report.put("averageExpense", expenses.isEmpty() ? 0 : totalExpenses / expenses.size());
        
        AppLogger.logInfo(logger, "getMonthlyReport", "Monthly report generated successfully");
        return report;
    }
    
    public Map<String, Object> getYearlyReport(String userId, Integer year) {
        AppLogger.logInfo(logger, "getYearlyReport", "Generating yearly report for userId: " + userId + ", year: " + year);
        
        if (year == null) year = LocalDateTime.now().getYear();
        
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        
        Map<String, Double> categoryBreakdown = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> e.getCategory().equals("Other") ? e.getCustomCategory() : e.getCategory(),
                Collectors.summingDouble(Expense::getAmount)
            ));
        
        Map<Integer, Double> monthlyBreakdown = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> e.getDate().getMonthValue(),
                Collectors.summingDouble(Expense::getAmount)
            ));
        
        Map<String, Object> report = new HashMap<>();
        report.put("year", year);
        report.put("totalExpenses", totalExpenses);
        report.put("expenseCount", expenses.size());
        report.put("categoryBreakdown", categoryBreakdown);
        report.put("monthlyBreakdown", monthlyBreakdown);
        report.put("averageMonthlyExpense", totalExpenses / 12);
        report.put("averageExpense", expenses.isEmpty() ? 0 : totalExpenses / expenses.size());
        
        AppLogger.logInfo(logger, "getYearlyReport", "Yearly report generated successfully");
        return report;
    }
}
