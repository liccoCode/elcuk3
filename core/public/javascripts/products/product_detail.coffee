$ ->
  # 图片
  dropbox = $('#dropbox')
  # 包装
  packageDropbox = $('#packageDropbox')
  # 说明书
  instructionsDropbox = $('#instructionsDropbox')
  # 丝印文件
  silkscreenDropbox = $('#silkscreenDropbox')

  # 加载此 SKU 所拥有的全部附件
  window.dropUpload.loadAttachs($('#p_sku').val())

  fidCallBack = ->
    sku = $('#p_sku').val()
    if _.isEmpty(sku)
      alert("没有 SKU, 错误页面!")
      return false
    {fid: sku, p: 'SKU'}

  # 初始化 上传 div
  window.dropUpload.iniDropbox(fidCallBack, dropbox)
  window.dropUpload.iniDropbox(fidCallBack, packageDropbox)
  window.dropUpload.iniDropbox(fidCallBack, instructionsDropbox)
  window.dropUpload.iniDropbox(fidCallBack, silkscreenDropbox)

  # 产品定位和产品卖点 点击按钮新增一行
  $("#update_product_form").on("click", "#more_locate_btn, #more_selling_point_btn",() ->
    $btn = $(@)
    $table = $("##{$btn.data("table")}")[0]
    # 获取表格的行数
    rowsCount = $table.rows.length
    # 通过 js 克隆出一个新的行 需要减去上面的标题行
    $tr = $("##{$btn.data("table")} tr:eq(#{rowsCount - 1})")[0]

    $newRow = if rowsCount > 1
      clone = $tr.cloneNode(true)
      # 修改 tr 元素内 textarea 的 name 属性
      textareas = clone.getElementsByTagName("textarea")
      textareas[0].name = "pro.#{$btn.data("name")}[#{rowsCount - 1}].title"
      textareas[0].value = ""
      textareas[1].name = "pro.#{$btn.data("name")}[#{rowsCount - 1}].content"
      textareas[1].value = ""
      clone
    else
      str = if $btn.attr("id") == "more_locate_btn"
        "<tr><td class='span4'><textarea rows=2' style='width:90%' name='pro.locate[0].title'></textarea></td><td><textarea rows='2' style='width:90%' name='pro.locate[0].content'></textarea></td><td><a class='btn' name='delete_locate_row'>删除</a></td></tr>"
      else
        "<tr><td class='span4'><textarea rows=2' style='width:90%' name='pro.sellingPoint[0].title'></textarea></td><td><textarea rows='2' style='width:90%' name='pro.sellingPoint[0].content'></textarea></td><td><a class='btn' name='delete_selling_point_row'>删除</a></td></tr>"
      parseDom(str)

    # 将生成的 row append 到表格的最下面
    $table.appendChild($newRow)
  ).on("click", "[name^='delete_locate_row'], [name^='delete_selling_point_row']", () ->
    $btn = $(@)
    # remove 掉按钮所在的那一行
    $btn.parent("td").parent().remove()
  )

  # 将字符串转化成Dom元素
  parseDom = (arg) ->
    objE = document.createElement("table")
    objE.innerHTML = arg
    objE.childNodes[0]

  $("#extends").on("click", "#add_template_btn",() ->
    temp_id = $("select[name='templateId']").val()
    if temp_id is ""
      noty({text: "请选择要加载的模板", type: 'error', timeout: 5000})
    else
      LoadMask.mask()
      $("#extends_atts_home").load("/Products/attrs", $("#select_template_form").serialize(), (r)->
        LoadMask.unmask()
      )
  ).on("click", "#remove_attr_btn",() ->
    LoadMask.mask()
    $btn = $(@)
    $.ajax('/products/delAttr',
    {type: 'GET', data: {sku: $btn.data("sku"), attrId: $btn.data("attr")}, dataType: 'json'})
    .done((r) ->
        msg = if r.flag is true
          # 删除 tr
          $btn.parent("td").parent().remove()
          {text: "附加属性 #{r.message} 删除成功.", type: 'success', timeout: 5000}
        else
          {text: "#{r.message}", type: 'error', timeout: 5000}
        noty(msg)
        LoadMask.unmask()
      )
  ).on("click", "#save_attrs_btn", () ->
    LoadMask.mask()
    $form = $("#save_attrs_form")
    $.ajax('/products/saveAttrs', {type: 'GET', data: $form.serialize(), dataType: 'json'})
    .done((r) ->
        msg = if r.flag is true
          {text: "保存成功.", type: 'success', timeout: 5000}
        else
          {text: "#{r.message}", type: 'error', timeout: 5000}
        noty(msg)
        LoadMask.unmask()
      )
  )

  $("#basicinfo").on("click", "#save_basic_btn",() ->
    if !validUpcAndPartNumber()
      return
    if $('input[name="pro.iscopy"]').val() is "2"
      return unless confirm('该SKU的产品名称与选择的SKU的产品名称一致,确定保存?')
    if $("#proabbreviation").val() is ""
      noty({text: "产品名称不允许为空.", type: 'error', timeout: 5000})
      return
    LoadMask.mask()
    $form = $("#update_product_form")
    $.ajax('/products/update', {type: 'POST', data: $form.serialize(), dataType: 'json'})
    .done((r) ->
        msg = if r.flag is true
          {text: "保存成功.", type: 'success', timeout: 5000}
        else
          {text: "#{r.message}", type: 'error', timeout: 5000}
        noty(msg)
        LoadMask.unmask()
      )
  ).on("change", "#proabbreviation", () ->
    $('input[name="pro.iscopy"]').val("1")
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
    # 将输入的重量换算成盎司(oz)
    $input = $(@)
    if($input.val() is "" or $input.val() < 0 or isNaN($input.val()))
      false
    else
      $span = $("<span style='margin-left:10px;'></span>").text("(oz: #{(($input.val()) * 35.2739619).toFixed(2)})")
      $input.parent().next().empty()
      $input.parent().after($span)
  ).on('click', '#deleteBtn', ->
    $deleteBtn = $(@)
    $logForm = $("#logForm")
    $($("#logForm input")[0]).val($deleteBtn.data('sku')) # 设置 SKU 到隐藏的 Modal 中
    $logForm.modal() # 展示 Modal
  )

  # 页面初始化时触发一次
  inputs = ["input[name='pro.lengths']", "input[name='pro.width']", "input[name='pro.heigh']", "input[name='pro.productLengths']", "input[name='pro.productWidth']", "input[name='pro.productHeigh']", "input[name='pro.weight']", "input[name='pro.productWeight']"]
  _.each(inputs, (value) ->
    $(value).trigger("change")
  )

  validUpcAndPartNumber = () ->
    flag = true
    if !$("#upc").val()
      noty({text: "UPC必须填写.", type: 'error', timeout: 5000})
      flag = false
    if !$("#partNumber").val()
      noty({text: "Part Number必须填写.", type: 'error', timeout: 5000})
      flag = false
    flag
