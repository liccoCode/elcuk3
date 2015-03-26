$ ->
  $('#skusearch').change (e) ->
    $input = $(@)
    islike = document.getElementById("islike").checked

    postval = $("#postVal").val()
    if $input.data('products') is undefined
      $input.data('products', $input.data('source'))

    return false if !(@value in $input.data('products'))

    if islike == true
      addSkus()
      return

    return false if postval.indexOf(@value) > 0
    trcount = $("#skutable tr").length
    gettr = document.getElementById("skutable").rows[trcount - 1]
    gettr.innerHTML += "<td  colspan=1><a href='javascript:;' rel='tooltip'>" + @value + "</a> <a name='skudelete' copItemId='" + @value + "' class='btn btn-mini delelte'><i class='icon-remove'></i></a></td>"
    tdcount = gettr.getElementsByTagName("td").length
    if tdcount != 0 and tdcount % 6 == 0
      $("#skutable").append("<tr  class='table table-condensed table-bordered'></tr>")
    $("#postVal").val(postval + "," + @value)

  addSkus = ->
    $input = $("#skusearch")
    prefix = $input.val().substr(0, $input.val().indexOf("-") + 1)
    _.each($input.data('products'), (product) ->
      postval = $("#postVal").val()
      if postval.indexOf(product) <= 0 and prefix == product.substr(0, prefix.length)
        trcount = $("#skutable tr").length
        gettr = document.getElementById("skutable").rows[trcount - 1]
        gettr.innerHTML += "<td  colspan=1><a href='javascript:;' rel='tooltip'>" + product + "</a> <a name='skudelete' copItemId='" + product + "' class='btn btn-mini delelte'><i class='icon-remove'></i></a></td>"
        tdcount = gettr.getElementsByTagName("td").length

        if tdcount != 0 and tdcount % 6 == 0
          $("#skutable").append("<tr  class='table table-condensed table-bordered'></tr>")
        $("#postVal").val(postval + "," + product)
    )

  $('#skutable').on('click', "[name='skudelete']", ->
    $sku = $(@)
    skuvalue = $sku.attr("copItemId")
    postval = $("#postVal").val()
    postval = postval.replace("," + skuvalue, '')
    $("#postVal").val(postval)

    $sku.parent("td").remove()

  )

  # Form 搜索功能
  $(".search_form").on("click", ".btn:contains(Search)",(e) ->
    e.preventDefault()
    if checkVal()
      LoadMask.mask()
      $('#sku_records').load('/AnalyzeSkus/processSkuSalesReport', $('.search_form').serialize(), (r) ->)
      LoadMask.unmask()
  ).on("click", ".btn:contains(Export)", (e) ->
    if checkVal()
      $form = $("#click_param")
      $form.attr('action', '/Excels/skuSalesReport')
      $form.attr('target', '_blank')
      $form.submit()
  )

  checkVal = ->
    postval = $("#postVal").val()
    if postval == ''
      alert('请添加SKU')
      return false
    else
      return true