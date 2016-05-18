namespace :deploy do
	desc "install supervisor"
	task :install_supervisor do
		on roles(:web)	do |host|
			execute('apt-get install supervisor -y')
		end	
	end
end