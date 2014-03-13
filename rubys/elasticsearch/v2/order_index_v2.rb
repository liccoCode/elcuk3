

SQL = %q(select s.asin, s.upc, o.orderId, o.account_id, group_concat(distinct(oi.promotionIDs)) promotionIDs, concat_ws(' ', o.address, o.address1) address, o.arriveDate, o.createDate, o.paymentDate, o.shipDate, o.buyer, o.email, o.market, o.state, o.trackNo, o.userid, group_concat(oi.selling_sellingId, "@") sids 
  from Orderr o
  left join OrderItem oi ON o.orderId=oi.order_orderId
  left join Selling s ON oi.selling_sellingId=s.sellingId
  where createDate>='2014-01-01'
  group by o.orderId)