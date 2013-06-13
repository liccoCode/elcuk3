# 给 OrderItem 添加 createDate 索引,便于范围搜索
CREATE INDEX orderitem_createdate ON OrderItem (createDate);

# 给 Orderr 标添加多列索引
CREATE INDEX orderr_createdate_state_warnning ON Orderr (createDate, state, warnning);

# 给 ElcukRecord 的 Fid 添加索引
CREATE INDEX elcuk_record_fid ON ElcukRecord (fid(14));
