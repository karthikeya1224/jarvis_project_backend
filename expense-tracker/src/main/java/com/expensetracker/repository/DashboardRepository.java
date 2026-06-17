package com.expensetracker.repository;

import com.expensetracker.model.Dashboard;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DashboardRepository extends MongoRepository<Dashboard, String> {
}
