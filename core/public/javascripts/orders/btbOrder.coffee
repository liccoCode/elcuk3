$ ->
  $("#addSkuBtn").click(->
    params =
      index: $("#skuDetailTable input[name$='sku']").length
    tr = _.template($('#copy-table-template').html(), params)
    $("#addSkuTr").before(tr)
    bindSkuSame()
  )

  bindSkuSame = () ->
    $("#skuDetailTable input[name$='sku']").each(->
      $sku = $(@)
      $sku.typeahead({
        source: (query, process) ->
          sku = $sku.val()
          $.get('/products/sameSku', {sku: sku})
          .done((c) ->
            process(c)
          )
        updater: (item) ->
          $.post('/products/findProductName', sku: item, (r) ->
            $sku.parent("td").next().next().find("input").val(r.name)
          )
          item
      })
    )

  bindSkuSame()

  $("#bankChargeSelect").change(->
    if $(@).val() == 'Other'
      $("#bankChargesOther").attr("readonly", false)
    else
      $("#bankChargesOther").attr("readonly", true)
  )

  $("a[name='orderNoBtn']").click(->
    tr = $(@).parent().parent("tr")
    order_id = $(@).attr("order_id")
    if tr.next("tr").find("div[id='sku#{order_id}']").html()
      tr.next("tr").toggle()
    else
      tr.after("<tr><td colspan='8'><div id='sku#{order_id}'></div></td></tr>")
      $("#sku" + order_id).load("/Orders/btbOrderItemList", id: order_id)
  )