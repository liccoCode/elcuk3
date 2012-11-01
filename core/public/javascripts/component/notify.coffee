# ------------------ 检查 Chrome 是否开启了 Notification 功能, 没有则提示打开
$.checkNotify = () ->
  browser = navigator.userAgent.toLowerCase().match(/chrome\/([\d.]+)/)
  if not browser
    alert('当前使用的非 Chrome 浏览器, 推荐切换到 Chrome 22 以上的浏览器版本')
  else
    if Number(browser[1].slice(0, 2)) < 21
      alert("请使用 Chrome 22 版本以上的浏览器, 当前版本为: #{browser[1]}")
    else
      # 为首页 header 栏增加 Notification 开关功能
      if window.webkitNotifications
        if webkitNotifications.checkPermission() isnt 0
          $('#notification_btn').css('background', 'yellow')
          alert("请点击页面上黄色高亮的 Notify 打开 Elcuk2 的 Notification 提醒功能, 以接收系统内的通知消息.")
          $('#notification_btn').click ->
            window.webkitNotifications.requestPermission()
      else
        console.log('无法使用 Notification 功能')
        $('#notification_btn').click ->
          alert('无法使用桌面 Notification 提醒, 请使用 Chrome 浏览器.')


$ ->
  if window.webkitNotifications
    setInterval(
      () ->
        if webkitNotifications.checkPermission() is 0
          $.get("/Notifications/nextNotification", {},
            (r) ->
              if r['title']
                notification = webkitNotifications.createNotification(
                  "/img/green.png", "#{r.title} #{r.createAt}", r.content
                ).show()
          )
      # 15 s
      , 15000
    )