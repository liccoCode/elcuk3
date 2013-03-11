# encoding: UTF-8
require "rubygems"
require "gmail"
Gmail.connect('wyatt@easya.cc', 'rdgiacruxfbdjfuh') do |gmail|
  gmail.deliver do
  	from "EasyAcc <wyatt@easya.cc>"
    to "all@easya.cc"
    #to "wppurking@gmail.com"
    #[日报] 3.19~3.23每日工作汇报+周总结和下周计划
    subject "[日报] #{Time.now.strftime('%m.%d')}~#{(Time.now + 604800).strftime('%m.%d')}每日工作汇报+周总结和下周计划"
    html_part do
      body "Mailed by ruby script on e.easya.cc"
    end
  end
end
