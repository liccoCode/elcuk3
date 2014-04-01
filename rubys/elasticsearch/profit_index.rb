require "sequel"
require "httparty"
require "multi_json"

ES_HOST = "http://localhost:9200"
DB_HOST = "localhost"

MAPPING = <<E
{ "profit": { "properties": { "salefeeId": { "type": "string"}, "createdate": { "type": "date", "format": "dateOptionalTime" }, 
"market": { "type": "string" }, "account_id": { "type": "string" }, "costInUSD": { "type": "string" }, "type_name": { "type": "string" }, 
"selling_sellingId": { "type": "string" }, "product_sku": { "type": "integer" } } } }
E


class ESWorker < Thread
  
  INDEX = "elcuk2"
  TYPE = "profit"
  DOCID = "profitId"

  def initialize(dcs, i)
    super(dcs, i) do |docs, i|
      print "Index Profit ... #{i} / 4326633\r"
      post_data = ""
      docs.each do |doc|
        post_data << MultiJson.dump({index: {_index: ESWorker::INDEX, _type: ESWorker::TYPE, _id: ESWorker::DOCID}}) << "\n"
        post_data << MultiJson.dump(doc) << "\n"
      end
      resp = HTTParty.post("#{ES_HOST}/_bulk", body: post_data)
      puts resp.body if resp.code != 200
    end
  end

  def init_mapping
    http = EM::HttpRequest.new("#{ES_HOST}/#{ESWorker::INDEX}/#{ESWorker::TYPE}/_mapping").put(body: MAPPING)
    puts http.response if not http.response_header.successful?
  end
end

 
#DB = Sequel.mysql2('elcuk2', host: 'aggron.easya.cc', user: 'root', password: 'crater10lake')
DB = Sequel.mysql2('elcuk2_t', host: DB_HOST,user: 'root', password: 'crater10lake') 


# SQL 语句
SQL = %q(SELECT s.id salefeeId, s.`date` createdate, s.market, s.account_id, s.usdCost costInUSD, s.type_name, oi.selling_sellingId, oi.product_sku FROM SaleFee s
 left join OrderItem oi ON oi.order_orderId=s.order_orderId where s.date>='2012-01-01')

i = 0
docs = []
DB.fetch(SQL) do |row|
  i += 1
  row[:createdate] = row[:createdate].utc.iso8601
  docs << row
  if i % 2000 == 0
    ESWorker.new(docs.dup, i)
    docs.clear
  end   
end

ESWorker.new(docs.dup, i) if docs.size > 0
puts "----------------------"
# 等待 20 s, 等待后面的线程执行完成
sleep(20)








 