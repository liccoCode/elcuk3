$ ->
  $('a[data-method=update]').click ->
    self = $(@)
    # 收集参数
    trParent = self.parents('tr')
    targetId = trParent.find('td:eq(0)').text()
    targetName = trParent.find('td:eq(1)').text()
    accountNum = trParent.find('td:eq(2)').text()
    coperId = trParent.find('td[copId]').attr('copId')

    # 填充参数
    $('#target_accountNum').val(accountNum)
    $('#target_cooper').val(coperId)
    $('#target_name').val(targetName)
    $('#update_modal').modal('show')
    $('#update_form').attr('action', "/paymenttarget/#{targetId}")

  $('a[data-method=delete]').click ->
    id = @getAttribute('data-id')
    $.ajax({url: "/paymenttarget/#{id}", type: 'DELETE'}).done((r) ->
      window.location.href = '/paymenttargets'
    )
