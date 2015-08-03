$ ->
  $('#createApplyBtn').click((e) ->
    e.preventDefault()
    #过滤掉apply为空的数据
    $ck = $("#search_form [type='checkbox']:checked")
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
      $('#search_form').attr('action', $(@).data('url'))
      $('#search_form').submit()
  )

  $("#download_excel").click((e) ->
    e.preventDefault()
    $btn = $(@)
    $form = $("#search_form")
    window.open('/Excels/shipmentDetails?' + $form.serialize() + "&" + $("#shipmentTable input[name='shipmentId']:checked").serialize(),
      "_blank")
  )

  $(':checkbox[class=checkbox_all]').change (e) ->
    $ck = $(@)
    $ck.parents('table').find(':checkbox').not(':first').prop("checked", $ck.prop('checked'))

