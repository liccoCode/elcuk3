# encoding: utf-8
require "httpclient"
require "gmail"

# 这里的 agent_name 调整后可有可无
clnt = HTTPClient.new(:agent_name => '77CD3A807B88CD0093940310D1FC4103')
res = clnt.get('http://t.easya.cc/api/cron.php')
#res = clnt.get('http://osticket.local/api/cron.php')
clnt.receive_timeout = 60000

exit if res.status == 200

puts "need warnning #{res.body}"
#Gmail.connect('wyatt@easyacceu.com', 'hfadantztmbchybf') do |gmail|
Gmail.connect('support@easyacceu.com', 'Fi0#GR4C^Y') do |gmail|
        gmail.deliver do
                #to "all@easyacceu.com"
                to "wppurking@gmail.com"
                #[日报] 3.19~3.23每日工作汇报+周总结和下周计划
                subject "OsTicket Fetch Mail 的线程有错误"
                html_part do
			content_type 'text/html; charset=UTF-8'
                        body "在 e.easya.cc 上的触发 OsTicket 抓取邮件的脚本有错误! 请立马检查"
                end
        end
end


