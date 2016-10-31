$ ->
  $('#goToDeliverymentApply').click ->
    $('#deliverys_form').attr('method', 'post').attr('action', @getAttribute('url')).submit()

  $("td[name='clickTd']").click(->
    tr = $(@).parent("tr")
    dmt_id = $(@).attr("dmt_id")
    format_id = dmt_id.replace(/\|/gi, '_')

    if $("#div" + format_id).html() != undefined
      tr.next("tr").toggle()
    else
      tr.after("<tr><td colspan='12'><div id='div#{format_id}'></div></td></tr>")
      $("#div" + format_id).load("/Deliveryments/showProcureUnitById", id: dmt_id)
  )