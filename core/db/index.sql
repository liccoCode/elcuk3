# 给 OrderItem 添加 createDate 索引,便于范围搜索
CREATE INDEX orderitem_createdate ON OrderItem (createDate);

# 给 Orderr 标添加多列索引
CREATE INDEX orderr_market_createdate_state_warnning ON Orderr (market(9), createDate, state, warnning);

# 给 Orderr 添加以 createDate 开头的多列索引
CREATE INDEX orderr_createdate_state_warnning ON Orderr(createDate, state, warnning);

# 给 ElcukRecord 的 Fid 添加索引
CREATE INDEX elcuk_record_fid ON ElcukRecord (fid(14));

# 为 Selling 的 MerchangSKU 添加索引(ajaxSession 等用)
CREATE INDEX selling_merchantsku ON Selling (merchantSKU(17));

# 首页统计 Ticket 信息用.
CREATE INDEX ticket_type_state_createAt ON Ticket (type, state, createAt);

# 进行 SellingRecord 的统计
CREATE INDEX sellingrecord_date ON SellingRecord(date);

# SaleFee 变大了, 这个很需要
CREATE INDEX salefee_date ON SaleFee (date)
