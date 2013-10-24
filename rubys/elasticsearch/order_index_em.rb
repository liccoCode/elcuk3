require "em-synchrony"
require "em-synchrony/em-http"
require "em-synchrony/mysql2"
require "time"
require "multi_json"
require "pp"

SQL = <<SQL
select o.orderId, o.account_id, concat_ws(' ', o.address, o.address1) address, o.arriveDate, o.createDate, o.paymentDate, o.shipDate, o.buyer, o.email, o.market, o.state, o.trackNo, o.userid, group_concat(oi.selling_sellingId, "@") sids 
from Orderr o
left join OrderItem oi ON o.orderId=oi.order_orderId
group by o.orderId
SQL

MAPPING = <<M
{"order": {"properties": {"account_id": {"type": "integer"},"address": {"type": "string"},"arriveDate": {"type": "date","format": "dateOptionalTime" }, "buyer": { "type": "string" }, "createDate": { "type": "date", "format": "dateOptionalTime" }, "email": { "type": "string" }, "market": { "type": "string" }, "orderId": { "type": "string" }, "paymentDate": { "type": "date", "format": "dateOptionalTime" }, "shipDate": { "type": "date", "format": "dateOptionalTime" }, "sids": { "type": "string" }, "state": { "type": "string" }, "trackNo": { "type": "string" }, "userid": { "type": "string" } } } }
M
ES_HOST = "http://localhost:9200"
DB_HOST = "localhost"

class OrderES
  INDEX = "elcuk2"
  TYPE = "order"
  DATE_COLUMNS = %w(arriveDate paymentDate createDate shipDate)

  class << self
    def bulk_index
      post_data = ""
      @docs.each do |es|
        post_data << MultiJson.dump({index: {_index: OrderES::INDEX, _type: OrderES::TYPE, _id: es._id} }) << "\n"
        post_data << es.to_json << "\n"
      end
      http = EM::HttpRequest.new("#{ES_HOST}/_bulk").post(body: post_data)
      puts http.response if not http.response_header.successful?
      @docs.clear
      # every bulk wait 0.5
      sleep(0.5)
    end

    def add_to_bulk(es)
      @docs ||= []
      es.flat_all
      @docs << es
    end
  end

  attr_reader :type

  def initialize(row)
    @row = row
    @row[:_id] = @row['orderId']
  end

  def _id
    @row[:_id]
  end

  def sids_column_flat
    @row['sids'] = @row['sids'].split('@,').map { |s| s.delete('@') } if @row['sids']
  end

  def date_column_flat
    OrderES::DATE_COLUMNS.each do |column|
      begin
        @row[column] = @row[column].utc.iso8601
      rescue Exception => e
      end
    end
  end

  def flat_all
    sids_column_flat
    date_column_flat
  end

  def post_index
    flat_all
    http = EM::HttpRequest.new("#{ES_HOST}/#{OrderES::INDEX}/#{OrderES::TYPE}/#{_id}").put(body: to_json)
    puts http.response if not http.response_header.successful?
  end

  def to_json
    row = @row
    row.delete(:_id)
    MultiJson.dump(row)
  end

end

# 批处理
def em_bulk_begin
  EM.synchrony do
    clt = Mysql2::EM::Client.new(host: DB_HOST, user: 'root', pass: 'crater10lake', db: 'elcuk2')

    rs = clt.query(SQL, stream: true)

    i = 0
    rs.each do |row|
      i += 1
      OrderES.add_to_bulk(OrderES.new(row))
      OrderES.bulk_index if i % 1000 == 0

      # us: 68989,  uk: 112474,  de: 482057
      print "Index orders ...  #{i} / 68989\r"
    end
    OrderES.bulk_index

    EM.stop
  end
end

# 单个处理
def em_index_begin
  EM.synchrony do
    clt = Mysql2::EM::Client.new(host: 'localhost', user: 'root', pass: 'crater10lake', db: 'elcuk2')

    rs = clt.query(SQL, stream: true)

    i = 0
    rs.each do |row|
      i += 1
      doc = OrderES.new(row)
      doc.post_index
      print "Index orders ...  #{i} / 482057\r"
    end

    EM.stop
  end
end

em_bulk_begin






