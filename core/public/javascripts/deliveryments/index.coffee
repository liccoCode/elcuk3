$ ->
  $('#goToDeliverymentApply').click ->
    $('#deliverys_form').attr('method', 'post').attr('action', @getAttribute('url')).submit()

  $("td[name='clickTd']").click(->
    tr = $(@).parent("tr")
    deliverplan_id = $(@).attr("deliverplan_id")
    format_id = deliverplan_id.replace(/\|/gi, '_')
    if tr.next("tr").find("td").find("div").length > 0
      tr.next("tr").toggle()
    else
      tr.after("<tr><td colspan='12'><div id='div#{format_id}'></div></td></tr>")
      $("#div" + format_id).load("/DeliverPlans/showProcureUnitById", id: deliverplan_id)
  )
