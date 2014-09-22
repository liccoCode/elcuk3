$ ->
  $('.paymentUnitCancel').click (e) ->
    e.preventDefault()
    $('#paymentUnit_destroy_form').attr('action', @getAttribute('url'))
    $('#paymentUnit_destroy').modal()

  $(document).on("click", "#billing_rework_pay_btn", (r) ->
    $("#modal_home").load('/Procureunits/loadChecklist', id: $(@).data("pid"), (r) ->
      $('#reworkpay_modal').modal('show')
    )
  ).on("click", "#sumbit_billing_btn", (r) ->
    # 计算用户勾选的费用记录
    checkids = []
    checkboxList = $('input[name="checkids"]')
    for checkbox in checkboxList when checkbox.checked then checkids.push(checkbox.value)
    if checkids.length is 0
      noty({text: '请选择费用记录！', type: 'error'})
      return false
    $("#checktask_id_list").val(checkids.join("_"))
    $("#billing_rework_pay_form").submit()
    $('#reworkpay_modal').modal('hide')
  )

  # Form 搜索功能
  $("#tailpaybtn").click (e) ->
    data = ""
    checkObj = document.all("unitids")
    i = 0
    while i < checkObj.length
      if checkObj[i].checked == true
        data = data + "&unitids="+ checkObj[i].value
      i++

    applyid = $('#applyid')
    data = data + "&applyid="+ applyid.val()
    $.get('/ProcureUnits/morebillingTailPay', data, (r) ->
      if r.flag is false
        alert(r.message)
        window.location.reload()
      else
        alert('申请尾款成功.')
        window.location.reload()
    )
    e.preventDefault()

  $("#prepaybtn").click (e) ->
    data = ""
    checkObj = document.all("unitids")
    i = 0
    while i < checkObj.length
      if checkObj[i].checked == true
        data = data + "&unitids="+ checkObj[i].value
      i++

    applyid = $('#applyid')
    data = data + "&applyid="+ applyid.val()

    $.get('/ProcureUnits/morebillingPrePay', data, (r) ->
      if r.flag is false
        alert(r.message)
        window.location.reload()
      else
        alert('申请预付款成功.')
        window.location.reload()
      form.unmask()
    )




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
