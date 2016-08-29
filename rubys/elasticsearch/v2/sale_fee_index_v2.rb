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
      if row[:order_id].nil?
        row[:order_id] = ""
      else
        row[:order_id].gsub!(/-/, '_')
      end
      row
    end
  end
end

SQL = %q(SELECT fee.md5_id as id,fee.date `date`,fee.transaction_type,case when product_sku is not null then (select selling_sellingid from OrderItem o where o.order_orderid=fee.order_orderid and o.product_sku=fee.product_sku) else (select selling_sellingid from OrderItem o where o.order_orderid=fee.order_orderid limit 1)  end selling_id, case when product_sku is not null then  product_sku else (select product_sku from OrderItem o where o.order_orderid=fee.order_orderid limit 1) end sku ,fee.market,fee.type_name fee_type, fee.usdCost cost_in_usd, fee.cost, fee.currency, fee.qty quantity, fee.order_orderId order_id FROM SaleFee fee WHERE fee.date>=? and fee.date<=? and fee.transaction_type is not null)
#SQL << " LIMIT 31000"
SaleFeeActor.new.init_mapping
process(dataset: DB[SQL, Time.now - 60 * 86400, Time.now].stream, actor: SaleFeeActor.pool(size: 6))