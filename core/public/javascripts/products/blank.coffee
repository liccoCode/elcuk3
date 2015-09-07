$ ->
  $('#pro_family').change (e) ->
    $('#pro_sku').val($(@).val() + "-").focus()

  $('#pro_sku').keyup (e) ->
    $(@).val($(@).val().toUpperCase())

  # 产品定位和产品卖点点击新增一行
  $("#create_product_form").on("click", "#more_locate_btn, #more_selling_point_btn",() ->
    $btn = $(@)
    $table = $("##{$btn.data("table")}")[0]
    # 获取表格的行数
    rowsCount = $table.rows.length
    # 通过 js 克隆出一个新的行 由于表格的第一行是标题行，所以使用 表格的行数减去1得到最后一行
    $tr = $("##{$btn.data("table")} tr:eq(#{rowsCount - 1})")[0]
    $newRow = $tr.cloneNode(true)
    # 修改 tr 元素内 textarea 的 name 属性
    textareas = $newRow.getElementsByTagName("textarea")
    setTextAreaName($btn.attr("id"), rowsCount, textareas)
    $table.appendChild($newRow)
  ).on("click", "[name^='delete_locate_row'], [name^='delete_selling_point_row']", () ->
    $btn = $(@)
    $btn.parent("td").parent().remove()
  )

  # 将输入的长度、宽度、高度换算成英寸(inch)
  $(document).on("change", "input[name='pro.lengths'], input[name='pro.width'], input[name='pro.heigh'], input[name='pro.productLengths'], input[name='pro.productWidth'], input[name='pro.productHeigh']", (r) ->
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val()))
      false
    else
      $span = $("<span style='margin-left:10px;'></span>").text("(inch: #{(($input.val()) * 0.0393701).toFixed(2)})")
      $input.parent().next().empty()
      $input.parent().after($span)
  ).on("change", "input[name='pro.weight'], input[name='pro.productWeight']", (r) ->
    # 将输入的重量(kg)换算成盎司(oz)
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val()))
      false
    else
      $span = $("<span style='margin-left:10px;'></span>").text("(oz: #{(($input.val()) * 35.2739619).toFixed(2)})")
      $input.parent().next().next().empty()
      $input.parent().next().after($span)
  ).on('change', "#pro_weight_g, #pro_productWeight_g", ->
    # 将输入的重量(g)换算成 kg
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val()))
      false
    else
      select = ""
      if($input.attr('id') == 'pro_weight_g')
        select = "input[name='pro.weight']"
      else
        select = "input[name='pro.productWeight']"
      $(select).val(($input.val() / 1000).toFixed(2))
      $(select).trigger('change')

  ).on('change', '#pro_category', ->
    categoryId = $(@).val()
    unless categoryId.length < 2 # 输入2 位数才有效(Category ID 不存在一位数的)
      $.get('/familys/categoryRelateFamily', {categoryId: categoryId})
        .done((c) ->
          data_source = c
          $("#pro_family").typeahead({
            source: data_source
          })
        )
  )

  $('#pro_family').popover({trigger: 'focus', content: "<a href='/familys/index' target='_blank'>新增一个 Family</a>", html: true, placement: "top"})

  # 根据点击按钮的不同判断text的名称
  setTextAreaName = (flag, rowsCount, textareas) ->
    if(flag == "more_locate_btn")
      textareas[0].name = "pro.locate[#{rowsCount - 1}].title"
      textareas[0].value = ""
      textareas[1].name = "pro.locate[#{rowsCount - 1}].content"
      textareas[1].value = ""
    else
      textareas[0].name = "pro.sellingPoint[#{rowsCount - 1}].title"
      textareas[0].value = ""
      textareas[1].name = "pro.sellingPoint[#{rowsCount - 1}].content"
      textareas[1].value = ""

  validUpcAndPartNumber = () ->
    flag = true
    if !$("#upc").val()
      noty({text: "UPC必须填写.", type: 'error', timeout: 5000})
      flag = false
    if !$("#partNumber").val()
      noty({text: "Part Number必须填写.", type: 'error', timeout: 5000})
      flag = false
    flag

