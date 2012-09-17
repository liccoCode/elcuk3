$ ->

  # 为两个 table 的全选 checkbox:label 添加功能
  $('input:checkbox[id*=checkbox_all]').each ->
    $(@).change (e) ->
      o = $(@)
      region = o.attr('id').split('_')[0].trim()
      $("input:checkbox.#{region}").prop("checked", o.prop("checked"))

  $("#plan_form_submit").click -> $("#plan_form").submit()
  $('#delunit_form_submit').click -> $('#delivery_form').attr("action", "/deliveryments/delunits").submit()

  fidCallBack = () ->
    {fid: $('#deliverymentId').text(), p: 'DELIVERYMENT'}
  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)


  $('button[name=unit_isplaced]').click (e) ->
    e.preventDefault()
    o = $(@)
    unitId = o.attr('id')
    mask = $('#container')
    mask.mask('更新中...')
    $.post('/procures/markPlace', id: unitId,
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert('成功')
          o.parents('tr').prev().find('img').attr('src', '/img/green.png')
          o.parent().remove()
        mask.unmask()
    )
