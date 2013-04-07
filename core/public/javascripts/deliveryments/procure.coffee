$ ->
  $('.paymentUnitCancel').click (e) ->
    e.preventDefault()
    $('#paymentUnit_destroy_form').attr('action', @getAttribute('url'))
    $('#paymentUnit_destroy').modal()

  # 处理 hash
  do ->
    paymentUnitId = window.location.hash[1..-1]
    targetTr = $("#paymentUnit#{paymentUnitId}")
    targetTr.parents('tr').prev().find('td[data-toggle]').click()
    EF.scoll(targetTr)
    EF.colorAnimate(targetTr)
