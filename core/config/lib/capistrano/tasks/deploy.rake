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
      puts '正在清除 CloudFlare 缓存.'
      run_locally do
        execute(:curl,
                '-X DELETE',
                "https://api.cloudflare.com/client/v4/zones/#{fetch(:zone_identifier)}/purge_cache",
                '-H "X-Auth-Email: wyatt@easya.cc"',
                '-H "X-Auth-Key: b5ca2092465f6465526f861dca21c3a510c40"',
                '-H "Content-Type: application/json"',
                '--data \'{"purge_everything":true}\''
        )
      end
    end
  end
end