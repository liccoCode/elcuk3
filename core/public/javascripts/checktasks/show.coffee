$ ->
  fidCallBack = () ->
    {fid: $("input[name='check.id']").text(), p: 'CHECKTASK'}

  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)