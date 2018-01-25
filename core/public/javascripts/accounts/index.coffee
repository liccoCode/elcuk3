$ ->

  # 为 account table 添加更新事件
  $(".account_list button[aid]").click (e) ->
    form = $(@).parents('form')
    form.mask('更新中...')
    $.post('/accounts/update', form.find(":input").fieldSerialize(false),
    (r) ->
      if r.flag is false
        alert(r.message)
      else
        alert('更新成功')
      form.unmask()
    )
    e.preventDefault()

  # 对 checkbox 进行 check 与 uncheck 事件添加
  $(':checkbox').change (e) ->
    o = $(@)
    o.val(o.is(':checked'))


  $('#newUser_form button[nid]').click (e) ->
    form = $('#newUser_form')
    form.mask('添加中...')
    $.post('/accounts/create', form.find(':input').fieldSerialize(false),
    (r) ->
      if r.flag is false
        alert(r.message)
      else
        location.reload()
      form.unmask()
    )
    e.preventDefault()
