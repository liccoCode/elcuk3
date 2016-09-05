$ ->
  $("#search_Form").on("click", "#batch_create_fba_btn, #downloadFBAZIP", (e) ->
    if getCheckedIds().length is 0
      noty({text: '请选择出库计划', type: 'error'})
      e.stopImmediatePropagation()# 取消冒泡
  ).on('click', '#batch_create_fba_btn', (e) ->
    window.location.replace('/ShipPlans/batchCreateFBA?' + $("[name='pids'], [name^='p.']").serialize())
  ).on("click", "#downloadFBAZIP", (e) ->
    showBoxNumberModal()
  ).on("click", "#sumbitDownloadFBAZIP", (r) ->
    LoadMask.mask()
    $form = jQuery('<form action="/shipplans/downloadFBAZIP" method="get" target="_blank"></form>')
    $form.append($("[name='pids']:checked, [name='boxNumbers']").clone()).appendTo('body').submit().remove()
    LoadMask.unmask()
    $('#box_number_modal').modal('hide')
  ).on("blur", "input[name='boxNumbers']", (e) ->
    $input = $(@)
    # 确保用户填写的是大于零的数字
    if($input.val() is "" or $input.val() <= 0 or isNaN($input.val())) then $input.val("1")
  ).on("click", "#download_excel", (r) ->
    window.open('/Excels/exportShipPlans?' + $("#search_Form").serialize(), "_blank")
  )

  $(document).on('click', '#deployFBAs', (e) ->
    $("#fba_carton_contents_modal").removeData("unit-source").data('modal-trigger', 'deployFBAs').modal('show')
  ).on('click', '#sumbitDeployFBAs', (e) ->
    $modal = $("#fba_carton_contents_modal")
    return if $modal.data('unit-source')

    $trigger = $("##{$modal.data('modal-trigger')}")
    form = $("<form method='post' action='#{$trigger.data('url')}'></form>")
    form.hide().append($('input[name="pids"]:checked')) #  采购计划 ID
    form.append($trigger.parents('form').find("[name*='p.']").clone()) # index form 表单
    form.append($modal.find(":input").clone()) # dtos
    form.appendTo('body')
    form.submit().remove()
  )

  getCheckedIds = () ->
    ids = []
    checkboxs = $('input[name="pids"]:checked')
    for checkbox in checkboxs
      ids.push(checkbox.value)
    return ids

  showBoxNumberModal = ->
    $table = $("#box_number_table")
    $table.find("tr").remove()
    _.each(getCheckedIds(), (value) ->
      $tr = $(
        "<tr><td>#{value}</td>" +
          "<td><div class='input-append'>" +
          "<input type='text' class='input-mini' name='boxNumbers' value='1' maxlength='3'/>" +
          "<span class='add-on'>箱</span>"
        "</div></td></tr>")
      $table.append($tr)
    )
    $('#box_number_modal').modal('show')