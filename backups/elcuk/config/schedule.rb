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

# update locale time (GMT+8)
set :output, '~/cron.log'
every 4.hours do
  command '/usr/sbin/ntpdate 0.cn.pool.ntp.org'
end

every :monday, at: '01' do
  command 'ruby /root/rubys/weekly.rb'
end

every 5.minutes do
  command 'ruby /root/rubys/osticket.rb'
end
