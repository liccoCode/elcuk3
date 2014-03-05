require "em-synchrony"
require "em-synchrony/em-http"
require "em-synchrony/mysql2"
require "time"
require "multi_json"
require "pp"

ES_HOST = "http://gengar.easya.cc:9200"
DB_HOST = "localhost"

MAPPING = <<E
{ "orderitem": { "properties": { "createDate": { "type": "date", "format": "dateOptionalTime" }, "cat": { "type": "string" }, "sku": { "type": "string" }, "msku": { "type": "string" }, "id": { "type": "string" }, "market": { "type": "string" }, "order_orderId": { "type": "string" }, "quantity": { "type": "integer" }, "selling_sellingId": { "type": "string" }, "usdCost": { "type": "double" } } } }
E


class OrderItemES
  SQL = """
  select p.category_categoryId cat, s.merchantSKU msku, oi.product_sku sku, oi.id, oi.createDate, oi.quantity, oi.order_orderId, oi.selling_sellingId, oi.usdCost, oi.market, oi.promotionIDs from OrderItem oi
  left join Selling s ON s.sellingId=oi.selling_sellingId
  left join Product p ON p.sku=oi.product_sku
  where oi.createDate>='2014-01-01'
  """

  INDEX = "elcuk2"
  TYPE = "orderitem"

  class << self
    def bulk_index
      post_data = ""
      @docs.each do |doc|
        post_data << MultiJson.dump({index: {_index: OrderItemES::INDEX, _type: OrderItemES::TYPE, _id: doc._id} }) << "\n"
        post_data << doc.to_json << "\n"
      end
      http = EM::HttpRequest.new("#{ES_HOST}/_bulk").post(body: post_data)
      puts http.response if not http.response_header.successful?
      @docs.clear
      sleep(0.5)
    end

    def add_to_bulk(doc)
      @docs ||= []
      doc.flat_all
      @docs << doc
    end

    def init_mapping
        http = EM::HttpRequest.new("#{ES_HOST}/#{OrderItemES::INDEX}/#{OrderItemES::TYPE}/_mapping").put(body: MAPPING)
        puts http.response if not http.response_header.successful?
    end

    def em_bulk_index
      EM.synchrony do

        init_mapping

        clt = Mysql2::EM::Client.new(host: DB_HOST, user: 'root', pass: 'crater10lake', db: 'elcuk2')
        rs = clt.query(OrderItemES::SQL, stream: true)

        i = 0
        rs.each do |row|
          i += 1
          OrderItemES.add_to_bulk(OrderItemES.new(row))
          OrderItemES.bulk_index if i % 1000 == 0

          # us: 68989,  uk: 112474,  de: 482057 -> 10.22
          print "Index orders ...  #{i} / 700000\r"
        end
        OrderItemES.bulk_index

        EM.stop
      end
    end
  end

  def initialize(row)
    @row = row
    @row[:_id] = row['id']
  end

  def flat_all
    date_column_flat
  end

  def date_column_flat
    @row['createDate'] = @row['createDate'].utc.iso8601
  rescue Exception => e
    #ignore
  end

  def _id
    @row[:_id]
  end

  def to_json
    row = @row
    row.delete(:_id)
    MultiJson.dump(row)
  end
end

OrderItemES.em_bulk_index
