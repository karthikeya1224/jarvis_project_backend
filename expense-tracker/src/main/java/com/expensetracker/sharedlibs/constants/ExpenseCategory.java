package com.expensetracker.sharedlibs.constants;

import java.util.Arrays;
import java.util.List;

public class ExpenseCategory {
    
    public static final String FOOD = "Food & Dining";
    public static final String TRANSPORT = "Transport";
    public static final String UTILITIES = "Utilities";
    public static final String RENT = "Rent";
    public static final String ENTERTAINMENT = "Entertainment";
    public static final String HEALTHCARE = "Healthcare";
    public static final String SHOPPING = "Shopping";
    public static final String EDUCATION = "Education";
    public static final String OTHER = "Other";
    
    public static final List<String> ALL_CATEGORIES = Arrays.asList(
        FOOD, TRANSPORT, UTILITIES, RENT, ENTERTAINMENT, 
        HEALTHCARE, SHOPPING, EDUCATION, OTHER
    );
    
    public static boolean isValid(String category) {
        return ALL_CATEGORIES.contains(category);
    }
}
