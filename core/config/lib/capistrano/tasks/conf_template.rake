namespace :conf do
  desc "use erb to render template conf file"
  task :application do
    on roles(:all) do |host|
      @jvm_opts = fetch(:jvm_opts)
      template("application.conf.erb", "#{current_path}/core/conf/application.conf", true)
    end
  end
end

def template(from, to, as_root = false)
  template_path = File.expand_path("../../templates/#{from}", __FILE__)
  template = ERB.new(File.new(template_path).read).result(binding)
  upload! StringIO.new(template), to

  execute :sudo, :chmod, "644 #{to}"
  execute :sudo, :chown, "root:root #{to}" if as_root == true
end
