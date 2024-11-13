package org.gridsuite.filter.server.migrations;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import static org.gridsuite.filter.server.migrations.MigrateEquipmentFilter.ExpertRuleCol.*;

public class MigrateEquipmentFilter implements CustomSqlChange
{
  private static final String  EXPERT_RULE_TABLE = "expert_rule";
  private static final String  EXPERT_RULE_VALUE_TABLE = "expert_rule_value";
  private static final String FREE_PROPS_ID = "free_properties_id";
  private static final String SUB_FREE_PROPS_ID = "substation_free_properties_id";
  private static final String NUMERIC_FILTER_ID = "numeric_filter_id_id";
  private static final String VALUE_COL = "value_";

  enum ExpertRuleCol {
    ID("id"),
    COMBINATOR("combinator"),
    DATA_TYPE("data_type"),
    FIELD("field"),
    OPERATOR("operator"),
    PARENT_RULE_ID("parent_rule_id");

    ExpertRuleCol(String colName) {
      name_ = colName;
    }
    String name_;

    @Override
    public String toString() {
      return name_;
    }
  }

  enum ColType {
    NUMERIC,
    PROPERTY,
    SUBSTATION_PROPERTY,
    ENUM
  }

  static class Column {
    private final String  name_;
    private final ColType type_;
    private       String  fieldValue_;

    String name() { return name_; }
    ColType type() { return type_; }
    String fieldValue() { return fieldValue_; }

    Column(String name, ColType type, String fieldValue) {
      name_ = name;
      type_ = type;
      fieldValue_ = fieldValue;
    }
  }

  static class CountryTable {
    private final String name_;
    private final String idCol_;
    private final String valueCol_;
    private final String fieldValue_;

    CountryTable(String name, String idCol, String valueCol, String fieldValue) {
      name_ = name;
      idCol_ = idCol;
      valueCol_ = valueCol;
      fieldValue_ = fieldValue;
    }

    public String name() { return name_; }
    public String idCol() { return idCol_; }
    public String valueCol() { return valueCol_; }
    public String fieldValue() { return fieldValue_; }
  }

