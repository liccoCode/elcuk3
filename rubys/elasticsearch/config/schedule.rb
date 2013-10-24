# Use this file to easily define all of your cron jobs.
#
# It's helpful, but not entirely necessary to understand cron before proceeding.
# http://en.wikipedia.org/wiki/Cron

# Example:
#
# set :output, "/path/to/my/cron_log.log"
#
# every 2.hours do
#   command "/usr/bin/some_great_command"
#   runner "MyModel.some_method"
#   rake "some:great:rake:task"
# end
#
# every 4.days do
#   runner "AnotherModel.prune_old_records"
# end

# Learn more: http://github.com/javan/whenever
set :output, '~/cron.log'
every 4.hours do
  command '/usr/sbin/ntpdate 0.cn.pool.ntp.org'
end

every :day, :at => "3:00" do
  command 'ruby /root/rubys/order_index_em.rb'
end

every :day, :at => "3:30" do
  command 'ruby /root/rubys/order_item_index_em.rb'
end