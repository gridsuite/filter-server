
    create table battery_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId_id uuid,
        primary key (id)
    );

    create table BatteryFilterEntity_countries (
       BatteryFilterEntity_id uuid not null,
        countries varchar(255)
    );

    create table busbar_section_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId_id uuid,
        primary key (id)
    );

    create table BusBarSectionFilterEntity_countries (
       BusBarSectionFilterEntity_id uuid not null,
        countries varchar(255)
    );

    create table dangling_line_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId_id uuid,
        primary key (id)
    );

    create table DanglingLineFilterEntity_countries (
       DanglingLineFilterEntity_id uuid not null,
        countries varchar(255)
    );

    create table generator_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId_id uuid,
        primary key (id)
    );

    create table GeneratorFilterEntity_countries (
       GeneratorFilterEntity_id uuid not null,
        countries varchar(255)
    );

    create table hvdc_line_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName1 varchar(255),
        substationName2 varchar(255),
        numericFilterId_id uuid,
        primary key (id)
    );

    create table HvdcLineFilterEntity_countries1 (
       HvdcLineFilterEntity_id uuid not null,
        countries1 varchar(255)
    );

    create table HvdcLineFilterEntity_countries2 (
       HvdcLineFilterEntity_id uuid not null,
        countries2 varchar(255)
    );

    create table lcc_converter_station_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId_id uuid,
        primary key (id)
    );

    create table LccConverterStationFilterEntity_countries (
       LccConverterStationFilterEntity_id uuid not null,
        countries varchar(255)
    );

    create table line_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName1 varchar(255),
        substationName2 varchar(255),
        numericFilterId1_id uuid,
        numericFilterId2_id uuid,
        primary key (id)
    );

    create table LineFilterEntity_countries1 (
       LineFilterEntity_id uuid not null,
        countries1 varchar(255)
    );

    create table LineFilterEntity_countries2 (
       LineFilterEntity_id uuid not null,
        countries2 varchar(255)
    );

    create table load_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId_id uuid,
        primary key (id)
    );

    create table LoadFilterEntity_countries (
       LoadFilterEntity_id uuid not null,
        countries varchar(255)
    );

    create table numericFilter (
       id uuid not null,
        rangeType varchar(255),
        value1 float8,
        value2 float8,
        primary key (id)
    );

    create table script_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        script TEXT,
        primary key (id)
    );

    create table shunt_compensator_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId_id uuid,
        primary key (id)
    );

    create table ShuntCompensatorFilterEntity_countries (
       ShuntCompensatorFilterEntity_id uuid not null,
        countries varchar(255)
    );

    create table static_var_compensator_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId_id uuid,
        primary key (id)
    );

    create table StaticVarCompensatorFilterEntity_countries (
       StaticVarCompensatorFilterEntity_id uuid not null,
        countries varchar(255)
    );

    create table three_windings_transformer_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId1_id uuid,
        numericFilterId2_id uuid,
        numericFilterId3_id uuid,
        primary key (id)
    );

    create table ThreeWindingsTransformerFilterEntity_countries (
       ThreeWindingsTransformerFilterEntity_id uuid not null,
        countries varchar(255)
    );

    create table two_windings_transformer_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId1_id uuid,
        numericFilterId2_id uuid,
        primary key (id)
    );

    create table TwoWindingsTransformerFilterEntity_countries (
       TwoWindingsTransformerFilterEntity_id uuid not null,
        countries varchar(255)
    );

    create table vsc_converter_station_filter (
       id uuid not null,
        creationDate timestamp,
        modificationDate timestamp,
        equipmentId varchar(255),
        equipmentName varchar(255),
        substationName varchar(255),
        numericFilterId_id uuid,
        primary key (id)
    );

    create table VscConverterStationFilterEntity_countries (
       VscConverterStationFilterEntity_id uuid not null,
        countries varchar(255)
    );
