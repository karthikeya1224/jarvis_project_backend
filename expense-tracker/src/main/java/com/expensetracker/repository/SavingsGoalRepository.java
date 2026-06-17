package com.expensetracker.repository;

import com.expensetracker.model.SavingsGoal;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SavingsGoalRepository extends MongoRepository<SavingsGoal, String> {
    List<SavingsGoal> findByUserId(String userId);
}
