$ ->
  ALERT_TEMPLATE = "<div class='alert alert-success fade in' style='text-align:center;'><button class='close' data-dismiss='alert'>×</button><div id='replace_it'></div></div>"
  IMGLI_TEMPLATE = "<li class='span2'><a class='thumbnail' target='_blank'><img></a><input style='width:55%;height:8px;padding-left:40%'></li>"

  # 图片初始化方法
  imageInit = ->
    imagesUL = $('#images')
    imageNameObj = {}
    for imageName, i in $('input[name=s\\.aps\\.imageName]').val().split('|-|')
      imageNameObj[imageName] = i
    $.getJSON('/products/images', sku: imagesUL.attr('sku'),
      (imgs) ->
        for img in imgs
          fName = img['fileName']
          imgLI = $(IMGLI_TEMPLATE).attr('filename', fName)
          imgLI.find('a').attr('href', '/attachs/image?a.fileName=' + fName)
          imgLI.find('img').attr('src', '/attachs/image?w=140&h=100&a.fileName=' + fName)
          imgLI.find('input').val(imageNameObj[fName]) if fName of imageNameObj
          imgLI.appendTo(imagesUL)
    )

  # 初始化图片
  imageInit()

  # update/deploy 按钮的基础方法
  updateAndDeployBaseBtn = (btn, remote) ->
    btnGroup = $(btn).parent()
    $.varClosure.params = remote: remote
    $('#container :input').map($.varClosure)
    btnGroup.mask('更新中...')
    $.post('/sellings/update', $.varClosure.params,
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert("Selling: " + r['sellingId'] + " 更新成功!")
        btnGroup.unmask()
    )

  # Update 按钮
  $('button:contains("Update")').click ->
    updateAndDeployBaseBtn(@, no)

  # Deploy 按钮
  $('button:contains("Deploy")').click ->
    updateAndDeployBaseBtn(@, yes)

  # Sync 按钮
  $('button:contains("Sync")').click ->
    return false if !confirm("确认要从 Amazon 同步吗? 同步后系统内的数据将被 Amazon 上的数据覆盖.")
    btnGroup = $(@).parent()
    btnGroup.mask('同步中...')
    $.post('/sellings/syncAmazon', sid: $('#s_sellingId').val(),
      (r) ->
        if r.flag is true
          alert('同步成功, 请刷新页面查看最新数据')
        else
          alert(r.message)
        btnGroup.unmask()
    )

  # Image 值计算的功能
  # 按照图片下方 input 中的索引进行图片的顺序排列, 如果索引不连续, 需要报告异常并停止取值,
  # 最后检查成功了返回 true, 否则返回 false
  imageIndexCal = ->
    goon = yes
    fNames = size: 0
    for imgLi in $('#images li[filename]')
      return false if goon is no
      index = $(imgLi).find('input').val().trim()
      continue if !index or index is ''
      if !$.isNumeric(index)
        alert('只能输入数字编号, 代表图片的位置')
        goon = no
      else
        fNames[index] = $(imgLi).attr('filename')
        fNames.size += 1

    names = []
    for i in [0...9]
      if !(i of fNames) and i < fNames.size
        alert("期待的索引应该是 " + i)
        return false
      names.push(fNames[i]) if fNames[i]

    if names.length <= 0
      alert("请填写索引!")
      return false
    else
      $('input[name=s\\.aps\\.imageName]').val(names.join('|-|'))
      return true


  # 图片上传的按钮
  $('#img_cal').click(imageIndexCal).find('~ button').click ->
    return false if !imageIndexCal()
    return false if !confirm("确定要更新到 " + $("select[name=s\\.market]").val() + " ?")
    imgDiv = $(@).parent()
    imgDiv.mask("上传图片中...")
    params =
      's.sellingId': $('#s_sellingId').val()
      imgs: $('#image_tr + tr input').val()
    $.post('/sellings/imageUpload', params,
      (r) ->
        if r.flag is true
          alertDiv = $(ALERT_TEMPLATE)
          alertDiv.find('#replace_it').replaceWith("<p>更新成功! <a target='_blank' href='" + r.message + "'>访问 Listing</a></p>")
          alertDiv.prependTo('#container')
        else
          alert(r.message)
        imgDiv.unmask()
    )

  #  自动补全的 sid 的功能条
  SID_PREVIEW_TEMPLATE = "<div><h3>Technical</h3><p id='t'></p><hr><h3>SearchTerms</h3><p id='s'></p><hr><h3>ProductDesc</h3><p id='p'></p></div>"
  $('#sid_helper').change ->
    toolBar = $(@).parent()
    toolBar.mask('加载数据中...')
    $.getJSON('/sellings/tsp', sid: @value,
      (json) ->
        html = $(SID_PREVIEW_TEMPLATE)
        html.find('#t').html(json['t'].join('<br/><br/>'))
        html.find('#s').html(json['s'].join('<br/><br/>'))
        html.find('#p').html(json['p'][0])
        $('#sid_preview_popover').attr('data-content', html.html()).data('tsp', json).click()
        toolBar.unmask()
    )

  # 加载 tsp 数据的按钮
  $('#sid_helper + button').click ->
    json = $('#sid_preview_popover').data('tsp')
    if json is undefined
      alert('还没有数据, 请先预览!')
      return false
    # product Desc
    $('[name=s\\.aps\\.productDesc]').val(json['p'][0]).blur()
    # technical
    for t, i in json['t']
      $('[name=s\\.aps\\.keyFeturess\\[' + i + '\\]]').val(t).blur()
    # searchTerms
    for s, i in json['s']
      $('[name=s\\.aps\\.searchTermss\\[' + i + '\\]]').val(s).blur()

