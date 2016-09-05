#
# 这个是用来将 bootstrap 的 popover 根据设置在 HTML 元素上的属性进行设置的增强
# 可以将 placement, trigger, width, data-content(content), title 直接使用元素的形式设置到节点上,
# placement, trigger, width, title  会在初始化的时候进行赋值初始化.
# data-content 是将 popover 的描述信息挂载在 html 元素上, 可修改元素上的 data-content 即可改变值
# content 是将 popover 的描述信息设置到 Popover 对象的 options['content'] 属性中, 修改 data-content 无法修改
#
# 重新初始化页面上所有的 popover 元素
window.$ui =
# 初始化所有 input.type=date
  dateinput: ->
    for input in $('input[type=date]')
      $input = $(input)
      return if $input.attr('native') != undefined

      if $input.attr('format') is undefined
        $input.dateinput(format: 'yyyy-mm-dd')
      else
        $input.dateinput(format: $input.attr('format'))
  datetimeinput: ->
    options = {format: 'yyyy-mm-dd', minView: 'month', autoclose: true}
    for input in $('input[type=datetime]')
      $input = $(input)
      continue if $input.attr('native') != undefined

      for key in  Object.keys(options)
        options[key] = $input.attr(key.toLowerCase()) if $input.attr(key.toLowerCase()) != undefined
      $input.datetimepicker(options)
  selectize: ->
    for select in $('select.selectize')
      $select = $(select)
      options = {plugins: ['remove_button']}
      for key in ['create', 'sortField', 'maxItems']
        options[key] = $select.data(key) if $select.data(key) != undefined
      $select.selectize(options)
  multiselect: ->
    for select in $('select[multiple]')
      $select = $(select)
      options = {buttonWidth: 150, nonSelectedText: '', maxHeight: 200, includeSelectAllOption: true}
      for key in ['buttonWidth', 'nonSelectedText', 'maxHeight', 'includeSelectAllOption']
        options[key] = $select.data(key.toLowerCase()) if $select.data(key.toLowerCase()) != undefined
      $select.multiselect(options)
  clipboard: ->
    clipboard = new Clipboard('.clipboard')
    clipboard.on('success', (e) ->
      tip = $(e.trigger)
      tip.tooltip(
        container: 'body',
        placement: 'bottom',
        title: 'Asin Copied!',
        trigger: 'manual'
      ).tooltip('show')
      setTimeout(->
        tip.tooltip('hide')
      , 300
      )
    )


# 初始化 popover, tooltip, dateinput
  init: ->
    @dateinput()
    @selectize()
    @datetimeinput()
    @multiselect()
    @clipboard()

# popover 与 tooltip 的基础方法
  relBase: (event, func)->
    tip = $(event.target)
    params =
      container: 'body'
      trigger: 'hover'
      placement: 'top'
      html: 'true'
    for key in ['full-width', 'animation', 'html', 'placement', 'selector', 'title', 'content', 'trigger', 'delay'
    , 'container']
      params[key] = tip.attr(key) if tip.attr(key)
    func.call(tip, params)

$(document).on('mouseover', '[rel=tooltip]', (event) ->
  window.$ui.relBase(event, (params) ->
    @tooltip(params).tooltip('show'))
)
$(document).on('mouseover', '[rel=popover]', (event) ->
  window.$ui.relBase(event, (params) ->
    @popover(params).popover('show')
    @data('popover').tip().css('max-width', '900px') if 'full-width' of params
  )
)

$ ->
  window.$ui.init()
