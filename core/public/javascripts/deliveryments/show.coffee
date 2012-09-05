$ ->

  # 为两个 table 的全选 checkbox:label 添加功能
  $('input:checkbox[id*=checkbox_all]').each ->
    $(@).change (e) ->
      o = $(@)
      region = o.attr('id').split('_')[0].trim()
      $("input:checkbox.#{region}").prop("checked", o.prop("checked"))

  $("#plan_form_submit").click -> $("#plan_form").submit()
  $('#delivery_form_submit').click -> $('#delivery_form').submit()

  fidCallBack = () ->
    {fid: $('#deliverymentId').text(), p: 'DELIVERYMENT'}
  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)
