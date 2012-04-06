require "logger"

loger = Logger.new('/var/log/nginx_watcher.log', 'weekly')
nginxPros = %x(ps -ef | grep nginx | grep -v 'grep' | grep -v 'watcher')

def nginx_start
	%x(nginx)
end

if nginxPros.split("\n").size == 0
	nginx_start
else
	nginxPros.split("\n").each do |line|
		if not line.index('nginx: master process nginx')
			loger.info('Nginx has restart!')
		else
			loger.info('Nginx has exist.')
		end
	end
end
