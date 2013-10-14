require "active_support/core_ext"
# 1. 设置时间天数
Time::DATE_FORMATS[:date] = "%Y-%m-%d"

def recaculate_records(begin_date, times)
  times ||= 3
  times.times do |i|
    cmd = "curl http://localhost:9000/login/job?date=#{(begin_date + i.days).to_s(:date)}"
    `#{cmd}`
  end
end

# 前 30, 29, 28 天
start = Time.now - 30.days
recaculate_records(start)

# 前  18, 17, 16 天
start = Time.now - 18.days
recaculate_records(start)

# 前 3, 2, 1 天的
start = Time.now - 3.days
recaculate_records(start)
