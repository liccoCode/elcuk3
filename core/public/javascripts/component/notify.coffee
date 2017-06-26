$ ->
  window.Notify =
    # 检查浏览器是否开启了 Notification 功能, 没有则提示打开
    checkNotify: () ->
      browser = navigator.userAgent.toLowerCase().match(/chrome\/([\d.]+)/)
      if not browser
        alert('当前使用的非 Chrome 浏览器, 推荐切换到 Chrome 22 以上的浏览器版本')
      else
        if Number(browser[1].slice(0, 2)) < 21
          alert("请使用 Chrome 22 版本以上的浏览器, 当前版本为: #{browser[1]}")
        else
          # 为首页 header 栏增加 Notification 开关功能
          if window.Notification
            if Notification.permission != "granted"
              $("#notification_btn").css("background", "yellow")
              alert("请点击页面上黄色高亮的 Notify 打开 Elcuk2 的 Notification 提醒功能, 以接收系统内的通知消息.")
              $('#notification_btn').click ->
                Notification.requestPermission()
          else
            console.log('无法使用 Notification 功能')
            $('#notification_btn').click ->
              alert('无法使用桌面 Notification 提醒, 请使用 Chrome 浏览器.')

    notify: (title, text, pic = '/img/green.png') ->
      new Notification(title, { icon: pic, body: text })

    alarm: (title, text) ->
      @notify(title, text, '/img/alarm.png')

    ok: (title, text) ->
      @notify(title, text, '/img/ok.png')

    # 是否可以发提示消息?
    isOn: () ->
      window.Notification and Notification.permission is "granted"

    # 轮询检查系统是否有消息发出
    loopCheck: (interval = 180) ->
      self = @
      if self.isOn()
        setInterval(->
          $.get("/Notifications/nextNotification", {}, (r) ->
            if r['title']
              if r['title'].indexOf("成功") > 0
                self.ok(r.title, r['content'])
              else if r['title'].indexOf("错误") > 0 || r['title'].indexOf("失败") > 0 || r['title'].indexOf("非法")
                self.alarm(r.title, r['content'])
              else
                self.notify(r.title, r['content'])
          )
          # 15 s
        , interval * 1000)

  # 检测 Notification 功能
  # Notify.checkNotify()
  # 循环 load 需要进行的通知内容
  # Notify.loopCheck()

  #统计当前用户的 新通知记录的条数
  newsCount = ->
    htmlobj = $.ajax({url: "/Notifications/amount"},{type: 'POST', dataType: 'json'}).done((r) ->
      if r.count>0
        $("#notifyimg").append("<img src='/public/images/notify.gif' width='30' height='20' />")
      $("#notifyNumber").html(r.count)
    )
  # newsCount()

  #将选中的通知状态更改成已读
  $("#updateState").on("click", (e) ->
    e.preventDefault()
    $checkbox = $('input:checkbox:checked[name="noteID"]')
    if $checkbox.serialize().length == 0
      noty({text: '未选中通知', type: 'error', timeout: 3000})
    else
      updateState($checkbox.serialize(), ->
         # newsCount()
         $('input:checkbox:checked[name="noteID"]').remove()
      )
  )

  #右上角提示框，已阅按钮事件
  $(document).on("click", ".dropdown-menu a[data-id]", (e) ->
      $span = $(@)
      updateState({noteID:$span.attr("data-id")}, ->
         $("#notificationBtn").click()
         # newsCount()
      )
    )

  #更改 通知信息的状态为 已读
  updateState = (datas, func)->
    LoadMask.mask()
    $.ajax("/Notifications/updateState", {type: 'POST', dataType: 'json', data:datas})
       .done((r)->
          type = if r.flag
                     'success'
                 else
                     'error'
          noty({text: r.message, type:type , timeout: 3000})
          func()
          LoadMask.unmask()
       )
       .fail((r)->
          noty({text: '服务器发生错误!', type: 'error', timeout: 5000})
          LoadMask.unmask()
       )