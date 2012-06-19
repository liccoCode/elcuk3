$ ->
  fidCallBack = () ->
    {fid: $('#deliveryId').html(), p: 'DELIVERYMENT'}

  window.dropUpload.iniDropbox(fidCallBack, $('#dropbox'), $('#dropbox .message'), $('#uploaded'))
