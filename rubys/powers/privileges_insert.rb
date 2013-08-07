# encoding=utf-8
require "mysql"
require "pp"

action = ARGV[0]
action ||= 'insert'

conn = Mysql.new('localhost', 'root', 'crater10lake', 'elcuk2_t', 3306)
conn.query("SET NAMES UTF8")

st = if action == 'drop'
  conn.prepare("DELETE FROM `Privilege` WHERE name=?")
else
  conn.prepare("INSERT INTO `Privilege` (`memo`, `name`) VALUES (?, ?)")
end


powers = []
open('./powers', 'r').each do |line|
  #privileges.add(new Privilege("paymenttargets.index", "支付方式列表页面"));
  powers << line[29...-4].gsub(/"/, '').split(',').map(&:strip)
end

powers.each do |power|
  if action == 'drop'
    # 1. 查找 power id
    # 2. 删除 User_Privilege 权限
    # 3. 删除 Privilege
    begin
      id = conn.query("SELECT id from Privilege WHERE name='#{power[0]}'").fetch_row()[0]
      conn.query("DELETE FROM User_Privilege WHERE privileges_id=#{id}")
      puts "User_Privilege Power Relation is delete."
      st.execute(power[0])
      puts "#{power[0]}(#{power[1]}) was delete."
    rescue Exception => e
      puts e
    end
  else
    count = conn.query("SELECT COUNT(*) FROM Privilege WHERE name='#{power[0]}'", ).fetch_row()[0]
    if count.to_i > 0
      puts "     #{power[0]}(#{power[1]}) was added."
    else
      puts "#{power[0]}(#{power[1]}) was insert."
      st.execute(power[1], power[0])
    end
  end
end
