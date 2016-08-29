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

    express_size = 0
    other_size = 0
    $("#shipmentTable input[name='shipmentId']:checked").each(->
      if $(@).attr("way") == 'EXPRESS'
        express_size++
      else
        other_size++
    )
    if other_size > 0 && express_size > 0
      noty({text: "快递不可与海空运运输同时导出，请重新选择！", type: 'warning'})
      return
    window.open('/Excels/shipmentDetails?' + $("#shipmentTable input[name='shipmentId']:checked").serialize(), "_blank")
  )

  $(':checkbox[class=checkbox_all]').change (e) ->
    $ck = $(@)
    $ck.parents('table').find(':checkbox').not(':first').prop("checked", $ck.prop('checked'))

  $("td[name='clickTd']").click(->
    tr = $(@).parent("tr")
    shipment_id = $(@).attr("shipment_id")
    memo = $(@).attr("memo")
    format_id = shipment_id.replace(/\|/gi, '_')
    if tr.next("tr").find("td").find("div").length > 0
      tr.next("tr").toggle()
    else
      tr.after("<tr><td colspan='14'><div><h4 class='text-info'>Comment</h4>#{memo}</div><hr><div id='div#{format_id}'></div></td></tr>")
      $("#div" + format_id).load("/Shipments/showProcureUnitList", id: shipment_id)
  )