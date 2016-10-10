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
  )

  loadStockObj = (stock_obj_id) ->
    return if stock_obj_id == "" || stock_obj_id == undefined
    $.get('/whouses/loadStockObj', {id: stock_obj_id},
      (r) ->
        $('input[name=stock_name]').val(r.name)
        $("input[name='record.stockObj.stockObjType']").val(r.type)
    )

  $("input[name='record.stockObj.stockObjId']").trigger('loadStockObj')
