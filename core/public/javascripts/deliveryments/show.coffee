window.jQuery = window.$
$ ->
  $(document).on("blur", "input[name='boxNumbers']", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() <= 0 or isNaN($input.val())) then $input.val("1") # 确保用户填写的是大于零的数字
  ).on('click', '#submitDownloadFBAZIP', (e) ->
    submitForm($("#downloadFBAZIP"))
    $('#box_number_modal').modal('hide')
  ).on('click', '#downloadFBAZIP', (e) ->
    checkboxs = $('input[name="pids"]:checked')
    expressid = $("input[name='expressid']").val()
    expressids = if _.isEmpty(expressid) then [] else expressid.split(",")

    if _.isEmpty(checkboxs)
      noty({
        text: '请选择需要下载的采购单元',
        type: 'error'
      })
      return true

    $table = $("#box_number_table")
    $table.find('tr').remove() # 删除表格内所有 tr
    _.each(checkboxs, (checkbox) ->
      $checkbox = $(checkbox)
      # 箱数
      boxNum = $checkbox.data('boxnum')
      # 尾箱箱内产品数量
      lastCartonNum = $checkbox.data('lastcartonnum')
      # 如果尾箱内有数量则表示箱数需要 + 1
      boxNum += 1 if !_.isEmpty(lastCartonNum) && lastCartonNum != 0

      tr = "<tr>" +
        "<td>#{$checkbox.val()}</td>" +
        "<td><div class='input-group'>" +
        "<input type='text' class='form-control' name='boxNumbers' value='#{boxNum}' maxlength='3'/>" +
        "<div class='input-group-addon'>" +
        "箱 #{if expressids.includes($checkbox.val()) > 0 then '(不加后缀)' else ''}" +
        "</div>" +
        "</div></td>" +
        "</tr>"
      $table.append(tr)
    )
    $('#box_number_modal').modal('show')
  ).on("click", "a[name='boxLabelBtn']", (e) ->
    $btn = $(@)
    boxNumber = prompt("请输入采购单元箱数(0-999)", 1)
    if(boxNumber is "" or boxNumber <= 0 or isNaN(boxNumber) or boxNumber > 999)
      noty({
        text: '请正确输入采购单元箱数',
        type: 'error'
      })
    else
      window.open("/FBAs/boxLabel?id=#{$btn.data('id')}&boxNumber=#{boxNumber}", "_blank")
  ).on('click', '#deployFBAs', (e) ->
    $("#fba_carton_contents_modal").modal('show')
    $("#sumbitDeployFBAs").data('url', $(@).data("url"))
    checkboxList = $('input[name="pids"]')
    unitIds = []
    for checkbox in checkboxList when checkbox.checked then unitIds.push(checkbox.value)
    return if _.isEmpty(unitIds)
    $("#refresh_div").load("/ProcureUnits/fbaCartonContents", {unitIds: unitIds})
  ).on('click', '#sumbitDeployFBAs', (e) ->
    $modal = $("#fba_carton_contents_modal")
    return if $modal.data('unit-source')
    $trigger = $("##{$modal.data('modal-trigger')}")
    $action = $("#sumbitDeployFBAs")
    form = $("<form method='post' action='#{$action.data('url')}'></form>")
    form.hide().append($trigger.parents('form').find('input[name="pids"]:checked')).append($modal.find(":input").clone()).appendTo('body')
    form.submit().remove()
  ).on('click', '#updateFbaCartonContents', (e) ->
    $("#fba_carton_contents_modal").modal('show')
    $("#sumbitDeployFBAs").data('url', $(@).data("url"))
    checkboxList = $('input[name="pids"]')
    unitIds = []
    for checkbox in checkboxList when checkbox.checked then unitIds.push(checkbox.value)
    return if _.isEmpty(unitIds)
    $("#refresh_div").load("/ProcureUnits/fbaCartonContents", {unitIds: unitIds})
  ).on('click', '#edit_memo', (e) ->
    if $("#memo").val() == null || $("#memo").val().trim().length == 0
      noty({
        text: '请输入备注!',
        type: 'error'
      })
    else
      $("#updateDeliverymentForm").submit()
  ).on('click', '#confirmBtn', (e) ->
    e.preventDefault()
    $.post("/Deliveryments/validDmtIsNeedApply", {id: $("input[name='dmt.id']").val()}, (r)->
      if r.flag
        return false if !confirm(r.message)
        $("#confirmForm").submit()
      else
        $("#confirmForm").submit()
    )
  ).on('click', '#generate_excel_btn', (e) ->
    e.preventDefault()
    $.post("/Deliveryments/validDmtIsNeedApply", {id: $("input[name='dmt.id']").val()}, (r)->
      if r.flag
        return false if !confirm(r.message)
        $("#confirmForm").submit()
      else
        $("#generate_excel").submit()
    )
  ).on('click', "#update_btn", () ->
    $("#updateDeliverymentForm").submit()
  ).on('click', '#generate_export_excel_btn', (e) ->
    action = $(@).data("url")
    e.preventDefault()
    $.post("/Deliveryments/validDmtIsNeedApply", {id: $("input[name='dmt.id']").val()}, (r)->
      if r.flag
        return false if !confirm(r.message)
        $("#confirmForm").submit()
      else
        $("#generate_excel").attr("action", action)
        $("#generate_excel").submit()
    )
  )

  $("select[name='result']").change(->
    if $(@).val() == 'false'
      $("#apply_input").show()
    else
      $("#apply_input").hide()
  )

  $("#fba_carton_contents_modal").on('change', "input[name='chooseType']", (e) ->
    radio = $("input[name='chooseType']:checked")
    $("#tr_" + radio.val() + " input[name$='boxNum']").val(radio.attr("boxNum"))
    $("#tr_" + radio.val() + " input[name$='num']").val(radio.attr("boxSize"))
    $("#tr_" + radio.val() + " input[name$='boxSize']").val(radio.attr("boxSize"))
    $("#tr_" + radio.val() + " input[name$='lastCartonNum']").val(radio.attr("lastCartonNum"))
    $("#tr_" + radio.val() + " input[name$='singleBoxWeight']").val(radio.attr("singleBoxWeight"))
    $("#tr_" + radio.val() + " input[name$='length']").val(radio.attr("boxLength"))
    $("#tr_" + radio.val() + " input[name$='width']").val(radio.attr("boxWidth"))
    $("#tr_" + radio.val() + " input[name$='height']").val(radio.attr("boxHeight"))
  )

  # 将字符串转化成Dom元素
  parseDom1 = (arg) ->
    objE = document.createElement("table")
    objE.innerHTML = arg
    objE.childNodes[0]

  # Form 提交
  submitForm = (btn)->
    $('#form_method').val(btn.data('method'))
    $form = $("#bulkpost")
    $form.attr('action', btn.data('url')).submit()

  # 为两个 table 的全选 checkbox:label 添加功能
  $('input:checkbox[id*=checkbox_all]').each ->
    $(@).change (e) ->
      o = $(@)
      region = o.attr('id').split('_')[0].trim()
      $("input:checkbox.#{region}").prop("checked", o.prop("checked"))

  fidCallBack = () ->
    {
      fid: $('#deliverymentId').val(),
      p: 'DELIVERYMENT'
    }

  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'col-md-1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)

  $("#chosebuyer").change (e) ->
    return unless $(@).val()
    mask = $('#generate_excel')
    mask.mask('加载中...')
    $.ajax("/users/showJson", {
      type: 'POST',
      dataType: 'json',
      data: {id: $(@).val()}
    }).done((r)->
      unless r.flag
        $('#excel_buyer').val(r['username'])
        $("#excel_buyerPhone").val(r['phone'])
      mask.unmask()
    )

  $("#tl").hide() # 隐藏 Timeline DIV
  _.each($(".selling_id a"), (a) -> # 删除Selling 链接的 href、target 属性
    a.removeAttribute("href")
    a.removeAttribute("target")
  )
  $("#unit_table").on("mouseenter focus", "table td.selling_id a", (e) ->
    $(@).css(
      'cursor': 'pointer')
  ).on("click", ".selling_id a", (e) ->
    $("#tl").show()
    $("#col-body").show()
    $("#tl").parent().parent().show();
    $td = $(@)
    loadTimeLine('sid', $td.text().trim())
  )

  $('#tl').show()
  $("#col-body").hide()

  loadTimeLine = (type, val)->
    $time_line_home = $("#tl")
    LoadMask.mask($time_line_home)
    $.post('/analyzes/ajaxProcureUnitTimeline', {
      type: type,
      val: val
    },
      (r) ->
        try
          if r.flag is false
            alert(r.message)
          else
            eventSource = $('#tl').data('source')
            eventSource.clear()
            eventSource.loadJSON(r, '/')
        finally
          LoadMask.unmask($time_line_home)
    )

  do ->
    paymentId = $("#paymentId").val()
    procureUnitId = window.location.hash[1..-1]
    targetTr = $("#procureUnit_#{procureUnitId}")
    if (targetTr.size() > 0 && paymentId == '')
      EF.scoll(targetTr)
      EF.colorAnimate(targetTr)