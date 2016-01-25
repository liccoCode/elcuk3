$ ->
  unitIds = []
  $(".search_form").on("click", "#downloadFBAZIP",(e) ->
    checkboxList = $('input[name="pids"]')
    unitIds = []
    for checkbox in checkboxList when checkbox.checked then unitIds.push(checkbox.value)
    if unitIds.length is 0
      noty({text: '请选择需要下载的采购单元', type: 'error'})
      return false
    $table = $("#box_number_table")[0]
    # 删除表格内所有 tr
    while($table.hasChildNodes())
      $table.removeChild($table.lastChild)
    # 使用选择的采购单 ID 生成新的 tr
    _.each(unitIds, (value) ->
      $tr = parseDom1("<tr><td>#{value}</td><td><div class='input-append'><input type='text' class='input-mini' name='boxNumbers' value='1' maxlength='3'/><span class='add-on'>箱</span></div></td></tr>")
      $table.appendChild($tr)
    )
    $('#box_number_modal').modal('show')
  ).on("blur", "input[name='boxNumbers']",(e) ->
    # 确保用户填写的是大于零的数字
    $input = $(@)
    if($input.val() is "" or $input.val() <= 0 or isNaN($input.val())) then $input.val("1")
  ).on("click", "#sumbitDownloadFBAZIP", (r) ->
    LoadMask.mask()
    $('#unitIds').val(unitIds.join("_"))
    $form = $("form.search_form")
    # ajax 模拟提交文件下载请求
    inputs = '';
    #formData = $form.serialize() + ""
    for pair in ($form.serialize() + "").split('&')
      inputData = pair.split('=')
      inputs += '<input type="hidden" name="' + inputData[0] + '" value="' + inputData[1] + '" />';
    jQuery('<form action="/procureunits/downloadFBAZIP" method="POST">' + inputs + '</form>')
    .appendTo('body').submit().remove()
    $('#unitIds').val("")
    LoadMask.unmask()
    $('#box_number_modal').modal('hide')
  )

  $("#select_cooper").selectize()

  # 将字符串转化成Dom元素
  parseDom1 = (arg) ->
    objE = document.createElement("table")
    objE.innerHTML = arg
    objE.childNodes[0]

  $("#download_excel").click((e) ->
    e.preventDefault()
    $btn = $(@)
    $form = $("#search_Form")
    window.open('/Excels/procureUnitSearchExcel?' + $form.serialize(), "_blank")
  )


  $("#create_deliverplan_btn").click (e) ->
    $form = $("#create_deliveryment")
    window.open('/deliverplans/deliverplan?' + $form.serialize(), "_blank")

  $("#create_deliveryment_btn").click (e) ->
    $form = $("#create_deliveryment")
    window.open('/deliveryments/create?' + $form.serialize(), "_blank")
