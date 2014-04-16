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

  # 产品定位和产品卖点点击新增一行
  $("#update_product_form").on("click", "#more_locate_btn, #more_selling_point_btn", () ->
    $btn = $(@)
    $table = $("##{$btn.data("table")}")[0]
    # 获取表格的行数
    rowsCount = $table.rows.length
    # 通过 js 克隆出一个新的行 由于表格的上下两行分别是标题行和button所在的行，所以使用 表格的行数减去2得到有效的最后一行
    $tr = $("##{$btn.data("table")} tr:eq(#{rowsCount - 1})")[0]
    $newRow
    if rowsCount > 1
      $newRow = $tr.cloneNode(true)
      # 修改 tr 元素内 textarea 的 name 属性
      textareas = $newRow.getElementsByTagName("textarea")
      setTextAreaName($btn.attr("id"), rowsCount, textareas)
    else
      str = if $btn.attr("id") == "more_locate_btn"
        "<tr><td class='span4'><textarea rows=2' style='width:90%' name='pro.locate[0].title'></textarea></td><td><textarea rows='2' style='width:90%' name='pro.locate[0].content'></textarea></td><td><a class='btn' name='delete_locate_row'>删除</a></td></tr>"
      else
        "<tr><td class='span4'><textarea rows=2' style='width:90%' name='pro.sellingPoint[0].title'></textarea></td><td><textarea rows='2' style='width:90%' name='pro.sellingPoint[0].content'></textarea></td><td><a class='btn' name='delete_selling_point_row'>删除</a></td></tr>"
      $newRow = parseDom(str)
    $table.appendChild($newRow)
  ).on("click", "[name^='delete_locate_row'], [name^='delete_selling_point_row']", () ->
    $btn = $(@)
    $btn.parent("td").parent().remove()
  )


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

  # 将字符串转化成Dom元素
  parseDom = (arg) ->
    objE = document.createElement("table")
    objE.innerHTML = arg
    objE.childNodes[0]