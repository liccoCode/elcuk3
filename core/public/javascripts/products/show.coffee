$ ->
# 图片
  dropbox = $('#dropbox')
  # 包装
  packageDropbox = $('#packageDropbox')
  # 说明书
  instructionsDropbox = $('#instructionsDropbox')
  # 丝印文件
  silkscreenDropbox = $('#silkscreenDropbox')

  # 加载此 SKU 所拥有的全部附件
  window.dropUpload.loadAttachs($('#p_sku').val())

  fidCallBack = ->
    sku = $('#p_sku').val()
    if _.isEmpty(sku)
      alert("没有 SKU, 错误页面!")
      return false
    {
      fid: sku,
      p: 'SKU'
    }

  # 初始化 上传 div
  window.dropUpload.iniDropbox(fidCallBack, dropbox)
  window.dropUpload.iniDropbox(fidCallBack, packageDropbox)
  window.dropUpload.iniDropbox(fidCallBack, instructionsDropbox)
  window.dropUpload.iniDropbox(fidCallBack, silkscreenDropbox)

  # 将字符串转化成Dom元素
  parseDom = (arg) ->
    objE = document.createElement("table")
    objE.innerHTML = arg
    objE.childNodes[0]


  whouseAttachsFidCallBack = ->
    {
      fid: $('#p_sku').val(),
      p: 'PRODUCTWHOUSE'
    }

  initWhouseAttachs = ->
    dropbox = $('#whouse_attrs_dropbox')
    window.dropUpload.loadImages(whouseAttachsFidCallBack()['fid'], dropbox, whouseAttachsFidCallBack()['p'], 'span1')
    window.dropUpload.iniDropbox(whouseAttachsFidCallBack, dropbox)

  $(document).ready ->
    initWhouseAttachs()