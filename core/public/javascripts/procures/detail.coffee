$ ->
  dropbox = $('#dropbox')
  message = $('#dropbox .message')
  uploaded = $('#uploaded')

  fidCallBack = () ->
    {fid: $('#deliveryId').html(), p: 'DELIVERYMENT'}
  window.dropUpload.loadImages(fidCallBack()['fid'], message, uploaded)
  window.dropUpload.iniDropbox(fidCallBack, dropbox, message, uploaded)
