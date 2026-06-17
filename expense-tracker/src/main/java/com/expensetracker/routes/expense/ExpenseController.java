package com.expensetracker.routes.expense;

import com.expensetracker.dao.expense.ExpenseDao;
import com.expensetracker.model.Expense;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/expenses")
@Validated
@Tag(name = "Expenses", description = "Expense Management APIs")
public class ExpenseController {
    
    @Autowired
    private ExpenseDao expenseDao;
    
    @PostMapping
    @Operation(summary = "Add a new expense")
    public ResponseEntity<Expense> addExpense(@Valid @RequestBody Expense expense) {
        return ResponseEntity.ok(expenseDao.addExpense(expense));
    }
    
    @GetMapping
    @Operation(summary = "Get all expenses with optional filters (category, date range)")
    public ResponseEntity<List<Expense>> getExpenses(
        @RequestParam String userId,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(expenseDao.getExpenses(userId, category, startDate, endDate));
    }
    
    @GetMapping("/categories")
    @Operation(summary = "Get all predefined expense categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(expenseDao.getCategories());
    }
    
    @PutMapping("/{expenseId}")
    @Operation(summary = "Update an existing expense")
    public ResponseEntity<Expense> updateExpense(
        @PathVariable String expenseId,
        @RequestParam String userId,
        @RequestBody Expense expense
    ) {
        return ResponseEntity.ok(expenseDao.updateExpense(expenseId, userId, expense));
    }
    
    @DeleteMapping("/{expenseId}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<Map<String, String>> deleteExpense(
        @PathVariable String expenseId,
        @RequestParam String userId
    ) {
        expenseDao.deleteExpense(expenseId, userId);
        return ResponseEntity.ok(Map.of("message", "Expense deleted successfully"));
    }
    
    @GetMapping("/analytics")
    @Operation(summary = "Get expense analytics and category breakdown for pie charts")
    public ResponseEntity<Map<String, Object>> getAnalytics(
        @RequestParam String userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(expenseDao.getAnalytics(userId, startDate, endDate));
    }
}
