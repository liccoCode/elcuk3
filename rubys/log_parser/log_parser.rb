require 'active_support/core_ext'

class P
  attr_accessor :file, :processor
  def initialize
    @file = File.new('./elcuk2.log')
    @processor = nil
  end
end

module Processor
  attr_accessor :body
  attr_reader :last_line

  def initialize line
    @body = [line]
  end


  def process line
    if body[0] != line && line.include?('[play]')
      @last_line = line
    else
      @body << line
    end
  end

  def end
    if @last_line
      true
    else
      false
    end
  end
end

# Server 500 错误的处理
class ServerErrorProcessor
  include Processor

  attr_accessor :eid

  class << self
    attr_accessor :msgs
  end

  @msgs = []


  def initialize error_line
    @eid = error_line[1..-1].strip
    @body = []
  end

  def process line
    if line.include?('[play]')
      @last_line = line
    elsif line.include?('@')
      @body << line
    else
      @body << line
    end
  end

  def end
    ServerErrorProcessor.msgs << {eid: @eid, body: @body}
    super
  end
end

# Warn 信息的处理
class WarnProcessor
  include Processor

  class << self
    attr_accessor :msgs
  end

  @msgs = []

  def end
    WarnProcessor.msgs << {body: @body}
    super
  end
end

# Error 信息的处理
class ErrorProcessor
  include Processor

  class << self
    attr_accessor :msgs
  end

  @msgs = []

  def end
    ErrorProcessor.msgs << {body: @body}
    super
  end
end


# 处理 line 的方法, 派送具体的处理类
def process_line p, line
  if line.index('@') == 0
    p.processor = ServerErrorProcessor.new(line) until p.processor
  elsif line.include?('WARN')
    p.processor = WarnProcessor.new(line) until p.processor
  elsif line.include?('ERROR')
    p.processor = ErrorProcessor.new(line) until p.processor
  end

  if p.processor
    p.processor.process line
    if p.processor.end
      p.processor = nil
    else
      p.processor.last_line
    end
  end
end


# ------------------------------------------------

#`scp -C root@e.easya.cc:/root/elcuk2/core/logs/elcuk2.log ./`

p = P.new
p.file.each do |line|
  line = line.strip
  if not line.blank?
    last_line = process_line p, line
    process_line p, line if last_line
  end
end


open_files = ['ServerError', 'Warn', 'Error']
open_files.each do |filename|
  File.open("./#{filename}.html", 'w') do |io|
    msg_hashs = eval("#{filename}Processor.msgs")
    msg_hashs.sort do |a, b|
      a[:body].size <=> b[:body].size
    end
    msg_hashs.each do |hash|
      hash[:body].each do |line|
        io.write line + "<br>"
      end
    end
  end
end

open_files.each do |filename|
  `open #{filename}.html`
end

