$ ->
  $("#submitSaleOpTargetsForm").on('blur', ".input-mini, input[name='reallySaleAmounts']", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val())) then $input.val("")

    $inputlast = $input.parents('tr').find(".input-qtylast")
    if $inputlast.val() <= 0
      $inputlast.val($input.val())

    # 修改了原先的值 则文本框颜色变化
    old = parseFloat($input.data("old"))
    if old != parseFloat($input.val())
      $input.parent().addClass("control-group error")

    saleQtySUM()
    reallySaleAmountsSUM()
    saleAmountsSUM()
    lastSaleAmountsSUM()
  )

  $("#submitSaleOpTargetsForm").on('change', "input", (e) ->
    $input = $(@)
    targetname = $input.attr("targetname")

    if targetname == '' or targetname == 'undefined'
      return

    targetList = $("input[targetname='" + targetname + "']")
    sumtarget = 0
    for target, i in targetList
      if target.value != ""
        sumtarget += parseFloat(target.value)

    $("#" + targetname).val(sumtarget)
  )

  $("#submitSaleOpTargetsForm").on('blur', ".input-small", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val())) then $input.val("")

    $inputlast = $input.parents('tr').find(".input-amountlast")
    if $inputlast.val() <= 0
      $inputlast.val($input.val())

    # 修改了原先的值 则文本框颜色变化
    old = parseFloat($input.data("old"))
    if old != parseFloat($input.val())
      $input.parent().addClass("control-group error")

    reallySaleAmountsSUM()
    saleAmountsSUM()
  )

  $("#submitSaleOpTargetsForm").on('blur', ".input-qtylast,.input-amountlast", (e) ->
    lastSaleAmountsSUM()
  )

  # 计算销售额目标汇总数据
  saleAmountsSUM = ->
    saSUM = 0.00
    saleAmountsList = $(".input-small")
    for saleAmounts, i in saleAmountsList
      if saleAmounts.value != ""
        value = parseFloat(saleAmounts.value)
        saSUM += value
    $("#saleAmountsSUM").val(saSUM.toFixed(2))

  # 计算销量汇总数据
  saleQtySUM = ->
    saSUM = 0
    count = 0
    saleAmountsList = $(".input-mini")
    for saleAmounts, i in saleAmountsList
      if saleAmounts.value != "" && saleAmounts.value != null
        value = parseFloat(saleAmounts.value)
        saSUM += value
        count += 1
    if count != 0
      $("#saleQtySUM").val((saSUM / count).toFixed(0))
    else
      $("#saleQtySUM").val(0)
    $("#saleMarketQtySUM").val(saSUM.toFixed(0))

  # 计算实际销售额汇总数据 与 销售额目标修正值
  reallySaleAmountsSUM = ->
    reviseSaleAmountsSUM = 0.00

    reallySaleAmountsList = $("input[name='reallySaleAmounts']")

    amount1 = 0
    amount2 = 0
    amount3 = 0
    amount4 = 0
    for reallySaleAmounts, i in reallySaleAmountsList
      if reallySaleAmounts.value != null && reallySaleAmounts.value != 0 && reallySaleAmounts.value != ""
        value = parseFloat(reallySaleAmounts.value)
        amount = 0
        if value > 0
          reviseSaleAmountsSUM += value
          amount = value
        if i >= 0 and i <= 2
          amount1 += amount
        else
          if i >= 3 and i <= 5
            amount2 += amount
          else
            if i >= 6 and i <= 8
              amount3 += amount
            else
              if i >= 9 and i <= 11
                amount4 += amount
    $("#reallySaleAmounts").val((amount1 + amount2 + amount3 + amount4).toFixed(2))
    $("#reviseSeasonSaleAmounts1").val(amount1.toFixed(2))
    $("#reviseSeasonSaleAmounts2").val(amount2.toFixed(2))
    $("#reviseSeasonSaleAmounts3").val(amount3.toFixed(2))
    $("#reviseSeasonSaleAmounts4").val(amount4.toFixed(2))

    reviseSaleQtysSUM = 0
    reallySaleQtysList = $("input[name='reallySaleQtys']")

    qty1 = 0
    qty2 = 0
    qty3 = 0
    qty4 = 0
    count = 0
    for reallySaleQtys, i in reallySaleQtysList
      if reallySaleQtys.value != ""
        value = parseFloat(reallySaleQtys.value)
        qty = 0
        if value > 0
          count += 1
          reviseSaleQtysSUM += value
          qty = value

        if i >= 0 and i <= 2
          qty1 += qty
        else
          if i >= 3 and i <= 5
            qty2 += qty
          else
            if i >= 6 and i <= 8
              qty3 += qty
            else
              if i >= 9 and i <= 11
                qty4 += qty
    if count > 0
      $("#reallySaleQtys").val(((qty1 + qty2 + qty3 + qty4) / count).toFixed(0))
    $("#reviseSeasonSaleQtys1").val((qty1 / 3).toFixed(0))
    $("#reviseSeasonSaleQtys2").val((qty2 / 3).toFixed(0))
    $("#reviseSeasonSaleQtys3").val((qty3 / 3).toFixed(0))
    $("#reviseSeasonSaleQtys4").val((qty4 / 3).toFixed(0))

  lastSaleAmountsSUM = ->
    lastSaleAmountsSUM = 0.00
    lastSaleAmountsList = $(".input-amountlast")

    amount1 = 0
    amount2 = 0
    amount3 = 0
    amount4 = 0
    for lastSaleAmounts, i in lastSaleAmountsList
      if lastSaleAmounts.value != null && lastSaleAmounts.value != 0 && lastSaleAmounts.value != ""
        value = parseFloat(lastSaleAmounts.value)
        amount = 0
        if value > 0
          lastSaleAmountsSUM += value
          amount = value
        if i >= 0 and i <= 2
          amount1 += amount
        else
          if i >= 3 and i <= 5
            amount2 += amount
          else
            if i >= 6 and i <= 8
              amount3 += amount
            else
              if i >= 9 and i <= 11
                amount4 += amount

    $("#lastSaleAmounts").val(lastSaleAmountsSUM.toFixed(2))
    $("#lastSeasonSaleAmounts1").val(amount1.toFixed(2))
    $("#lastSeasonSaleAmounts2").val(amount2.toFixed(2))
    $("#lastSeasonSaleAmounts3").val(amount3.toFixed(2))
    $("#lastSeasonSaleAmounts4").val(amount4.toFixed(2))

    lastSaleQtysList = $(".input-qtylast")

    qty1 = 0
    qty2 = 0
    qty3 = 0
    qty4 = 0
    lastSaleQtysSUM = 0
    count = 0
    for lastSaleQtys, i in lastSaleQtysList
      if lastSaleQtys.value != ""
        value = parseFloat(lastSaleQtys.value)
        qty = 0
        if value > 0
          count += 1
          lastSaleQtysSUM += value
          qty = value

        if i >= 0 and i <= 2
          qty1 += qty
        else
          if i >= 3 and i <= 5
            qty2 += qty
          else
            if i >= 6 and i <= 8
              qty3 += qty
            else
              if i >= 9 and i <= 11
                qty4 += qty

    $("#lastSaleQtys").val((lastSaleQtysSUM / count).toFixed(0))
    $("#lastSeasonSaleQtys1").val((qty1 / 3).toFixed(0))
    $("#lastSeasonSaleQtys2").val((qty2 / 3).toFixed(0))
    $("#lastSeasonSaleQtys3").val((qty3 / 3).toFixed(0))
    $("#lastSeasonSaleQtys4").val((qty4 / 3).toFixed(0))

  reallySaleAmountsSUM()
  lastSaleAmountsSUM()
  saleAmountsSUM()
  saleQtySUM()

  $("#submit").click ->
    $("#submitSaleOpTargetsForm").submit()