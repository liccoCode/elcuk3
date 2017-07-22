window.jQuery = window.$
$ ->


  fidCallBack = () ->
    {
      fid: $('#deliverymentId').text(),
      p: 'MATERIALPURCHASES'
    }
  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)


