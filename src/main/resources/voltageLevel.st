import org.gridsuite.filter.server.utils.FiltersUtils;

for (equipment in network.<collectionName>) {
  <if(noEmptyFilter)>if (<endif>
      <if(equipmentId || equipmentName)>(<endif><\\>
      <if(equipmentId)>FiltersUtils.matchID('<equipmentId>', equipment)<endif><if(equipmentId && equipmentName)> || <endif><if(equipmentName)>FiltersUtils.matchName('<equipmentName>', equipment)<endif><if(equipmentId || equipmentName)>)<endif>
      <if((equipmentId || equipmentName) && countries)>&& <endif><\\>
      <if(countries)>FiltersUtils.isLocatedIn(<countries>, equipment)<endif>
      <if((equipmentId || equipmentName || countries) && nominalVType)>&& <endif><\\>
      <if(nominalVEquality)>FiltersUtils.isEqualityNominalVoltage(equipment, <nominalV>)<endif><\\>
      <if(nominalVRange)>FiltersUtils.isRangeNominalVoltage(equipment, <minNominalV>, <maxNominalV>)<endif>
     <if(noEmptyFilter)>) {<endif>
           filter(equipment.id) { equipments equipment.id }
     <if(noEmptyFilter)>}<endif>
}