create index hvdcLineFilterEntity_countries_idx1 on HvdcLineFilterEntity_countries1 (HvdcLineFilterEntity_id);
create index hvdcLineFilterEntity_countries_idx2 on HvdcLineFilterEntity_countries2 (HvdcLineFilterEntity_id);
create index lineFilterEntity_countries_idx1 on LineFilterEntity_countries1 (LineFilterEntity_id);
create index lineFilterEntity_countries_idx2 on LineFilterEntity_countries2 (LineFilterEntity_id);
create index threeWindingsTransformerFilterEntity_countries_idx on ThreeWindingsTransformerFilterEntity_countries (ThreeWindingsTransformerFilterEntity_id);
create index twoWindingsTransformerFilterEntity_countries_idx on TwoWindingsTransformerFilterEntity_countries (TwoWindingsTransformerFilterEntity_id);

    alter table if exists battery_filter 
       add constraint numericFilterId_id_fk 
       foreign key (numericFilterId_id) 
       references numericFilter;

    alter table if exists BatteryFilterEntity_countries 
       add constraint abstractInjectionFilterEntity_countries_fk 
       foreign key (BatteryFilterEntity_id) 
       references battery_filter;

    alter table if exists busbar_section_filter 
       add constraint numericFilterId_id_fk 
       foreign key (numericFilterId_id) 
       references numericFilter;

    alter table if exists BusBarSectionFilterEntity_countries 
       add constraint abstractInjectionFilterEntity_countries_fk 
       foreign key (BusBarSectionFilterEntity_id) 
       references busbar_section_filter;

    alter table if exists dangling_line_filter 
       add constraint numericFilterId_id_fk 
       foreign key (numericFilterId_id) 
       references numericFilter;

    alter table if exists DanglingLineFilterEntity_countries 
       add constraint abstractInjectionFilterEntity_countries_fk 
       foreign key (DanglingLineFilterEntity_id) 
       references dangling_line_filter;

    alter table if exists generator_filter 
       add constraint numericFilterId_id_fk 
       foreign key (numericFilterId_id) 
       references numericFilter;

    alter table if exists GeneratorFilterEntity_countries 
       add constraint abstractInjectionFilterEntity_countries_fk 
       foreign key (GeneratorFilterEntity_id) 
       references generator_filter;

    alter table if exists hvdc_line_filter 
       add constraint numericFilterId_id_fk 
       foreign key (numericFilterId_id) 
       references numericFilter;

    alter table if exists HvdcLineFilterEntity_countries1 
       add constraint hvdcLineFilterEntity_countries_fk1 
       foreign key (HvdcLineFilterEntity_id) 
       references hvdc_line_filter;

    alter table if exists HvdcLineFilterEntity_countries2 
       add constraint hvdcLineFilterEntity_countries_fk2 
       foreign key (HvdcLineFilterEntity_id) 
       references hvdc_line_filter;

    alter table if exists lcc_converter_station_filter 
       add constraint numericFilterId_id_fk 
       foreign key (numericFilterId_id) 
       references numericFilter;

    alter table if exists LccConverterStationFilterEntity_countries 
       add constraint abstractInjectionFilterEntity_countries_fk 
       foreign key (LccConverterStationFilterEntity_id) 
       references lcc_converter_station_filter;

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
       foreign key (LineFilterEntity_id) 
       references line_filter;

    alter table if exists LineFilterEntity_countries2 
       add constraint lineFilterEntity_countries_fk2 
       foreign key (LineFilterEntity_id) 
       references line_filter;

    alter table if exists load_filter 
       add constraint numericFilterId_id_fk 
       foreign key (numericFilterId_id) 
       references numericFilter;

    alter table if exists LoadFilterEntity_countries 
       add constraint abstractInjectionFilterEntity_countries_fk 
       foreign key (LoadFilterEntity_id) 
       references load_filter;

    alter table if exists shunt_compensator_filter 
       add constraint numericFilterId_id_fk 
       foreign key (numericFilterId_id) 
       references numericFilter;

    alter table if exists ShuntCompensatorFilterEntity_countries 
       add constraint abstractInjectionFilterEntity_countries_fk 
       foreign key (ShuntCompensatorFilterEntity_id) 
       references shunt_compensator_filter;

    alter table if exists static_var_compensator_filter 
       add constraint numericFilterId_id_fk 
       foreign key (numericFilterId_id) 
       references numericFilter;

    alter table if exists StaticVarCompensatorFilterEntity_countries 
       add constraint abstractInjectionFilterEntity_countries_fk 
       foreign key (StaticVarCompensatorFilterEntity_id) 
       references static_var_compensator_filter;

    alter table if exists three_windings_transformer_filter 
       add constraint numericFilterId_id_fk1 
       foreign key (numericFilterId1_id) 
       references numericFilter;

    alter table if exists three_windings_transformer_filter 
       add constraint numericFilterId_id_fk2 
       foreign key (numericFilterId2_id) 
       references numericFilter;

    alter table if exists three_windings_transformer_filter 
       add constraint numericFilterId_id_fk3 
       foreign key (numericFilterId3_id) 
       references numericFilter;

    alter table if exists ThreeWindingsTransformerFilterEntity_countries 
       add constraint threeWindingsTransformerFilterEntity_countries_fk 
       foreign key (ThreeWindingsTransformerFilterEntity_id) 
       references three_windings_transformer_filter;

    alter table if exists two_windings_transformer_filter 
       add constraint numericFilterId_id_fk1 
       foreign key (numericFilterId1_id) 
       references numericFilter;

    alter table if exists two_windings_transformer_filter 
       add constraint numericFilterId_id_fk2 
       foreign key (numericFilterId2_id) 
       references numericFilter;

    alter table if exists TwoWindingsTransformerFilterEntity_countries 
       add constraint twoWindingsTransformerFilterEntity_countries_fk 
       foreign key (TwoWindingsTransformerFilterEntity_id) 
       references two_windings_transformer_filter;

    alter table if exists vsc_converter_station_filter 
       add constraint numericFilterId_id_fk 
       foreign key (numericFilterId_id) 
       references numericFilter;

    alter table if exists VscConverterStationFilterEntity_countries 
       add constraint abstractInjectionFilterEntity_countries_fk 
       foreign key (VscConverterStationFilterEntity_id) 
       references vsc_converter_station_filter;
