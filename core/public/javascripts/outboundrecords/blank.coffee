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
    $typeSelect = $(@)
    $marketSelect = $("select[name='record.market']")
    $targetSelect = $("select[name='record.targetId']")

    $targetSelectize = $targetSelect[0].selectize
    $marketSelectize = $marketSelect[0].selectize

    # 删除 Selectize 化
    $targetSelectize.destroy() unless _.isEmpty($targetSelectize)
    # 默认去往国家不允许自定义添加
    $marketSelectize.settings.create = false

    $targetSelect.empty()
    type = $typeSelect.val()
    switch type
      when 'Normal', 'B2B'
        _.each($('#shipperOptions').find('option').clone(), (option) ->
          $targetSelect.append(option)
        )
      when 'Refund'
        _.each($('#supplierOptions').find('option').clone(), (option) ->
          $targetSelect.append(option)
        )
      when 'Process'
        _.each($('#processOptions').find('option').clone(), (option) ->
          $targetSelect.append(option)
        )
      when 'Sample'
        _.each($('#sampleOptions').find('option').clone(), (option) ->
          $targetSelect.append(option)
        )
      else
        $marketSelectize.settings.create = true

    $targetSelect.selectize({
      persist: false,
      create: type == 'Other',
      load: (query, callback) ->
        return callback() if !query.length || !type.length || type != 'Other'
        $.ajax({
          url: '/Cooperators/findSameCooperator',
          type: 'GET',
          dataType: 'json',
          data: {name: query},
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
  )

  loadStockObj = (stock_obj_id) ->
    return if _.isEmpty(stock_obj_id)
    $.get('/whouses/loadStockObj', {id: stock_obj_id},
      (r) ->
        $('input[name=stock_name]').val(r.name)
        $("input[name='record.stockObj.stockObjType']").val(r.type)
    )

  $("select[name='record.market']").selectize({
    persist: false,
    create: (input) ->
      {
        value: input,
        text: input
      }
  })

  $(document).ready ->
    $("input[name='record.stockObj.stockObjId']").trigger('loadStockObj')
    $("select[name='record.type']").trigger("change")
