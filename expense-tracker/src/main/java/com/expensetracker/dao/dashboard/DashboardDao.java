package com.expensetracker.dao.dashboard;

import com.expensetracker.model.Dashboard;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.DashboardRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.sharedlibs.logger.AppLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardDao {
    
    private static final Logger logger = AppLogger.getLogger(DashboardDao.class);
    
    @Autowired
    private DashboardRepository dashboardRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    public Dashboard setIncome(String userId, Double monthlyIncome, Double savingsTarget) {
        AppLogger.logInfo(logger, "setIncome", "Setting income for userId: " + userId);
        
        if (monthlyIncome == null || monthlyIncome < 0) {
            throw new RuntimeException("Monthly income must be positive");
        }
        
        Dashboard dashboard = dashboardRepository.findById(userId).orElse(new Dashboard());
        
        dashboard.setUserId(userId);
        dashboard.setMonthlyIncome(monthlyIncome);
        if (savingsTarget != null) {
            dashboard.setSavingsTarget(savingsTarget);
        }
        
        Dashboard saved = dashboardRepository.save(dashboard);
        AppLogger.logInfo(logger, "setIncome", "Income set successfully for userId: " + userId);
        return saved;
    }
    
    public Dashboard updateIncome(String userId, Double monthlyIncome, Double savingsTarget) {
        AppLogger.logInfo(logger, "updateIncome", "Updating income for userId: " + userId);
        
        Dashboard dashboard = dashboardRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Dashboard not found. Please set income first."));
        
        if (monthlyIncome != null && monthlyIncome >= 0) {
            dashboard.setMonthlyIncome(monthlyIncome);
        }
        if (savingsTarget != null && savingsTarget >= 0) {
            dashboard.setSavingsTarget(savingsTarget);
        }
        
        Dashboard updated = dashboardRepository.save(dashboard);
        AppLogger.logInfo(logger, "updateIncome", "Income updated successfully for userId: " + userId);
        return updated;
    }
    
    public Map<String, Object> getSummary(String userId) {
        AppLogger.logInfo(logger, "getSummary", "Fetching summary for userId: " + userId);
        
        Dashboard dashboard = dashboardRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Dashboard not found. Please set income first."));
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(1).plusMonths(1).minusSeconds(1);
        
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);
        
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double monthlyIncome = dashboard.getMonthlyIncome() != null ? dashboard.getMonthlyIncome() : 0;
        double savingsTarget = dashboard.getSavingsTarget() != null ? dashboard.getSavingsTarget() : 0;
        double balance = monthlyIncome - totalExpenses;
        double actualSavings = Math.max(0, balance);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("userId", userId);
        summary.put("monthlyIncome", monthlyIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("balance", balance);
        summary.put("savingsTarget", savingsTarget);
        summary.put("actualSavings", actualSavings);
        summary.put("savingsProgress", savingsTarget > 0 ? (actualSavings / savingsTarget) * 100 : 0);
        summary.put("expenseCount", expenses.size());
        
        AppLogger.logInfo(logger, "getSummary", "Summary fetched successfully for userId: " + userId);
        return summary;
    }
}
