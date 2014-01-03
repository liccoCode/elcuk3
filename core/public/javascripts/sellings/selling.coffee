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

  # Update 按钮
  $('#amz-update').click ->
    return false unless imageIndexCal()
    LoadMask.mask()
    $.ajax($(@).data('url'), {type: 'POST', data: $('#saleAmazonForm').serialize()})
      .done((r) ->
        msg = if r.flag is true
            {text: "#{r.message} Selling 更新成功", type: 'success'}
          else
            {text: r.message, type: 'error'}
        noty(msg)
        LoadMask.unmask()
      )
      .fail((r) ->
        noty({text: r.responseText, type: 'error'})
        LoadMask.unmask()
      )
    false

  # Deploy 按钮
  $('#amz-deploy').click ->
    # check account 与 market 不一样, 要提醒
    switch $('[name=s\\.account\\.id]').val()
      when 1
        if $('[name=s\\.market]').val() != 'AMAZON_UK'
          return unless confirm("注意! Account 是 UK 与 Selling 所在市场不一样, 已经取消这样销售, 确认要提交?")
      when 2
        if $('[name=s\\.market]').val() != 'AMAZON_DE'
          return unless confirm("注意! Account 是 DE 与 Selling 所在市场不一样, 已经取消这样销售, 确认要提交?")
      when 131
        if $('[name=s\\.market]').val() != 'AMAZON_US'
          return unless confirm("注意! Account 是 US 与 Selling 所在市场不一样, 已经取消这样销售, 确认要提交?")
      else
    LoadMask.mask()
    $.ajax($(@).data('url'), {type: 'POST', data: $('#saleAmazonForm').serialize()})
      .done((feed) ->
        if feed.flag is false
          noty({text: feed.message, type: 'error'})
        else
          noty({text: "成功创建 Feed(#{feed.id})"}, type: 'success')
        LoadMask.unmask()
      )
      .fail((r) ->
        noty({text: r.responseText, type: 'error'})
        LoadMask.unmask()
      )
    false

  # Sync 按钮
  $('#amz-sync').click ->
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
        noty({text: "只能输入数字编号, 代表图片的位置", type: 'warning'})
        goon = no
      else
        fNames[index] = $(imgLi).attr('filename')
        fNames.size += 1

    names = []
    for i in [0...9]
      if !(i of fNames) and i < fNames.size
        noty({text: "期待的图片索引应该是 #{i}", type: 'warning'})
        return false
      names.push(fNames[i]) if fNames[i]

    if names.length > 0
      $('input[name=s\\.aps\\.imageName]').val(names.join('|-|'))
      true

  $('#showFeedsButton').on('shown',(e) ->
    sellingId=$('input[name="s.sellingId"]').val()
    LoadMask.mask()
    $("#feedsHome").load("/Sellings/feeds?sellingId="+sellingId+"")
    LoadMask.unmask()
  )

  # 图片上传的按钮
  $('#img_cal').click(imageIndexCal)
  $("#feedProductType").trigger('adjust')

