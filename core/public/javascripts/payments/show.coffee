$ ->
  $('.paymentUnitDeny').click (e) ->
    e.preventDefault()
    self = $(@)
    $('#deny_form').attr('action', self.attr('url'))
    $('#deny_title').text(self.parents('td').next().text())
    $('#deny_modal').modal('show')