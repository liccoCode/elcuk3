$ ->
  $(document).on("showOrHide", "select[name='wh.type']", (r) ->
    $self = $(@)
    $tr = $("#forward_shipment_tr")
    if $self.val() != "FBA"
      $tr.show("slow")
    else
      $tr.hide('slow')
  )
  $(document).on("change", "select[name='wh.type']", (r) ->
    $(@).trigger('showOrHide')
  )
  # 页面加载时初始化触发一次方法
  $("select[name='wh.type']").trigger('showOrHide')
  # 对 checkbox 进行 check 与 uncheck 事件添加
  $(':checkbox').change (e) ->
    o = $(@)
    o.val(o.is(':checked'))
    $("##{o.data('id')}").val(o.is(':checked'))
  $("#submit_update_btn").click (e) ->
    form = $("#update_form")
    form.mask('更新中...')
    $.post('/whouses/update', form.serialize(), (r) ->
      if r.flag is false
        alert(r.message)
      else
        alert('更新成功')
      form.unmask()
    )
    e.preventDefault()