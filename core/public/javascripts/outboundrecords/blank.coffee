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

  $("#new_outbound_record").on('loadStockObj', "input[name='record.stockObj.stockObjId']", (e) ->
    loadStockObj($(@).val())
  ).on('change', "select[name='record.type']", (e) ->
    $targetSelectize = $targetIdSelect[0].selectize
    $targetSelectize.clearOptions()

    switch $(@).val()
      when 'Normal', 'B2B', 'Refund'
        $targetSelectize.settings.create = false
      when 'Process'
        $targetSelectize.addOption({value: '品拓生产部', text: '品拓生产部'})
        $targetSelectize.addItem('品拓生产部')
      when 'Sample'
        _.each(["质检部", "采购部", "运营部", "研发部", "生产部"], (v) ->
          $targetSelectize.addOption({value: v, text: v})
        )
        $targetSelectize.addItem('质检部')
      else # Other
        console.log($targetSelectize.settings)
        $targetSelectize.settings.create = true
  )

  loadStockObj = (stock_obj_id) ->
    return if _.isEmpty(stock_obj_id)
    $.get('/whouses/loadStockObj', {id: stock_obj_id},
      (r) ->
        $('input[name=stock_name]').val(r.name)
        $("input[name='record.stockObj.stockObjType']").val(r.type)
    )
  $("input[name='record.stockObj.stockObjId']").trigger('loadStockObj')

  $targetIdSelect = $("select[name='record.targetId']").selectize({
    persist: false,
    create: false,
    load: (query, callback) ->
      type = $("select[name='record.type']").val()
      return callback() if !query.length || !type.length || !$.inArray(type, ["Process", "Sample", "Other"])
      $.ajax({
        url: '/Cooperators/findSameCooperator',
        type: 'GET',
        dataType: 'json',
        data: {name: query, type: if type == 'Refund' then 'SUPPLIER' else 'SHIPPER'},
        error: ->
          callback()
        success: (res) ->
          coopers = []
          _.each(res, (cooper) ->
            [t, v] = cooper.split('-')
            coopers.push({value: v, text: t})
          )
          callback(coopers)
      })
  })

  $("select[name='record.market']").selectize({
    persist: false,
    create: (input) ->
      {
        value: input,
        text: input
      }
  })
