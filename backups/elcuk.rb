# encoding: utf-8

##
# Backup Generated: elcuk
# Once configured, you can run the backup with the following command:
#
# $ backup perform -t elcuk [-c <path_to_configuration_file>]
#
Backup::Model.new(:elcuk, 'Description for elcuk') do
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
    db.name               = "elcuk2"
    db.username           = "root"
    db.password           = "crater10lake"
    db.host               = "localhost"
    db.port               = 3306
    #db.socket             = "/tmp/mysql.sock"
    # Note: when using `skip_tables` with the `db.name = :all` option,
    # table names should be prefixed with a database name.
    # e.g. ["db_name.table_to_skip", ...]
    db.skip_tables        = ["skip", "these", "tables"]
    #db.only_tables        = ["only", "these" "tables"]
    #db.additional_options = ["--quick", "--single-transaction"]
    # Optional: Use to set the location of this utility
    #   if it cannot be found by name in your $PATH
    # db.mysqldump_utility = "/opt/local/bin/mysqldump"
  end

  ##
  # Local (Copy) [Storage]
  #
  store_with Local do |local|
    local.path       = "~/backups/"
    local.keep       = 5
  end

  # TODO 增加备份到 S3

end
