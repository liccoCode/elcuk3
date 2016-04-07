$ ->
  $code_input = $("input[name='record.stockObj.stockObjId']")
  $code_input.typeahead({
    source: (query, process) ->
      $.get('/whouses/sameCode', {search: $code_input.val()})
      .done((c) ->
        process(c)
      )
    updater: (item) ->
      $.get('/whouses/loadStockObj', {id: item},
        (r) ->
          $('input[name=stock_name]').val(r.name)

      )
  })
