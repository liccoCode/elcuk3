$ ->
  $(document).on('click', '#updateBtn', ->
    $self = $(@)
    LoadMask.mask()
    $.ajax($self.data('url'), {dataType: 'json'})
      .done((r) ->
        LoadMask.unmask()
        # 填充参数
        _.each(["accountAddress", "accountNumber", "accountUser", "name", "id", "cooper.id"], (field) ->
          if field == "cooper.id"
            $("#target_cooperId").val(r["cooper"]["id"])
          else
            $("#target_#{field}").val(r[field])
        )
        $('#update_modal').modal('show')
      )
      .fail((r) ->
        noty({text: r.responseType, type: 'error'})
        LoadMask.unmask()
      )
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
