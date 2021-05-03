
    create table line_filter (
       name varchar(255) not null,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName1 varchar(255),
        substationName2 varchar(255),
        numericFilterId1_id uuid,
        numericFilterId2_id uuid,
        primary key (name)
    );

    create table LineFilterEntity_countries1 (
       LineFilterEntity_name varchar(255) not null,
        countries1 varchar(255)
    );

    create table LineFilterEntity_countries2 (
       LineFilterEntity_name varchar(255) not null,
        countries2 varchar(255)
    );

    create table numericFilter (
       id uuid not null,
        rangeType varchar(255),
        value1 float8,
        value2 float8,
        primary key (id)
    );

    create table script_filter (
       name varchar(255) not null,
        script varchar(255),
        primary key (name)
    );
create index lineFilterEntity_countries_idx1 on LineFilterEntity_countries1 (LineFilterEntity_name);
create index lineFilterEntity_countries_idx2 on LineFilterEntity_countries2 (LineFilterEntity_name);

    alter table if exists line_filter 
       add constraint numericFilterId_id_fk1 
       foreign key (numericFilterId1_id) 
       references numericFilter;

    alter table if exists line_filter 
       add constraint numericFilterId_id_fk2 
       foreign key (numericFilterId2_id) 
       references numericFilter;

    alter table if exists LineFilterEntity_countries1 
       add constraint lineFilterEntity_countries_fk1 
       foreign key (LineFilterEntity_name) 
       references line_filter;

    alter table if exists LineFilterEntity_countries2 
       add constraint lineFilterEntity_countries_fk2 
       foreign key (LineFilterEntity_name) 
       references line_filter;
