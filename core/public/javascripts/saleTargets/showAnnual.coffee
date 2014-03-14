$ ->
  $("#updateAnnual").click ->
    LoadMask.mask()
    $updateform = $("#updateAnnualForm")

    inputs = $('input[name="indexId"]')
    indexIds = []
    indexIds = for input in inputs
      input.value

    # 准备生成 Json 数组
    jsons = []
    for indexid in indexIds
      data = {}
      # ID
      data["id"] = $("##{indexid}_Id").val()
      # 销售金额
      data["saleAmounts"] = $("##{indexid}_saleAmounts").val()
      # 利润率
      data["profitMargin"] = $("##{indexid}_profitMargin").val()
      # 外键 ID
      data["fid"] = $("##{indexid}_fid").val()
      # 父销售目标的 id
      data["parentId"] = $("##{indexid}_parentId").val()
      # 销售目标月份
      data["targetMonth"] = $("##{indexid}_targetMonth").val()
      # 将数据添加到 Json 数组内
      jsons.push(data)

    #准备发起 Ajax 请求将数据传送到服务器  jsonstr: JSON.stringify(jsons), targetDate: targetDate}
    $.ajax("/saletargets/updateAnnual", {type: 'GET', data: $updateform.serialize()}, dataType: 'json')
    .done((r) ->
        if r.flag
          # 更新成功后再次发起 Ajax 请求去 创建 或 更新 子销售目标
          $.ajax("/saletargets/saleTarget", {type: 'GET', data: {jsonstr: JSON.stringify(jsons)}, dataType: 'json'})
          .done((r) ->
              if r.flag
                noty({text: r.message, type: 'success', timeout: 3000})
              else
                noty({text: r.message, type: 'error', timeout: 3000})
            )
        else
          noty({text: r.message, type: 'error', timeout: 3000})
      )
    LoadMask.unmask()

