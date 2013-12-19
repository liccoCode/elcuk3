$ ->
  $('#check_apply').click(->
    $('#msku').val(->
      $('#check_modal').modal('hide')
      "#{@value.split(",")[0]},#{$('#upc').val()}"
    )
    false
  )
  $('#check_cancel').click(->
    $('#msku').val(->
      $('#check_modal').modal('hide')
      "#{@value.split(",")[0]}"
    )
    false
  )

  # 显示 Selling 上架信息的 Modal 窗口
  showSellingModal = (title, sellings) ->
    modal = $('#check_modal').find('#upc_num').html(title).end()
    if sellings.length == 0
      modal.find('.innder-modal').html('<p>暂时没有上架 Selling</p>')
    else
      template = modal.find('.innder-modal').html('').end().find('.template')
      sellings.forEach (obj, index, arr) ->
        modal.find('.innder-modal').append(
          template.clone().removeClass('template').find('.check_id').html('SellingId: ' + obj.sellingId).end()
            .find('.check_title').html(obj.aps.title).end()
        )
    modal.modal('show')

  $('#market').change ->
    market = $(@).val()
    $.getJSON('/products/skuMarketCheck', {sku: $('#msku').val(), market: market})
      .done((r) ->
        if r.flag is false
          noty({text: r.message, type: 'warning'})
        else
          showSellingModal("#{$('#msku').val()} (#{r.length})", r)
      )
    false

  # 账号对应的市场切换
  $('#account').change ->
    $("#market option:contains(#{$(@).find("option:selected").text().split('_')[0]})").prop('selected', true).change()

  # UPC 检查
  $('#upc').blur (e) ->
    $('#msku').val(-> @value.split(',')[0])
    $self = $(@)
    upc = $(@).removeClass('btn-warning btn-success').val()
    if !$.isNumeric(upc)
      $self.addClass('btn-warning')
      noty({text: 'UPC 必须是数字', type: 'warning'})
      return false

    $.ajax('/products/upcCheck', {type: 'GET', data: {upc: upc}, dataType: 'json'})
      .done((r) ->
        if r.flag is false
          noty({text: r.message, type: 'error', layout: 'topCenter'})
        else
          showSellingModal("#{$('#msku').val()} (#{r.length})", r)
      )
    false

  $('#submitSale').click ->
    $form = $('#saleAmazonForm')
    LoadMask.mask()
    $.ajax($form.attr('action'), {data: $form.serialize(), method: 'POST'})
      .done((r) ->
        if r.flag
          noty({text: "成功创建 Selling #{r.message}"})
        else
          noty({text: r.message, type: 'error'})
        LoadMask.unmask()
      )
      .fail((r) ->
        noty({text: r.responseText, type: 'error'})
        LoadMask.unmask()
      )
