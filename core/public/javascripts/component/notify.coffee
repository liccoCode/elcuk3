window.Notify =
# 检查浏览器是否开启了 Notification 功能, 没有则提示打开
  checkNotify: () ->
    browser = navigator.userAgent.toLowerCase().match(/chrome\/([\d.]+)/)
    if not browser
      alert('当前使用的非 Chrome 浏览器, 推荐切换到 Chrome 22 以上的浏览器版本')
    else
      if Number(browser[1].slice(0, 2)) < 21
        alert("请使用 Chrome 22 版本以上的浏览器, 当前版本为: #{browser[1]}")

$ ->
   #统计当前用户的 新通知记录的条数
   newsCount = ->
    htmlobj = $.ajax({url: "/Notifications/amount" , async: false})
    $("#Notify_number").html(htmlobj.responseText);

   newsCount()

   #加载当前用户最新的八条信息
   $("#notification_btn").on("click",(e) ->
       e.preventDefault()
       $.ajax("/Notifications/latest", {type: 'POST', dataType: 'json'}).done((r) ->
              param = if r
                          r
                      else
                          [{title: 'See Notifications'}]
              #清空数据
              $("#notifications").find("li").remove()
              $.each(param,(index,element)->
                  $("#notifications").append(_.template($('#news-Notifications-model-template').html(), {noti:element}))
              )
       )

   )

   #将选中的通知状态更改成已读
   $("#update_state").on("click",(e) ->
         e.preventDefault()
         $checkbox = $('input:checkbox:checked[name="noteID"]')
         if $checkbox.serialize().length == 0
           noty({text: '未选中通知', type: 'error', timeout: 3000})
         else
           LoadMask.mask()
           $.ajax("/Notifications/updateState",{type:'POST',dataType:'json',data:$checkbox.serialize()}).done((r)->
             noty({text: r.message, type: 'success', timeout: 3000})
             $checkbox.remove()
             newsCount()
           ).fail((r)->
             noty({text: '服务器发生错误!', type: 'error', timeout: 5000})
           )
           LoadMask.unmask()
     )
