package com.expensetracker.dao.emi;

import com.expensetracker.sharedlibs.logger.AppLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EMIDao {
    
    private static final Logger logger = AppLogger.getLogger(EMIDao.class);
    
    public Map<String, Object> calculateEMI(Double principal, Double annualInterestRate, Integer tenureMonths) {
        AppLogger.logInfo(logger, "calculateEMI", "Calculating EMI for principal: " + principal + ", rate: " + annualInterestRate + "%, tenure: " + tenureMonths + " months");
        
        if (principal == null || principal <= 0) {
            throw new RuntimeException("Principal amount must be positive");
        }
        if (annualInterestRate == null || annualInterestRate < 0) {
            throw new RuntimeException("Interest rate must be non-negative");
        }
        if (tenureMonths == null || tenureMonths <= 0) {
            throw new RuntimeException("Tenure must be positive");
        }
        
        double monthlyRate = annualInterestRate / (12 * 100);
        double emi;
        
        if (monthlyRate == 0) {
            emi = principal / tenureMonths;
        } else {
            emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths)) 
                  / (Math.pow(1 + monthlyRate, tenureMonths) - 1);
        }
        
        double totalAmount = emi * tenureMonths;
        double totalInterest = totalAmount - principal;
        
        Map<String, Object> result = new HashMap<>();
        result.put("emi", Math.round(emi * 100.0) / 100.0);
        result.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);
        result.put("totalInterest", Math.round(totalInterest * 100.0) / 100.0);
        result.put("principal", principal);
        result.put("interestRate", annualInterestRate);
        result.put("tenure", tenureMonths);
        
        AppLogger.logInfo(logger, "calculateEMI", "EMI calculated successfully: " + emi);
        return result;
    }
}
