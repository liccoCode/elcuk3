$ ->
  $(document).on('click', '#create_btn', ->
    $self = $(@)
    $("#target_userName").val("")
    $("#target_teamName").val($self.data("teamname"))
    $("#target_teamId").val($self.data("teamid"))
    $("#target_CategoryName").val($self.data('categoryname'))
    $("#target_CategoryId").val($self.data("categoryid"))
    $('#create_modal').modal('show')
  ).on('click', '#delete_btn', ->
    return unless confirm("确认删除#{$(@).data('assname')}?")
    $.post('/categoryinfos/deleteAssignById', {assid: $(@).data('assid')},
      (r) ->
        if(r.flag)
          alert("删除成功.", $('#data_table').submit())
        else
          alert(r.message)
    )
  ).on('click', '#update_btn', ->
    $self = $(@)
    $("#update_userName").val($self.data('username'))
    $("#update_assid").val($self.data('assid'))
    $("#update_teamName").val($self.data("teamname"))
    $("#update_teamId").val($self.data("teamid"))
    $("#update_CategoryName").val($self.data('categoryname'))
    $("#update_CategoryId").val($self.data("categoryid"))
    $("#update_isCharge").prop('checked', $self.data('ischarge'))
    $("#update_isCharge").val($self.data('ischarge'))
    $('#update_modal').modal('show')
  )

  $("#create_modal").on('click', '[type=submit]', (r) ->
    $form = $('#create_form')
    LoadMask.mask()
    $.ajax($form.attr('action'), {type: 'POST', data: $form.serialize()})
    .done((r) ->
      type = if r.flag is false then 'error' else 'success'
      noty({text: r.message, type: type})
      LoadMask.unmask()
      if (type == 'success')
        $('#data_table').submit()
    )
    .fail((r) ->
      noty({text: r.responseType, type: 'error'})
      LoadMask.unmask()
    )
  )

  $("#update_modal").on('click', '[type=submit]', (r) ->
    $form = $('#update_form')
    LoadMask.mask()
    $.ajax($form.attr('action'), {type: 'POST', data: $form.serialize() + '&c.isCharge=' + $('#update_isCharge').prop("checked")})
    .done((r) ->
      type = if r.flag is false then 'error' else 'success'
      noty({text: r.message, type: type})
      LoadMask.unmask()
      if (type == 'success')
        $('#data_table').submit()
    )
    .fail((r) ->
      noty({text: r.responseType, type: 'error'})
      LoadMask.unmask()
    )
  )

  $('#create_modal').on('click', '#target_isCharge', (e) ->
    $(@).val($(@).prop('checked'))
  )

  $('#update_modal').on('click', '#update_isCharge', (e) ->
    $(@).val($(@).prop('checked'))
  )