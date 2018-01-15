$ ->
  ALERT_TEMPLATE = "<div class='alert alert-success fade in' style='text-align:center;'><button class='close' data-dismiss='alert'>×</button><div id='replace_it'></div></div>"
  IMGLI_TEMPLATE = "<div class='col-xs-2 col-md-2'><div class='thumbnail'><a class='thumbnail' target='_blank'
 style='height:230px'><img
 alt='100%x180'></a>
<div class='caption'><input style='height:33px;text-align:center;'>&nbsp;&nbsp;<button class='btn btn-mini btn-danger'
 name='delImage'><i class='icon-trash' style='width:22%;'></i></button></li></div></div></div>"

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
    LoadMask.mask('#btns')
    $.ajax($(@).data('url'), {type: 'POST', data: $('#saleAmazonForm').serialize()})
    .done((r) ->
      msg = if r.flag is true
        "#{r.message} AMAZON的Selling局部更新成功"
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
        div = $('<div>').html($("<div>").html(this.html()).text())
        div.find('div').replaceWith(->
          return $(this).contents()
        )
        div.find('span').replaceWith(->
          return $(this).contents()
        )
        htmlCode = div.html()
        count = htmlCode.length
        $('#productDesc').val(htmlCode)
        $("#productDesc_hint").html((2000 - count) + " bytes left")
        $('#previewDesc').html($('#productDesc').val())
      items: ['source', '|', '|', 'forecolor', 'bold']
    });
  )