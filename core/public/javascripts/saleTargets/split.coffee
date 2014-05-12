$ ->

  $("#submitSaleTargetsForm").on('blur', ".input-small, input[name='reallySaleAmounts']", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val())) then $input.val("0.0")

    # 修改了原先的值 则文本框颜色变化
    old = parseFloat($input.data("old"))
    if old != parseFloat($input.val())
      $input.parent().addClass("control-group error")

    reallySaleAmountsSUM()
    saleAmountsSUM()
  )

  $("#submitSaleTargetsForm").on('blur', ".input-mini", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val())) then $input.val("0.0")
  )

  # 计算实际销售额汇总数据 与 销售额目标修正值
  reallySaleAmountsSUM = ->
    reallySUM = 0
    reviseSaleAmountsSUM = 0

    reallySaleAmountsList = $("input[name='reallySaleAmounts']")
    saleAmountsList = $(".input-small")

    for reallySaleAmounts, i in reallySaleAmountsList
      value = parseFloat(reallySaleAmounts.value)
      if value > 0
        reviseSaleAmountsSUM += value
      else
        reviseSaleAmountsSUM += parseFloat(saleAmountsList[i].value)
      reallySUM += value

    $("#reallySaleAmountsSUM").val(reallySUM.toFixed(2))
    $("#reviseSaleAmounts").val(reviseSaleAmountsSUM.toFixed(2))

  # 计算销售额目标汇总数据
  saleAmountsSUM = ->
    saSUM = 0
    saleAmountsList = $(".input-small")
    for saleAmounts, i in saleAmountsList
      value = parseFloat(saleAmounts.value)
      saSUM += value
    $("#saleAmountsSUM").val(saSUM.toFixed(2))

  reallySaleAmountsSUM()
  saleAmountsSUM()

  $("#submit").click ->
    $("#submitSaleTargetsForm").submit()
