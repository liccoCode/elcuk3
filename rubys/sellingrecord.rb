require "active_support/core_ext"
# 1. 设置时间天数
# 2. 两个线程各自计算
start = Time.parse('2013-08-04')
Time::DATE_FORMATS[:date] = "%Y-%m-%d"

6.times do |i|
  cmd = "curl http://localhost:9000/login/job?date=#{(start + i.days).to_s(:date)}"
  puts cmd
  puts `#{cmd}`
end