  private enum Equipment
  {
    BATTERY("battery_filter"),
    DANGLING_LINE( "dangling_line_filter"),
    GENERATOR("generator_filter") {
      @Override
      Column[] columns() {
        return new Column[]
          { new Column(NUMERIC_FILTER_ID, ColType.NUMERIC, "NOMINAL_VOLTAGE"),
            new Column(SUB_FREE_PROPS_ID, ColType.SUBSTATION_PROPERTY, "SUBSTATION_PROPERTIES"),
            new Column(FREE_PROPS_ID, ColType.PROPERTY, "FREE_PROPERTIES"),
            new Column("energy_source", ColType.ENUM, "ENERGY_SOURCE")
          };
      }
    } ,
    HVDC_LINE("hvdc_line_filter") {
      @Override
      Column[] columns() {
        return new Column[]
          { new Column("hvdc_line_filter_entity_numeric_filter_id_id", ColType.NUMERIC, "DC_NOMINAL_VOLTAGE"),
            new Column("substation_free_properties1_id", ColType.SUBSTATION_PROPERTY, "SUBSTATION_PROPERTIES_1"),
            new Column("substation_free_properties2_id", ColType.SUBSTATION_PROPERTY, "SUBSTATION_PROPERTIES_2")
          };
      }
      @Override
      CountryTable[] countriesTables()
      {
        return new CountryTable[]
          { new CountryTable(equipmentTable_ + "_entity_countries1", equipmentTable_ + "_entity_id", "countries1", "COUNTRY_1"),
            new CountryTable(equipmentTable_ + "_entity_countries2", equipmentTable_ + "_entity_id", "countries2", "COUNTRY_2")
          };
      }
    },
    LINE("line_filter") {
      @Override
      Column[] columns() {
        return new Column[]
          { new Column("numeric_filter_id1_id", ColType.NUMERIC, "NOMINAL_VOLTAGE_1"),
            new Column("numeric_filter_id2_id", ColType.NUMERIC, "NOMINAL_VOLTAGE_2"),
            new Column("substation_free_properties1_id", ColType.SUBSTATION_PROPERTY, "SUBSTATION_PROPERTIES_1"),
            new Column("substation_free_properties2_id", ColType.SUBSTATION_PROPERTY, "SUBSTATION_PROPERTIES_2"),
            new Column(FREE_PROPS_ID, ColType.PROPERTY, "FREE_PROPERTIES"),
          };
      }

      @Override
      CountryTable[] countriesTables()
      {
        return new CountryTable[]
          { new CountryTable(equipmentTable_ + "_entity_countries1", equipmentTable_ + "_entity_id", "countries1", "COUNTRY_1"),
            new CountryTable(equipmentTable_ + "_entity_countries2", equipmentTable_ + "_entity_id", "countries2", "COUNTRY_2")
          };
      }
    },
    LOAD("load_filter"),
    SHUNT_COMPENSATOR("shunt_compensator_filter"),
    STATIC_VAR_COMPENSATOR("static_var_compensator_filter"),
    SUBSTATION("substation_filter") {
      @Override
      Column[] columns() {
        return new Column[] { new Column(FREE_PROPS_ID, ColType.PROPERTY, "FREE_PROPERTIES") };
      }
    },
    THREE_WINDINGS_TRANSFORMER("three_windings_transformer_filter") {
      @Override
      Column[] columns() {
        return new Column[]
          { new Column("three_windings_transformer_numeric_filter_id1_id", ColType.NUMERIC, "NOMINAL_VOLTAGE_1"),
            new Column("three_windings_transformer_numeric_filter_id2_id", ColType.NUMERIC, "NOMINAL_VOLTAGE_2"),
            new Column("three_windings_transformer_numeric_filter_id3_id", ColType.NUMERIC, "NOMINAL_VOLTAGE_3"),
            new Column(SUB_FREE_PROPS_ID, ColType.SUBSTATION_PROPERTY, "SUBSTATION_PROPERTIES_1")
          };
      }
    },
    TWO_WINDINGS_TRANSFORMER("two_windings_transformer_filter") {
      @Override
      Column[] columns() {
        return new Column[]
          { new Column("numeric_filter_id1_id", ColType.NUMERIC, "NOMINAL_VOLTAGE_1"),
            new Column("two_windings_transformernumeric_filter_id2_id", ColType.NUMERIC, "NOMINAL_VOLTAGE_2"),
            new Column(SUB_FREE_PROPS_ID, ColType.SUBSTATION_PROPERTY, "SUBSTATION_PROPERTIES_1"),
            new Column(FREE_PROPS_ID, ColType.PROPERTY, "FREE_PROPERTIES")
          };
      }
    },
    VOLTAGE_LEVEL("voltage_level_filter");

    Equipment(String equipmentTable) {
      equipmentTable_ = equipmentTable;
    }
    String table() { return equipmentTable_; }
    CountryTable[] countriesTables() {
      return new CountryTable[]
        {new CountryTable(equipmentTable_ + "_entity_countries", equipmentTable_ + "_entity_id", "countries", "COUNTRY")};
    }
    Column[] columns() {
      return new Column[]
        { new Column(NUMERIC_FILTER_ID, ColType.NUMERIC, "NOMINAL_VOLTAGE"),
          new Column(SUB_FREE_PROPS_ID, ColType.SUBSTATION_PROPERTY, "SUBSTATION_PROPERTIES"),
          new Column(FREE_PROPS_ID, ColType.PROPERTY, "FREE_PROPERTIES")
        };
    }
    String equipmentTable_;
  }

