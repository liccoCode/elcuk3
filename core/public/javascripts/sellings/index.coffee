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
          noty({text: r.message, type: 'error'})
        LoadMask.unmask()
      )
  )

  $("#search_form").on("click", "#createBtn", (e) ->
    e.preventDefault()
    $("#create_modal").modal('show')
  )

  $("#create_modal").on("click", "#submitSale", (e) ->
    e.preventDefault();
    if $("[name='createtype']:checked").length == 0
      noty({text: '请选择处理类型', type: 'error'})
      return false;
    if $("#sku").val()
      $.ajax($form.attr('action'), {data: $form.serialize(), type: 'POST'})
      .done((r) ->
        if r.flag
          noty({
            text: "成功创建 Selling #{r.message}, Amazon 与系统正在处理中, 请等待 5~10 分钟后再查看",
            layout: 'top',
            type: 'success',
            timeout: false
          })
        else
          noty({text: r.message, type: 'error'})
        LoadMask.unmask()
      )
    else
      noty({text: '请选择SKU', type: 'error'})
      return false;
  )