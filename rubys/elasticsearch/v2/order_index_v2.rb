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
         "promotion_id": {
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
         "create_date": {
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
         "selling_id": {
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
    @es_type = "shippayunit"
  end

end

SQL = %q(select s.asin, s.upc, o.orderId, o.account_id, group_concat(distinct(oi.promotionIDs)) promotionIDs, concat_ws(' ', o.address, o.address1) address, o.arriveDate, o.createDate, o.paymentDate, o.shipDate, o.buyer, o.email, o.market, o.state, o.trackNo, o.userid, group_concat(oi.selling_sellingId, "@") sids 
  from Orderr o
  left join OrderItem oi ON o.orderId=oi.order_orderId
  left join Selling s ON oi.selling_sellingId=s.sellingId
  where createDate>='2014-01-01'
  group by o.orderId)