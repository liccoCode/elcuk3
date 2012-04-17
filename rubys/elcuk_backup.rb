require "rubygems"
=begin
    备份三个服务器
	1. 检查当天的数据是否备份, 如果没备份则备份数据库, 将当天的数据库备份文件下载回来
	2. 备份服务器的文件
=end

print 'Back Server: 1. Elcuk2; 2. Redmine; 3. OsTicket: '
server = gets

#myMac /Volumes/wyatt/backup
#e.eac ~/backup-db
#r.eac ~/
paths = {
	:mac => '/Volumes/wyatt/backup/',
	:e => '~/backup-db/',
	:t => '~/backup-db/'
}

# 服务器的地址
urls = {
	:e => 'root@e.easyacceu.com',
	:r => 'root@r.easyacceu.com',
	:t => 'root@t.easyacceu.com'
}

# 数据库的名称
dbname = {
	:e => 'elcuk2',
	:r => 'redmine',
	:t => 'osticket'
}

# 服务器需要备份的上传文件的地址
upload_files = {
	:elog => '~/elcuk2/core/logs/',
	:edata => '~/elcuk2-data/',
	:r => '/opt/redmine/files/',
	:t => '/opt/osticket/files/'
}

backupPath = ''
a = ''
dbn = ''


# dbname: 数据库名称
# url: ssh, scp 登陆的服务器地址
# backpath: 本地备份的地址
def back_db(dbname, url, backpath)
	ssh = "ssh #{url} "
	scp = "scp #{url}:"
	date = Time.new.strftime('%F')

	#backup-db check
	puts 'Checking Database File...'
	system(ssh + "'echo \"if [ ! -d \"~/backup-db\" ]; then mkdir ~/backup-db; fi\" > f;sh f;rm -rf f;'")
	exist = system(ssh + "'test -e ~/backup-db/#{dbname}.sql.#{date}.tar.gz'")
	puts(exist ? "#{dbname} File Exsit, so just copy it to #{backpath}." : "#{dbname} File is not exist, first dump it, then copy it to #{backpath}.")

=begin
	使用 7zip 压缩; 内存不够用...
	system(SSH + "'mysqldump -uroot -pcrater10lake elcuk > ~/backup/elcuk.sql.#{date};7z a ~/backup/elcuk.sql.#{date}.7z ~/backup/elcuk.sql.#{date};rm ~/backup/elcuk.sql.#{date}'") if not exist
		
	system(SCP + "~/backup/elcuk.sql.#{date}.7z /Volumes/wyatt/backup/")
=end

	#使用 tar czf
	system(ssh + "'mysqldump -uroot -pcrater10lake #{dbname} > ~/backup-db/#{dbname}.sql.#{date};" + 
	"cd ~/backup-db;tar czf #{dbname}.sql.#{date}.tar.gz #{dbname}.sql.#{date};rm ~/backup-db/#{dbname}.sql.#{date}'") if not exist
	system(scp + "~/backup-db/#{dbname}.sql.#{date}.tar.gz #{backpath}")
end

def checkdir(dir)
	%x(echo "if [ ! -d "#{dir}" ]; then mkdir #{dir}; fi" | bash -s)
end


def back_files(from, to, serv)
	# rsync 
	puts ''
	puts "Backup #{from} to #{to}..."
	system("rsync -azx --progress #{serv}:#{from} #{to}")
end


checkdir('~/server_bak')
if server.to_i == 1 # e.easyacceu.com
	back_db(dbname[:e], urls[:e], paths[:mac])
	back_files(upload_files[:elog], '~/elcuk2-plogs/', urls[:e])
	back_files(upload_files[:edata], '~/elcuk2-data/', urls[:e])
elsif server.to_i == 2 # r.easyacceu.com
	back_db(dbname[:r], urls[:r], paths[:mac])
	checkdir('~/server_bak/redmine')
	back_files(upload_files[:r], '~/server_bak/redmine', urls[:r])
elsif server.to_i == 3 # t.easyacceu.com
	back_db(dbname[:t], urls[:t], paths[:mac])
	checkdir('~/server_bak/osticket')
	back_files(upload_files[:t], '~/server_bak/osticket', urls[:t])
else
	puts 'Please input 1 or 2 or 3!'
	exit
end
