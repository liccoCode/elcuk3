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
      return unless confirm("确认入库?")
      $("form[name=confirm_form]").submit()
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
    },
      (r) ->
        if r.flag is false
          noty({text: r.message, type: 'error'})
        else
          msg = if _.isEmpty(AttrsFormat[attr]) then attr else AttrsFormat[attr]
          noty({text: "更新#{msg}成功!", type: 'success'})
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
    "qty": "实际入库",
    "memo": "备注",
    "whouse": "仓库",
    "targetId": "出库对象",
    "outboundDate": "完成时间"
  }

  $("form[name=confirm_form] table").trigger("disabledInput")

