namespace :log do

  desc '查看 Elcuk2 log'
  task :elcuk2 do
    on roles(:app) do |host|
      within("#{current_path}/core") do
        execute(:tail, '-f', 'logs/elcuk2.log')
      end
    end
  end
end