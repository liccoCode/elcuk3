$ ->
  $('#search_form').on('click', '.btn', (e) ->
    #过滤掉apply为空的数据
    $("#search_form [type='checkbox']:checked").each(->
      if !$(@).attr("apply")
        $(@).prop("checked", false)
    )
    $('#search_form').attr('action', $(@).data('url'))
  )

  $("#download_excel").click((e) ->
    e.preventDefault()
    $btn = $(@)
    $form = $("#search_form")
    window.open('/Excels/shipmentDetails?'+$form.serialize(),"_blank")
  )

  $(':checkbox[class=checkbox_all]').change (e) ->
    $ck = $(@)
    $ck.parents('table').find(':checkbox').not(':first').prop("checked", $ck.prop('checked'))

