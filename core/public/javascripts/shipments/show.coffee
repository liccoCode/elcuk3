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
        jobNumber: $("#jobNumber").val()
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

  $('#adjust_shipitems').on('click', '.btn.adjust',->
    shipmentId = $("input[name='shipmentId']").val()
    if shipmentId
      $('#adjust_shipitems').attr('action',(i, v) ->
        "#{v[0...v.lastIndexOf('/')]}/#{shipmentId}"
      ).submit()
    false
  ).on('click', '.btn.preview',(e) ->
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
  ).on('click', '.btn[data-url]:contains(L)',(e) ->
    $i = $(@)
    params =
      url: $i.data('url')
      select_currency: $("[name='fee.currency']")[0].outerHTML
      itm:
        id: $i.parents('tr').attr('id')
    $('#popLogModel').html(_.template($('#form-logfee-model-template').html(), params)).modal('show')
    false
  ).on('dblclick', '[name=recivedQty]', (e) ->
    self = $(@)

    c = self.parents('tr').data('currency')
    type = self.parents('tr').data('compentype')
    params =
      url: self.parents('tr').data('received-url')
      qty: self.text()
      lossqty: self.parents('tr').data('lossqty')
      compenamt: self.parents('tr').data('compenamt')
    $('#popLogModel').html(_.template($('#form-logreceive-qty-model-template').html(), params)).modal('show')

    $('#compentype').find('option').each((index, element)->
      if (element.text.toLowerCase() == type.toLowerCase())
        element.setAttribute('selected', 'selected')
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


  # trace_no新增一行
  $("#shipment_form").on("click", "#more_trackno_btn",() ->
    $btn = $(@)
    $table = $("##{$btn.data("table")}")[0]
    # 获取表格的行数
    rowsCount = $table.rows.length
    # 通过 js 克隆出一个新的行 由于表格的第一行是标题行，所以使用 表格的行数减去1得到最后一行
    $tr = $("##{$btn.data("table")} tr:eq(#{rowsCount - 1})")[0]
    $newRow = $tr.cloneNode(true)
    # 修改 tr 元素内 textarea 的 name 属性
    inputs = $newRow.getElementsByTagName("input")

    setInputName($btn.attr("id"), rowsCount, inputs)
    $table.appendChild($newRow)
  ).on("click", "[name^='delete_trackno_row']", () ->
    $btn = $(@)
    $btn.parent("td").parent().remove()
  )


  # trace_no新增一行
  $("#adjust_shipitems").on("click", "#unitbutton", () ->
    $td =$(@)
    sid = $td.text().trim()
    paintProcureUnitInTimeline('sid', sid)
  )

  # 根据点击按钮的不同判断text的名称
  setInputName = (flag, rowsCount, inputs) ->
    inputs[0].name = "ship.tracknolist[#{rowsCount - 1}]"
    inputs[0].value = ""


  paintProcureUnitInTimeline = (type, val)->
    LoadMask.mask()
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
        LoadMask.unmask()
    )

  $('#all_check').click (e) ->

    $("#shipitemTable [type='checkbox']").each(->
      $(@).attr('checked', !$(@).attr('checked'))
    )


  $(':checkbox[class=checkbox_all]').change (e) ->
    o = $(@)
    o.parents('form').find(':checkbox[id*=checkbox]').prop("checked", o.prop('checked'))