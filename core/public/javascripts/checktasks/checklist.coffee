$ ->
  $('.search_form').on('change', "select[name='p.dateType']", (e) ->
    $select = $(@)
    $from = $("input[name='p.from']")
    $to = $("input[name='p.to']")

    date = new Date()
    if $select.val() is 'units.attrs.planShipDate'
      $from.val($.DateUtil.fmt2(date))
      $to.val($.DateUtil.fmt2($.DateUtil.addDay(2, date)))
    else
      $from.val($.DateUtil.fmt2($.DateUtil.addDay(-2, date)))
      $to.val($.DateUtil.fmt2(date))
  )