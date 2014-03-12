require "bundler/setup"

# 让所有引入了 backend 的文件, 正常使用 Gemfile 里面的依赖, 不用再手动调用 require
Bundler.require(:default)

# 让 HTTP 请求变为异步处理
class Request
  include Celluloid

  def post(url, params)
    HTTParty.post(url, params)
  end
end

module ActorBase 

  def es_url
    "#{ES_HOST}/#{@es_index}/#{@es_type}"
  end


  # 初始化 orderitem type 的 mapping
  def init_mapping
    resp = HTTParty.put("#{es_url}/_mapping", body: self.class.const_get('MAPPING'))
    if resp.code != 200
      puts resp.body
    else
      true
    end
  end


  # 用来处理 bulk_submit, 变化的动态特性使用传入 block 完成
  def submit(rows, &block)
    self.class.doc_size += rows.size
    post_body = ""
    if block_given?
      rows.each { |row| block.call(row, post_body) }
    else
      rows.each do |row|
        row[:date] = row[:date].utc.iso8601
        post_body << MultiJson.dump({ index: { "_index" => @es_index, "_type" => @es_type, "_id" => row.delete(:id)} }) << "\n"
        post_body << MultiJson.dump(row) << "\n"
      end
    end
    # refer: https://github.com/celluloid/celluloid/wiki/Futures
    future = @http.future.post("#{ES_HOST}/_bulk", body: post_body)
    loop_check(future)
  end

  # 循环检查是否执行完成
  def loop_check(future)
    loop do
      # refer: http://rubydoc.info/gems/celluloid/Celluloid/Future
      if future.ready? 
        resp = future.value
        if resp.code == 200
          print "Submit Response Code is #{resp.code} and Deals #{self.class.doc_size} docs...\r"
        else
          puts resp.body
        end
        break
      else
        sleep(1)
      end
    end
  end

  # refer: http://www.railstips.org/blog/archives/2009/05/15/include-vs-extend-in-ruby/
  def self.included(mod)
    # 动态添加 es 相关的两个参数
    mod.instance_eval do
      attr_reader :es_index
      attr_reader :es_type
    end

    # Ruby 中定义 OrderItemActor 的 class instance variable. 类级别的实例变量, 类似与 Java 的 Class Variable
    # refer: http://www.railstips.org/blog/archives/2006/11/18/class-and-instance-variables-in-ruby/
    class << mod
      attr_accessor :doc_size
      attr_accessor :wait_seconds
    end

    def init_attrs
      [:doc_size, :wait_seconds].each do |att|
        # 这里使用 self.class 而非 self 是因为, 当真正调用 init_attrs 方法的时候, self 已经为 include 这个 module 的 actor 实例了
        # 所以需要使用其 class 去设置 class instance variable
        self.class.send("#{att}=", 0)
      end
    end
  end
end



# dataset: 传入处理好的 Sequel DataSet. 会尝试动态获取当前环境下的 DB[SQL].stream API
# actor: 用于处理数据的 Celluloid Actor. require
# docs: 可选的文档数量. default: []
# interval: 提交 actor 任务的时间间隔. default: 0.4
def process(dataset: DB[SQL].stream, actor: nil, docs: [], interval: 0.4)
  check_es_server(actor)
  if actor.respond_to?(:bulk_submit)
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
  else
    raise "#{actor.class} must have [bulk_submit] method"
  end
end

def check_es_server(actor)
  HTTParty.get(actor.es_url)
end