  @Override
  public SqlStatement[] generateStatements(Database database) throws CustomChangeException
  {
    List<SqlStatement> statements = new ArrayList<>();

    for (Equipment equipment : Equipment.values()) {
      StringBuilder builder = new StringBuilder("");
      builder.append("select * from ").append(equipment.table());

      try {
        JdbcConnection connection = (JdbcConnection) database.getConnection();

        // All filters to migrate in equipment filter table
        try (ResultSet filters = connection.createStatement().executeQuery(builder.toString())) {
          while (filters.next()) {
            // Add a parent rule (And combinator) in expert_rule table
            UUID parentRuleId = createParentRuleStatement(statements, database);

            // create and add filter
            createFilterStatement(statements, database, parentRuleId, filters, equipment.name());

            for (Column column : equipment.columns()) {
              String colId = filters.getString(column.name());

              switch (column.type()) {
                case NUMERIC:
                  createNumericRuleStatements(connection, statements, database, parentRuleId, colId, column.fieldValue());
                  break;
                case SUBSTATION_PROPERTY, PROPERTY:
                  createPropertiesStatements(statements, database, connection, colId, parentRuleId, column.fieldValue());
                break;
                case ENUM:
                  createEnumRuleStatement(statements, database, parentRuleId, colId, column.fieldValue());
                  break;
                default: break;
              }
            }

            // Countries
            for (int i = 0;  i < equipment.countriesTables().length; i++) {
              CountryTable countryTable = equipment.countriesTables()[i];
              createCountryStatement(connection, statements, database, parentRuleId, countryTable, filters.getString("id"));
            }
          }
        }
      } catch (Exception throwables) {
        throwables.printStackTrace();
        return new SqlStatement[0]; // If any exception occurs don't do any migration
      }
    }


    //Create Rule from numeric_filter + Substation free properties id  + free properties id

    return statements.toArray(new SqlStatement[0]);
  }

  private String convertOperator(String operatorCriteriaFilter)
  {
    if (operatorCriteriaFilter == null) {
      return "";
    }

    return switch (operatorCriteriaFilter) {
      case "EQUALITY" -> "EQUALS";
      case "GREATER_THAN" -> "GREATER";
      case "GREATER_OR_EQUAL" -> "GREATER_OR_EQUALS";
      case "LESS_THAN" -> "LOWER";
      case "LESS_OR_EQUAL" -> "LOWER_OR_EQUALS";
      case "RANGE" -> "BETWEEN";
      default -> "";
    };
  }

  private void createFilterStatement (List<SqlStatement> statements, Database database, UUID parentRuleId, ResultSet filters, String equipment)
    throws SQLException
  {
    InsertStatement filterStatement = new InsertStatement(database.getDefaultCatalogName(), database.getDefaultSchemaName(), "expert_filter")
      .addColumnValue("id", filters.getString("id"))
      .addColumnValue("modification_date", filters.getString("modification_date"))
      .addColumnValue("equipment_type", equipment)
      .addColumnValue("rules_id", parentRuleId.toString() );
    statements.add(filterStatement);
  }

  private UUID createParentRuleStatement(List<SqlStatement> statements, Database database) {
    UUID parentRuleId = UUID.randomUUID();
    EnumMap<ExpertRuleCol, String> values = new EnumMap<>(ExpertRuleCol.class);
    values.put(ID, parentRuleId.toString());
    values.put(COMBINATOR, "AND");
    values.put(DATA_TYPE, "COMBINATOR");
    createRuleStatement(statements, database, values);
    return parentRuleId;
  }

  private void createRuleStatement(List<SqlStatement> statements, Database database, EnumMap<ExpertRuleCol, String> values)
  {
    InsertStatement ruleStatement = new InsertStatement(database.getDefaultCatalogName(), database.getDefaultSchemaName(),
      EXPERT_RULE_TABLE);

    values.forEach((key, value) ->
    {
      if (value != null) {
        ruleStatement.addColumnValue(key.toString(), value);
      }
    });
    statements.add(ruleStatement);
  }

