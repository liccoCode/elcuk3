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
    if(sku is undefined || sku is '')
      alert("没有 SKU, 错误页面!")
      return false
    {fid: sku, p: 'SKU'}

  # 初始化 上传 div
  window.dropUpload.iniDropbox(fidCallBack, dropbox)
  window.dropUpload.iniDropbox(fidCallBack, packageDropbox)
  window.dropUpload.iniDropbox(fidCallBack, instructionsDropbox)
  window.dropUpload.iniDropbox(fidCallBack, silkscreenDropbox)

  # 产品定位和产品卖点点击新增一行
  $("#update_product_form").on("click", "#more_locate_btn, #more_selling_point_btn", () ->
    alert(1)
  )