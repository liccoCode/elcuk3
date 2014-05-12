require "./backend"

class ProcurePayUnitActor
  include Celluloid
  include ActorBase
  include HTTParty
  include Rates

  headers "Accept-Encoding" => "gzip", "User-Agent" => "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36"

  def initialize
    init_attrs
    @es_index = "elcuk2"
    @es_type = "procurepayunit"
  end

  MAPPING = %q({
   "procurepayunit": {
      "properties": {
         "cost": {
            "type": "float"
         },
         "cost_in_usd": {
            "type": "float"
         },
         "currency": {
            "type": "string"
         },
         "date": {
            "type": "date",
            "format": "date_optional_time"
         },
         "delivery_id": {
            "type": "string"
         },
         "fee_type": {
            "type": "string"
         },
         "market": {
            "type": "string"
         },
         "procureunit_id": {
            "type": "string"
         },
         "quantity": {
            "type": "integer"
         },
         "unit_price": {
             "type": "float"
         },
         "selling_id": {
            "type": "string"
         },
         "sku": {
            "type": "string"
         }
      }
   }
})

  def bulk_submit(rows)
    submit(rows) do |row|
      row[:cost_in_usd] = routine_cost_in_usd(row)
      row
    end
  end
end

SQL = %q(SELECT pau.id, pau.createdAt `date`, pu.product_sku sku, pu.selling_sellingId selling_id, s.market, pu.qty quantity, pu.price unit_price, pau.deliveryment_id delivery_id, pau.procureUnit_id procureunit_id, pau.feeType_name fee_type, pau.amount + pau.fixValue cost, pau.currency, p.state, p.rate FROM PaymentUnit pau
 LEFT JOIN ProcureUnit pu ON pau.procureUnit_id=pu.id
 LEFT JOIN Payment p ON pau.payment_id=p.id
 LEFT JOIN Selling s ON pu.selling_sellingId=s.sellingId
 WHERE pau.procureUnit_id IS NOT NULL)
#SQL << " LIMIT 100"
ProcurePayUnitActor.new.init_mapping
process(actor: ProcurePayUnitActor.pool(size: 6))
