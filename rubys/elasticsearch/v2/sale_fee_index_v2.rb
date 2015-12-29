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
    submit(rows) do |row|
      row[:order_id].gsub!(/-/, '_')
      row
    end
  end
end

SQL = %q(SELECT fee.id, oi.createDate `date`, oi.selling_sellingId selling_id, 
  case when fee.product_sku is not null then fee.product_sku when fee.orderitem_sku is not null then fee.orderitem_sku else  oi.product_sku end sku, 
    oi.market, fee.type_name fee_type, fee.usdCost cost_in_usd, fee.cost, fee.currency, fee.qty quantity, fee.order_orderId order_id
  FROM SaleFee fee
  LEFT JOIN OrderItem oi ON fee.order_orderId=oi.order_orderId
  WHERE oi.product_sku IS NOT NULL AND oi.market IS NOT NULL AND date>=?)
#SQL << " LIMIT 31000"
SaleFeeActor.new.init_mapping
process(dataset: DB[SQL, Time.parse('2012-01-01')].stream, actor: SaleFeeActor.pool(size: 6))