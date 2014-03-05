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
    htmlobj = $.ajax({url: "/Notifications/amount", async: false},{type: 'POST', dataType: 'json'})
          .done((r) ->
            if r.count>0
              $("#notifyimg").append("<img src='/public/images/notify.gif' width='30' height='20' />")
            $("#notifyNumber").html(r.count)
          )
  newsCount()


  #将选中的通知状态更改成已读
  $("#updateState").on("click", (e) ->
    e.preventDefault()
    $checkbox = $('input:checkbox:checked[name="noteID"]')
    if $checkbox.serialize().length == 0
      noty({text: '未选中通知', type: 'error', timeout: 3000})
    else
      updateState($checkbox.serialize(), ->
         newsCount()
         $('input:checkbox:checked[name="noteID"]').remove()
      )
  )

  #右上角提示框，已阅按钮事件
  $(document).on("click", ".dropdown-menu a[data-id]", (e) ->
      $span = $(@)
      updateState({noteID:$span.attr("data-id")}, ->
         $("#notificationBtn").click()
         newsCount()
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