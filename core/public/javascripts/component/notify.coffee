$ ->
  # 为首页 header 栏增加 Notification 开关功能
  if window.webkitNotifications
    $('#notification_btn').click ->
      if webkitNotifications.checkPermission() isnt 0
        window.webkitNotifications.requestPermission()
  else
    console.log('无法使用 Notification 功能')
    $('#notification_btn').click ->
      alert('无法使用桌面 Notification 提醒, 请使用 Chrome 浏览器.')

  setInterval(
    () ->
      if webkitNotifications.checkPermission() is 0
        $.get("/Notifications/nextNotification", {},
          (r) ->
            if r.flag is undefined
              notification = webkitNotifications.createNotification(
                "/img/green.png", "#{r.title} #{r.createAt}", r.content
              ).show()

              # 删除 '亲..'
#              if $('#notifications a:contains(亲, 还没有通知哦)').size() > 0
#                $('##notifications a').remove()

              # 在最前面添加一个

              # 判断数量是否在 20 个, 大于 20 个删除最后第一
        )
    # 10 s
    , 10000
  )