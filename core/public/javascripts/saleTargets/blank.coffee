$ ->
  # 如果输入值小于0 则回填0
  $("#saleTargetsForm").on('blur', ".input-small", (e) ->
    saleAmountsSUM = 0.0
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val())) then $input.val("0.0")
    inputList = $(".input-small")
    _.each(inputList, (value) ->
      saleAmountsSUM += parseFloat(value.value)
    )

    $("input[name='yearSt.saleAmounts']").val(saleAmountsSUM)
  )

  $("#saleTargetsForm").on('blur', ".input-mini", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val())) then $input.val("0.0")
  )

  $("#submitSaleTargets").click ->
    $("#saleTargetsForm").submit()
