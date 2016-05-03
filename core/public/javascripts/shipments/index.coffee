$ ->
  $('#createApplyBtn').click((e) ->

#过滤掉apply为空的数据
    $ck = $("#shipmentTable [type='checkbox']:checked")
    size = $ck.length
    i = 0
    $ck.each(->
      if $(@).attr("apply")
        $(@).prop("checked", false)
        i++
    )
    if i == size && size != 0
      noty({text: "您选择的运输单全部都已经创建过请款单了，请重新选择！", type: 'warning'})
    else
      $('#search_form').attr('action', $(@).data('url')).submit()
  )

  $("#download_excel").click((e) ->
    e.preventDefault()
    $form = $("#search_form")
    window.open('/Excels/shipmentDetails?' + $form.serialize() + "&" + $("#shipmentTable input[name='shipmentId']:checked").serialize(),
      "_blank")
  )

  $('#search_form').on('click', '#outboundBtn', (e) ->
    $btn = $(@)
    checkboxs = $btn.parents('form').find("input:checkbox[name='shipmentId']:checked")
    if checkboxs.length == 0
      noty({text: "请选择运输单！", type: 'warning'})
      return

    form = $('<form method="post" action=""></form>')
    form.attr('action', $btn.data('url')).attr('target', $btn.data('target'))
    form.hide().append(checkboxs.clone()).appendTo('body')
    form.submit().remove()
  )

  $(':checkbox[class=checkbox_all]').change (e) ->
    $ck = $(@)
    $ck.parents('table').find(':checkbox').not(':first').prop("checked", $ck.prop('checked'))