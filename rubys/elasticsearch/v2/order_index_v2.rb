require "./backend"

class OrderActor
  include Celluloid
  include ActorBase

  MAPPING = %q({
   "order": {
      "properties": {
         "asin": {
            "type": "string"
         },
         "upc": {
            "type": "string"
         },
         "promotion_ids": {
            "type": "string"
         },
         "account_id": {
            "type": "integer"
         },
         "address": {
            "type": "string"
         },
         "arrive_date": {
            "type": "date",
            "format": "dateOptionalTime"
         },
         "buyer": {
            "type": "string"
         },
         "date": {
            "type": "date",
            "format": "dateOptionalTime"
         },
         "email": {
            "type": "string"
         },
         "market": {
            "type": "string"
         },
         "order_id": {
            "type": "string"
         },
         "payment_date": {
            "type": "date",
            "format": "dateOptionalTime"
         },
         "ship_date": {
            "type": "date",
            "format": "dateOptionalTime"
         },
         "selling_ids": {
            "type": "string"
         },
         "state": {
            "type": "string"
         },
         "track_no": {
            "type": "string"
         },
         "userid": {
            "type": "string"
         }
      }
   }
})

  def initialize
    init_attrs
    @es_index = "elcuk2"
    @es_type = "order"
  end

  def bulk_submit(rows)
    submit(rows) do |row|
      row[:arrive_date] = routine_date_format(row[:arrive_date])
      row[:payment_date] = routine_date_format(row[:payment_date])
      row[:ship_date] = routine_date_format(row[:ship_date])
      row
    end
  end

end

#DB.loggers << Logger.new($stdout)

SQL = %q(SELECT o.orderId id, s.asin, s.upc, o.orderId order_id, o.account_id, group_concat(distinct(oi.promotionIDs)) promotion_ids, concat_ws(' ', o.address, o.address1) address, o.arriveDate arrive_date, o.createDate date, o.paymentDate payment_date, o.shipDate ship_date, o.buyer, o.email, o.market, o.state, o.trackNo track_no, o.userid, group_concat(oi.selling_sellingId, "@") selling_ids 
  FROM Orderr o
  LEFT JOIN OrderItem oi ON o.orderId=oi.order_orderId
  LEFT JOIN Selling s ON oi.selling_sellingId=s.sellingId
  WHERE o.createDate>=?
  GROUP BY o.orderId)


OrderActor.new.init_mapping
process(dataset: DB[SQL, Time.parse('2012-01-01')].stream, actor: OrderActor.pool(size: 6))
#process(dataset: DB[SQL, (Time.now - 30 * 24 * 3600)].stream, actor: OrderActor.pool(size: 6))