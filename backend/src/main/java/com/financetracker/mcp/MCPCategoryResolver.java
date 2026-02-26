package com.financetracker.mcp;

import com.financetracker.model.Category;
import com.financetracker.model.User;
import com.financetracker.model.enums.TransactionType;
import com.financetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resolves category names from MCP/NLP input to actual database categories
 * Implements fuzzy matching logic to find the closest category match
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MCPCategoryResolver {

    private final CategoryRepository categoryRepository;

    /**
     * Resolve a category hint to an actual category
     * 
     * @param categoryHint The user-provided category name or hint (e.g., "groceries", "food & dining")
     * @param userCategories List of available categories for the user
     * @return Optional containing the matched category
     */
    public Optional<Category> resolveCategory(String categoryHint, List<Category> userCategories) {
        if (categoryHint == null || categoryHint.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedHint = categoryHint.toLowerCase(Locale.ENGLISH).trim();
        
        // Exact match (case-insensitive)
        Optional<Category> exactMatch = userCategories.stream()
                .filter(c -> c.getName().toLowerCase(Locale.ENGLISH).equals(normalizedHint))
                .findFirst();
        
        if (exactMatch.isPresent()) {
            log.debug("Found exact category match: {}", exactMatch.get().getName());
            return exactMatch;
        }

        // Partial match - check if any category name contains the hint
        Optional<Category> partialMatch = userCategories.stream()
                .filter(c -> c.getName().toLowerCase(Locale.ENGLISH).contains(normalizedHint))
                .findFirst();
        
        if (partialMatch.isPresent()) {
            log.debug("Found partial category match: {}", partialMatch.get().getName());
            return partialMatch;
        }

        // Reverse partial match - check if hint contains any category name
        Optional<Category> reverseMatch = userCategories.stream()
                .filter(c -> normalizedHint.contains(c.getName().toLowerCase(Locale.ENGLISH)))
                .findFirst();
        
        if (reverseMatch.isPresent()) {
            log.debug("Found reverse partial category match: {}", reverseMatch.get().getName());
            return reverseMatch;
        }

        // Similarity-based matching (Levenshtein distance)
        Optional<Category> similarMatch = userCategories.stream()
                .min((c1, c2) -> {
                    int dist1 = levenshteinDistance(normalizedHint, c1.getName().toLowerCase(Locale.ENGLISH));
                    int dist2 = levenshteinDistance(normalizedHint, c2.getName().toLowerCase(Locale.ENGLISH));
                    return Integer.compare(dist1, dist2);
                })
                .filter(c -> {
                    int distance = levenshteinDistance(normalizedHint, c.getName().toLowerCase(Locale.ENGLISH));
                    // If distance is reasonable (< 3 or < 40% of length), consider it a match
                    return distance < 3 || distance < Math.max(normalizedHint.length(), c.getName().length()) * 0.4;
                });
        
        if (similarMatch.isPresent()) {
            log.debug("Found similar category match: {}", similarMatch.get().getName());
            return similarMatch;
        }

        log.debug("No category match found for hint: {}", categoryHint);
        return Optional.empty();
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }

        return dp[str1.length()][str2.length()];
    }

    /**
     * Get suggestion string for available categories
     */
    public String getCategorySuggestions(List<Category> userCategories) {
        return String.join(", ",
                userCategories.stream()
                        .map(Category::getName)
                        .toArray(String[]::new));
    }
}
