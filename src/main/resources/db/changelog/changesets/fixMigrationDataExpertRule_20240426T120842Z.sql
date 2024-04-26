INSERT INTO expert_rule_value (id, value_)
SELECT id, NULL
FROM expert_rule
WHERE OPERATOR = 'EXISTS';