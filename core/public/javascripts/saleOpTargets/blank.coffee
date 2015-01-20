$ ->
  # 如果输入值小于0 则回填0
  $("#saleOpTargetsForm").on('blur', ".input-small", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val())) then $input.val("")
    amountSUM()
  )

  amountSUM = ->
    saleAmountsSUM = 0.0
    inputList = $(".input-small")
    _.each(inputList, (value) ->
      if value.value != ""
        saleAmountsSUM += parseFloat(value.value)
    )
    $("input[name='yearSt.saleAmounts']").val(saleAmountsSUM.toFixed(2))


  $("#saleOpTargetsForm").on('blur', ".input-mini", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val())) then $input.val("")
    qtySUM()
  )

  qtySUM = ->
    saleQtysSUM = 0.0
    inputList = $(".input-mini")
    _.each(inputList, (value) ->
      if value.value != ""
        saleQtysSUM += parseFloat(value.value)
    )
    $("input[name='yearSt.saleQty']").val(saleQtysSUM)


  $("#saleOpTargetsForm").on('blur', ".input-mini", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() <= 0  or isNaN($input.val())) then $input.val("")
  )



  amountSUM()
  qtySUM()

  $("#submitSaleOpTargets").click ->
    $("#saleOpTargetsForm").submit()
