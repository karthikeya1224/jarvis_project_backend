package com.expensetracker.routes.dashboard;

import com.expensetracker.dao.dashboard.DashboardDao;
import com.expensetracker.model.Dashboard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@Validated
@Tag(name = "Dashboard", description = "Dashboard Income & Summary APIs")
public class DashboardController {
    
    @Autowired
    private DashboardDao dashboardDao;
    
    record IncomeRequest(
        @NotBlank(message = "User ID is required") String userId,
        @NotNull(message = "Monthly income is required") @Min(value = 0, message = "Income must be positive") Double monthlyIncome,
        @Min(value = 0, message = "Savings target must be positive") Double savingsTarget
    ) {}
    
    @PostMapping("/income")
    @Operation(summary = "Set monthly income and savings target")
    public ResponseEntity<Dashboard> setIncome(@Validated @RequestBody IncomeRequest req) {
        return ResponseEntity.ok(dashboardDao.setIncome(req.userId(), req.monthlyIncome(), req.savingsTarget()));
    }
    
    @PutMapping("/income")
    @Operation(summary = "Update monthly income and savings target")
    public ResponseEntity<Dashboard> updateIncome(@Validated @RequestBody IncomeRequest req) {
        return ResponseEntity.ok(dashboardDao.updateIncome(req.userId(), req.monthlyIncome(), req.savingsTarget()));
    }
    
    @GetMapping("/summary/{userId}")
    @Operation(summary = "Get dashboard summary with income, expenses, balance, savings")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable String userId) {
        return ResponseEntity.ok(dashboardDao.getSummary(userId));
    }
}
