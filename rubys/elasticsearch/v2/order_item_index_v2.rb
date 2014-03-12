require "celluloid/autostart"
require "multi_json"
require "httparty"
require "sequel"
require "./backend"

#ES_HOST = "http://gengar.easya.cc:9200"
ES_HOST = "http://192.168.1.99:9200"
ES_INDEX = "elcuk2"
ES_TYPE = "orderitem"
ES_URL = "#{ES_HOST}/#{ES_INDEX}/#{ES_TYPE}"

#DB_HOST = "http://aggron.easya.cc"
DB_HOST = "localhost"
#DB_NAME = "elcuk2"
DB_NAME = "elcuk2_t"

class OrderItemActor
  include Celluloid

  # 引入公用的 loop_check 方法
  include LoopCheck

  # Ruby 中定义 OrderItemActor 的 class instance variable. 类级别的实例变量, 类似与 Java 的 Class Variable
  # refer: http://www.railstips.org/blog/archives/2006/11/18/class-and-instance-variables-in-ruby/
  class << self
    attr_accessor :doc_size
    attr_accessor :wait_seconds
  end


  def initialize
    OrderItemActor.doc_size = 0
    OrderItemActor.wait_seconds = 0
    @http = Request.new
  end

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

  # 固定方法, 必须存在
  def bulk_submit(rows)
    OrderItemActor.doc_size += rows.size
    post_body = ""
    rows.each do |row|
      row[:date] = row[:date].utc.iso8601
      post_body << MultiJson.dump({ index: { "_index" => "elcuk2", "_type" => "orderitem", "_id" => row.delete(:id)} }) << "\n"
      post_body << MultiJson.dump(row) << "\n"
    end
    # refer: https://github.com/celluloid/celluloid/wiki/Futures
    future = @http.future.post("#{ES_HOST}/_bulk", body: post_body)
    loop_check(future)
  end
end


DB = Sequel.mysql2(DB_NAME, host: DB_HOST, user: 'root', password: 'crater10lake')
# select oi.createDate date, oi.selling_sellingId selling_id, oi.product_sku sku, oi.market, oi.quantity, oi.order_orderId order_id from OrderItem oi limit 10;
SQL = "SELECT oi.id, oi.createDate date, oi.selling_sellingId selling_id, oi.product_sku sku, oi.market, oi.quantity, oi.order_orderId order_id FROM OrderItem oi"
# =============================================================================================================
# 1. 初始化 OrderItemActor 用于多线程计算
# 2. 使用流的方式加载数据库中数据, 每 2000 行数据派发给 Actor 一个任务, 并且每个任务间隔 0.3s 控制内存以及总处理速度
# 3. Actor 内部使用异步 HTTP 来完成请求
# 4. 最后处理不满足 % 2000 数量剩下的数据
# =============================================================================================================
process(actor: OrderItemActor.new)