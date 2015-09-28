$ ->
  ALERT_TEMPLATE = "<div class='alert alert-success fade in' style='text-align:center;'><button class='close' data-dismiss='alert'>×</button><div id='replace_it'></div></div>"
  IMGLI_TEMPLATE = "<li class='span2'><a class='thumbnail' target='_blank'><img width='180px' height='30px'></a><label></label>
<input style='width:80%;height:17px;text-align:center;'>&nbsp;&nbsp;<button class='btn btn-mini btn-danger' name='delImage'><i class='icon-trash' style='width:22%;'></i></button></li>"

  # 图片初始化方法
  imageInit = ->
    imagesUL = $('#images')
    imageNameObj = {}
    for imageName, i in $('input[name=s\\.aps\\.imageName]').val().split('|-|')
      imageNameObj[imageName] = i
    $.getJSON('/attachs/explorerImages', fid: imagesUL.attr('sku'),
      (imgs) ->
        for img in imgs
          fName = img['name']
          href = img['href']
          imgLI = $(IMGLI_TEMPLATE).attr('filename', fName)
          imgLI.find('label').text(fName)
          imgLI.find('a').attr('href', href)
          imgLI.find('img').attr('src', href)
          imgLI.find('input').val(imageNameObj[fName]) if fName of imageNameObj
          imgLI.appendTo(imagesUL)

        $("button[name='delImage']").click ((e)->
          btn = $(@)
          e.preventDefault()
          fileName = btn.parent("li").attr("filename")
          return false if !confirm("确认删除图片" + fileName + "吗?")
          sku = $("#images").attr("sku")
          params =
            'sku': sku
            'fileName': fileName
          $.post('/sellings/deleteImage', params, (r) ->
            if r.flag is true
              btn.parent("li").remove()
              noty({text: "删除成功", type: 'success'})
            else
              noty({text: "删除失败，原因(#{r.message})", type: 'error'})
          )
        )
    )

  # 初始化图片
  imageInit()



  # Update 按钮
  $('#amz-update').click ->
    if !previewBtn.call($("#productDesc"))
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


  # AMA局部更新 按钮
  $('#amz-part-update').click ->
    if !previewBtn.call($("#productDesc"))
      LoadMask.mask('#btns')
      $.ajax($(@).data('url'), {type: 'POST', data: $('#saleAmazonForm').serialize() })
      .done((r) ->
          msg = if r.flag is true
            "#{r.message} 已经成功向AMAZON提交feed，请稍后查看feed状态。"
          else
            r.message
          alert msg
          LoadMask.unmask('#btns')
        )
      .fail((r) ->
          alert r.responseText
          LoadMask.unmask('#btns')
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
          noty({text: "成功创建 Feed(#{feed.id})", type: 'success'})
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
    $.post('/sellings/syncAmazon', sid: $('input[name="s.sellingId"]').val(),
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
        alert "图片索引#{i}未找到,请填写图片索引信息!"
        return false
      names.push(fNames[i]) if fNames[i]

    if names.length <= 0
      alert '图片索引为空'
      true
    else
      $('input[name=s\\.aps\\.imageName]').val(names.join('|-|'))
      true

  $('#showFeedsButton').on('shown', (e) ->
    sellingId = $('input[name="s.sellingId"]').val()
    LoadMask.mask()
    $("#feedsHome").load("/Sellings/feeds?sellingId=#{sellingId}")
    LoadMask.unmask()
  )

  $("#feedProductType").trigger('adjust')

  $("#upAndDownForm").on("click", "#sellingUp, #sellingDown", (r) ->
    LoadMask.mask()
    $btn = $(@)
    flag = if $btn.attr("id") == "sellingUp"
      true
    else
      false
    $.ajax("/sellings/changeSellingType",
    {type: 'POST', data: {sellingId: $("#sellingId").val(), flag: flag}, dataType: 'json'})
    .done((r) ->
        msg = if r.flag is true and flag is true
          $("#sellingState").val("SELLING")
          {text: "#{r.message} 系统上架成功", type: 'success'}
        else if r.flag is true and flag is false
          $("#sellingState").val("DOWN")
          {text: "#{r.message} 系统下架成功", type: 'warning'}
        else
          {text: r.message, type: 'error'}
        noty(msg)
        LoadMask.unmask()
      )
  )
  # 刪除 Selling
  $('#btns').on('click', 'a[action=remove]', (li) ->
    return unless confirm('确认删除?')
    LoadMask.mask()
    $btn = $(@)
    $.ajax($btn.data('url'))
      .done((r) ->
        type = if r.flag
          alert(r.message)
          window.close()
        else
          noty({text: r.message, type: 'error'})
        LoadMask.unmask()
      )
    )

  # 图片上传的按钮
  $('#img_cal').click ->
    return false if !imageIndexCal()
    return false if !confirm("图片确定要更新到 " + $("input[name=s\\.market]").val() + " ?")
    imgDiv = $(@).parent()
    imgDiv.mask("上传图片中...")
    params =
      'sid': $('[name=s\\.sellingId]').val()
      imgs: $('[name=s\\.aps\\.imageName]').val()
    $.post('/sellings/imageUpload', params, (r) ->
      if r.flag is true
        alert "AMAZON图片正在更新,请查看更新日志!"
      else
        alert(r.message)
      imgDiv.unmask()
    )

  $('#volumeunit').change ->
    $unit = $(@)
    productLengths = $('#productLengths')
    productWidth = $('#productWidth')
    productHeigh = $('#productHeigh')

    if $unit.val() == 'IN'
      productLengths.val((productLengths.val() * 0.0393701).toFixed(2))
      productWidth.val((productWidth.val() * 0.0393701).toFixed(2))
      productHeigh.val((productHeigh.val() * 0.0393701).toFixed(2))
    else
      productLengths.val($('#dbLengths').val())
      productWidth.val($('#dbWidth').val())
      productHeigh.val($('#dbHeigh').val())

  $('#sproductWeight').change ->
    $unit = $(@)
    productWeight = $('#productWeight')
    if $unit.val() == 'OZ'
      productWeight.val((productWeight.val() * 35.2739619).toFixed(2))
    else
      productWeight.val($('#hproductWeight').val())

  $('#sweight').change ->
    $unit = $(@)
    weight = $('#weight')
    if $unit.val() == 'OZ'
      weight.val((weight.val() * 35.2739619).toFixed(2))
    else
      weight.val($('#hweight').val())

  KindEditor.ready((K) ->
    window.editor = K.create('#productDesc', {
      resizeType: 1
      allowPreviewEmoticons: false
      allowImageUpload: false
      newlineTag: 'br'
      afterChange: ->
        this.sync()
        $("#productDesc").find('~ .help-inline').html((2000 - this.count()) + " bytes left")
        $('#previewDesc').html($('#productDesc').val())
      items: ['source','|', '|', 'forecolor', 'bold']
    });
  )

  previewBtn = (e) ->
    invalidTag = false
    for tag in $('#previewDesc').html($('#productDesc').val()).find('*')
      switch tag.nodeName.toString().toLowerCase()
        when 'br','p','b','#text'
          break
        else
          invalidTag = true
          $(tag).css('background', 'yellow')
    noty({text: '使用了 Amazon 不允许使用的 Tag, 请查看预览中黄色高亮部分!', type: 'error', timeout: 3000}) if invalidTag is true
    invalidTag


