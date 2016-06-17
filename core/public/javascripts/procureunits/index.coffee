$ ->
  unitIds = []
  $(".search_form").on("click", "#downloadFBAZIP, #createdeliveryment, #createdeliverplan", (e) ->
    if $('input[name="pids"]:checked').size() is 0
      noty({text: '请选择采购计划', type: 'error'})
      e.stopImmediatePropagation()# 取消冒泡
  ).on("click", "#downloadFBAZIP", (e) ->
    showBoxNumberModal()
  ).on("click", "#createdeliveryment", (e) ->
    if $('input[name="pids"][data-stage="PLAN"]:checked').size() is 0
      noty({text: '请选择状态为计划中的采购计划', type: 'error'})
    else
      $('input[name="pids"][data-stage!="PLAN"]:checked').prop('checked', false)
      window.open('/deliveryments/create?' + $("#create_deliveryment").serialize(), "_blank")
  ).on("click", "#createdeliverplan", (e) ->
    if $('input[name="pids"][data-stage="DELIVERY"]:checked').size() is 0
      noty({text: '请选择状态为采购中的采购计划', type: 'error'})
    else
      $('input[name="pids"][data-stage!="DELIVERY"]:checked').prop('checked', false)
      window.open('/deliverplans/deliverplan?' + $("#create_deliveryment").serialize(), "_blank")
  ).on("blur", "input[name='boxNumbers']", (e) ->
    $input = $(@)
    # 确保用户填写的是大于零的数字
    if($input.val() is "" or $input.val() <= 0 or isNaN($input.val())) then $input.val("1")
  ).on("click", "#sumbitDownloadFBAZIP", (r) ->
    LoadMask.mask()
    $('#unitIds').val(getCheckedUnitIds().join("_"))
    $form = $("form.search_form")
    # ajax 模拟提交文件下载请求
    inputs = '';
    for pair in ($form.serialize() + "").split('&')
      inputData = pair.split('=')
      inputs += '<input type="hidden" name="' + inputData[0] + '" value="' + inputData[1] + '" />';
    jQuery('<form action="/procureunits/downloadFBAZIP" method="POST">' + inputs + '</form>')
    .appendTo('body').submit().remove()
    $('#unitIds').val("")
    LoadMask.unmask()
    $('#box_number_modal').modal('hide')
  ).on("click", "#download_excel", (r) ->
    window.open('/Excels/procureUnitSearchExcel?' + $("#search_Form").serialize(), "_blank")
  )

  # 将字符串转化成Dom元素
  parseDom1 = (arg) ->
    objE = document.createElement("table")
    objE.innerHTML = arg
    objE.childNodes[0]

  showBoxNumberModal = ->
    $table = $("#box_number_table")[0]
    # 删除表格内所有 tr
    while($table.hasChildNodes())
      $table.removeChild($table.lastChild)
    # 使用选择的采购单 ID 生成新的 tr
    _.each(getCheckedUnitIds(), (value) ->
      $tr = parseDom1("<tr><td>#{value}</td><td><div class='input-append'><input type='text' class='input-mini' name='boxNumbers' value='1' maxlength='3'/><span class='add-on'>箱</span></div></td></tr>")
      $table.appendChild($tr)
    )
    $('#box_number_modal').modal('show')

  $("#create_deliveryment_btn").click (e) ->
    $form = $("#create_deliveryment")
    window.open('/deliveryments/create?' + $form.serialize(), "_blank")

  $("#batch_create_fba_btn").click (->
    $btn = $(@)
    checkboxList = $('input[name="pids"]')
    unitIds = []
    for checkbox in checkboxList when checkbox.checked then unitIds.push(checkbox.value)
    if unitIds.length is 0
      noty({text: '请选择需要批量创建FBA的采购单元', type: 'error'})
      return false

    $form = $("#create_deliveryment")
    $form.attr("action", $btn.data('url')).submit()
  )

  getCheckedUnitIds = () ->
    unitIds = []
    checkboxs = $('input[name="pids"]:checked')
    for checkbox in checkboxs
      unitIds.push(checkbox.value)
    return unitIds
