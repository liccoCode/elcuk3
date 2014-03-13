require "bundler/setup"

# 让所有引入了 backend 的文件, 正常使用 Gemfile 里面的依赖, 不用再手动调用 require
Bundler.require(:default)




# =============================== setup const ================================
#ES_HOST = "http://gengar.easya.cc:9200"
ES_HOST = "http://192.168.1.99:9200"

#DB_HOST = "http://aggron.easya.cc"
DB_HOST = "localhost"
#DB_NAME = "elcuk2"
DB_NAME = "elcuk2_t"

DB = Sequel.mysql2(DB_NAME, host: DB_HOST, user: 'root', password: 'crater10lake')

# ============================================================================

class MoniActor
  include Celluloid

  attr_reader :close, :backlog

  def initialize
    @close = false
    @backlog = 0
  end

  def begin
    @backlog += 1
  end

  def done
    @backlog -= 1
  end

  def complete?
    @close && @backlog == 0
  end

  def wait_for_complete
    if @close
      puts "已经在等待关闭中, 无需重复关闭"
    else
      while(!complete?) do
        @close = true unless @close
        print ""
        print "Wait for complete, left #{@backlog} jobs.\r"
        # sleep 为的是 1. 流出一小片 CPU 时间片给其他方法运行, 2. 不至于让 CPU 空跑满
        sleep(0.5)
      end
      puts "All Task Complete."
    end
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
    # 需要保证获取任务的时候一定在 done 前面, 所以用同步方法
    Celluloid::Actor[:monit].begin
    post_body = ""
    # 如果有闭包, 则调用闭包的方法处理一次 rows
    rows.map! { |row| block.call(row) } if block_given?
    rows.each do |row|
      row[:date] = row[:date].utc.iso8601
      post_body << MultiJson.dump({ index: { "_index" => @es_index, "_type" => @es_type, "_id" => row.delete(:id)} }) << "\n"
      post_body << MultiJson.dump(row) << "\n"
    end
    # refer: https://github.com/celluloid/celluloid/wiki/Futures
    resp = HTTParty.post("#{ES_HOST}/_bulk", body: post_body)
    self.class.doc_size += rows.size
    print "Http Code: #{resp.code}; Handled: #{self.class.doc_size} docs #{self.current_actor}\r"
    Celluloid::Actor[:monit].async.done
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
# actor: 用于处理数据的 Celluloid Actor/PoolManager. require
# docs: 可选的文档数量. default: []
# interval: 提交 actor 任务的时间间隔. default: 0.4
def process(dataset: DB[SQL].stream, actor: nil, docs: [], interval: 0.1)
  check_es_server(actor)
  if actor.respond_to?(:bulk_submit)
    MoniActor.supervise_as(:monit)

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
      puts "Last #{docs.size} rows from DB."
      actor.async.bulk_submit(docs.slice!(0..-1))
    end

    Celluloid::Actor[:monit].wait_for_complete
  else
    raise "#{actor.class} must have [bulk_submit] method"
  end
end

def check_es_server(actor)
  HTTParty.get(actor.es_url)
end