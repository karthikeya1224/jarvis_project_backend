package com.expensetracker.routes.emi;

import com.expensetracker.dao.emi.EMIDao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/emi")
@Validated
@Tag(name = "EMI Calculator", description = "EMI Calculation APIs")
public class EMIController {
    
    @Autowired
    private EMIDao emiDao;
    
    record EMIRequest(
        @NotNull(message = "Principal amount is required") @Min(value = 1, message = "Principal must be positive") Double principal,
        @NotNull(message = "Annual interest rate is required") @Min(value = 0, message = "Interest rate must be non-negative") Double annualInterestRate,
        @NotNull(message = "Tenure in months is required") @Min(value = 1, message = "Tenure must be at least 1 month") Integer tenureMonths
    ) {}
    
    @PostMapping("/calculate")
    @Operation(summary = "Calculate EMI for a product", description = "Calculate monthly EMI, total amount, and interest based on principal, interest rate, and tenure")
    public ResponseEntity<Map<String, Object>> calculateEMI(@Validated @RequestBody EMIRequest req) {
        return ResponseEntity.ok(emiDao.calculateEMI(req.principal(), req.annualInterestRate(), req.tenureMonths()));
    }
}
