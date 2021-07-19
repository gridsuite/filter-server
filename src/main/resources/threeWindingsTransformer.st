import org.gridsuite.filter.server.utils.FiltersUtils;

for (equipment in network.<collectionName>) {
  <if(noEmptyFilter)>if (<endif>
      <if(equipmentId || equipmentName)>(<endif><\\>
      <if(equipmentId)>FiltersUtils.matchID('<equipmentId>', equipment)<endif><if(equipmentId && equipmentName)> || <endif><if(equipmentName)>FiltersUtils.matchName('<equipmentName>', equipment)<endif><if(equipmentId || equipmentName)>)<endif>
      <if((equipmentId || equipmentName) && countries)>&& <endif><\\>
      <if(countries)>(FiltersUtils.isLocatedIn(<countries>, equipment.terminal1) || FiltersUtils.isLocatedIn(<countries>, equipment.terminal2) || FiltersUtils.isLocatedIn(<countries>, equipment.terminal3))<endif>
      <if((equipmentId || equipmentName || countries) && nominalV1Type)>&& <endif><\\>
      <if(nominalV1Equality)>FiltersUtils.isEqualityNominalVoltage(equipment.terminal1, <nominalV1>)<endif><\\>
      <if(nominalV1Range)>FiltersUtils.isRangeNominalVoltage(equipment.terminal1, <minNominalV1>, <maxNominalV1>)<endif><\\>
      <if(nominalV1Approx)>FiltersUtils.isApproxNominalVoltage(equipment.terminal1, <nominalV1>, <percentNominalV1>)<endif>
      <if((equipmentId || equipmentName || countries || nominalV1Type) && nominalV2Type)>&& <endif><\\>
      <if(nominalV2Equality)>FiltersUtils.isEqualityNominalVoltage(equipment.terminal2, <nominalV2>)<endif><\\>
      <if(nominalV2Range)>FiltersUtils.isRangeNominalVoltage(equipment.terminal2, <minNominalV2>, <maxNominalV2>)<endif><\\>
      <if(nominalV2Approx)>FiltersUtils.isApproxNominalVoltage(equipment.terminal2, <nominalV2>, <percentNominalV2>)<endif>
      <if((equipmentId || equipmentName || countries || nominalV1Type || nominalV2Type) && nominalV3Type)>&& <endif><\\>
      <if(nominalV3Equality)>FiltersUtils.isEqualityNominalVoltage(equipment.terminal3, <nominalV3>)<endif><\\>
      <if(nominalV3Range)>FiltersUtils.isRangeNominalVoltage(equipment.terminal3, <minNominalV3>, <maxNominalV3>)<endif><\\>
      <if(nominalV3Approx)>FiltersUtils.isApproxNominalVoltage(equipment.terminal3, <nominalV3>, <percentNominalV3>)<endif>
      <if((equipmentId || equipmentName || countries || nominalV1Type || nominalV2Type || nominalV3Type) && substationName)>&& <endif><\\>
      <if(substationName)>(equipment.terminal1.voltageLevel.substation.name.equals('<substationName>') || equipment.terminal2.voltageLevel.substation.name.equals('<substationName>') || equipment.terminal3.voltageLevel.substation.name.equals('<substationName>'))<endif>
     <if(noEmptyFilter)>) {<endif>
           filter(equipment.id) { equipments equipment.id }
     <if(noEmptyFilter)>}<endif>
}
