INSERT INTO expert_rule_value (id, value_)
SELECT id, value_
FROM expert_rule
WHERE value_ IS NOT NULL;