$ ->
  $("#fba_carton_contents_modal").on('click', '#sumbitDeployFBAs', (e) ->
    $('#fba_carton_contents_modal').modal('hide')
  ).on('show', (e) ->
    unless processModalTableLines($(@))
      return e.preventDefault()
  ).on('blur', ":input", (e) ->
    $input = $(@)
    if($input.val() is "" or $input.val() <= 0 or isNaN($input.val()))
      $input.val("1") # 确保用户填写的是大于零的数字
  )

  # 填充数据行到 Modal 中的 Table
  processModalTableLines = (modal) ->
    $tabel = modal.find("#fba_carton_contents_table")
    $tabel.find("tr").not(":eq(0)").remove()

    units = [] # [{id: 1, boxNum: 2}]
    if modal.data('unit-source')
      units.push({id: modal.data('unit-source'), boxNum: 1})
    else
      for checkbox in $('input[name="pids"]:checked')
        units.push({id: checkbox.val(), boxNum: checkbox.attr("boxNum")})

    if _.isEmpty(units)
      noty({text: '请选择采购单元!', type: 'error'})
      false
    else
      units.forEach(unit, index) ->
        tr = "<tr>" +
          "<td>#{unit.id}</td>" +
          "<td><div class='input-append'><input type='text' class='input-mini' name='dtos[#{index}].boxNum' value='#{unit.boxNum}' maxlength='3'/></div></td>" +
          "<td><div class='input-append'><input type='text' class='input-mini' name='dtos[#{index}].num' value='' maxlength='3'/></div></td>" +
          "<td><div class='input-append'><input type='text' class='input-mini' name='dtos[#{index}].singleBoxWeight' value='' maxlength='3'/></div></td>" +
          "<td><div class='input-append'><input type='text' class='input-mini' name='dtos[#{index}].length' value='' maxlength='3'/></div></td>" +
          "<td><div class='input-append'><input type='text' class='input-mini' name='dtos[#{index}].width' value='' maxlength='3'/></div></td>" +
          "<td><div class='input-append'><input type='text' class='input-mini' name='dtos[#{index}].height' value='' maxlength='3'/></div></td>" +
          "</tr>"
        $tabel.append(tr)
      true