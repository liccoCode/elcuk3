# encoding: utf-8

#  
# 这个是在 OsTicket 所在的服务器上的备份文件, 请首先在 OsTicket 服务器上设置好 ruby backup gem, 
# 然后将这个文件存放到 ~/backup/models/...  (仅供参考)
#

##
# Backup Generated: osticket
# Once configured, you can run the backup with the following command:
#
# $ backup perform -t osticket [-c <path_to_configuration_file>]
#
Backup::Model.new(:osticket, 'Description for osticket') do
  ##
  # Split [Splitter]
  #
  # Split the backup file in to chunks of 250 megabytes
  # if the backup file size exceeds 250 megabytes
  #
  split_into_chunks_of 250

  ##
  # MySQL [Database]
  #
  database MySQL do |db|
    # To dump all databases, set `db.name = :all` (or leave blank)
    db.name               = "osticket"
    db.username           = "root"
    db.password           = "crater10lake"
    db.host               = "localhost"
    db.port               = 3306
    #db.socket             = "/tmp/mysql.sock"
    # Note: when using `skip_tables` with the `db.name = :all` option,
    # table names should be prefixed with a database name.
    # e.g. ["db_name.table_to_skip", ...]
    #db.skip_tables        = ["skip", "these", "tables"]
    #db.only_tables        = ["only", "these" "tables"]
    db.additional_options = ["--quick", "--single-transaction"]
    # Optional: Use to set the location of this utility
    #   if it cannot be found by name in your $PATH
    # db.mysqldump_utility = "/opt/local/bin/mysqldump"
  end

  ##
  # Local (Copy) [Storage]
  #
  store_with Local do |local|
    local.path       = "~/backups/"
    local.keep       = 10
  end

  ##
  # RSync::Local [Syncer]
  #
  sync_with RSync::Local do |rsync|
    rsync.path     = "~/backups/osticket"
    rsync.mirror   = true

    rsync.directories do |directory|
      directory.add "/var/www/osticket/files"
    end
  end

  sync_with RSync::Push do |rsync|
    rsync.ip = "bak.easya.cc"
    #rsync.port = 22
    rsync.username = "root"
    rsync.path = "~/backups/"
    rsync.mirror = true

    rsync.directories do |dir|
      dir.add "~/backups/osticket"
    end
  end


  ##
  # Gzip [Compressor]
  #
  compress_with Gzip

end
