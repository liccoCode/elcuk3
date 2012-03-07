require "rubygems"
=begin
    检查当天的数据是否备份, 如果没备份则备份数据库
    将当天的数据库备份文件下载回来
=end

backupPath = '~/backup-db'
a = 'root@e.easyacceu.com'
SSH = "ssh #{a} "
SCP = "scp #{a}:"
date = Time.new.strftime('%F')

exist = system(SSH + "'test -e ~/backup/elcuk.sql.#{date}'")
puts(exist ? "File Exsit, so just copy it." : "File is not exist, first dump it, then copy it.")

=begin
使用 7zip 压缩; 内存不够用...
system(SSH + "'mysqldump -uroot -pcrater10lake elcuk > ~/backup/elcuk.sql.#{date};7z a ~/backup/elcuk.sql.#{date}.7z ~/backup/elcuk.sql.#{date};rm ~/backup/elcuk.sql.#{date}'") if not exist
	
system(SCP + "~/backup/elcuk.sql.#{date}.7z /Volumes/wyatt/backup/")
=end
#使用 tar czf
system(SSH + "'mysqldump -uroot -pcrater10lake elcuk2 > ~/backup-db/elcuk2.sql.#{date};" + 
"cd ~/backup-db;tar czf elcuk2.sql.#{date}.tar.gz elcuk2.sql.#{date};rm ~/backup-db/elcuk2.sql.#{date}'") if not exist
system(SCP + "~/backup-db/elcuk2.sql.#{date}.tar.gz #{backupPath}")

