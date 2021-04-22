
CREATE TABLE filters.script_filters (
    name text,
    script text,
    PRIMARY KEY (name)
);

create TYPE filters.numericFilter (
    value1 double,
    value2 double,
    filterType text
);

CREATE TABLE filters.line_filters (
    name text,
    equipmentId text,
    equipmentName text,
    substationName1 text,
    substationName2 text,
    countries1 frozen<list<text>>,
    countries2 frozen<list<text>>,
    nominalVoltage1 numericFilter,
    nominalVoltage2 numericFilter,
    PRIMARY KEY (name)
);

