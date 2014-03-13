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

  def routine_sku(skus)
    if skus.is_a?(Array)
      skus.map { |sku| routine_sku(sku) }
    elsif skus.is_a?(String)
      skus.split("-").join("")
    end
  end

  def routine_selling_id(selling_ids)
    if selling_ids.is_a?(Array)
      selling_ids.map { |selling_id| routine_selling_id(selling_id) }
    elsif selling_ids.is_a?(String)
      selling_ids.delete('-').delete(',').delete('|')
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
      row[:sku] = routine_sku(row[:sku]) if row[:sku]
      row[:selling_id] = routine_selling_id(row[:selling_id]) if row[:selling_id]
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