  private void createPropertiesStatements(List<SqlStatement> statements, Database database, JdbcConnection connection,
    String propId, UUID parentRuleId, String fieldValue) throws SQLException, DatabaseException
  {
    if (propId == null) { return; }
    StringBuilder builder = new StringBuilder("");

    builder.append("select free_property_filter_entities_id from free_properties_free_property_filter_entities")
      .append(" where free_properties_filter_entity_id = ").append("'").append(propId).append("'");

    try (ResultSet propertyQuery = connection.createStatement().executeQuery(builder.toString())) {
      while (propertyQuery.next()) {
        String propertyId = propertyQuery.getString(1);

        builder.setLength(0);
        builder.append("select prop_name from free_property where id = ").append("'").append(propertyId).append("'");

        // Property name
        String propName = null;
        try (ResultSet propertyNameQuery = connection.createStatement().executeQuery(builder.toString())) {
          if (propertyNameQuery.next()) {
            propName = propertyNameQuery.getString("prop_name");
          }
        }

        //Property values
        if (propName == null) { continue; }
        builder.setLength(0);
        builder.append("select prop_values from prop_value where free_property_filter_entity_id = ").append("'").append(propertyId).append("'");

        try (ResultSet propertyValueQuery = connection.createStatement().executeQuery(builder.toString())) {
          createPropertyRuleStatements(statements, database, parentRuleId, propName, propertyValueQuery, fieldValue);
        }
      }
    }
  }


  private void createPropertyRuleStatements(List<SqlStatement> statements, Database database, UUID parentRuleId, String propName,
                                            ResultSet propertyValueQuery, String fieldValue) throws SQLException
  {
    UUID propsRuleId = UUID.randomUUID();
    EnumMap<ExpertRuleCol, String> values = new EnumMap<>(ExpertRuleCol.class);
    values.put(ID, propsRuleId.toString());
    values.put(DATA_TYPE, "PROPERTIES");
    values.put(FIELD, fieldValue);
    values.put(OPERATOR, "IN");
    values.put(PARENT_RULE_ID, parentRuleId.toString());
    createRuleStatement(statements, database, values);

    //add property name
    SqlStatement statement = new InsertStatement(database.getDefaultCatalogName(), database.getDefaultSchemaName(), "expert_rule_properties")
      .addColumnValue("id", propsRuleId.toString())
      .addColumnValue("property_name", propName);
    statements.add(statement);

    //insert Values
    int pos = 0;
    while(propertyValueQuery.next()) {
      String propValue = propertyValueQuery.getString(1);
      statement = new InsertStatement(database.getDefaultCatalogName(), database.getDefaultSchemaName(), "expert_rule_property_value")
        .addColumnValue("expert_rule_properties_entity_id", propsRuleId.toString())
        .addColumnValue("property_values", propValue)
        .addColumnValue("pos", String.valueOf(pos));
      pos++;
      statements.add(statement);
    }

  }

  private void createCountryStatement(JdbcConnection connection, List<SqlStatement> statements, Database database, UUID parentRuleId,
                                      CountryTable countryTable, String filterIdToMigrate)
    throws DatabaseException, SQLException
  {

    // Add country values
    StringBuilder builder = new StringBuilder();
    builder.append("select ").append(countryTable.valueCol()).append(" from ").append(countryTable.name())
      .append(" where ").append(countryTable.idCol()).append(" = ")
      .append("'").append(filterIdToMigrate).append("'");

    // Add country Country rule
    UUID ruleId = UUID.randomUUID();
    EnumMap<ExpertRuleCol, String> values = new EnumMap<>(ExpertRuleCol.class);
    values.put(ID, ruleId.toString());
    values.put(DATA_TYPE, "ENUM");
    values.put(FIELD, countryTable.fieldValue());
    values.put(OPERATOR, "IN");
    values.put(PARENT_RULE_ID, parentRuleId.toString());

    try (ResultSet countryValueQuery = connection.createStatement().executeQuery(builder.toString())) {
      ArrayList<String> countries = new ArrayList<>();
      while (countryValueQuery.next()) {
        String country = countryValueQuery.getString(countryTable.valueCol());
        if (country != null) { countries.add(country); }
      }
      String countriesValue = !countries.isEmpty() ? String.join(",", countries) : null;
      if (countriesValue == null) { return; } //don't create a rule for countries if null

      SqlStatement statement = new InsertStatement(database.getDefaultCatalogName(), database.getDefaultSchemaName(),
        EXPERT_RULE_VALUE_TABLE)
          .addColumnValue("id", ruleId.toString())
          .addColumnValue(VALUE_COL, countriesValue);
      statements.add(statement);

    }
    createRuleStatement(statements, database, values);
  }

