require "./backend"

class ShipPayUnitActor
  include Celluloid
  include ActorBase
  include HTTParty

  def initialize
    init_attrs
    @es_index = "elcuk2"
    @es_type = "shippayunit"
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
end

SQL = %q(SELECT pau.id, pau.createdAt `date`, pu.product_sku sku, pu.selling_sellingId selling_id, s.market, pu.qty quantity, pu.price unit_price, pau.deliveryment_id delivery_id, pau.procureUnit_id procureunit_id, pau.feeType_name fee_type, pau.amount + pau.fixValue cost, pau.currency, p.state, p.rate FROM PaymentUnit pau
 LEFT JOIN ProcureUnit pu ON pau.procureUnit_id=pu.id
 LEFT JOIN Payment p ON pau.payment_id=p.id
 LEFT JOIN Selling s ON pu.selling_sellingId=s.sellingId
 WHERE pau.procureUnit_id IS NOT NULL)
#SQL << " LIMIT 100"
#process(actor: ShipPayUnitActor.pool(size: 6))
ShipPayUnitActor.new.init_mapping