$ ->
  $('a[rel=tooltip]').tooltip({placement: 'right'})
  $('a[rel=popover]').popover({placement: 'left'})

  $('#cci').click ->
    $.post('/application/clearCache', {},
      (r) ->
        if r.flag then alert('清理首页缓存成功') else alert('清理失败.')
    )

  $('#change_passwd_btn').click ->
    $.varClosure.params = {}
    $('#change_passwd :input').map($.varClosure)
    if $.varClosure.params['u.password'] isnt $.varClosure.params['c_password']
      alert('两次密码不一致!')
      return false

    maskDiv = $('#change_passwd')
    maskDiv.mask("更新中...")
    $.post('/users/passwd', $.varClosure.params,
      (data) ->
        if data['username'] is $.varClosure.params['u.username']
          alert('更新成功')
        else
          alert('更新失败;\r\n' + JSON.stringify(data))
        maskDiv.unmask()
    )
    false


