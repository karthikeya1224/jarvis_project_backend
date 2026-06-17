package com.expensetracker.routes.reports;

import com.expensetracker.dao.reports.ReportsDao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
@Tag(name = "Reports", description = "Expense Reports & Analytics APIs")
public class ReportsController {
    
    @Autowired
    private ReportsDao reportsDao;
    
    @GetMapping("/monthly")
    @Operation(summary = "Get monthly expense report with category breakdown")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
        @RequestParam String userId,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(reportsDao.getMonthlyReport(userId, year, month));
    }
    
    @GetMapping("/yearly")
    @Operation(summary = "Get yearly expense summary with monthly and category breakdown")
    public ResponseEntity<Map<String, Object>> getYearlyReport(
        @RequestParam String userId,
        @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(reportsDao.getYearlyReport(userId, year));
    }
}
