$ ->
# 这个是放在面板上的事件源
  eventSource = new Timeline.DefaultEventSource()


  # 这个是放在 Timeline 上的面板
  bandInfos = [
    Timeline.createBandInfo(
      eventSource: eventSource
      width: "85%"
      #      trackHeight: 0.5,
      intervalUnit: Timeline.DateTime.DAY
      intervalPixels: 38
    ),
    Timeline.createBandInfo(
      #      showEventText: false,
      trackHeight: 0.5
      trackGap: 0.2
      eventSource: eventSource
      width: "15%"
      intervalUnit: Timeline.DateTime.MONTH
      intervalPixels: 200
      overview: true
    )
  ]
  # 添加同步
  bandInfos[1].syncWith = 0
  bandInfos[1].highlight = true

  # 添加装饰器
  for bandInfo in bandInfos
    bandInfo.decorators = [
      new Timeline.SpanHighlightDecorator(
        startDate: new Date()
        endDate: $.DateUtil.addDay(30, new Date())
        color: '#ffc080'
        opacity: 30
        startLabel: 'Procure'
      )
    ]

  # 构造 Timeline
  tl = Timeline.create($('#tl')[0], bandInfos)
  $('#tl')
  # 把 timeline 的对象缓存到 #tl 身上, 让其他 js 元素可以获取到
  .data('timeline', tl)
  # 把 eventSource 对象缓存到 #tl 身上, 其他 js 元素可以获取到
  .data('source', eventSource)

  # 这里是当界窗口进行调整的时候, 整个布局也重新调整
  resizeTimerId = null
  $(window).resize(->
      if resizeTimerId is null
        resizeTimerId = window.setTimeout(
          ->
            resizeTimerId = null
            tl.layout()
          , 500
        )
  )
