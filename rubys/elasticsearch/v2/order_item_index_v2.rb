require "./backend"

class OrderItemActor
  include Celluloid

  # 引入 Actor 的公用方法
  include ActorBase

  def initialize
    init_attrs
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

# select oi.createDate date, oi.selling_sellingId selling_id, oi.product_sku sku, oi.market, oi.quantity, oi.order_orderId order_id from OrderItem oi limit 10;
SQL = "SELECT oi.id, oi.createDate date, oi.selling_sellingId selling_id, oi.product_sku sku, oi.market, oi.quantity, oi.order_orderId order_id FROM OrderItem oi WHERE oi.product_sku IS NOT NULL"
# =============================================================================================================
# 1. 初始化 OrderItemActor 用于多线程计算
# 2. 使用流的方式加载数据库中数据, 每 2000 行数据派发给 Actor 一个任务, 并且每个任务间隔 0.3s 控制内存以及总处理速度
# 3. Actor 内部使用异步 HTTP 来完成请求
# 4. 最后处理不满足 % 2000 数量剩下的数据
# =============================================================================================================
process(actor: OrderItemActor.pool(size: 6))
#OrderItemActor.new.init_mapping
