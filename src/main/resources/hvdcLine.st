import org.gridsuite.filter.server.utils.FiltersUtils;

for (equipment in network.<collectionName>) {
  <if(noEmptyFilter)>if (<endif>
      <if(equipmentId || equipmentName)>(<endif><\\>
      <if(equipmentId)>FiltersUtils.matchID('<equipmentId>', equipment)<endif><if(equipmentId && equipmentName)> || <endif><if(equipmentName)>FiltersUtils.matchName('<equipmentName>', equipment)<endif><if(equipmentId || equipmentName)>)<endif>
      <if((equipmentId || equipmentName) && countries1)>&& <endif><\\>
      <if(countries1)>FiltersUtils.isLocatedIn(<countries1>, equipment.converterStation1.terminal)<endif>
      <if((equipmentId || equipmentName || countries1) && countries2)>&& <endif><\\>
      <if(countries2)>FiltersUtils.isLocatedIn(<countries2>, equipment.converterStation2.terminal)<endif>
      <if((equipmentId || equipmentName || countries1 || countries2) && nominalV1Type)>&& <endif><\\>
      <if(nominalV1Equality)>FiltersUtils.isEqualityNominalVoltage(equipment.converterStation1.terminal, <nominalV1>)<endif><\\>
      <if(nominalV1Range)>FiltersUtils.isRangeNominalVoltage(equipment.converterStation1.terminal, <minNominalV1>, <maxNominalV1>)<endif><\\>
      <if(nominalV1Approx)>FiltersUtils.isApproxNominalVoltage(equipment.converterStation1.terminal, <nominalV1>, <percentNominalV1>)<endif>
      <if((equipmentId || equipmentName || countries1 || countries2 || nominalV1Type) && nominalV2Type)>&& <endif><\\>
      <if(nominalV2Equality)>FiltersUtils.isEqualityNominalVoltage(equipment.converterStation2.terminal, <nominalV2>)<endif><\\>
      <if(nominalV2Range)>FiltersUtils.isRangeNominalVoltage(equipment.converterStation2.terminal, <minNominalV2>, <maxNominalV2>)<endif><\\>
      <if(nominalV2Approx)>FiltersUtils.isApproxNominalVoltage(equipment.converterStation2.terminal, <nominalV2>, <percentNominalV2>)<endif>
      <if((equipmentId || equipmentName || countries1 || countries2 || nominalV1Type || nominalV2Type) && substationName1)>&& <endif><\\>
      <if(substationName1)>equipment.converterStation1.terminal.voltageLevel.substation.name.equals('<substationName1>')<endif>
      <if((equipmentId || equipmentName || countries1 || countries2 || nominalV1Type|| nominalV2Type) && substationName2)>&& <endif><\\>
      <if(substationName2)>equipment.converterStation2.terminal.voltageLevel.substation.name.equals('<substationName2>')<endif>
     <if(noEmptyFilter)>) {<endif>
           filter(equipment.id) { equipments equipment.id }
     <if(noEmptyFilter)>}<endif>
}
