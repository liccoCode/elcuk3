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
    if(sku is undefined || sku is '')
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

  # 点击button load 模板参数
  $("#extends").on("click", "#add_template_btn", () ->
    LoadMask.mask()
    $("#extends_atts_home").load("/Products/attrs", $("#select_template_form").serialize(), (r)->
      LoadMask.unmask()
    )
  ).on("click", "#remove_attr_btn", () ->

  )
