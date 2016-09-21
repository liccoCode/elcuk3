$ ->
  $("#fba_carton_contents_modal").on('click', '#sumbitDeployFBAs', (e) ->
    $modal = $('#fba_carton_contents_modal')
    for input in $modal.find("input[name*=boxNum], input[name*=num]")
      if _.isEmpty($(input).val())
        noty({text: '箱数 和 个数 不允许为空!', type: 'error'})
        return e.stopPropagation()
    $modal.modal('hide')
  ).on('show', (e) ->
    unless processModalTableLines($(@))
      return e.preventDefault()
  ).on('blur', ":input", (e) ->
    $input = $(@)
    if _.isEmpty($input.val())
      return
    else if $input.val() <= 0 or isNaN($input.val())
      $input.val("1") # 确保用户填写的是大于零的数字
  )

  # 填充数据行到 Modal 中的 Table
  processModalTableLines = (modal) ->
    $tabel = modal.find("#fba_carton_contents_table")
    $tabel.find("tr").not(":eq(0)").remove()

    units = [] # [{id: 1, boxNum: 2}]
    if modal.data('unit-source')
      $checkbox = $("input[name='pids'][value='#{modal.data('unit-source')}']")
      units.push({
        id: modal.data('unit-source'),
        boxNum: $checkbox.data("boxnum"),
        num: $checkbox.data("num"),
        lastCartonNum: $checkbox.data("lastcartonnum"),
        singleBoxWeight: $checkbox.data("singleboxweight"),
        length: $checkbox.data("length"),
        width: $checkbox.data("width"),
        height: $checkbox.data("height")
      })
    else
      for checkbox in $('input[name="pids"]:checked')
        $checkbox = $(checkbox)
        units.push({
          id: $checkbox.val(),
          boxNum: $checkbox.data("boxnum"),
          num: $checkbox.data("num"),
          lastCartonNum: $checkbox.data("lastcartonnum"),
          singleBoxWeight: $checkbox.data("singleboxweight"),
          length: $checkbox.data("length"),
          width: $checkbox.data("width"),
          height: $checkbox.data("height")
        })

    if _.isEmpty(units)
      noty({text: '请选择采购单元!', type: 'error'})
      false
    else
      if modal.data('unit-source')
        unit = units[0]
        tr = "<tr>" +
          "<td>#{unit.id}</td>" +
          "<td><input type='text' style='width: 30px;' name='dto.boxNum' value='#{unit.boxNum}' maxlength='4'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dto.num'  value='#{unit.num}' maxlength='4'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dto.lastCartonNum'  value='#{unit.lastCartonNum}' placeholder='可选' maxlength='4'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dto.singleBoxWeight' value='#{unit.singleBoxWeight}' maxlength='4'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dto.length' value='#{unit.length}' maxlength='4'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dto.width' value='#{unit.width}'  maxlength='4'/></td>" +
          "<td><input type='text' style='width: 30px;' name='dto.height' value='#{unit.height}'  maxlength='4'/></td>" +
          "</tr>"
        $tabel.append(tr)
      else
        units.forEach((unit, index) ->
          tr = "<tr>" +
              "<td>#{unit.id}</td>" +
              "<td><input type='text' style='width: 30px;' name='dtos[#{index}].boxNum' value='#{unit.boxNum}' maxlength='4'/></td>" +
              "<td><input type='text' style='width: 30px;' name='dtos[#{index}].num'  value='#{unit.num}' maxlength='4'/></td>" +
              "<td><input type='text' style='width: 30px;' name='dtos[#{index}].lastCartonNum'  value='#{unit.lastCartonNum}' placeholder='可选' maxlength='4'/></td>" +
              "<td><input type='text' style='width: 30px;' name='dtos[#{index}].singleBoxWeight' value='#{unit.singleBoxWeight}' maxlength='4'/></td>" +
              "<td><input type='text' style='width: 30px;' name='dtos[#{index}].length' value='#{unit.length}' maxlength='4'/></td>" +
              "<td><input type='text' style='width: 30px;' name='dtos[#{index}].width' value='#{unit.width}'  maxlength='4'/></td>" +
              "<td><input type='text' style='width: 30px;' name='dtos[#{index}].height' value='#{unit.height}'  maxlength='4'/></td>" +
              "</tr>"
          $tabel.append(tr)
        )
      true