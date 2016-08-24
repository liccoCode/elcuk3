$ ->
  $('#cci').click ->
    $.post('/application/clearCache', {},
    (r) ->
      if r.flag then alert('清理首页缓存成功') else alert('清理失败.')
    )

  $('#change_passwd_btn').click (e) ->
    params = $.formArrayToObj($('#change_passwd form').formToArray())
    if params['u.password'] isnt params['u.confirm']
      alert('两次密码不一致!')
      e.preventDefault()
      return false

    maskDiv = $('#change_passwd')
    maskDiv.mask("更新中...")
    $.post('/users/passwd', params,
    (data) ->
      if data['username'] is params['u.username']
        alert('更新成功')
      else
        alert('更新失败;\r\n' + JSON.stringify(data))
      maskDiv.unmask()
    )
    e.preventDefault()