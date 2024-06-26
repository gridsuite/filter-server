-- Remove rules on no more used field RATIO_REGULATING for two windings transformers
DELETE FROM expert_rule_value t1
WHERE EXISTS (SELECT * FROM expert_rule t2 WHERE t1.id = t2.id AND t2.field='RATIO_REGULATING');

DELETE FROM expert_rule t
WHERE t.field='RATIO_REGULATING';

-- Remove rules on no more used field PHASE_REGULATING for two windings transformers
DELETE FROM expert_rule_value t1
WHERE EXISTS (SELECT * FROM expert_rule t2 WHERE t1.id = t2.id AND t2.field='PHASE_REGULATING');

DELETE FROM expert_rule t
WHERE t.field='PHASE_REGULATING';

-- Remove rules on field RATIO_REGULATION_MODE for two windings transformers (rule values list has changed)
DELETE FROM expert_rule_value t1
WHERE EXISTS (SELECT * FROM expert_rule t2 WHERE t1.id = t2.id AND t2.field='RATIO_REGULATION_MODE');

DELETE FROM expert_rule t
WHERE t.field='RATIO_REGULATION_MODE';
