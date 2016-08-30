window.jQuery = window.$
$ ->
  $(document).on('click', "#delunit_form_submit,  #downloadProcureunitsOrder",
    (e) ->
      $btn = $(@)
      return false unless confirm("确认 #{$btn.text().trim()} ?")
      submitForm($btn)
  ).on("blur", "input[name='boxNumbers']", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() <= 0 or isNaN($input.val())) then $input.val("1") # 确保用户填写的是大于零的数字
  ).on('click', '#sumbitDownloadFBAZIP', (e) ->
    submitForm($("#downloadFBAZIP"))
    $('#box_number_modal').modal('hide')
  ).on('click', '#downloadFBAZIP', (e) ->
    checkboxList = $('input[name="pids"]:checked')
    expressid = $("input[name='expressid']").val()
    unitIds = []
    checkboxList.each(->
      unitIds.push($(@).val() + "-" + $(@).attr("boxNum"))
    )
    if unitIds.length is 0
      noty({text: '请选择需要下载的采购单元', type: 'error'})
      return false

    $table = $("#box_number_table")[0]
    # 删除表格内所有 tr
    while($table.hasChildNodes())
      $table.removeChild($table.lastChild)
    # 使用选择的采购单 ID 生成新的 tr
    _.each(unitIds, (value) ->
      if expressid.indexOf(',' + value + ',') > 0
        $tr = parseDom1("<tr><td>#{value.split('-')[0]}</td><td><div class='input-append'><input type='text' class='input-mini' name='boxNumbers' value='#{value.split('-')[1]}' maxlength='3'/><span class='add-on'>箱</span></div></td></tr>")
      else
        $tr = parseDom1("<tr><td>#{value.split('-')[0]}</td><td><div class='input-append'><input type='text' class='input-mini' name='boxNumbers' value='#{value.split('-')[1]}' maxlength='3'/><span class='add-on'>箱(不加后缀)</span></div></td></tr>")
      $table.appendChild($tr)
    )
    $('#box_number_modal').modal('show')
  ).on("click", "a[name='boxLabelBtn']", (e) ->
    $btn = $(@)
    boxNumber = prompt("请输入采购单元箱数(0-999)", 1)
    if(boxNumber is "" or boxNumber <= 0 or isNaN(boxNumber) or boxNumber > 999)
      noty({text: '请正确输入采购单元箱数', type: 'error'})
    else
      window.open("/FBAs/boxLabel?id=#{$btn.data('id')}&boxNumber=#{boxNumber}", "_blank")
  ).on('click', '#deployFBAs', (e) ->
    $("#fba_carton_contents_modal").removeData("unit-source").modal('show')
  ).on('click', '#sumbitDeployFBAs', (e) ->
    $modal = $("#fba_carton_contents_modal")
    return if $modal.data('unit-source')
    $trigger = $("#deployFBAs")
    form = $("<form method='post' action='#{$trigger.data('url')}'></form>")
    form.hide().append($trigger.parents('form').find('input[name="pids"]:checked')).append($modal.find(":input").clone()).appendTo('body')
    form.submit().remove()
  )

  # 将字符串转化成Dom元素
  parseDom1 = (arg) ->
    objE = document.createElement("table")
    objE.innerHTML = arg
    objE.childNodes[0]

  # Form 提交
  submitForm = (btn)->
    $('#form_method').val(btn.data('method'))
    $form = btn.parents('form')
    $form.data('method', btn.data('method')).attr('action', btn.data('url')).submit()

  # 为两个 table 的全选 checkbox:label 添加功能
  $('input:checkbox[id*=checkbox_all]').each ->
    $(@).change (e) ->
      o = $(@)
      region = o.attr('id').split('_')[0].trim()
      $("input:checkbox.#{region}").prop("checked", o.prop("checked"))

  fidCallBack = () ->
    {fid: $('#deliverymentId').text(), p: 'DELIVERYMENT'}
  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)

  $("#chosebuyer").change (e) ->
    return unless $(@).val()
    mask = $('#generate_excel')
    mask.mask('加载中...')
    $.ajax("/users/showJson", {type: 'POST', dataType: 'json', data: {id: $(@).val()}})
    .done((r)->
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
  $("#unit_list").on("mouseenter focus", "table td.selling_id a", (e) ->
    $(@).css('cursor': 'pointer')
  ).on("click", "table td.selling_id a", (e) ->
    $("#tl").show()
    $td = $(@)
    loadTimeLine('sid', $td.text().trim())
  )

  loadTimeLine = (type, val)->
    $time_line_home = $("#tl")
    LoadMask.mask($time_line_home)
    $.post('/analyzes/ajaxProcureUnitTimeline', {type: type, val: val},
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
    procureUntiId = window.location.hash[1..-1]
    targetTr = $("#procureUnit_#{procureUntiId}")
    if targetTr.size() > 0
      EF.scoll(targetTr)
      EF.colorAnimate(targetTr)