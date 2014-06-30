$ ->
  $(document).on('click', '#copyBtn', ->
    $self = $(@)

    $tr = $(@).parents('tr')
    id = $tr.find('td:eq(0)').text().trim()

    $("#target_choseid").val(id)
    $('#copy_modal').modal('show')
  )

  $('#update_modal').on('click', '[type=submit]', (r) ->
    $form = $('#update_form')
    LoadMask.mask()
    $.ajax($form.attr('action'), {type: 'POST', data: $form.serialize()})
      .done((r) ->
        type = if r.flag is false then 'error' else 'success'
        noty({text: r.message, type: type})
        LoadMask.unmask()
      )
      .fail((r) ->
        noty({text: r.responseType, type: 'error'})
        LoadMask.unmask()
      )
  )

  $('a[data-method=delete]').click ->
    id = @getAttribute('data-id')
    $.ajax({url: "/paymenttarget/#{id}", type: 'DELETE'}).done((r) ->
      window.location.href = '/paymenttargets'
    )


  $(document).on('click', '#backupBtn', ->
    $self = $(@)

    $tr = $(@).parents('tr')
    id = $tr.find('td:eq(0)').text().trim()
    family = $tr.find('td:eq(1)').text().trim()

    $("#backup_choseid").val(id)
    $("#back_sku").val(id)
    $("#back_families").val(family)
    $('#backup_modal').modal('show')
  )
