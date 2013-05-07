$ ->
  $('.paymentUnitCancel').click (e) ->
    e.preventDefault()
    $('#paymentUnit_destroy_form').attr('action', @getAttribute('url'))
    $('#paymentUnit_destroy').modal()

  calculateSumery = ->
    $('.table_summary').each ->
      cny_summery = 0
      usd_summery = 0
      unkown_summery = 0
      $(@).parents('table').find('td.price').each ->
        text = @innerText
        if text.indexOf("$") >= 0
          usd_summery += parseFloat(text.split(' ')[1])
        else if text.indexOf('¥') >= 0
          cny_summery += parseFloat(text.split(' ')[1])
        else
          unkown_summery += parseFloat(text.split(' ')[1])
      $(@).find('.usd').text("$ #{usd_summery}").end()
        .find('.cny').text("¥ #{cny_summery}").end()
        .find('.unknow').text("? #{unkown_summery}")


  # 处理 hash
  do ->
    paymentUnitId = window.location.hash[1..-1]
    targetTr = $("#paymentUnit#{paymentUnitId}")
    if targetTr.size() > 0
      targetTr.parents('tr').prev().find('td[data-toggle]').click()
      EF.scoll(targetTr)
      EF.colorAnimate(targetTr)

    calculateSumery()
