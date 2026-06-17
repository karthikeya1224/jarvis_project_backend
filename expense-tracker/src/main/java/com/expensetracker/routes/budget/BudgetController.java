package com.expensetracker.routes.budget;

import com.expensetracker.dao.budget.BudgetDao;
import com.expensetracker.model.Budget;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/budget")
@Validated
@Tag(name = "Budget", description = "Budget Management APIs")
public class BudgetController {
    
    @Autowired
    private BudgetDao budgetDao;
    
    record BudgetRequest(
        @NotBlank(message = "User ID is required") String userId,
        @NotBlank(message = "Category is required") String category,
        @NotNull(message = "Budget limit is required") @Min(value = 0, message = "Budget must be positive") Double budgetLimit
    ) {}
    
    @PostMapping
    @Operation(summary = "Set monthly budget for a category")
    public ResponseEntity<Budget> setBudget(@Validated @RequestBody BudgetRequest req) {
        return ResponseEntity.ok(budgetDao.setBudget(req.userId(), req.category(), req.budgetLimit()));
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get budget vs actual spending comparison")
    public ResponseEntity<Map<String, Object>> getBudgetComparison(@PathVariable String userId) {
        return ResponseEntity.ok(budgetDao.getBudgetComparison(userId));
    }
    
    @GetMapping("/alerts/{userId}")
    @Operation(summary = "Get overspending alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlerts(@PathVariable String userId) {
        return ResponseEntity.ok(budgetDao.getAlerts(userId));
    }
}
