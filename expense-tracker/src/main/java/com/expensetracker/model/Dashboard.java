package com.expensetracker.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "dashboards")
public class Dashboard {
    
    @Id
    private String userId;
    
    @Min(value = 0, message = "Monthly income must be positive")
    private Double monthlyIncome;
    
    @Min(value = 0, message = "Savings target must be positive")
    private Double savingsTarget;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Dashboard() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(Double monthlyIncome) { 
        this.monthlyIncome = monthlyIncome;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Double getSavingsTarget() { return savingsTarget; }
    public void setSavingsTarget(Double savingsTarget) { 
        this.savingsTarget = savingsTarget;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
