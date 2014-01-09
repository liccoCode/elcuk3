$ ->
  $('.privilege_form').ajaxForm({
  dataType: 'json',
  success: (r) ->
    alert(r.message)
  })

  # 设置关闭和打开用户后页面相关的展示
  setCloseOrOpen = (flag, id, a) ->
    if flag is "open"
      $("##{id}").attr("src", "/img/red.png")
      $(a).attr("data-original-title", "打开此账户")
      $(a).attr("id", "openUser")
      $(a).text("Open")
    else
      $("##{id}").attr("src", "/img/green.png")
      $(a).attr("data-original-title", "关闭此账户")
      $(a).attr("id", "closeUser")
      $(a).text("Close")

  # 关闭用户
  $(document).on('click', '#closeUser', (r)  ->
      return unless confirm('确认关闭该用户吗?')
      $a = $(@)
      id = $a.data("userid")
      LoadMask.mask()
      $.post("/users/#{id}/closeUser", (r) ->
        try
          if r.flag
            noty({text: r.message, type: 'success', timeout: 3000})
            setCloseOrOpen("open", id, $a)
          else
            noty({text: r.message, type: 'error', timeout: 3000})
        finally
          LoadMask.unmask()
      )
  )

  # 打开用户
  $(document).on('click', '#openUser', (r)  ->
        return unless confirm('确认打开该用户吗?')
        $a = $(@)
        id = $a.data("userid")
        LoadMask.mask()
        $.post("/users/#{id}/openUser", (r) ->
          try
            if r.flag
              noty({text: r.message, type: 'success', timeout: 3000})
              setCloseOrOpen("close", id, $a)
            else
              noty({text: r.message, type: 'error', timeout: 3000})
          finally
            LoadMask.unmask()
        )
  )


