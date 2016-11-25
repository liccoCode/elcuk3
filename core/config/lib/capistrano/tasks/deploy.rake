namespace :deploy do
  desc "install supervisor"
  task :install_supervisor do
    on roles(:all) do |host|
      execute('apt-get install supervisor -y')
    end
  end

  desc "清除 CloudFlare 的缓存"
  task :purge_cache do
    on roles(:app, filter: :production) do
      run_locally do
        puts '正在清除 CloudFlare 缓存.'
        resp = HTTParty.delete('https://api.cloudflare.com/client/v4/zones/67bb7f5bb1fa7d5944b96838a34e162a/purge_cache',
                               headers: {'Content-Type' => 'application/json', 'X-Auth-Key' => 'b5ca2092465f6465526f861dca21c3a510c40', 'X-Auth-Email' => 'wyatt@easya.cc'},
                               body: {'purge_everything' => true}.to_json)
        puts "CloudFlare 处理结果: #{resp.body}"
      end
    end
  end
end