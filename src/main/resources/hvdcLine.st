import org.gridsuite.filter.server.utils.FiltersUtils;

for (equipment in network.<collectionName>) {
  <if(noEmptyFilter)>if (<endif>
      <if(equipmentId || equipmentName)>(<endif><\\>
      <if(equipmentId)>FiltersUtils.matchID('<equipmentId>', equipment)<endif><if(equipmentId && equipmentName)> || <endif><if(equipmentName)>FiltersUtils.matchName('<equipmentName>', equipment)<endif><if(equipmentId || equipmentName)>)<endif>
      <if((equipmentId || equipmentName) && countries1)>&& <endif><\\>
      <if(countries1)>FiltersUtils.isLocatedIn(<countries1>, equipment.converterStation1.terminal)<endif>
      <if((equipmentId || equipmentName || countries1) && countries2)>&& <endif><\\>
      <if(countries2)>FiltersUtils.isLocatedIn(<countries2>, equipment.converterStation2.terminal)<endif>
      <if((equipmentId || equipmentName || countries1 || countries2) && nominalVType)>&& <endif><\\>
      <if(nominalVEquality)>FiltersUtils.isEqualityNominalVoltage(equipment.nominalVoltage, <nominalV>)<endif><\\>
      <if(nominalVRange)>FiltersUtils.isRangeNominalVoltage(equipment.nominalVoltage, <minNominalV>, <maxNominalV>)<endif>
      <if((equipmentId || equipmentName || countries1 || countries2 || nominalVType) && substationName1)>&& <endif><\\>
      <if(substationName1)>equipment.converterStation1.terminal.voltageLevel.substation.name.equals('<substationName1>')<endif>
      <if((equipmentId || equipmentName || countries1 || countries2 || nominalVType) && substationName2)>&& <endif><\\>
      <if(substationName2)>equipment.converterStation2.terminal.voltageLevel.substation.name.equals('<substationName2>')<endif>
     <if(noEmptyFilter)>) {<endif>
           filter(equipment.id) { equipments equipment.id }
     <if(noEmptyFilter)>}<endif>
}
