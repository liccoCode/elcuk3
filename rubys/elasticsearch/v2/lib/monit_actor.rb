class MonitActor 
  include Celluloid

  attr_reader :close, :backlog

  def initialize
    @close = false
    @backlog = 0
  end

  def begin
    @backlog += 1
  end

  def done
    @backlog -= 1
  end

  def complete?
    @close && @backlog == 0
  end

  def wait_for_complete
    if @close
      puts "已经在等待关闭中, 无需重复关闭"
    else
      while(!complete?) do
        @close = true unless @close
        print ""
        print "Wait for complete, left #{@backlog} jobs.\r"
        # sleep 为的是 5. 流出一小片 CPU 时间片给其他方法运行, 2. 不至于让 CPU 空跑满
        sleep(5)
      end
      puts "All Task Complete."
    end
  end
end