  private void createEnumRuleStatement(List<SqlStatement> statements, Database database, UUID parentRuleId, String value,
                                            String fieldValue) {

    if (value != null) {
      EnumMap<ExpertRuleCol, String> values = new EnumMap<>(ExpertRuleCol.class);
      UUID ruleId = UUID.randomUUID();
      values.put(ID, ruleId.toString());
      values.put(DATA_TYPE, "ENUM");
      values.put(FIELD, fieldValue);
      values.put(OPERATOR, "EQUALS");
      values.put(PARENT_RULE_ID, parentRuleId.toString());
      createRuleStatement(statements, database, values);

      // value table
      InsertStatement ruleValueStatement = new InsertStatement(database.getDefaultCatalogName(),
        database.getDefaultSchemaName(), EXPERT_RULE_VALUE_TABLE)
        .addColumnValue("id", ruleId.toString())
        .addColumnValue(VALUE_COL, value);
      statements.add(ruleValueStatement);
    }
  }

  private void createNumericRuleStatements(JdbcConnection connection, List<SqlStatement> statements,
                                            Database database, UUID parentRuleId, String numericId, String fieldValue)
    throws SQLException, DatabaseException
  {

    if (numericId == null) { return; }
    StringBuilder builder = new StringBuilder("");
    builder.append("select * from numeric_filter where id = ").append("'").append(numericId).append("'");

    try (ResultSet infosFromNumericFilter = connection.createStatement().executeQuery(builder.toString())) {
      List<String> values = new ArrayList<>();
      UUID ruleId = UUID.randomUUID();
      String operatorExpert = null;

      // values
      if (infosFromNumericFilter.next()) {
        operatorExpert = convertOperator(infosFromNumericFilter.getString("range_type"));
        // insert rule into expert_rule table (get id and then insert values
        String valueTmp = infosFromNumericFilter.getString("value1");
        if (valueTmp != null) {
          values.add(valueTmp);
        }

        valueTmp = infosFromNumericFilter.getString("value2");
        if (valueTmp != null) {
          values.add(valueTmp);
        }
      }

      if (values.isEmpty()) { return; } // No value don't add anything

      // insert in expert_rule
      EnumMap<ExpertRuleCol, String> enumValues = new EnumMap<>(ExpertRuleCol.class);
      enumValues.put(ID, ruleId.toString());
      enumValues.put(DATA_TYPE, "NUMBER");
      enumValues.put(FIELD, fieldValue);
      enumValues.put(OPERATOR, operatorExpert);
      enumValues.put(PARENT_RULE_ID, parentRuleId.toString());
      createRuleStatement(statements, database, enumValues);

      // insert expert_rule_value
      String value = String.join(",", values);
      InsertStatement ruleValueStatement = new InsertStatement(database.getDefaultCatalogName(),
        database.getDefaultSchemaName(), EXPERT_RULE_VALUE_TABLE)
        .addColumnValue("id", ruleId.toString())
        .addColumnValue(VALUE_COL, value);
      statements.add(ruleValueStatement);

    }
  }

  @Override
  public String getConfirmationMessage()
  {
    return null;
  }

  @Override
  public void setUp() throws SetupException
  {
    // implement
  }

  @Override
  public void setFileOpener(ResourceAccessor resourceAccessor)
  {
    // implement
  }

  @Override
  public ValidationErrors validate(Database database)
  {
    return null;
  }
}
