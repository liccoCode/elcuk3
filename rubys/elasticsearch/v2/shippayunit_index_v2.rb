require "./backend"

class ShipPayUnitActor
  include Celluloid
  include ActorBase
  include HTTParty
  include Rates


  def initialize
    init_attrs
    @es_index = "elcuk2"
    @es_type = "shippayunit"
    @shipment_hash = {}
    @wname_market_mapping = {
      "FBA_DE" => "AMAZON_DE",
      "FBA_US" => "AMAZON_US",
      "FBA_UK" => "AMAZON_UK",
      "FBA_IT" => "AMAZON_IT",
      "FBA_JP" => "AMAZON_JP",
      "FBA_FR" => "AMAZON_FR",
      "FBA_CA" => "AMAZON_CA",
      "FBA_ES" => "AMAZON_ES"
    }
  end

  MAPPING = %q({
   "shippayunit": {
      "properties": {
         "date": {
            "type": "date",
            "format": "date_optional_time"
         },
         "selling_id": {
            "type": "string"
         },
         "sku": {
            "type": "string"
         },
         "market": {
            "type": "string"
         },
         "shipment_id": {
             "type": "string"
         },
         "shipitem_id": {
             "type": "string"
         },
         "ship_type": {
             "type": "string"
         },
         "cost": {
             "type": "float"
         },
         "currency": {
             "type": "string"
         },
         "cost_in_usd": {
             "type": "float"
         },
         "weight": {
             "type": "float"
         },
         "ship_date": {
            "type": "date",
            "format": "date_optional_time"    
         }
      }
   }
})

  # return: partial hash: {sku: [..], selling_id: [..]}
  def shipitem_relate(shipitem_id)
    # [{sku: xx, selling_id: yy}, {sku: zz, selling_id: cc}]
    row_gorup = {sku: [], selling_id: [], weight: 0}
    DB["SELECT pu.product_sku sku, pu.selling_sellingId selling_id, (CASE WHEN pu.qty>0 THEN pro.weight*pu.qty WHEN pu.qty=0 THEN pro.weight*pu.planQty WHEN pu.qty IS NULL THEN pro.weight*pu.planQty END) weight FROM ShipItem si LEFT JOIN ProcureUnit pu ON si.unit_id=pu.id LEFT JOIN Product pro on pu.product_sku = pro.sku WHERE si.id=?", shipitem_id].each do |row|
      row_gorup[:sku] << row[:sku] unless row_gorup[:sku].include?(row[:sku])
      row_gorup[:selling_id] << row[:selling_id] unless row_gorup[:selling_id].include?(row[:selling_id])
      row_gorup[:weight] += row[:weight] unless row[:weight] == nil or row[:weight] == "NULL"
    end
    row_gorup
  end

  # return: partial hash: {sku: [..], selling_id: [..]}
  def shipment(shipment_id)
    unless @shipment_hash.key?(shipment_id)
      row_gorup = {sku: [], selling_id: []}
      DB["SELECT pu.product_sku sku, pu.selling_sellingId selling_id, (CASE WHEN pu.qty>0 THEN pro.weight*pu.qty WHEN pu.qty=0 THEN pro.weight*pu.planQty WHEN pu.qty IS NULL THEN pro.weight*pu.planQty END) weight FROM ShipItem si LEFT JOIN Shipment s ON si.shipment_id=s.id LEFT JOIN ProcureUnit pu ON si.unit_id=pu.id LEFT JOIN Product pro on pu.product_sku = pro.sku WHERE si.shipment_id=?", shipment_id].each do |row|
        row_gorup[:sku] << row[:sku] unless row_gorup[:sku].include?(row[:sku])
        row_gorup[:selling_id] << row[:selling_id] unless row_gorup[:selling_id].include?(row[:selling_id])
      end
      @shipment_hash[shipment_id] = row_gorup
    end
    @shipment_hash[shipment_id]
  end

  def wname_to_market(wname)
    @wname_market_mapping[wname]
  end

  def bulk_submit(rows)
    submit(rows) do |row|
      row[:cost_in_usd] = routine_cost_in_usd(row)
      row[:market] = wname_to_market(row.delete(:wname))
      row[:ship_date] = routine_date_format(row[:ship_date])
      row
    end
  end
end

pool = ShipPayUnitActor.pool(size: 6)
# 1. 找出需要的 PaymentUnit
# 2. 补全这些 PaymentUnit 中的 sku 与 selling_id
SQL = %q(SELECT pau.id, pau.createdAt `date`, s.id shipment_id, pau.shipItem_id shipitem_id, s.type ship_type, pau.amount - pau.fixValue cost, pau.currency, pau.feeType_name fee_type, w.name wname, s.planBeginDate `ship_date` FROM PaymentUnit pau
 LEFT JOIN Shipment s ON pau.shipment_id=s.id
 LEFT JOIN Whouse w ON w.id=s.whouse_id
 WHERE pau.shipment_id IS NOT NULL;)
ds = DB[SQL].stream.map do |row, i|
  if row[:shipitem_id]
    row.merge(pool.shipitem_relate(row[:shipitem_id]))
  else
    row.merge(pool.shipment(row[:shipment_id]))
  end
end

ShipPayUnitActor.new.init_mapping
process(dataset: ds, actor: pool)


