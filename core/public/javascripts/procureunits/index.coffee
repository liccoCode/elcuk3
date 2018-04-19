$ ->
  unitIds = []
  $(".form-inline").on("click", "#downloadFBAZIP", (e) ->
    checkboxList = $('input[name="pids"]:checked')
    if checkboxList.length is 0
      noty({
        text: '请选择需要下载的采购单元',
        type: 'error'
      })
      return false
    else
      $("#box_number_table").html("")
      checkboxList.each(->
        $tr = "<tr><td style='vertical-align:middle'>#{$(@).val()}</td><td><div class='input-group'>
<input type='text' class='form-control' name='boxNumbers' value='#{$(@).data("boxnum")}' maxlength='3'/>
<span class='input-group-addon'>箱</span></div></td></tr>"
        $("#box_number_table").append($tr)
        unitIds.push($(@).val())
      )
      $('#box_number_modal').modal('show')
  ).on("blur", "input[name='boxNumbers']", (e) ->
# 确保用户填写的是大于零的数字
    $input = $(@)
    if($input.val() is "" or $input.val() <= 0 or isNaN($input.val())) then $input.val("1")
  )

  $("#submitDownloadFBAZIP").click(->
    LoadMask.mask()
    $('#unitIds').val(unitIds.join("_"))
    $form = $("#search_Form")
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

  # 将字符串转化成Dom元素
  parseDom1 = (arg) ->
    objE = document.createElement("table")
    objE.innerHTML = arg
    objE.childNodes[0]



