$ ->
  $('#downloadFBAZIP').click ->
    LoadMask.mask()
    checkboxList = $('input[name="pids"]')
    unitIds = ""
    unitIds += for checkbox in checkboxList when checkbox.checked then checkbox.value + "|"
    $('#unitIds').val(unitIds)
    $form = $("form.search_form")
    if unitIds == ""
      noty({text: '请选择需要下载的采购单', type: 'error'})
      LoadMask.unmask()
      return false
    # ajax 模拟提交完成文件下载
    inputs = '';
    #formData = $form.serialize() + ""
    for pair in ($form.serialize() + "").split('&')
      inputData = pair.split('=')
      inputs += '<input type="hidden" name="' + inputData[0] + '" value="' + inputData[1] + '" />';
    jQuery('<form action="/procureunits/downloadFBAZIP" method="POST">' + inputs + '</form>')
    .appendTo('body').submit().remove()
    LoadMask.unmask()