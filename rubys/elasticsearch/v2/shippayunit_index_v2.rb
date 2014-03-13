require "./backend"

class ShipPayUnitActor
  include Celluloid
  include ActorBase
  include HTTParty

  def initialize
    init_attrs
    @es_index = "elcuk2"
    @es_type = "shippayunit"
    @shipment_hash = {}
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
         }
      }
   }
})

  # return: partial hash: {sku: [..], selling_id: [..]}
  def shipitem_relate(shipitem_id)
    # [{sku: xx, selling_id: yy}, {sku: zz, selling_id: cc}]
    row_gorup = {sku: [], selling_id: []}
    DB["SELECT pu.product_sku sku, pu.selling_sellingId selling_id FROM ShipItem si LEFT JOIN ProcureUnit pu ON si.unit_id=pu.id WHERE si.id=?", shipitem_id].each do |row|
      row_gorup[:sku] << row[:sku] unless row_gorup[:sku].include?(row[:sku])
      row_gorup[:selling_id] << row[:selling_id] unless row_gorup[:selling_id].include?(row[:selling_id])
    end
    row_gorup
  end

  # return: partial hash: {sku: [..], selling_id: [..]}
  def shipment(shipment_id)
    unless @shipment_hash.key?(shipment_id)
      row_gorup = {sku: [], selling_id: []}
      DB["SELECT pu.product_sku sku, pu.selling_sellingId selling_id FROM ShipItem si LEFT JOIN Shipment s ON si.shipment_id=s.id LEFT JOIN ProcureUnit pu ON si.unit_id=pu.id WHERE si.shipment_id=?", shipment_id].each do |row|
        row_gorup[:sku] << row[:sku] unless row_gorup[:sku].include?(row[:sku])
        row_gorup[:selling_id] << row[:selling_id] unless row_gorup[:selling_id].include?(row[:selling_id])
      end
      @shipment_hash[shipment_id] = row_gorup
    end
    @shipment_hash[shipment_id]
  end

  def bulk_submit(rows)
    submit(rows)
  end
end

#DB.loggers << Logger.new($stdout)

pool = ShipPayUnitActor.pool(size: 6)
# 1. 找出需要的 PaymentUnit
# 2. 补全这些 PaymentUnit 中的 sku 与 selling_id
SQL = %q(SELECT pau.id, pau.createdAt `date`, s.id shipment_id, pau.shipItem_id shipitem_id, s.type ship_type, pau.amount - pau.fixValue cost, pau.currency, pau.feeType_name fee_type  FROM PaymentUnit pau
 left join Shipment s ON pau.shipment_id=s.id
 where pau.shipment_id IS NOT NULL)
ds = DB[SQL].stream.map do |row, i|
  if row[:shipitem_id]
    row.merge(pool.shipitem_relate(row[:shipitem_id]))
  else
    row.merge(pool.shipment(row[:shipment_id]))
  end
end

process(dataset: ds, actor: pool)
#ShipPayUnitActor.new.init_mapping


