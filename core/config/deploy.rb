# config valid only for current version of Capistrano
lock '3.5.0'

set :application, 'elcuk2'
set :repo_url, 'ssh://git@tig.easyacc.com:21022/ea/elcuk2.git'
set :deploy_to, '/root/cap_elcuk2'

# Default branch is :master
# ask :branch, `git rev-parse --abbrev-ref HEAD`.chomp

# Default deploy_to directory is /var/www/my_app_name
# set :deploy_to, '/var/www/my_app_name'

# Default value for :scm is :git
# set :scm, :git

# Default value for :format is :pretty
# set :format, :pretty

# Default value for :log_level is :debug
# set :log_level, :debug

# Default value for :pty is false
# set :pty, true

# Default value for :linked_files is []
# set :linked_files, fetch(:linked_files, []).push('config/database.yml', 'config/secrets.yml')

# Default value for linked_dirs is []
# set :linked_dirs, fetch(:linked_dirs, []).push('log', 'tmp/pids', 'tmp/cache', 'tmp/sockets', 'vendor/bundle', 'public/system')
set :linked_dirs, fetch(:linked_dirs, []).push('core/logs')

# Default value for default_env is {}
set :default_env, {JAVA_HOME: '/opt/jdk1.8.0_102', PATH: '$JAVA_HOME/bin:$PATH', CLASSPATH: '$JAVA_HOME/lib'}

# Default value for keep_releases is 5
# set :keep_releases, 5
set :keep_releases, 3

set :ssh_options, {keys: %w(/home/dev/.ssh/dev.key)}

# 注册 play 命令
SSHKit.config.command_map[:play] = '/opt/play-1.4.2/play'

# rbenv
set :rbenv_type, :user
set :rbenv_ruby, '2.3.0'
set :rbenv_prefix, "RBENV_ROOT=#{fetch(:rbenv_path)} RBENV_VERSION=#{fetch(:rbenv_ruby)} #{fetch(:rbenv_path)}/bin/rbenv exec"
set :rbenv_map_bins, %w{rake gem bundle ruby}
set :rbenv_roles, :all

namespace :deploy do
  task :restart do
    on roles(:app) do
      within("#{current_path}/core") do
        execute(:play, 'deps --sync')
        execute(:supervisorctl, 'restart', 'erp')
      end
    end
  end

  # 在完成发布之后
  after 'deploy:publishing', 'conf:application'
  after 'deploy:published', :purge_cache
  after 'deploy:published', :restart
end
