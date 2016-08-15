$ ->
  $('#outboundrecord_check_all').change ->
    o = $(@)
    region = o.attr('id').split('_')[0].trim()
    $("input:checkbox:not(:disabled).#{region}").prop("checked", o.prop("checked"))

  $("form.search_form").on('click', 'a[name=confirm]', (e) ->
    if $('input[name="rids"]:checked').size() is 0
      noty({text: '请选择出库记录！', type: 'error'})
      return false
    else
      return unless confirm("确认出库?")
      window.location.replace("/OutboundRecords/confirm?#{$("[name='rids']").serialize()}&#{$("form.search_form").serialize()}")
  ).on('click', '#download_excel', (e) ->
    window.open('/Excels/exportOutboundRecords?' + $("form.search_form").serialize(), "_blank")
  )

  $("form[name=confirm_form]").on('change', "td>:input[name]", (e) -> #"input[name=qty],input[name=memo],select[name=whouse],select[name=targetId]"
    $input = $(@)
    attr = $input.attr('name')
    value = $input.val()

    return if _.isEmpty(value)

    $.post("/OutboundRecords/update", {
      id: $input.parents('tr').find('input:checkbox[name=rids]').val(),
      attr: attr,
      value: value
    }, (r) ->
      if r.flag
        msg = if _.isEmpty(AttrsFormat[attr]) then attr else AttrsFormat[attr]
        noty({text: "更新#{msg}成功!", type: 'success'})
        $input.trigger('flushQty') #需要更新 Qty 字段
      else
        noty({text: r.message, type: 'error'})
    )
  ).on('disabledInput', "table", (e) ->
    _.each($(@).find("tr"), (tr) ->
      $tr = $(tr)
      state = $tr.find('input[name=state]').val()
      if state != 'Pending'
        _.each($tr.find(":input[name]"), (input) ->
          $input = $(input)
          if $input.is(':checkbox')
            $input.remove()
          else if $input.is('select')
            $input.parent().text($input.data('value'))
          else
            $input.parent().text($input.val())
        )
    )
  ).on('initSelectize', "select[name='targetId']", (e) ->
    $select = $(@)
    type = $select.data('type')
    $select.selectize({
      persist: false,
      create: if type == "Other" then true else false,
      load: (query, callback) ->
        return callback() if !query.length || !type.length || $.inArray(type, ["Process", "Sample"]) > -1

        dataType = if $.inArray(type, ["Normal", "B2B"]) > -1
          'SHIPPER'
        else if type == 'Refund'
          'SUPPLIER'
        else
          null
        $.ajax({
          url: '/Cooperators/findSameCooperator',
          type: 'GET',
          dataType: 'json',
          data: {name: query, type: dataType},
          error: ->
            callback()
          success: (res) ->
            coopers = []
            _.each(res, (cooper) ->
              [t, v] = cooper.split('-')
              coopers.push({value: v, text: t})
            )
            callback(coopers)
        })
    })
  ).on('flushQty', "td>:input[name*='Box']", (e) ->
    $input = $(@)
    $tr = $input.parents('tr')
    mainBoxNum = parseInt($tr.find("input[name='mainBox.boxNum']").val())
    mainNum = parseInt($tr.find("input[name='mainBox.num']").val())

    lastBoxNum = parseInt($tr.find("input[name='lastBox.boxNum']").val())
    lastNum = parseInt($tr.find("input[name='lastBox.num']").val())

    tmpSum = 0
    tmpSum += mainBoxNum * mainNum if _.isInteger(mainBoxNum) && _.isInteger(mainNum)
    tmpSum += lastBoxNum * lastNum if _.isInteger(lastBoxNum) && _.isInteger(lastNum)
    $tr.find("input[name='qty']").val(tmpSum)
  )

  $(document).on('click', 'a[name=tryIdMatch]', (e) ->
    $form = $('form.search_form')
    $searchInput = $form.find("input[name='p.search']")
    $searchInput.val('id:123')
    EF.colorAnimate($searchInput)
    setTimeout(->
      $form.submit()
    , 1000)
  )

  AttrsFormat = {
    "qty": "实际出货数量",
    "memo": "备注",
    "whouse": "仓库",
    "targetId": "出库对象",
    "outboundDate": "完成时间",
    "mainBox.boxNum": "主箱箱数",
    "mainBox.num": "主箱数量",
    "mainBox.singleBoxWeight": "主箱重量",
    "mainBox.length": "主箱长",
    "mainBox.width": "主箱宽",
    "mainBox.height": "主箱高",
    "lastBox.boxNum": "尾箱箱数",
    "lastBox.num": "尾箱数量",
    "lastBox.singleBoxWeight": "尾箱重量",
    "lastBox.length": "尾箱长",
    "lastBox.width": "尾箱宽",
    "lastBox.height": "尾箱高",
    "clearanceType": "报关类型"
  }

  $(document).ready ->
    $("form[name=confirm_form] table").trigger("disabledInput")
    $("select[name='targetId']").trigger("initSelectize")


