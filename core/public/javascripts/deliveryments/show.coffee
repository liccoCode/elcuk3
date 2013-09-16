$ ->
  $('#bulkpost').on('click', "#delunit_form_submit, #deployFBAs", (e) ->
    $btn = $(@)
    return false unless confirm("确认 #{$btn.text().trim()} ?")
    $('#form_method').val($btn.data('method'))
    $form = $btn.parents('form')
    $form.attr('action', $btn.data('url')).submit()
    false
  )

  # 为两个 table 的全选 checkbox:label 添加功能
  $('input:checkbox[id*=checkbox_all]').each ->
    $(@).change (e) ->
      o = $(@)
      region = o.attr('id').split('_')[0].trim()
      $("input:checkbox.#{region}").prop("checked", o.prop("checked"))

  fidCallBack = () ->
    {fid: $('#deliverymentId').text(), p: 'DELIVERYMENT'}
  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)

  # 选择采购单的收货人
  $('#chosereceiver').change (e) ->
    return unless $(@).val()
    mask = $('#generate_excel')
    mask.mask('加载中...')
    receiver = $.getJSON('/cooperators/showJson', {id: $(@).val()},
    (r) ->
      unless r.flag
        $('#excel_receiver').val(r['contacter'])
        $('#excel_receiverPhone').val(r['phone'])
        $('#excel_receiverTel').val(r['tel'])
        $('#excel_deliveryAddress').val(r['address'])
      mask.unmask()
    )

  $("#chosebuyer").change (e) ->
    return unless $(@).val()
    mask = $('#generate_excel')
    mask.mask('加载中...')
    buyer = $.getJSON('/users/showJson', {id: $(@).val()},
    (r) ->
      unless r.flag
        $('#excel_buyer').val(r['username'])
        $("#excel_buyerPhone").val(r['phone'])
      mask.unmask()
    )

  do ->
    procureUntiId = window.location.hash[1..-1]
    targetTr = $("#procureUnit_#{procureUntiId}")
    EF.scoll(targetTr)
    EF.colorAnimate(targetTr)
