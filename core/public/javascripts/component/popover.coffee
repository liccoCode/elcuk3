#
# 这个是用来将 bootstrap 的 popover 根据设置在 HTML 元素上的属性进行设置的增强
# 可以将 placement, trigger, width, data-content(content), title 直接使用元素的形式设置到节点上,
# placement, trigger, width, title  会在初始化的时候进行赋值初始化.
# data-content 是将 popover 的描述信息挂载在 html 元素上, 可修改元素上的 data-content 即可改变值
# content 是将 popover 的描述信息设置到 Popover 对象的 options['content'] 属性中, 修改 data-content 无法修改
#
# 重新初始化页面上所有的 popover 元素
window.$ui =
# 初始化所有 rel=popover
  popover: ->
    for aPopover in $('*[rel=popover]')
      $pop = $(aPopover)
      popParam =
        placement: 'top'
        trigger: 'hover'
      for k in ['placement', 'trigger', 'width', 'data-content', 'content', 'title']
        value = $pop.attr(k)
        continue if value is undefined or value is ''
        # 如果 trigger 为 toggle 自行转换
        if k is 'trigger' and value is 'toggle'
          popParam['trigger'] = 'manual'
          # 添加 click 时间进行 toggle 开关
          $pop.click ->
            $(@).popover('toggle')
            false
        else if k is 'data-content'
          $pop.attr(k, value) #如果是 data-content 直接设置到元素上, 不影响原本的 content 属性
        else
          popParam[k] = value
      $pop.popover(popParam)

  # 初始化所有 rel=tooltip
  tooltip: ->
    for aTooltip in $('[rel=tooltip]')
      $tip = $(aTooltip)
      tipParam =
        placement: 'top'
        trigger: 'hover'
      for k in ['placement', 'trigger', 'data-original-title', 'title']
        value = $tip.attr(k)
        continue if value is undefined or value is ''
        # 自行添加了 toogle 值的处理
        if k is 'trigger' and value is 'toggle'
          tipParam['trigger'] = 'manual'
          $tip.click ->
            $(@).tooltip('toggle')
            false
        else if k is 'data-original-title'
          $tip.attr(k, value)
        else
          tipParam[k] = value
      $tip.tooltip(tipParam)

  # 初始化所有 input.type=date
  dateinput: ->
    for input in $('input[type=date]')
      $input = $(input)
      return if $input.attr('native') != undefined
      if $input.attr('format') is undefined
        $input.dateinput(format: 'yyyy-mm-dd')
      else
        $input.dateinput(format: $input.attr('format'))

  # 初始化 popover, tooltip, dateinput
  init: ->
    @popover()
    @tooltip()
    @dateinput()


$ ->
  window.$ui.init()

  # 为首页 header 栏增加 Notification 开关功能
  if window.webkitNotifications
    $('#notification_btn').click ->
      window.webkitNotifications.requestPermission()
  else
    console.log('无法使用 Notification 功能')
    $('#notification_btn').click ->
      alert('无法使用桌面 Notification 提醒')
