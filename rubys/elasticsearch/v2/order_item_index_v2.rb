require "./backend"

#ES_HOST = "http://gengar.easya.cc:9200"
ES_HOST = "http://192.168.1.99:9200"

#DB_HOST = "http://aggron.easya.cc"
DB_HOST = "localhost"
#DB_NAME = "elcuk2"
DB_NAME = "elcuk2_t"

class OrderItemActor
  include Celluloid

  # 引入 Actor 的公用方法
  include ActorBase

  def initialize
    init_attrs
    @http = Request.new
    @es_index = "elcuk2"
    @es_type = "orderitem"
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

  # 固定方法, 必须存在
  def bulk_submit(rows)
    submit(rows)
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
#process(actor: OrderItemActor.new)
OrderItemActor.new.init_mapping