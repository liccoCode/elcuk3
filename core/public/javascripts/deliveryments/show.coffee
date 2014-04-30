$ ->
  $('#bulkpost').on('click', "#delunit_form_submit, #deployFBAs, #sumbitDownloadFBAZIP, #downloadProcureunitsOrder",
  (e) ->
    $btn = $(@)
    return false unless confirm("确认 #{$btn.text().trim()} ?")
    $('#form_method').val($btn.data('method'))
    $form = $btn.parents('form')
    $form.attr('action', $btn.data('url')).submit()
    false
  )

  $("#downloadFBAZIP").click ->
    checkboxList = $('input[name="pids"]')
    unitIds = []
    for checkbox in checkboxList when checkbox.checked then unitIds.push(checkbox.value)
    if unitIds.length is 0
      noty({text: '请选择需要下载的采购单元', type: 'error'})
      return false

    $table = $("#box_number_table")[0]
    # 先删除表格内所有的行
    while($table.hasChildNodes())
      $table.removeChild($table.lastChild)
    # 使用选择的 ID 生成行
    _.each(unitIds, (value) ->
      $tr = parseDom1("<tr><td>#{value}</td><td><div class='input-append'><input type='text' class='input-mini' name='boxNumbers' value='001'/><span class='add-on'>箱</span></div></td></tr>")
      $table.appendChild($tr)
    )
    $('#box_number_modal').modal('show')

  # 将字符串转化成Dom元素
  parseDom1 = (arg) ->
    objE = document.createElement("table")
    objE.innerHTML = arg
    objE.childNodes[0]

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

  do ->
    procureUntiId = window.location.hash[1..-1]
    targetTr = $("#procureUnit_#{procureUntiId}")
    EF.scoll(targetTr)
    EF.colorAnimate(targetTr)