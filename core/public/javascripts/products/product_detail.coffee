$ ->
  dropbox = $('#dropbox')
  window.dropUpload.loadImages($('#p_sku').val(), dropbox)

  fidCallBack = ->
    sku = $('#p_sku').val()
    if(sku is undefined || sku is '')
      alert("没有 SKU, 错误页面!")
      return false
    {fid: sku, p: 'SKU'}

  window.dropUpload.iniDropbox(fidCallBack, dropbox)
