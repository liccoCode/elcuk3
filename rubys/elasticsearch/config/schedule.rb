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

every :day, :at => "8:30, 16:30, 23:25" do
  command 'cd /root/rubys/v2;ruby order_index_v2.rb'
end

every :day, :at => "8:20, 16:00, 23:30" do
  command 'cd /root/rubys/v2;ruby order_item_index_v2.rb'
end

every :day, :at => "9:00, 22:00" do
  command 'cd /root/rubys/v2;ruby procurepayunit_index_v2.rb'
end

every :day, :at => "9:05, 22:05" do
  command 'cd /root/rubys/v2;ruby shippayunit_index_v2.rb'
end

every :day, :at => "9:10, 21:10" do
  command 'cd /root/rubys/v2;ruby sale_fee_index_v2.rb'
end

every :day, :at => "8:45, 16:20" do
  command 'curl "http://job.easya.cc/api/JobsInitialize/initAbnormalFetchJob?auth_token=baef851cab745d3441d4bc7ff6f27b28"'
end

every :day, :at => "5:00" do
  command 'curl "http://job.easya.cc/api/JobsInitialize/initCategoryInfoFetchJob?auth_token=baef851cab745d3441d4bc7ff6f27b28"'
end

every :day, :at => "3:00am" do
  command 'curl "http://job.easya.cc/api/JobsInitialize/initTargetInfoFetchJob?auth_token=baef851cab745d3441d4bc7ff6f27b28&teamid=1"'
end

every :day, :at => "3:30am" do
   command 'curl "http://job.easya.cc/api/JobsInitialize/initTargetInfoFetchJob?auth_token=baef851cab745d3441d4bc7ff6f27b28&teamid=2"'
end

every :day, :at => "4:00am" do
  command 'curl "http://job.easya.cc/api/JobsInitialize/initTargetInfoFetchJob?auth_token=baef851cab745d3441d4bc7ff6f27b28&teamid=3"'
end

every :day, :at => "4:30am" do
  command 'curl "http://job.easya.cc/api/JobsInitialize/initTargetInfoFetchJob?auth_token=baef851cab745d3441d4bc7ff6f27b28&teamid=4"'
end

every :day, :at => "3:00, 9:00, 12:00, 15:00, 20:00" do
  command 'curl "http://rock.easya.cc:4567/salefee_update_sku"'
end

every 20.minutes do
  command 'curl "http://rock.easya.cc:4567/keep_session_power"'
end

every 4.hours do
  command 'curl "http://rock.easya.cc:4567/amazon_fba_capaticy_watcher"'
end

every 40.minutes do
 command 'curl "http://rock.easya.cc:4567/amazon_fbainventory_received"'
end

every 10.minutes do
 command 'curl "http://rock.easya.cc:4567/amazon_order_discover"'
end

every 30.minutes do
 command 'curl "http://rock.easya.cc:4567/amazon_fba_qty_sync"'
end

every 2.days do
 command 'curl "http://rock.easya.cc:4567/sale_fee_file_fetcher"'
end

every 1.days do
 command 'curl "http://rock.easya.cc:4567/sale_fee_click"'
end

every 2.hours do
 command 'curl "http://rock.easya.cc:4567/amazon_order_update"'
end

every 5.minutes do
 command 'curl "http://rock.easya.cc:4567/amazon_orderitem_discover"'
end

every :day, :at => "7:00, 15:00, 23:00" do
 command 'curl "http://rock.easya.cc:4567/amazon_order_fetcher"'
end

every 5.minutes do
 command 'curl "http://rock.easya.cc:4567/amazon_review_check"'
end

every 5.minutes do
 command 'curl "http://rock.easya.cc:4567/amazon_review_crawl"'
end

every 30.minutes do
 command 'curl "http://rock.easya.cc:4567/order_mail_check"'
end

every 20.minutes do
 command 'curl "http://rock.easya.cc:4567/amazon_finace_check_job"'
end

every 60.minutes do
 command 'curl "http://rock.easya.cc:4567/amazon_selling_sale_price"'
end

every :day, :at => "0:20" do
 command 'curl "http://rock.easya.cc:4567/feedback_and_review_notification"'
end

every 5.minutes do
 command 'curl "http://rock.easya.cc:4567/order_info_fetch"'
end

every 5.minutes do
 command 'curl "http://rock.easya.cc:4567/feedback_check"'
end

every 3.hours do
 command 'curl "http://rock.easya.cc:4567/feedback_crawl"'
end

every 4.hours do
 command 'curl "http://rock.easya.cc:4567/selling_record_check"'
end

every 4.hours do
 command 'curl "http://rock.easya.cc:4567/shipment_sync"'
end

every 'monday', :at => '8:30 am' do
  command 'curl "http://rock.easya.cc:4567/selling_inventory_reports"'
end

every 'monday', :at => '8:30 am' do
  command 'curl "http://rock.easya.cc:4567/inventory_rationality_report"'
end

every 1.days do
 command 'curl "http://rock.easya.cc:4567/selling_cycle_monthly_report"'
end

every '0 0 15 * *' do
 command 'curl "http://rock.easya.cc:4567/sku_month_profit"'
end

every '0 0 16 * *' do
 command 'curl "http://rock.easya.cc:4567/profit_report_year"'
end

every '0 0 15 * *' do
 command 'curl "http://rock.easya.cc:4567/pay_bill_detail_reports"'
end
