ALTER TABLE identifier_list_filter ADD COLUMN equipment_type_temporary integer;
UPDATE identifier_list_filter SET equipment_type_temporary = 0 WHERE equipment_type ='LINE';
UPDATE identifier_list_filter SET equipment_type_temporary = 1 WHERE equipment_type ='GENERATOR';
UPDATE identifier_list_filter SET equipment_type_temporary = 2 WHERE equipment_type ='LOAD';
UPDATE identifier_list_filter SET equipment_type_temporary = 3 WHERE equipment_type ='SHUNT_COMPENSATOR';
UPDATE identifier_list_filter SET equipment_type_temporary = 4 WHERE equipment_type ='STATIC_VAR_COMPENSATOR';
UPDATE identifier_list_filter SET equipment_type_temporary = 5 WHERE equipment_type ='BATTERY';
UPDATE identifier_list_filter SET equipment_type_temporary = 6 WHERE equipment_type ='BUS';
UPDATE identifier_list_filter SET equipment_type_temporary = 7 WHERE equipment_type ='BUSBAR_SECTION';
UPDATE identifier_list_filter SET equipment_type_temporary = 8 WHERE equipment_type ='DANGLING_LINE';
UPDATE identifier_list_filter SET equipment_type_temporary = 9 WHERE equipment_type ='LCC_CONVERTER_STATION';
UPDATE identifier_list_filter SET equipment_type_temporary = 10 WHERE equipment_type = 'VSC_CONVERTER_STATION';
UPDATE identifier_list_filter SET equipment_type_temporary = 11 WHERE equipment_type = 'TWO_WINDINGS_TRANSFORMER';
UPDATE identifier_list_filter SET equipment_type_temporary = 12 WHERE equipment_type = 'THREE_WINDINGS_TRANSFORMER';
UPDATE identifier_list_filter SET equipment_type_temporary = 13 WHERE equipment_type = 'HVDC_LINE';
UPDATE identifier_list_filter SET equipment_type_temporary = 14 WHERE equipment_type = 'SUBSTATION';
UPDATE identifier_list_filter SET equipment_type_temporary = 15 WHERE equipment_type = 'VOLTAGE_LEVEL';
ALTER TABLE identifier_list_filter DROP COLUMN equipment_type;
ALTER TABLE identifier_list_filter RENAME COLUMN equipment_type_temporary TO equipment_type;