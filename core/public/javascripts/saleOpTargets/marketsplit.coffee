$ ->
  $("#submitSaleOpTargetsForm").on('blur', ".input-mini, .input-small, input[name='reallySaleAmounts']", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val())) then $input.val("")

    # 修改了原先的值 则文本框颜色变化
    old = parseFloat($input.data("old"))
    if old != parseFloat($input.val())
      $input.parent().addClass("control-group error")

    reallySaleAmountsSUM()
    saleAmountsSUM()
    saleQtySUM()
  )

  $("#submitSaleOpTargetsForm").on('blur', ".input-mini", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() <= 0 or isNaN($input.val())) then $input.val("")
  )



  # 计算销售额目标汇总数据
  saleAmountsSUM = ->
    saSUM = 0
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
      if saleAmounts.value != ""
        value = parseFloat(saleAmounts.value)
        saSUM += value
        count += 1
    $("#saleQtySUM").val((saSUM/count).toFixed(0))
    $("#saleMarketQtySUM").val(saSUM.toFixed(0))

  # 计算实际销售额汇总数据 与 销售额目标修正值
  reallySaleAmountsSUM = ->
    reviseSaleAmountsSUM = 0

    reallySaleAmountsList = $("input[name='reallySaleAmounts']")
    saleAmountsList = $(".input-small")

    amount1 = 0
    amount2 = 0
    amount3 = 0
    amount4 = 0
    for reallySaleAmounts, i in reallySaleAmountsList
      if reallySaleAmounts.value!=null && reallySaleAmounts.value!=0 && reallySaleAmounts.value!=""
        value = parseFloat(reallySaleAmounts.value)
        amount = 0
        if value > 0
          reviseSaleAmountsSUM += value
          amount = value
        else
          reviseSaleAmountsSUM += parseFloat(saleAmountsList[i].value)
          amount = parseFloat(saleAmountsList[i].value)

        if i>=0 and i<=2
         amount1 += amount
        else
         if i>=3 and i<=5
          amount2 += amount
         else
          if i>=6 and i<=8
           amount3 += amount
          else
           if i>=9 and i<=11
            amount4 += amount

    $("#reviseSaleAmounts").val(reviseSaleAmountsSUM.toFixed(2))
    $("#reviseSeasonSaleAmounts1").val(amount1.toFixed(2))
    $("#reviseSeasonSaleAmounts2").val(amount2.toFixed(2))
    $("#reviseSeasonSaleAmounts3").val(amount3.toFixed(2))
    $("#reviseSeasonSaleAmounts4").val(amount4.toFixed(2))

    reviseSaleQtysSUM = 0
    reallySaleQtysList = $("input[name='reallySaleQtys']")
    saleQtysList = $(".input-mini")

    qty1 = 0
    qty2 = 0
    qty3 = 0
    qty4 = 0
    for reallySaleQtys, i in reallySaleQtysList
      if reallySaleQtys.value!=""
        value = parseFloat(reallySaleQtys.value)
        qty = 0
        if value > 0
          reviseSaleQtysSUM += value
          qty = value
        else
          reviseSaleQtysSUM += parseFloat(saleQtysList[i].value)
          qty = parseFloat(saleQtysList[i].value)

        if i>=0 and i<=2
          qty1 += qty
        else
          if i>=3 and i<=5
            qty2 += qty
          else
            if i>=6 and i<=8
              qty3 += qty
            else
              if i>=9 and i<=11
                qty4 += qty

    $("#reviseSaleQtys").val((reviseSaleQtysSUM/12).toFixed(0))
    $("#reviseSeasonSaleQtys1").val((qty1/3).toFixed(0))
    $("#reviseSeasonSaleQtys2").val((qty2/3).toFixed(0))
    $("#reviseSeasonSaleQtys3").val((qty3/3).toFixed(0))
    $("#reviseSeasonSaleQtys4").val((qty4/3).toFixed(0))




  amountRMB = ->
    amount = $("#amount")
    $("#amountrmb").val("￥"+(amount.val()*6.2203).toFixed(4))

    reviseSaleAmounts = $("#reviseSaleAmounts")
    $("#reviseamountrmb").val("￥"+(reviseSaleAmounts.val()*6.2203).toFixed(4))







  reallySaleAmountsSUM()
  saleAmountsSUM()
  saleQtySUM()
  amountRMB()

  $("#submit").click ->
    $("#submitSaleOpTargetsForm").submit()
