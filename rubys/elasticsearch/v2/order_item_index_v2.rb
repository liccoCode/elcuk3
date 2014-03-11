require "celluloid/autostart"
require "oj"
require "httparty"
require "sequel"

ES_HOST = "http://aggron.easya.cc:9200"
ES_INDEX = "elcuk2"
ES_TYPE = "orderitem"
ES_URL = "#{ES_HOST}/#{ES_INDEX}/#{ES_TYPE}"
DB_HOST = "http://aggron.easya.cc"

class OrderItemActor
  include Celluloid

  MAPPING = %q({
    "orderitem": {
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
            "quantity": {
                "type": "integer"
            },
            "order_id": {
                "type": "string"
            }
        }
    }
})

  # 初始化 orderitem type 的 mapping
  def init_mapping
    resp = HTTParty.put("#{ES_URL}/_mapping", body: OrderItemActor::MAPPING)
    puts resp.code
    resp.code == 200
  end

  def bulk_submit(rows)
    post_body = ""
    rows.each do |row|
      post_body << Oj.dump({ index: { "_index" => "elcuk2", "_type" => "orderitem", "_id": row.delete(:id)} }) << "\n"
      post_body << Oj.dump(row)
    end
    resp = HTTParty.post("#{ES_HOST}/_bulk", body: post_body)
    puts "Bulk Submit: #{resp.code}"
    resp.code == 200
  end

end



DB = Sequel.mysql2('elcuk2', host: DB_HOST, user: 'root', password: 'crater10lake')

# select oi.createDate date, oi.selling_sellingId selling_id, oi.product_sku sku, oi.market, oi.quantity, oi.order_orderId order_id from OrderItem oi limit 10;
orderitem = DB["SELECT oi.createDate date, oi.selling_sellingId selling_id, oi.product_sku sku, oi.market, oi.quantity, oi.order_orderId order_id FROM OrderItem oi LIMIT 10"].stream

orderitem.where("createDate>=#{Time.now - (30 * 24 * 3600)}").each do |row|
  # deal rows....
end






