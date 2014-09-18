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
  $(".procureunit_form").on("click",".btn:contains(尾款)",(e) ->
      $.post('/ProcureUnits/morebillingTailPay', $('.procureunit_form').formSerialize(), (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert('申请尾款成功.')
          window.location.reload()
      )
      e.preventDefault()
  ).on("click",".btn:contains(付款)",(e) ->
      $.post('/ProcureUnits/morebillingPrePay', $('.procureunit_form').formSerialize(), (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert('申请预付款成功.')
          window.location.reload()
      )
      e.preventDefault()
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
