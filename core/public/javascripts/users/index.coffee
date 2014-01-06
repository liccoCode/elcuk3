$ ->
  $('.privilege_form').ajaxForm({
  dataType: 'json',
  success: (r) ->
    alert(r.message)
  })

  $(document).on('click', 'a.delete', (r)  ->
      return unless confirm('确认关闭该用户吗?')
      $a = $(@)
      id = $a.data("userid")
      LoadMask.mask()
      $.post("/users/#{id}/closeUser", (r) ->
        try
          if r.flag
            Notify.ok("关闭账户", r.message)
          else
            Notify.alarm("关闭账户失败", r.message)
        finally
          LoadMask.unmask()
      )
  )
