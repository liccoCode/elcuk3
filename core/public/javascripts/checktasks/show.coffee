$ ->
  $(document).on("change", "select[name^='check.result']", (r) ->
    $select = $(@)
    $ship = $("select[name='check.isship']")
    if($select.val() == 'AGREE')
      $ship.val("SHIP").attr("disabled", 'true')
    else if ($select.val() == 'NOTAGREE')
      $ship.val("NOTSHIP").attr("disabled", 'true')
    else
      $ship.removeAttr("disabled")
  )

  $("#update_form").on("click", "#update_btn, #fullupdate_btn", (r) ->
    $btn = $(@)
    $form = $("#update_form")

    msg = if $btn.attr("id") == 'fullupdate_btn'
      '提交后将会生成仓库入库记录,确定？'
    else
      '确认保存?'
    return unless confirm(msg)

    $form.attr("action", "/checktasks/#{$btn.attr("id").split("_")[0]}")
    # 提交表单前将下拉项的disabled属性取消掉
    $resultSelect = $("select[name='check.result']")
    $resultSelect.removeAttr("disabled")
    $form.submit()
    $resultSelect.attr("disabled", 'true')
  )

  $("#update_form").on("click", "#submitqc_btn", (r) ->
    $btn = $(@)
    $ship = $("select[name='check.isship']")
    if ($ship.val() == 'NOTSHIP')
      return unless confirm('不发货则表示流程流转到采购,确认提交?')
    else
      return unless confirm('确认提交?')

    $form = $("#update_form")
    $form.attr("action", "/checktasks/submitactiviti")
    # 提交表单前将下拉项的disabled属性取消掉
    $ship.removeAttr("disabled")
    $form.submit()
    $ship.val("NOTSHIP").attr("disabled", 'true')
  )

  $("#update_form").on("click", "#submitqcroll_btn", (r) ->
    $btn = $(@)
    return unless confirm('取消费用将流转到采购,确认提交?')

    $form = $("#update_form")
    $form.attr("action", "/checktasks/rollactiviti")
    # 提交表单前将下拉项的disabled属性取消掉
    $form.submit()
  )

  $("#update_form").on("click", "#planactiviti_btn", (r) ->
    $btn = $(@)
    $dealway = $("select[name='check.dealway']")
    if ($dealway.val() == '')
      alert('请填写处理方式!')
      return
    return unless confirm('确认提交?')

    $form = $("#update_form")
    $ship = $("select[name='check.isship']")
    $form.attr("action", "/checktasks/submitactiviti")
    # 提交表单前将下拉项的disabled属性取消掉
    $ship.removeAttr("disabled")
    $form.submit()
    $ship.val("NOTSHIP").attr("disabled", 'true')
  )

  $("#update_form").on("click", "#operateactiviti_btn", (r) ->
    return unless confirm('确认后则表示已确认该采购计划的预计时间,确认提交?')
    $btn = $(@)
    $form = $("#update_form")
    $ship = $("select[name='check.isship']")
    $form.attr("action", "/checktasks/operateupdateprocess")
    $form.submit()
  )

  $("#update_form").on("click", "#updateprocess_btn", (r) ->
    $btn = $(@)
    $form = $("#update_form")
    $form.attr("action", "/procureunits/updateprocess")
    $form.submit()
  )

  lr = new LocalResize(document.getElementById('file_home'), {})
  lr.success (stop, data) ->
    $file_home = $('#file_home')
    $file_home.data('base64_file', data['base64Clean'])
    $file_home.data('origin_name', data['original']['name'])
    stop()

  $('#submit_button').click ->
    $file_home = $('#file_home')
    $.post('/attachs/uploadForBase64', {
        p: 'CHECKTASK',
        fid: $file_home.data('fid'),
        base64File: $file_home.data('base64_file'),
        originName: $file_home.data('origin_name')
      },
      (r) ->
        alert(r.message)
      window.location.reload()
    )

  fidCallBack = () ->
    {fid: $("input[name='attid']").val(), p: 'CHECKTASK'}
  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)
  initAttachs = ->
    window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1')
    window.dropUpload.iniDropbox(fidCallBack, dropbox)

  size = $("#addMultiBox").attr("data-listSize")

  $('#addMultiBox').click ->
    $btn = $(@)
    $table = $("##{$btn.data("table")}")[0]
    # 获取表格的行数
    rowsCount = $table.rows.length
    # 通过 js 克隆出一个新的行 需要减去上面的标题行
    newRow = "<tr><th>箱数量：</th><td colspan='2'><input type='text' class='input-mini' name='check.tailBoxQctInfos[#{size}].boxNum'/>箱 x "
    newRow += " <input type='text' class='input-mini' name='check.tailBoxQctInfos[#{size}].num'/>个</td>"
    newRow += "<th>单箱体积(m³)：</th><td colspan='3'><span></span></td></tr><tr><th>单箱重量(kg)：</th><td colspan='2'>"
    newRow += "<input type='text' name='check.tailBoxQctInfos[#{size}].singleBoxWeight'></td><th>单箱长宽高(cm)：</th><td colspan='3'>"
    newRow += "<input type='text' class='input-mini' name='check.tailBoxQctInfos[#{size}].length'> x "
    newRow += "<input type='text' class='input-mini' name='check.tailBoxQctInfos[#{size}].width'> x "
    newRow += "<input type='text' class='input-mini' name='check.tailBoxQctInfos[#{size}].height'></td>"
    newRow += "<td><a class='btn' name='delete_tailBoxQctInfos_row'>删除</a></td></tr>";
    # 将生成的 row append 到表格的最下面
    $("#tailBoxQctInfosTable").append(newRow)
    size++

  $("#update_form").on("click", "[name^='delete_tailBoxQctInfos_row']", () ->
    $btn = $(@)
    # remove 掉按钮所在的那一行
    $btn.parent("td").parent().prev().remove()
    $btn.parent("td").parent().remove()
  )

  $("#update_form").on("keyup", "[name$='length'],[name$='width'],[name$='height']", (e) ->
    $td = $(@).parent("td")
    $returnValue = $(@).parent("td").parent().prev().find("td:eq(1)")
    value = 1;
    $td.find("input").each(()->
      value *= $(@).val()
    )
    $returnValue.text((value / 1000000).toFixed(2))
  ).on('click', '#more_desc_btn', (r) ->
    $btn = $(@)
    size = $btn.data('descsize')
    $table = $("#aqlTable")
    $newRow = "<tr><td>#{size + 1}</td><td><input type='text' name='check.aqlBadDesc[#{size}].badDesc' value=''></td><td>"
    $newRow += "<span class='span3'><input type='checkbox' name='check.aqlBadDesc[#{size}].inspectionResult[0]' value='CR'> CR</span>"
    $newRow += "<span class='span3'><input type='checkbox' name='check.aqlBadDesc[#{size}].inspectionResult[1]' value='MAJ'> MAJ</span>"
    $newRow += "<span class='span3'><input type='checkbox' name='check.aqlBadDesc[#{size}].inspectionResult[2]' value='MIN'> MIN</span>"
    $newRow += "</span></td><td><a class='btn btn-danger' name='remove_desc_btn'><i class='icon-remove'></i> 删除</a></td></tr>"
    $table.append($newRow)
    $btn.data('descsize', size + 1)
  ).on('click', '[name=remove_desc_btn]', (r) ->
    $btn = $(@)
    $btn.parent("td").parent().remove()
  )

  $(document).ready ->
    $('#more_desc_btn').trigger('click') if $("#aqlTable > tbody > tr").length == 1


