$ ->
  format_Num = (num) ->
    num = parseFloat(num).toFixed(2)
    num.toString().replace(/(\d{1,3})(?=(\d{3})+(?:\.))/g, "$1,")

  $('.paymentUnitCancel').click (e) ->
    e.preventDefault()
    $('#paymentUnit_destroy_form').attr('action', @getAttribute('url'))
    $('#paymentUnit_destroy').modal()

  $(document).on("click", "#billing_rework_pay_btn", (r) ->
    $("#modal_home").load('/Procureunits/loadChecklist',
      id: $(@).data("pid"), (r) ->
      $('#reworkpay_modal').modal('show')
    )
  ).on("click", "#sumbit_billing_btn", (r) ->
# 计算用户勾选的费用记录
    checkids = []
    checkboxList = $('input[name="checkids"]')
    for checkbox in checkboxList when checkbox.checked then checkids.push(checkbox.value)
    if checkids.length is 0
      noty({
        text: '请选择费用记录！',
        type: 'error'
      })
      return false
    $("#checktask_id_list").val(checkids.join("_"))
    $("#billing_rework_pay_form").submit()
    $('#reworkpay_modal').modal('hide')
  ).on("change", "#switch_pay", (r) ->
    self = $(@)
    feesize = self.parents('tr').find("input[name='feesize']").val()
    if feesize > 0
      alert '存在费用明细,不可以更改收款状态!'
      window.location.reload()
    else
      $('#edit_pay_form').attr('action', @getAttribute('url'))
      $('#edit_pay').modal()
  )

  calculateSumery = ->
    total_plan_qty = 0
    total_qty = 0
    total_inbound_qty = 0
    total_cny_summery = 0
    total_usd_summery = 0
    total_unknown_summery = 0
    $('.table_summary').each ->
      table_summary = $(@)
      cny_summery = 0
      usd_summery = 0
      unknown_summery = 0
      planQty = 0
      qty = 0
      inboundQty = 0
      table_summary.parent().find("td.qty").each ->
        planQty += parseInt($(@).attr('planQty'))
        qty += parseInt($(@).attr('qty'))
        inboundQty += parseInt($(@).attr('inboundQty'))
      table_summary.parents('table').find('td.price').each ->
        text = @innerText
        $td = $(@)
        if text.indexOf("$") >= 0
          usd_summery += parseFloat($td.attr("amount"))
        else if text.indexOf('¥') >= 0
          cny_summery += parseFloat($td.attr("amount"))
        else
          unknown_summery += parseFloat($td.attr("amount"))

      table_summary.find('.totalNum').text("#{planQty} / #{qty} / #{inboundQty}").end()
        .find('.usd').text("$ #{format_Num(usd_summery)}").end()
        .find('.cny').text("¥ #{format_Num(cny_summery)}").end()
        .find('.unknow').text("? #{format_Num(unknown_summery)}")

      total_plan_qty += planQty
      total_qty += qty
      total_inbound_qty += inboundQty
      total_cny_summery += cny_summery
      total_usd_summery += usd_summery
      total_unknown_summery += unknown_summery

    $('#totalQty').val("#{total_plan_qty} / #{total_qty} / #{total_inbound_qty}")
    $('#totalCost').val("¥ #{format_Num(total_cny_summery)} | $ #{format_Num(total_usd_summery)} | ? #{format_Num(total_unknown_summery)}")

    pay_cny = 0
    pay_usd = 0
    pay_unknown = 0
    $("#relate_payment_table").find("td.total_price").each ->
      text = $(@).attr("symbol")
      if text.indexOf("$") >= 0
        pay_usd += parseFloat($(@).attr("amount"))
      else if text.indexOf('¥') >= 0
        pay_cny += parseFloat($(@).attr("amount"))
      else
        pay_unknown += parseFloat($(@).attr("amount"))
      $("#relate_payment_table").find('.usd').text("$ #{format_Num(pay_usd)}").end()
        .find('.cny').text("¥ #{format_Num(pay_cny)}").end()
        .find('.unknown').text("? #{format_Num(pay_unknown)}")

  # 处理 hash
  do ->
    paymentUnitId = window.location.hash[1..-1]
    targetTr = $("#paymentUnit#{paymentUnitId}")
    if targetTr.size() > 0
      targetTr.parents('tr').prev().find('td[data-toggle]').click()
      EF.scoll(targetTr)
      EF.colorAnimate(targetTr)

    calculateSumery()

