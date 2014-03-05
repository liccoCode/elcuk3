require "mysql2"
require "httparty"
require "multi_json"


class ESWorker < Thread
	def initialize(dcs)
		super(dcs) do |docs|
			post_data = ""
			docs.each do |doc|
				post_data << MultiJson.dump({index: {_index: "elcuk2", _type: "salefee", _id: doc["salefeeId"]}}) << "\n"
				post_data << MultiJson.dump(doc) << "\n"
			end
			resp = HTTParty.post("http://gengar.easya.cc:9200/_bulk", body: post_data)
			puts resp.body if resp.code != 200
		end
	end
end

SQL = %q(SELECT s.id salefeeId, s.`date`, s.market, s.account_id, s.usdCost costInUSD, s.type_name, oi.selling_sellingId, oi.product_sku FROM SaleFee s
 left join OrderItem oi ON oi.order_orderId=s.order_orderId where s.date>='2013-12-30')

clt = Mysql2::Client.new(host: 'aggron.easya.cc', username: 'root', password: 'crater10lake', database: 'elcuk2')

rs = clt.query(SQL, stream: true)

i = 0
docs = []
rs.each do |row|
	i += 1
	row['date'] = row['date'].utc.iso8601
	docs << row
	if i % 2000 == 0
		ESWorker.new(docs.dup)
		docs.clear
	end
	print "Index SaleFee ... #{i} / 4326633\r"
	#print "Index SaleFee ... #{i} / 3034267\r"
end
ESWorker.new(docs.dup) if docs.size > 0
puts "----------------------"
# 等待 20 s, 等待后面的线程执行完成
sleep(20)
