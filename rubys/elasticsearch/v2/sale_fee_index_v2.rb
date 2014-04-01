require "./backend"

class SaleFeeActor
  include Celluloid
  include ActorBase

  def initialize
    init_attrs
    @es_index = "elcuk2"
    @es_type = "salefee"
  end

  MAPPING = %q({
   "salefee": {
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
         "fee_type": {
             "type": "string"
         },
         "cost": {
             "type": "float"
         },
         "cost_in_usd": {
             "type": "float"
         },
         "currency": {
             "type": "string"
         },
         "quantity": {
             "type": "integer"
         },
         "order_id": {
             "type": "string"
         }
      }
   }
})

  def bulk_submit(rows)
    async.submit(rows)
  end

end

SQL = "SELECT fee.id, fee.date `date`, oi.selling_sellingId selling_id, oi.product_sku sku, oi.market, fee.type_name fee_type, fee.usdCost cost_in_usd, fee.cost, fee.currency, fee.qty quantity, fee.order_orderId FROM SaleFee fee LEFT JOIN OrderItem oi ON fee.order_orderId=oi.order_orderId WHERE oi.product_sku IS NOT NULL and oi.market IS NOT NULL"
#SQL << " LIMIT 31000"
SaleFeeActor.new.init_mapping
process(actor: SaleFeeActor.pool(size: 6))
