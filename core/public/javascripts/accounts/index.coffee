$ ->

# 添加 table 下拉列表
  $(".account_list tr[aid]").css('cursor', 'pointer').click (e) ->
    $("#acc_#{@getAttribute('aid')}").toggle('fast')
    e.preventDefault()

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


  # 高亮新添加的 Account
  activeAccount = ->
    args = location.hash.substr(1).split('/')
    $("tr[aid=#{args[0]}]").css('background', '#FF6A78')

  activeAccount()

  window.$ui.popover()
