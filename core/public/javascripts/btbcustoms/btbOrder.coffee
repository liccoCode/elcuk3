$ ->
  index = $("#skuDetailTable input[name$='sku']").length - 1
  $("#addSkuBtn").click(->
    index++
    params =
      index: index
    tr = _.template($('#copy-table-template').html())(params)
    $("#addSkuTr").before(tr)
    bindSkuSame()
    bindDeleteBtn()
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
          $.post('/products/findProductName',
            sku: item, (r) ->
            $sku.parent("td").next().next().find("input").val(r.name)
          )
          item
      })
    )

  bindDeleteBtn = () ->
    $("#skuDetailTable input[name='deleteBtn']").click(->
      $btn = $(@)
      $btn.parent().parent().remove()
    )

  bindSkuSame()
  bindDeleteBtn()

  $("#bankChargeSelect").change(->
    if $(@).val() == 'Other'
      $("#bankChargesOther").attr("readonly", false)
    else
      $("#bankChargesOther").attr("readonly", true)
  )

  $("td[name='clickTd']").click(->
    tr = $(@).parent("tr")
    order_id = $(@).data("id")
    if $("#div" + order_id).html() != undefined
      tr.next("tr").toggle()
    else
      tr.after("<tr style='background-color:#F2F2F2'><td colspan='9'><div id='div#{order_id}'></div></td></tr>")
      $("#div" + order_id).load("/BtbCustoms/btbOrderItemList",
        id: order_id)
  )

  $("#download_excel").click((e) ->
    e.preventDefault()
    $form = $("#search_form")
    window.open('/Excels/btbOrderDetailReport?' + $form.serialize(), "_blank")
  )

  $("#add_new_address").click(->
    window.location.href = $(this).data("url") + "?id=" + $("select[name='b.btbCustom.id']").val();
  )