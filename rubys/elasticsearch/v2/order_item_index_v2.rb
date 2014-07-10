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
            },
            "category_id": {
                "type": "string"
            },
            "msku": {
                "type": "string"
            },
            "state": {
                "type": "string"
            }
        }
    }
})

  # 固定方法, 必须存在
  def bulk_submit(rows)
    submit(rows)
  end

  def clear_cache 
    HTTParty.get("http://e.easya.cc/api/APICache/esCacheClear", headers: { 'AUTH_TOKEN' => 'baef851cab745d3441d4bc7ff6f27b28' } ).body
  end
end

# select oi.createDate date, oi.selling_sellingId selling_id, oi.product_sku sku, oi.market, oi.quantity, oi.order_orderId order_id from OrderItem oi limit 10;

SQL = %q(SELECT oi.id, oi.createDate date, oi.selling_sellingId selling_id, s.merchantSKU msku, oi.product_sku sku, p.category_categoryId category_id, oi.market, oi.quantity, oi.order_orderId order_id,o.state state
  FROM OrderItem oi
  LEFT JOIN Product p ON p.sku=oi.product_sku
  LEFT JOIN Selling s ON oi.selling_sellingId=s.sellingId
  LEFT JOIN Orderr o on oi.order_orderid=o.orderid
  WHERE oi.product_sku IS NOT NULL AND  oi.createDate>=?)

# =============================================================================================================
# 1. 初始化 OrderItemActor 用于多线程计算
# 2. 使用流的方式加载数据库中数据, 每 2000 行数据派发给 Actor 一个任务, 并且每个任务间隔 0.3s 控制内存以及总处理速度
# 3. Actor 内部使用异步 HTTP 来完成请求
# 4. 最后处理不满足 % 2000 数量剩下的数据
# =============================================================================================================
pool = OrderItemActor.pool(size: 6)
pool.init_mapping
process(dataset: DB[SQL, Time.parse('2012-01-01')].stream, actor: pool)
puts pool.clear_cache
