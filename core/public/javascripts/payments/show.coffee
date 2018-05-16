$ ->
  currencyMap = {
    CNY: '人民币',
    EUR: '欧元',
    GBP: '英镑',
    HKD: '港币',
    USD: '美元'
  }

  $(document).on('click', 'button.paymentUnitDeny', (e) ->
    $btn = $(@)
    $('#reason_model').modal('show')
    $('#model_title').text("驳回 #{$btn.parents('tr').find('td:eq(6)').text()} 请款项目")
    $('#model_form').attr('action', $btn.data('url'))
    false
  ).on('click', '#check_all', (e) ->
    $("#apply_table input[type='checkbox']").not(":first").each(->
      $(@).prop("checked", !$(@).prop("checked"))
    )
  ).on('blur','#actualPaid', (e) ->
    if $(@).val()
      $(@).val(parseFloat($(@).val()).toFixed(2));
  )

  $('#pay_form').on('change', 'select[name=currency]',(e) ->
    $slt = $(@)
    if $slt.val()
      updateMainInfo($slt.val())
    else
      $('#ratioInput').val(-1)
      $('#ratioDateTimeInput').val('')
    false
  ).on('change', 'select[name=paymentTargetId]', (e) ->
    $slt = $(@)
    text = $slt.find(':selected').text()
    name = text.split(']')[1]
    [username, account] = text.split(']')[0][1..-1].split(' ')
    $('#paymentTargetInfo').html(
      "账户: #{username}<br>账号: #{account}<br>名称: #{name}"
    )
    false
  )
  $('#shouldPaid').change((e) ->
    $input = $(@)
    if $('#currency').val() is ''
      $input.val('')
      noty({text: '请选择支付币种再继续', type: 'warning', timeout: 3000})
    else
      LoadMask.mask()
      $.ajax($input.data('url'), {type: 'POST', data: $input.parents('form').serialize()})
        .done((r) ->
          if r.flag is false
            text = _.map(JSON.parse(r.message),(err) ->
              err.message
            ).join('<br>')
            noty({text: text, type: 'error', timeoout: 3000})
          else
            noty({text: '应付金额更新成功', type: 'success', timeout: 1000})
          LoadMask.unmask()
        )
    false
  ).keyup((e) ->
    amount = $('#paidCurrencyAmount').text()
    $input = $(@)
    val = $input.val()
    $next = $input.next().text(amount - val).removeClass('text-error text-success')
    if (amount - val) > 0
      $next.addClass('text-error')
    else
      $next.addClass('text-success')
  )

  updateMainInfo = (target) ->
    $mainInfo = $('#mainInfo')
    ratio = extraRatio(target)
    # 汇率 from -> target
    $('#currencyFromTo').text("#{$('#request_currency').text()} -> #{target}")
    $('#ratioTo').text(ratio)

    # 汇率 target -> from
    $('#currencyToFrom').text("#{target} -> #{$('#request_currency').text()}")
    $('#reverRatioTo').text((1 / ratio).toFixed(8))

    # 需要支付的 币种/金额
    $('#paidCurrency').text(target)
    $('#paidCurrencyAmount').text(($('#finalAppied').text() * ratio).toFixed(4))

    # 汇率时间
    $('#ratioTime').text($.DateUtil.fmt3(extraRatioTime(target)))

    $mainInfo.data('ratio', ratio)

  # 抽取两个挂牌价中的汇率
  extraRatio = (target) ->
    from = $('#request_currency').text()
    ratio = if 'CNY' == from
      if from == target
        1.toFixed(8)
      else
        tr = $("#boc_rate tr td:contains(#{currencyMap[target]})").parents('tr')
        (100 / parseFloat(tr.find('td:eq(1)').css('color', 'red').text())).toFixed(8)
    else
      tr = $("#ex_rate tr td:contains(#{target})").parents('tr')
      (parseFloat(tr.find('td:eq(2)').css('color', 'red').text())).toFixed(8)
    $('#ratioInput').val(ratio)
    ratio

  # 抽取挂牌价时间
  extraRatioTime = (target) ->
    date = if 'CNY' == target
      tr = $('#boc_rate tr:eq(1)')
      new Date("#{tr.find('td:eq(6)').text()} #{tr.find('td:eq(7)').text()}")
    else
      new Date()
    $('#ratioDateTimeInput').val($.DateUtil.fmt3(date))
    date

  ajaxBocRate = ->
    $('#boc_rate').load('/payment/boc', ->
      $('#currency').change()
    )

  ajaxExRate = (from = $('#request_currency').text()) ->
    $('#ex_rate').load("/payment/xe?currency=#{from}", ->
      $('#currency').change()
    )

  ajaxBocRate()
  ajaxExRate()


  do ->
    paymentUnitId = window.location.hash[1..-1]
    EF.colorAnimate("#paymentUnit_#{paymentUnitId}")
