$ ->
  $('#receiverecord_check_all').change ->
    o = $(@)
    region = o.attr('id').split('_')[0].trim()
    $("input:checkbox:not(:disabled).#{region}").prop("checked", o.prop("checked"))

  $("form.search_form").on('click', 'a[name=confirm]', (e) ->
    if $('input[name="rids"]:checked').size() is 0
      noty({text: '请选择一条记录！', type: 'error'})
      return false
    else
      return unless confirm("确认收货?")
      $("form[name=confirm_form]").submit()
  ).on('click', "#exportBtn", (e) ->
    $btn = $(@)
    form = $('<form method="post" action=""></form>')
    form.attr('action', $btn.data('url')).attr('target', $btn.data('target'))
    form.hide().append($btn.parents('form').find(":input").clone()).appendTo('body')
    form.submit().remove()
  )

  $("form[name=confirm_form]").on('change', "td>:input[name*='Box']", (e) ->
    $input = $(@)
    attr = $input.attr('name')
    value = $input.val()
    return if _.isEmpty(value)

    $.post("/ReceiveRecords/update", {
      id: $input.parents('tr').find('input:checkbox[name=rids]').val(),
      attr: attr,
      value: value
    },
      (r) ->
        if r.flag is false
          noty({text: r.message, type: 'error'})
        else
          noty({text: "更新#{attr}成功!", type: 'success'})
    )
  ).on('disabledInput', "table", (e) ->
    _.each($(@).find("tr"), (tr) ->
      $tr = $(tr)
      state = $tr.find('td[data-name=state]').text().trim()
      if state == '已收货'
        _.each($tr.find(":input[name]"), (input) ->
          $input = $(input)
          if $input.is(':checkbox')
            $input.remove()
          else if $input.is('select')
            $input.parent().text($input.data('value'))
          else
            $input.parent().text($input.val())
        )
    )
  ).on('change', "td>:input[name*='um']", (e) ->
    $input = $(@)
    $tr = $input.parents('tr')
    mainBoxNum = parseInt($tr.find("input[name='mainBox.boxNum']").val())
    mainNum = parseInt($tr.find("input[name='mainBox.num']").val())

    lastBoxNum = parseInt($tr.find("input[name='lastBox.boxNum']").val())
    lastNum = parseInt($tr.find("input[name='lastBox.num']").val())

    tmpSum = 0
    tmpSum += mainBoxNum * mainNum if _.isInteger(mainBoxNum) && _.isInteger(mainNum)
    tmpSum += lastBoxNum * lastNum if _.isInteger(lastBoxNum) && _.isInteger(lastNum)
    $tr.find("input[name='qty']").val(tmpSum)
  )

  $(document).on('click', 'a[name=tryIdMatch]', (e) ->
    $form = $('form.search_form')
    $searchInput = $form.find("input[name='p.search']")
    $searchInput.val('id:123')
    EF.colorAnimate($searchInput)
    setTimeout(->
      $form.submit()
    , 1000)
  )

  $(document).ready ->
    $("form[name=confirm_form] table").trigger("disabledInput")
