$ ->
  $code_input = $("input[name='record.stockObj.stockObjId']")
  $code_input.typeahead({
    source: (query, process) ->
      $.get('/whouses/sameCode', {search: $code_input.val()})
      .done((c) ->
        process(c)
      )
    updater: (item) ->
      loadStockObj(item)
      item
  })

  $("#new_inbound_record").on('loadStockObj', "input[name='record.stockObj.stockObjId']", (e) ->
    loadStockObj($(@).val())
  ).on('change', "input[name='shipPlanId']", (e) ->
    $shipPlanId = $(@)
    $("[name='record.memo']").val("关联的出库计划: #{$shipPlanId.val()}")
  ).on('change', "input[name='outboundRecordId']", (e) ->
    outboundRecordId = $(@).val()
    console.log($.isNumeric(outboundRecordId))
    if $.isNumeric(outboundRecordId)
      $fba = $("input[name='fba']")
      $fnsku = $("input[name='fnsku']")

      $.getJSON("/OutboundRecords/attributes", {id: outboundRecordId}, (r) ->
        if r.flag == false
          noty({text: r.message, type: 'error', timeout: 2000})
          return
        $fba.val(r.fba)
        $fnsku.val(r.fnsku)
      )
  )

  loadStockObj = (stock_obj_id) ->
    return if stock_obj_id == "" || stock_obj_id == undefined
    $.get('/whouses/loadStockObj', {id: stock_obj_id},
      (r) ->
        $('input[name=stock_name]').val(r.name)
        $("input[name='record.stockObj.stockObjType']").val(r.type)
    )

  $("input[name='record.stockObj.stockObjId']").trigger('loadStockObj')
