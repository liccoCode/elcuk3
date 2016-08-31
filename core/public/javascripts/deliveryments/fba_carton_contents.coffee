$ ->
  $("#fba_carton_contents_modal").on('click', '#sumbitDeployFBAs', (e) ->
    $modal = $('#fba_carton_contents_modal')
    for input in $modal.find("input[name*=um]")
      if _.isEmpty($(input).val())
        noty({text: '箱数 和 个数 不允许为空!', type: 'error'})
        return e.stopPropagation()
    $modal.modal('hide')
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
        $checkbox = $(checkbox)
        units.push({id: $checkbox.val(), boxNum: $checkbox.attr("boxNum")})

    if _.isEmpty(units)
      noty({text: '请选择采购单元!', type: 'error'})
      false
    else
      units.forEach((unit, index) ->
        tr = "<tr>" +
          "<td>#{unit.id}</td>" +
          "<td><input type='text' style='width: 30px;' name='dtos[#{index}].boxNum' value='#{unit.boxNum}' maxlength='3'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dtos[#{index}].num'  maxlength='3'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dtos[#{index}].singleBoxWeight'  maxlength='3'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dtos[#{index}].length' maxlength='3'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dtos[#{index}].width'  maxlength='3'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dtos[#{index}].height'  maxlength='3'/></td>" +
          "</tr>"
        $tabel.append(tr)
      )
      true