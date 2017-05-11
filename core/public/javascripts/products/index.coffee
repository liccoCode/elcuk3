$ ->
  $(document).on('click', '#copyBtn', ->
    id = $(@).data('sku')
    $("#target_choseid").val(id)
    $('#copy_modal').modal('show')
  )

  $('#update_modal').on('click', '[type=submit]', (r) ->
    $form = $('#update_form')
    LoadMask.mask()
    $.ajax($form.attr('action'), {
      type: 'POST',
      data: $form.serialize()
    }).done((r) ->
      type = if r.flag is false then 'error' else 'success'
      noty({
        text: r.message,
        type: type
      })
      LoadMask.unmask()
    ).fail((r) ->
      noty({
        text: r.responseType,
        type: 'error'
      })
      LoadMask.unmask()
    )
  )

  $('a[data-method=delete]').click ->
    id = @getAttribute('data-id')
    $.ajax({
      url: "/paymenttarget/#{id}",
      type: 'DELETE'
    }).done((r) ->
      window.location.href = '/paymenttargets'
    )

  $(document).on('click', '#backupBtn', ->
    id = $(@).data('sku')
    family = $(@).data('family')
    $("#backup_choseid").val(id)
    $("#back_sku").val(id)
    $("#back_families").val(family)
    $('#backup_modal').modal('show')
  ).on("change", "select[name='pro.state'], select[name='pro.salesLevel']", (r) ->
    $select = $(@)
    firstTd = $select.parents('tr').children('td')[1]
    sku = $(firstTd).children('a')[1].innerHTML
    LoadMask.mask()
    if $select.val() != "" and sku != ""
      if $select.attr('name') == 'pro.state'
        $.post('/products/updateState', {
          state: $select.val(),
          sku: sku
        })
      else
        $.post('/products/updateSalesLevel', {
          salesLevel: $select.val(),
          sku: sku
        })
    LoadMask.unmask()
  ).on('click', '#deleteBtn', ->
    $deleteBtn = $(@)
    $logForm = $("#logForm")
    firstTd = $deleteBtn.parents('tr').children('td')[1]
    sku = $(firstTd).children('a')[0].innerHTML # 得到 SKU
    $($("#logForm input")[0]).val(sku) # 设置 SKU 到隐藏的 Modal 中
    $logForm.modal() # 展示 Modal
  ).on('click', '#searchBtn, #downloadBtn', ->
    $btn = $(@)
    $form = $($btn.parents('form')[0])
    $form.attr('action', $btn.data('href'))

    if $btn.attr('id') is "downloadBtn"
      $form.attr("target", "_blank")
    else
      $form.removeAttr('target')

    $form.submit()
  )

  $("input[name='p.search']").typeahead({
    source: (query, process) ->
      $.get('/products/source', {search: query})
      .done((c) ->
        process(c)
      )
  })
