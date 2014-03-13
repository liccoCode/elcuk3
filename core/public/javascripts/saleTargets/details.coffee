$ ->
  # 生成年度销售目标的 option 从今年 至 往后十年
  $targetDate = $('select[name="targetDate"]')
  year = new Date().getFullYear()
  range = [-4..5]
  for n in range
    $targetDate.append("<option value='#{year + n}'>#{year + n}</option>")

  # 按钮点击事件 获取点击的 id
  # 根据获取到的按钮的id去获取对应的fid 、saleAmounts、profitMargin值
  # ajax提交数据到服务器
  # 显示消息
  $(".btn-success").click ->
    LoadMask.mask()
    # 按钮的 ID
    indexId = this.id
    # 外键 ID
    fid = $("##{indexId}_fid").val()
    # 销售金额
    saleAmounts = $("##{indexId}_saleAmounts").val()
    # 利润率
    profitMargin = $("##{indexId}_profitMargin").val()
    # 父对象的 ID
    parentId = $("##{indexId}_parentId").val()
    # 自己的 ID
    pid = $("##{indexId}_Id").val()
    # 销售目标的目标时间
    targetDate = $('select[name="targetDate"]').val()
    alert(targetDate)
    if(saleAmounts == null or "")
      noty({text: '销售金额不能为空', type: 'error', timeout: 3000})
    if(saleAmounts == null or "")
      noty({text: '利润率不能为空', type: 'error', timeout: 3000})

    if pid == null or pid == ""
      # Ajax 创建新的子销售目标
      $.get('/saletargets/create',
      {parentId: parentId, fid: fid, saleAmounts: saleAmounts, profitMargin: profitMargin, targetDate: targetDate})
      .done((r) ->
          if r.flag
            noty({text: r.message, type: 'success', timeout: 3000})
        )
    else
      # Ajax 修改子销售目标
      $.get('/saletargets/update',
      {pid: pid, parentId: parentId, fid: fid, saleAmounts: saleAmounts, profitMargin: profitMargin, targetDate: targetDate})
      .done((r) ->
          if r.flag
            noty({text: r.message, type: 'success', timeout: 3000})
        )
    LoadMask.unmask()


