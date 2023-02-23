import org.gridsuite.filter.server.utils.FiltersUtils;

for (equipment in network.<collectionName>) {
  <if(noEmptyFilter)>if (<endif>
      <if(equipmentId || equipmentName)>(<endif><\\>
      <if(equipmentId)>FiltersUtils.matchID('<equipmentId>', equipment)<endif><if(equipmentId && equipmentName)> || <endif><if(equipmentName)>FiltersUtils.matchName('<equipmentName>', equipment)<endif><if(equipmentId || equipmentName)>)<endif>
      <if((equipmentId || equipmentName) && countries1)>&& <endif><\\>
      <if(countries1)>FiltersUtils.isLocatedIn(<countries1>, equipment.terminal1)<endif>
      <if((equipmentId || equipmentName || countries1) && countries2)>&& <endif><\\>
      <if(countries2)>FiltersUtils.isLocatedIn(<countries2>, equipment.terminal2)<endif>
      <if((equipmentId || equipmentName || countries1 || countries2) && freeProperties1)>&& <endif><\\>
      <if(freeProperties1)>FiltersUtils.matchesFreeProps(<freeProperties1>, equipment.terminal1))<endif>
      <if((equipmentId || equipmentName || countries1 || countries2 || freeProperties1) && freeProperties2)>&& <endif><\\>
      <if(freeProperties2)>FiltersUtils.matchesFreeProps(<freeProperties2>, equipment.terminal2))<endif>
      <if((equipmentId || equipmentName || countries || freeProperties1 || freeProperties2) && nominalV1Type)>&& <endif><\\>
      <if(nominalV1Equality)>FiltersUtils.isEqualityNominalVoltage(equipment.terminal1, <nominalV1>)<endif><\\>
      <if(nominalV1Range)>FiltersUtils.isRangeNominalVoltage(equipment.terminal1, <minNominalV1>, <maxNominalV1>)<endif>
      <if((equipmentId || equipmentName || countries1 || countries2 || nominalV1Type) && nominalV2Type)>&& <endif><\\>
      <if(nominalV2Equality)>FiltersUtils.isEqualityNominalVoltage(equipment.terminal2, <nominalV2>)<endif><\\>
      <if(nominalV2Range)>FiltersUtils.isRangeNominalVoltage(equipment.terminal2, <minNominalV2>, <maxNominalV2>)<endif>
      <if((equipmentId || equipmentName || countries1 || countries2 || nominalV1Type || nominalV2Type) && substationName1)>&& <endif><\\>
      <if(substationName1)>equipment.terminal1.voltageLevel.substation.name.equals('<substationName1>')<endif>
      <if((equipmentId || equipmentName || countries1 || countries2 || nominalV1Type || nominalV2Type) && substationName2)>&& <endif><\\>
      <if(substationName2)>equipment.terminal2.voltageLevel.substation.name.equals('<substationName2>')<endif>
     <if(noEmptyFilter)>) {<endif>
           filter(equipment.id) { equipments equipment.id }
     <if(noEmptyFilter)>}<endif>
}
