$ ->
  $('#ship_comment').click (e) ->
    e.preventDefault()
    mask = $('#container')
    mask.mask('更新 Comment')
    $.post('/shipments/comment',
      {
        id: $("input[name=shipid]").val(),
        cmt: $("#ship_memo").val().trim(),
        track: $("[name=ship\\.trackNo]").val(),
        jobNumber: $("#jobNumber").val(),
        totalWeightShipment: $("#totalWeightShipment").val(),
        totalVolumeShipment: $("#totalVolumeShipment").val(),
        totalStockShipment: $("#totalStockShipment").val(),
        reason: $("#reason").val()
      },
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert('更新成功.')
        mask.unmask()
    )

  fidCallBack = () ->
    {fid: $('#shipmentId').val(), p: 'SHIPMENT'}
  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)

  $("#split_shipment").click (e) ->
    if !confirm('确认提交?')
      e.preventDefault()
    else
      $(@).parents('form').attr('action', '/Shipments/splitShipment').submit()

  # 所有的 btnFucs 下的 button action
  $('#btnFucs').on('click', '.func', ->
    funcsForm = $('#funcsForm').find('form').attr('action', @getAttribute('url')).end()
    .find('#action').text(@textContent).end()
    .find("input[name=date]").val($.DateUtil.fmt2(new Date())).end()
    .modal('show');
  )

  $('#adjust_ship_items').on('click', '.btn.adjust', ->
    shipmentId = $("input[name='shipmentId']").val()
    if shipmentId
      $('#adjust_ship_items').submit()
    false
  ).on('click', '.btn.preview', (e) ->
    shipment = $("[name='shipmentId']")
    unless shipment.val()
      EF.colorAnimate(shipment)
    else
      LoadMask.mask()
      $.getScript("/shipment/#{shipment.val()}/preview")
      .done(->
        LoadMask.unmask()
      )
      .fail(->
        LoadMask.unmask()
      )
    false
  ).on('click', '.btn[data-url]:contains(L)', (e) ->
    $i = $(@)
    params =
      url: $i.data('url')
      select_currency: $("[name='fee.currency']")[0].outerHTML
      itm:
        id: $i.parents('tr').attr('id')
    $('#popLogModel').html(_.template($('#form-logfee-model-template').html())(params)).modal('show')
    false
  ).on('dblclick', '[name=adjustQty]', (e) ->
    self = $(@)
    c = self.parents('tr').data('currency')
    type = self.parents('tr').data('compentype')
    params =
      url: self.parents('tr').data('received-url')
      qty: self.text().trim()
      lossqty: self.parents('tr').data('lossqty')
      compenamt: self.parents('tr').data('compenamt')

    $('#popLogModel').html(_.template($('#form-logreceive-qty-model-template').html())(params)).modal('show')
    $('#compentype').find('option').each((index, element)->
      if (element.text.toLowerCase() == type.toLowerCase())
        element.setAttribute('selected', 'selected')
    )

    $('#popLogModel').on('change', '#compentype', (e) ->
      if $('#compentype').val() == 'easyacc'
        if !$("#popLogModel input[name='compenamt']").val()
          $("#popLogModel input[name='compenamt']").val('0')
    )

    $('#currency').find('option').each((index, element)->
      if (element.text.toLowerCase() == c.toLowerCase())
        element.setAttribute('selected', 'selected')
    )
    false
  )

  do ->
    paymentUnitId = window.location.hash[1..-1]
    targetTr = $("##{paymentUnitId}")
    if targetTr.size() > 0
      targetTr.parents('tr').prev().find('td[data-toggle]').click()
      EF.scoll(targetTr)
      EF.colorAnimate(targetTr)

  $("#adjust_ship_items").on("click", "#unitbutton", () ->
    $td = $(@)
    sid = $td.text().trim()
    paintProcureUnitInTimeline('sid', sid)
  )

  paintProcureUnitInTimeline = (type, val)->
    $("#tl").show()
    $("#col-body").show()
    $time_line_home = $("#tl")
    LoadMask.mask($time_line_home)
    $.post('/analyzes/ajaxProcureUnitTimeline', {type: type, val: val},
      (r) ->
        try
          if r.flag is false
            alert(r.message)
          else
            eventSource = $('#tl').data('source')
            eventSource.clear()
            eventSource.loadJSON(r, '/')
        finally
          LoadMask.unmask($time_line_home)
    )

  $('#all_check').click (e) ->
    $("#shipitemTable [type='checkbox']").each(->
      $(@).prop('checked', !$(@).prop('checked'))
    )

  $(':checkbox[class=checkbox_all]').change (e) ->
    $o = $(@)
    $o.parents('form').find(':checkbox[id*=checkbox]').prop("checked", $o.prop('checked'))

  $('#shipmentInfoBtn').click ->
    $("#fileManagerment").fadeOut()
    $("#shipmentInfo").fadeIn()

  $('#fileManagermentBtn').click ->
    $("#fileManagerment").fadeIn()
    $("#shipmentInfo").fadeOut()

  $('input[name="editBoxInfo"]').click ->
    $("#fba_carton_contents_modal").modal('show')
    id = $(this).data("id")
    $("#refresh_div").load("/ProcureUnits/refreshFbaCartonContentsByIds", {ids: id})

  $("#col-body").hide()