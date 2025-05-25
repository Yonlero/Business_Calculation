package br.com.yonlero.calculation.service.infrastructure.redis;

import br.com.yonlero.calculation.service.domain.model.BudgetOpening;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public List<BudgetOpening> getBudgetOpenings(String key) {
        Object result = redisTemplate.opsForValue().get(key);

        if (result instanceof List) {
            List<BudgetOpening> budgetOpenings = new ArrayList<>();

            for (Object item : (List<?>) result) {
                Map<String, Object> map = (Map<String, Object>) item;
                BudgetOpening budgetOpening = new BudgetOpening(
                        UUID.fromString((String) map.get("id")),
                        YearMonth.parse((String) map.get("yearMonth")),
                        (String) map.get("account"),
                        (String) map.get("costCenter"),
                        (String) map.get("businessUnit"),
                        BigDecimal.valueOf((Double) map.get("plannedValue")),
                        BigDecimal.valueOf((Double) map.get("projectedValue")),
                        BigDecimal.valueOf((Double) map.get("apportionmentValue"))
                );
                budgetOpenings.add(budgetOpening);
            }

            return budgetOpenings;
        }

        return Collections.emptyList();
    }
}