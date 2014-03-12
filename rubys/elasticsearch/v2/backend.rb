# 让 HTTP 请求变为异步处理
class Request
  include Celluloid

  def post(url, params)
    HTTParty.post(url, params)
  end
end


# dataset: 传入处理好的 Sequel DataSet. require
# actor: 用于处理数据的 Celluloid Actor. require
# docs: 可选的文档数量. default: []
# interval: 提交 actor 任务的时间间隔. default: 0.4
def process(dataset: nil, actor: nil, docs: [], interval: 0.4)
  dataset.each_with_index do |row, i|
    # deal rows....
    # row[id, date, selling_id, sku, market, quantity, order_id,]
    docs << row
    if (i > 0) && (i % 2000 == 0)
      actor.async.bulk_submit(docs.slice!(0..-1))
      # 避免内存增长过快, 并且可以控制处理的总时长
      sleep(interval)
    end
  end

  if docs.size > 0
    actor.bulk_submit(docs.slice!(0..-1))
  else
    # 如果无法调用一次 bulk_submit 则暂停 6 秒
    sleep(6)
  end
end
