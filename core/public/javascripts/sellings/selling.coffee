$ ->
  ALERT_TEMPLATE = "<div class='alert alert-success fade in' style='text-align:center;'><button class='close' data-dismiss='alert'>×</button><div id='replace_it'></div></div>"
  IMGLI_TEMPLATE = "<li class='span2'><a class='thumbnail' target='_blank'><img></a><input style='width:95%;height:12px;text-align:center;'></li>"

  # 图片初始化方法
  imageInit = ->
    imagesUL = $('#images')
    imageNameObj = {}
    for imageName, i in $('input[name=s\\.aps\\.imageName]').val().split('|-|')
      imageNameObj[imageName] = i
    $.getJSON('/attachs/images', fid: imagesUL.attr('sku'),
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
    $.params = remote: remote
    $('#container :input').map($.varClosure)
    btnGroup.mask('更新中...')
    $.post('/sellings/update', $.params,
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
          alertDiv.find('#replace_it').replaceWith("<p>更新成功!</p>")
          alertDiv.prependTo('#container')
        else
          alert(r.message)
        imgDiv.unmask()
    )

