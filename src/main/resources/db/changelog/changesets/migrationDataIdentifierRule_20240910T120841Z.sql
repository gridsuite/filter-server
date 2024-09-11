ALTER TABLE identifier_list_filter ADD COLUMN equipment_typ varchar;
UPDATE identifier_list_filter SET equipment_typ = 'LINE' WHERE equipment_type = 0;
UPDATE identifier_list_filter SET equipment_typ = 'GENERATOR' WHERE equipment_type = 1;
UPDATE identifier_list_filter SET equipment_typ = 'LOAD' WHERE equipment_type = 2;
UPDATE identifier_list_filter SET equipment_typ = 'SHUNT_COMPENSATOR' WHERE equipment_type = 3;
UPDATE identifier_list_filter SET equipment_typ = 'STATIC_VAR_COMPENSATOR' WHERE equipment_type = 4;
UPDATE identifier_list_filter SET equipment_typ = 'BATTERY' WHERE equipment_type = 5;
UPDATE identifier_list_filter SET equipment_typ = 'BUS' WHERE equipment_type = 6;
UPDATE identifier_list_filter SET equipment_typ = 'BUSBAR_SECTION' WHERE equipment_type = 7;
UPDATE identifier_list_filter SET equipment_typ = 'DANGLING_LINE' WHERE equipment_type = 8;
UPDATE identifier_list_filter SET equipment_typ = 'LCC_CONVERTER_STATION' WHERE equipment_type = 9;
UPDATE identifier_list_filter SET equipment_typ = 'VSC_CONVERTER_STATION' WHERE equipment_type = 10;
UPDATE identifier_list_filter SET equipment_typ = 'TWO_WINDINGS_TRANSFORMER' WHERE equipment_type = 11;
UPDATE identifier_list_filter SET equipment_typ = 'THREE_WINDINGS_TRANSFORMER' WHERE equipment_type = 12;
UPDATE identifier_list_filter SET equipment_typ = 'HVDC_LINE' WHERE equipment_type = 13;
UPDATE identifier_list_filter SET equipment_typ = 'SUBSTATION' WHERE equipment_type = 14;
UPDATE identifier_list_filter SET equipment_typ = 'VOLTAGE_LEVEL' WHERE equipment_type = 15;
ALTER TABLE identifier_list_filter DROP COLUMN equipment_type;
ALTER TABLE identifier_list_filter RENAME COLUMN equipment_typ TO equipment_type;