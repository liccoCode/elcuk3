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
      window.open('/deliveryments/create?' + $("#create_deliveryment").serialize(), "_blank") if checkCooperators()
  ).on("click", "#createdeliverplan", (e) ->
    if $('input[name="pids"][data-stage="DELIVERY"]:checked').size() is 0
      noty({text: '请选择状态为采购中的采购计划', type: 'error'})
    else
      $('input[name="pids"][data-stage!="DELIVERY"]:checked').prop('checked', false)
      window.open('/deliverplans/deliverplan?' + $("#create_deliveryment").serialize(), "_blank") if checkCooperators()
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

  $(document).on('click', '#deployFBAs', (e) ->
    $("#fba_carton_contents_modal").removeData("unit-source").data('modal-trigger', 'deployFBAs').modal('show')
  ).on('click', '#sumbitDeployFBAs', (e) ->
    $modal = $("#fba_carton_contents_modal")
    return if $modal.data('unit-source')

    $trigger = $("##{$modal.data('modal-trigger')}")
    form = $("<form method='post' action='#{$trigger.data('url')}'></form>")
    form.hide().append($('input[name="pids"]:checked')) #  采购计划 ID
    form.append($trigger.parents('form').find("[name*='p.']").clone()) # index form 表单
    form.append($modal.find(":input").clone()) # dtos
    form.appendTo('body')
    form.submit().remove()
  )

  $('#create_deliveryment').on('change', "select[name=unitCooperator]", (e) ->
    $select = $(@)
    $checkbox = $select.parents('tr').find('input[name="pids"]')
    $.ajax('/procureUnits/updateAttr', {
      type: 'GET',
      data: {id: $checkbox.val(), attr: 'CooperatorId', value: $select.val()},
      dataType: 'json'
    })
    .done((r) ->
      msg = if r.flag is true
        {text: r.message, type: 'success', timeout: 5000}
        $checkbox.data('cooperatorid', $select.val())
      else
        {text: "#{r.message}", type: 'error', timeout: 5000}
      noty(msg)
    )
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

  $("#create_deliveryment").on("click", "a[name='deleteBtn']", (e) ->
    return unless confirm("确认删除吗?")
      e.preventDefault()
  )

  getCheckedUnitIds = () ->
    unitIds = []
    checkboxs = $('input[name="pids"]:checked')
    for checkbox in checkboxs
      unitIds.push(checkbox.value)
    return unitIds

  checkCooperators = () ->
    checkboxs = $('input[name="pids"]:checked')
    cooperatorId = $(checkboxs[0]).data('cooperatorid')
    valid = true
    # 检查是否选择同一供应商
    _.each(checkboxs, (checkbox) ->
      if $(checkbox).data('cooperatorid') != cooperatorId
        noty({text: '请选择供应商相同的采购计划', type: 'error'})
        valid = false
    )
    return valid