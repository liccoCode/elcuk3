require "bundler/setup"
# 让所有引入了 backend 的文件, 正常使用 Gemfile 里面的依赖, 不用再手动调用 require
Bundler.require(:default)
Dir['lib/*.rb'].each { |file| require File.expand_path(file) }


# =============================== setup const ================================
ES_HOST = "http://gengar.easya.cc:9200"
#ES_HOST = "http://192.168.1.150:9200"

DB_HOST = "http://aggron.easya.cc"
#DB_HOST = "localhost"
DB_NAME = "elcuk2"
#DB_NAME = "elcuk2_t"

DB = Sequel.mysql2(DB_NAME, host: DB_HOST, user: 'root', password: 'crater10lake')
# ============================================================================


# dataset: 传入处理好的 Sequel DataSet. 会尝试动态获取当前环境下的 DB[SQL].stream API
# actor: 用于处理数据的 Celluloid Actor/PoolManager. require
# docs: 可选的文档数量. default: []
# interval: 提交 actor 任务的时间间隔. default: 0.4
def process(dataset: DB[SQL].stream, actor: nil, docs: [], interval: 0.1)
  check_es_server(actor)
  if actor.respond_to?(:bulk_submit)
    MonitActor.supervise_as(:monit)

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