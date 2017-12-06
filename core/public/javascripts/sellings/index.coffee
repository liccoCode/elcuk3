$ ->
  $sku = $("#inputsku")
  $sku.typeahead({
    source: (query, process) ->
      sku = $sku.val()
      $.get('/products/sameSku', {sku: sku})
        .done((c) ->
        process(c)
      )
  })

  $("a[name='remove']").click(->
    if confirm('确认删除?')
      LoadMask.mask()
      $btn = $(@)
      $.ajax($btn.data('url'))
        .done((r) ->
        type = if r.flag
          alert(r.message)
          window.close()
        else
          noty({
            text: r.message,
            type: 'error'
          })
        LoadMask.unmask()
      )
  )

  $("#search_form").on("click", "#createBtn", (e) ->
    e.preventDefault()
    $("#create_modal").modal('show')
  )

  $("button[name='amz-sync']").click ->
    return false if !confirm("确认要从 Amazon 同步吗? 同步后系统内的数据将被 Amazon 上的数据覆盖.")
    sid = $(@).data("sid")
    btnGroup = $(@).parent().parent()
    btnGroup.mask('同步中...')
    $.post('/sellings/syncAmazon',
      sid: sid,
      (r) ->
        if r.flag is true
          alert('正在同步, 请10秒刷新页面查看最新数据')
        else
          alert(r.message)
        btnGroup.unmask()
    )

  $("#batchDown").click(->
    if $("#data-table input[type='checkbox']:checked").length == 0
      noty({
        text: "请选择需要下架的Selling",
        type: 'error',
        timeout: 2000
      })
    else
      return false if !confirm("需要同时将" + $("#data-table input[type='checkbox']:checked").length + "个selling系统内下架？")
      sellingIds = []
      checkboxList = $("#data-table input[type='checkbox']:checked")
      for checkbox in checkboxList when checkbox.checked then sellingIds.push(checkbox.value)
      $.post('/sellings/batchDownSelling',
        sellingIds: sellingIds,
        (r) ->
          if r.flag is true
            alert('同步成功, 请刷新页面查看最新数据!')
          else
            alert(r.message)
      )
  )