require "rubygems"
=begin
    检查当天的数据是否备份, 如果没备份则备份数据库
    将当天的数据库备份文件下载回来
=end

print 'Back Server: 1. e.easyacceu.com; 2. r.easyacceu.com: '
server = gets

#myMac /Volumes/wyatt/backup
#e.eac ~/backup-db
#r.eac ~/
paths = {
	:mac => '/Volumes/wyatt/backup',
	:e => '~/backup-db'
}

urls = {
	:e => 'root@e.easyacceu.com',
	:r => 'root@r.easyacceu.com'
}

dbname = {
	:e => 'elcuk2',
	:r => 'redmine'
}

backupPath = ''
a = ''
dbn = ''

if server.to_i == 1
	a = urls[:e]
	backupPath = paths[:mac]
	dbn = dbname[:e]
elsif server.to_i == 2
	a = urls[:r]
	backupPath = paths[:mac]
	dbn = dbname[:r]
else
	puts 'Please input 1 or 2!'
	exit
end


SSH = "ssh #{a} "
SCP = "scp #{a}:"
date = Time.new.strftime('%F')

#backup-db check
system(SSH + "'echo \"if [ ! -d \"~/backup-db\" ]; then mkdir ~/backup-db; fi\" > f;sh f;rm -rf f;'")
exist = system(SSH + "'test -e ~/backup-db/#{dbn}.sql.#{date}.tar.gz'")
puts(exist ? "#{dbn} File Exsit, so just copy it." : "#{dbn} File is not exist, first dump it, then copy it.")

=begin
使用 7zip 压缩; 内存不够用...
system(SSH + "'mysqldump -uroot -pcrater10lake elcuk > ~/backup/elcuk.sql.#{date};7z a ~/backup/elcuk.sql.#{date}.7z ~/backup/elcuk.sql.#{date};rm ~/backup/elcuk.sql.#{date}'") if not exist
	
system(SCP + "~/backup/elcuk.sql.#{date}.7z /Volumes/wyatt/backup/")
=end
#使用 tar czf
system(SSH + "'mysqldump -uroot -pcrater10lake #{dbn} > ~/backup-db/#{dbn}.sql.#{date};" + 
"cd ~/backup-db;tar czf #{dbn}.sql.#{date}.tar.gz #{dbn}.sql.#{date};rm ~/backup-db/#{dbn}.sql.#{date}'") if not exist
system(SCP + "~/backup-db/#{dbn}.sql.#{date}.tar.gz #{backupPath}")

