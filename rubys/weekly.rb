# encoding: UTF-8
require "rubygems"
require "gmail"
Gmail.connect('support@easyacceu.com', 'Fi0#GR4C^Y') do |gmail|
  gmail.deliver do
    to "c@easyacceu.com,p@easyacceu.com,m@easyacceu.com,s@easyacceu.com"
    #to "wppurking@gmail.com"
    #[日报] 3.19~3.23每日工作汇报+周总结和下周计划
    subject "[日报] #{Time.now.strftime('%m.%d')}~#{(Time.now + 604800).strftime('%m.%d')}每日工作汇报+周总结和下周计划"
    html_part do
      body "Mailed by ruby script on e.easyacceu.com"
    end
  end
end
